package com.zwyouto.main;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Pattern;

/**
 * GM 管理会话。
 * ------------------------------------------------------------------
 * 与游戏通道完全独立的第二条 MUD 连接，逻辑对齐网页网关的
 * hell/web/mudsession.js：
 *
 *   1. 连接后服务端发 "ver1.0,&lt;key&gt;"，客户端回一行占位；
 *   2. 收到 "版本验证成功" 后发 "账号║密码║密文║邮箱"（密文段不校验）；
 *   3. 收到 ESC"000"+"0007"（SYSY"0007"）即登录成功，随后用 ping 探测管理权限；
 *   4. 之后按 "gm -r &lt;请求号&gt; &lt;子命令&gt;" 串行下发，
 *      服务端以 ESC"030"+JSON 回包（见 cmds/adm/gm.c）。
 *
 * 之所以不复用网页网关：网关监听的是使用者本机的 8080，
 * 手机连不到；但双方最终都是直连服务端 6666，所以这里自己连。
 *
 * 登录失败时会把服务端最近输出一起报出来（见 getRecentOutput），
 * 便于区分「连不上 / 版本没通过 / 密码错 / 权限不足 / 账号已在线」。
 */
public class GmSession {

    public interface ConnectCallback {
        /** 登录成功且权限校验通过。 */
        void onReady(String wizhood, int online);

        /** 登录失败/断线。 */
        void onError(String message);
    }

    /** 登录各阶段的即时通知，用于把“卡在哪一步”落地保存。 */
    public interface StageListener {
        void onStage(String stage);
    }

    public interface CommandCallback {
        void onResult(boolean ok, String msg, JSONObject data);
    }

    /** 登录字段分隔符 ║（U+2551），与 logind.c 的 explode(arg,"║") 对应。 */
    private static final String SEP = "\u2551";
    /**
     * 登录成功码：logind.c 里写的是 write(SYSY"0007")，而 SYSY = ESC + "000"
     * （include/zjmud.h），所以线路上的字节是 ESC + "000" + "0007"。
     * zj 协议是「ESC + 3 位码 + 载荷」：000 是状态机码、0007 是它的载荷，
     * 网页端由 zj.js 的 case '000' 把载荷拆出来（mudsession.js 走的是解析器，
     * 所以没事）。这里必须整体匹配，写成 "\u001b0007" 永远匹配不上 ——
     * 表现就是「能登录、但一直停在登录页刷服务端原始文本，进不去管理界面」。
     */
    private static final String CODE_LOGIN_OK = "\u001b" + "000" + "0007";
    /** gm.c 的回包前缀（ESA"030"，ESA 是真实 ESC 字节）。 */
    private static final String CODE_GM = "\u001b030";
    /** ZJFORCECMD "014"：服务端要求客户端代为执行的命令（如 reconnect 后的 look）。 */
    private static final String CODE_FORCECMD = "\u001b014";
    private static final String TEXT_VERIFY_OK = "版本验证成功";

    /** 登录阶段出现这些文本说明已被服务端拒绝，直接结束并报原文。 */
    private static final String[] FATAL_TEXTS = {
            "客户端非法", "版本验证失败", "游戏密码错误", "人物存档出现问题",
            "账号数据校验错误", "未知错误，请重试", "你的地址在本 MUD 不受欢迎",
    };

    private static final int CONNECT_TIMEOUT_MS = 12000;
    private static final int LOGIN_TIMEOUT_MS = 20000;
    private static final int CMD_TIMEOUT_MS = 10000;
    private static final long KEEPALIVE_MS = 4L * 60L * 1000L;
    private static final int RECENT_MAX = 40;

    /** 与 mudsession.js 相同的子命令白名单，防止任意巫师命令注入。 */
    private static final Pattern ALLOWED = Pattern.compile(
            "^(ping|list|show|set|add|skill|items|give|remove|family|title)\\b");
    private static final Pattern WIZHOOD = Pattern.compile("\\((arch|admin|wizard)\\)");

    private static final class Job {
        final String sub;
        final CommandCallback cb;

        Job(String sub, CommandCallback cb) {
            this.sub = sub;
            this.cb = cb;
        }
    }

    private final Handler main = new Handler(Looper.getMainLooper());
    private final Object lock = new Object();
    private final ArrayDeque<Job> queue = new ArrayDeque<>();
    private final ByteArrayOutputStream lineBuf = new ByteArrayOutputStream();
    private final StringBuilder textBuf = new StringBuilder();
    /** 服务端最近原始输出（已剥掉控制码），登录失败时随错误一起显示。 */
    private final ArrayDeque<String> recent = new ArrayDeque<>();

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private Thread reader;
    /** 串行写队列：所有 socket 写操作都在这个后台线程执行。 */
    private ExecutorService writer;

    private String host = "";
    private int port;
    private String id = "";
    private String password = "";
    private ConnectCallback connectCb;
    private StageListener stageCb;

    private boolean handshakeSent;
    private boolean credentialSent;
    private volatile boolean loginOk;
    private volatile boolean ready;
    private volatile boolean closed;
    private volatile boolean gotAnyData;
    private String wizhood = "";
    private int online;

    private int seq;
    private String pendingReq;
    private Job pendingJob;

    /** telnet 协商过滤状态机（与 mudsession.js 的 makeTelnetFilter 一致）。 */
    private int telnetState;

    private final Runnable cmdTimeout = new Runnable() {
        @Override
        public void run() {
            final Job job;
            synchronized (lock) {
                if (pendingJob == null) return;
                job = pendingJob;
                pendingJob = null;
                pendingReq = null;
            }
            main.post(new Runnable() {
                @Override
                public void run() {
                    job.cb.onResult(false, "命令超时：" + job.sub, null);
                }
            });
            pump();
        }
    };

    private final Runnable loginTimeout = new Runnable() {
        @Override
        public void run() {
            if (ready) return;

            String why;
            if (!gotAnyData) {
                why = "已建立 TCP 连接，但 20 秒内服务端没有返回任何数据。\n"
                        + "请确认这里填的是游戏端口，且与登录游戏时用的地址完全一致。";
            } else if (!credentialSent) {
                why = "服务端有回应，但始终没有出现「版本验证成功」，"
                        + "账号密码根本没有被提交（可能被服务端判定为非法客户端）。";
            } else if (!loginOk) {
                why = "账号密码已提交，但服务端没有返回登录成功码 0007。\n"
                        + "该账号可能不存在、密码不符，或账号名不符合规则。";
            } else {
                why = "登录流程未完成。";
            }

            failConnect("GM 登录超时（20 秒）\n" + why
                    + "\n\n—— 服务端最近输出 ——\n" + getRecentOutput());
        }
    };

    private final Runnable probeTask = new Runnable() {
        @Override
        public void run() {
            note("【客户端】开始探测管理权限（ping）");
            gm("ping", new CommandCallback() {
                @Override
                public void onResult(boolean ok, String msg, JSONObject data) {
                    if (ok && data != null) {
                        finishLogin(data);
                        return;
                    }
                    // 命令表可能未刷新（gm 是新增命令），补一次 rehash 再试。
                    note("【客户端】ping 无响应，补发 rehash 后重试");
                    sendRaw("rehash /cmds/adm/");
                    main.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gm("ping", new CommandCallback() {
                                @Override
                                public void onResult(boolean ok2, String msg2, JSONObject data2) {
                                    if (ok2 && data2 != null) {
                                        finishLogin(data2);
                                        return;
                                    }
                                    failConnect("管理接口无响应：" + msg2
                                            + "\n\n可能原因：\n"
                                            + "1) 该账号不是 (arch) 权限；\n"
                                            + "2) 该账号正开在游戏客户端里在线（服务端走了 reconnect 分支）。\n"
                                            + "请在游戏内退出该账号后重试。"
                                            + "\n\n—— 服务端最近输出 ——\n" + getRecentOutput());
                                }
                            });
                        }
                    }, 800);
                }
            });
        }
    };

    private final Runnable keepAlive = new Runnable() {
        @Override
        public void run() {
            if (closed || !ready) return;
            gm("ping", new CommandCallback() {
                @Override
                public void onResult(boolean ok, String msg, JSONObject data) {
                    // 心跳失败交由断线逻辑处理
                }
            });
            main.postDelayed(keepAlive, KEEPALIVE_MS);
        }
    };

    /* ---------------- 连接与登录 ---------------- */

    public void setStageListener(StageListener l) {
        this.stageCb = l;
    }

    /** 上报当前阶段；回调方自己负责落盘，这里绝不让它影响主流程。 */
    private void stage(String s) {
        StageListener l = stageCb;
        if (l == null) return;
        try {
            l.onStage(s);
        } catch (Throwable ignore) {
            // 记录失败不影响登录
        }
    }

    public void connect(String host, int port, String id, String password, ConnectCallback cb) {
        this.host = host == null ? "" : host.trim();
        this.port = port;
        this.id = id == null ? "" : id.trim().toLowerCase();
        this.password = password == null ? "" : password;
        this.connectCb = cb;

        if (writer != null) writer.shutdownNow();
        writer = Executors.newSingleThreadExecutor(new java.util.concurrent.ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "gm-writer");
                t.setDaemon(true);
                return t;
            }
        });

        this.handshakeSent = false;
        this.credentialSent = false;
        this.loginOk = false;
        this.ready = false;
        this.closed = false;
        this.gotAnyData = false;
        this.telnetState = 0;
        this.seq = 0;
        synchronized (lock) {
            queue.clear();
            pendingJob = null;
            pendingReq = null;
        }
        synchronized (recent) {
            recent.clear();
        }
        lineBuf.reset();
        textBuf.setLength(0);

        reader = new Thread(new Runnable() {
            @Override
            public void run() {
                doConnect();
                readLoop();
            }
        }, "gm-session");
        reader.setDaemon(true);
        reader.start();
    }

    private void doConnect() {
        note("【客户端】连接 " + host + ":" + port);
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            s.setKeepAlive(true);
            s.setTcpNoDelay(true);
            socket = s;
            in = s.getInputStream();
            out = s.getOutputStream();
        } catch (Exception e) {
            failConnect("连接服务端失败：" + e.getMessage()
                    + "\n\n请确认地址与端口正确（应与登录游戏时一致）。");
            return;
        }
        stage("TCP 已连接 " + host + ":" + port + "，等待服务端回应");
        main.postDelayed(loginTimeout, LOGIN_TIMEOUT_MS);
    }

    private void readLoop() {
        if (in == null) return;
        byte[] buf = new byte[4096];
        try {
            int n;
            while (!closed && (n = in.read(buf)) > 0) {
                processBytes(buf, n);
            }
        } catch (IOException e) {
            // 断线，统一由下方处理
        }
        if (!closed) {
            if (ready) {
                main.post(new Runnable() {
                    @Override
                    public void run() {
                        ConnectCallback cb = connectCb;
                        if (cb == null) return;
                        try {
                            cb.onError("与服务端的连接已断开");
                        } catch (Throwable t) {
                            Log.e("GmSession", "onError 回调异常", t);
                        }
                    }
                });
            } else {
                failConnect("与服务端的连接已断开"
                        + "\n\n—— 服务端最近输出 ——\n" + getRecentOutput());
            }
        }
    }

    private void processBytes(byte[] buf, int len) {
        if (!handshakeSent) {
            handshakeSent = true;
            gotAnyData = true;
            note("【客户端】已连上服务端，发送握手占位行");
            stage("服务端有回应，发送握手");
            // logind.c 的 jiance() 只认 "6278038"，其余原样放行（老格式 "web" 同样兼容）。
            sendRaw("web|android");
        }
        for (int i = 0; i < len; i++) {
            int b = buf[i] & 0xFF;

            switch (telnetState) {
                case 0:
                    if (b == 0xFF) {
                        telnetState = 1;
                        continue;
                    }
                    break;
                case 1:
                    if (b == 0xFF) {
                        telnetState = 0;
                    } else if (b == 0xFA) {
                        telnetState = 2;
                    } else if (b >= 0xFB && b <= 0xFE) {
                        telnetState = 3;
                    } else {
                        telnetState = 0;
                    }
                    continue;
                case 3:
                    telnetState = 0;
                    continue;
                case 2:
                    if (b == 0xFF) telnetState = 4;
                    continue;
                case 4:
                    telnetState = (b == 0xF0) ? 0 : 2;
                    continue;
                default:
                    telnetState = 0;
                    continue;
            }

            if (b == '\n') {
                byte[] raw = lineBuf.toByteArray();
                lineBuf.reset();
                if (raw.length == 0) continue;
                String line = new String(raw, StandardCharsets.UTF_8);
                if (line.endsWith("\r")) line = line.substring(0, line.length() - 1);
                onLine(line);
            } else if (b != '\r') {
                lineBuf.write(b);
                if (lineBuf.size() > 65536) lineBuf.reset();   // 异常保护
            }
        }
    }

    private void onLine(String line) {
        note(line);

        /* 服务端要求客户端代为执行的命令（ZJFORCECMD "014"），例如
         * reconnect 之后让客户端补一次 look。必须与游戏客户端一致回写，
         * 否则会话状态会与正常客户端不同。 */
        int fc = line.indexOf(CODE_FORCECMD);
        if (fc >= 0) {
            String cmd = line.substring(fc + CODE_FORCECMD.length()).trim();
            if (cmd.length() > 0 && cmd.indexOf('\n') < 0 && cmd.indexOf('\r') < 0) {
                note("【客户端】服务端要求执行：" + cmd);
                sendRaw(cmd);
            }
        }

        if (!credentialSent) {
            textBuf.append(line).append('\n');
            if (textBuf.length() > 8192) textBuf.delete(0, textBuf.length() - 4096);
            if (textBuf.indexOf(TEXT_VERIFY_OK) >= 0) {
                credentialSent = true;
                note("【客户端】版本验证成功，发送账号密码");
                stage("版本验证通过，已提交账号密码");
                sendRaw(id + SEP + password + SEP + "x" + SEP + id + "@web");
            }
        }

        if (!loginOk && line.indexOf(CODE_LOGIN_OK) >= 0) {
            loginOk = true;
            note("【客户端】收到 0007，登录成功");
            stage("收到登录成功码 0007，开始探测管理权限");
            main.postDelayed(probeTask, 400);
        }

        // 登录阶段被服务端拒绝：立即结束，把原文报给用户
        if (!loginOk) {
            for (String bad : FATAL_TEXTS) {
                if (line.indexOf(bad) >= 0) {
                    failConnect("服务端拒绝登录：" + cleanText(line));
                    return;
                }
            }
        }

        int p = line.indexOf(CODE_GM);
        if (p >= 0) {
            onGmReply(line.substring(p + CODE_GM.length()));
        }
    }

    private void onGmReply(String json) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(json);
        } catch (Exception e) {
            // 回包解析失败：当作当前请求失败处理
        }

        final Job job;
        synchronized (lock) {
            if (pendingJob == null) return;
            String req = obj == null ? "" : obj.optString("req", "");
            if (!pendingReq.equals(req)) return;   // 不是当前这条的回包
            job = pendingJob;
            pendingJob = null;
            pendingReq = null;
        }
        main.removeCallbacks(cmdTimeout);

        final JSONObject res = obj;
        main.post(new Runnable() {
            @Override
            public void run() {
                dispatch(job, res);
            }
        });
        pump();
    }

    private void dispatch(Job job, JSONObject res) {
        boolean ok = res != null && res.optInt("ok", 0) == 1;
        String msg = res == null ? "服务端回包解析失败" : res.optString("msg", "");
        JSONObject data = res == null ? null : res.optJSONObject("data");
        try {
            job.cb.onResult(ok, msg, data);
        } catch (Throwable t) {
            /* 界面渲染出问题也只当作本命令失败，绝不让整个进程跟着挂掉 */
            Log.e("GmSession", "命令回调异常：" + job.sub, t);
            try {
                job.cb.onResult(false, "客户端处理回包异常：" + t, null);
            } catch (Throwable ignore) {
                // 二次异常不再处理
            }
        }
    }

    private void finishLogin(JSONObject data) {
        wizhood = data.optString("wizhood", "");
        online = data.optInt("online", 0);

        if (!WIZHOOD.matcher(wizhood).find()) {
            failConnect("该账号权限不足，需要 wizard 及以上（当前：" + wizhood + "）"
                    + "\n\n注意：gm 命令在服务端要求 (arch) 权限。"
                    + "\n\n—— 服务端最近输出 ——\n" + getRecentOutput());
            return;
        }

        ready = true;
        note("【客户端】权限校验通过：" + wizhood);
        stage("权限校验通过：" + wizhood);
        main.removeCallbacks(loginTimeout);
        main.postDelayed(keepAlive, KEEPALIVE_MS);

        ConnectCallback cb = connectCb;
        if (cb != null) {
            try {
                cb.onReady(wizhood, online);
            } catch (Throwable t) {
                Log.e("GmSession", "onReady 回调异常", t);
                try {
                    cb.onError("客户端内部错误：" + t);
                } catch (Throwable ignore) {
                    // ignore
                }
            }
        }
    }

    private void failConnect(final String message) {
        int nl = message == null ? -1 : message.indexOf('\n');
        stage("失败：" + (nl < 0 ? message : message.substring(0, nl)));
        closed = true;
        ready = false;
        main.removeCallbacks(loginTimeout);
        main.removeCallbacks(cmdTimeout);
        main.removeCallbacks(keepAlive);
        drainQueue("连接不可用");
        closeSocket();
        Log.d("GmSession", "fail: " + message);

        ConnectCallback cb = connectCb;
        if (cb != null) {
            main.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        cb.onError(message);
                    } catch (Throwable t) {
                        Log.e("GmSession", "onError 回调异常", t);
                    }
                }
            });
        }
    }

    /* ---------------- 记录服务端原始输出 ---------------- */

    private void note(String line) {
        String s = cleanText(line);
        if (s.length() == 0) return;
        if (s.length() > 160) s = s.substring(0, 160) + "...";
        synchronized (recent) {
            recent.addLast(s);
            while (recent.size() > RECENT_MAX) recent.pollFirst();
        }
        Log.d("GmSession", s);
    }

    /** 去掉 ESC 及其后的 3 位指令码，便于阅读。 */
    private String cleanText(String line) {
        String s = line.replace('\u001b', '\u0000');
        s = s.replaceAll("\u0000\\d{3}", "");
        s = s.replace('\u0000', ' ');
        return s.trim();
    }

    /** 服务端最近输出，登录失败时用于定位问题。 */
    public String getRecentOutput() {
        StringBuilder sb = new StringBuilder();
        synchronized (recent) {
            for (String s : recent) sb.append(s).append('\n');
        }
        return sb.length() == 0 ? "（服务端没有任何输出）" : sb.toString();
    }

    /* ---------------- 命令执行（串行 + 请求号） ---------------- */

    /**
     * 执行一条 gm 子命令，例如 "show zhangsan"。
     * 只允许白名单内的子命令，且禁止换行注入。
     */
    public void gm(String sub, CommandCallback cb) {
        String s = sub == null ? "" : sub.trim();

        if (!canSend()) {
            cb.onResult(false, "会话未就绪", null);
            return;
        }
        if (s.isEmpty()) {
            cb.onResult(false, "命令为空", null);
            return;
        }
        if (s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0) {
            cb.onResult(false, "命令包含非法字符", null);
            return;
        }
        if (!ALLOWED.matcher(s).find()) {
            cb.onResult(false, "不在允许的命令白名单内：" + s.split("\\s+")[0], null);
            return;
        }

        synchronized (lock) {
            queue.add(new Job(s, cb));
        }
        pump();
    }

    private boolean canSend() {
        return !closed && (loginOk || ready) && socket != null && !socket.isClosed();
    }

    private void pump() {
        for (; ; ) {
            final Job job;
            synchronized (lock) {
                if (pendingJob != null || queue.isEmpty()) return;

                if (out == null || closed) {
                    job = queue.poll();   // 连接不可用，立即失败
                } else {
                    pendingJob = queue.poll();
                    pendingReq = String.valueOf(++seq);
                    sendRaw("gm -r " + pendingReq + " " + pendingJob.sub);
                    main.removeCallbacks(cmdTimeout);
                    main.postDelayed(cmdTimeout, CMD_TIMEOUT_MS);
                    return;
                }
            }
            main.post(new Runnable() {
                @Override
                public void run() {
                    job.cb.onResult(false, "连接已关闭", null);
                }
            });
        }
    }

    /**
     * 发送一行文本。
     * ------------------------------------------------------------------
     * 必须异步：pump() 会在主线程被调用（onReady / probeTask / 心跳），
     * 一旦在这里直接 out.write()，Android 会抛
     * NetworkOnMainThreadException 直接把进程干掉 ——
     * 表现就是「GM 面板点连接后毫无征兆地退回登录页」。
     * 写操作统一丢给单线程队列，既保证顺序，又保证永远不在主线程。
     */
    private void sendRaw(final String line) {
        final byte[] data = (line + "\n").getBytes(StandardCharsets.UTF_8);
        ExecutorService w = writer;
        if (w == null) return;
        try {
            w.execute(new Runnable() {
                @Override
                public void run() {
                    OutputStream o = out;
                    if (o == null) return;
                    try {
                        synchronized (lock) {
                            o.write(data);
                            o.flush();
                        }
                    } catch (IOException e) {
                        // 写失败交由读线程的断线逻辑处理
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            // 会话已关闭，直接丢弃
        }
    }

    /* ---------------- 生命周期 ---------------- */

    public boolean isReady() {
        return ready;
    }

    public String getWizhood() {
        return wizhood;
    }

    public void close() {
        closed = true;
        ready = false;
        loginOk = false;
        main.removeCallbacks(loginTimeout);
        main.removeCallbacks(cmdTimeout);
        main.removeCallbacks(keepAlive);
        main.removeCallbacks(probeTask);
        drainQueue("会话已关闭");
        closeSocket();
    }

    private void drainQueue(String reason) {
        ArrayDeque<Job> jobs = new ArrayDeque<>();
        synchronized (lock) {
            jobs.addAll(queue);
            queue.clear();
            if (pendingJob != null) {
                jobs.add(pendingJob);
                pendingJob = null;
                pendingReq = null;
            }
        }
        for (final Job job : jobs) {
            main.post(new Runnable() {
                @Override
                public void run() {
                    job.cb.onResult(false, reason, null);
                }
            });
        }
    }

    private void closeSocket() {
        Socket s = socket;
        socket = null;
        in = null;
        out = null;
        ExecutorService w = writer;
        writer = null;
        if (w != null) w.shutdownNow();
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
