package com.zwyouto.main;

import android.app.Application;
import android.util.Log;
import com.zwyouto.mud.BuildConfig;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Objects;

public class mudsocketd extends Application {
    /**
     * 与服务端 driver（FluffOS）一致的线路编码。
     * 本 mudlib 已统一为 UTF-8（见 web/readme.md「编码」一节），
     * 旧的 gb2312 会导致中文全部乱码。
     */
    public static final String MUD_CHARSET = "UTF-8";
    /** 与服务端 include/zjmud.h 的 ZJKEY 保持一致（用于 zj 握手）。 */
    public static final String ZJKEY = "123456789abcd";
    /** 默认服务器地址（反代出来的 6666）。登录页可改。 */
    public static final String DEFAULT_HOST = "frp-net.com";
    public static final int DEFAULT_PORT = 27356;
    private static PrintWriter out;
    private boolean action;
    private String addr;
    private boolean bbact;
    private String content;
    private String gameip;
    private String gamekey;
    private String gamepart;
    private String gameport;
    private boolean gxact;
    private BufferedReader in;
    private boolean ingame;
    private boolean iscon;
    private boolean jobact;
    private String jobs;
    private boolean lookact;
    private boolean mailact;
    private String mubiaoid;
    private String mubiaoimg;
    private String mubiaojinengs;
    private String mubiaoname;
    private String mubiaowupins;
    private String myimg;
    private String mylevel;
    private String myname;
    private String myshort;
    private int port;
    private String senceexit;
    private String sencenpc;
    private String senceshort;
    private static Socket socket = null;
    private static boolean cmdss = true;
    private String pindaoa = "世界";
    private String pindaob = "chat";
    private String myid = "null";
    private String sencelong = "无";
    private String mubiaoinfo = "无";
    private String zhuzaowu = "无";
    private String mails = "无";
    private String mailtexts = "无";
    private String guanxi = "无";
    private String ucenter_id = BuildConfig.FLAVOR;
    private String ucenter_pass = BuildConfig.FLAVOR;
    private String ucenter_phone = BuildConfig.FLAVOR;
    private String ucenter_email = BuildConfig.FLAVOR;
    private String ucenter_key = BuildConfig.FLAVOR;
    private boolean canpay = true;

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        installCrashRecorder();
    }

    /**
     * 把未捕获异常写到内部存储 gm_crash.txt。
     * 用文件而不是 SharedPreferences：进程被杀/重启后仍能读到，
     * 也不受多进程缓存影响。登录页恢复时读取并展示，读完即删。
     */
    private void installCrashRecorder() {
        final Thread.UncaughtExceptionHandler prev = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                try {
                    java.io.StringWriter sw = new java.io.StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    pw.flush();
                    String text = "线程 " + thread.getName() + "  " + new java.util.Date() + "\n" + sw;
                    if (text.length() > 3000) text = text.substring(0, 3000);
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(
                            new java.io.File(getFilesDir(), "gm_crash.txt"));
                    fos.write(text.getBytes(MUD_CHARSET));
                    fos.close();
                } catch (Throwable ignore) {
                    // 记录失败也不能影响系统默认的崩溃处理
                }
                if (prev != null) {
                    prev.uncaughtException(thread, e);
                } else {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(10);
                }
            }
        });
    }

    public void new_sock(String ip, int pt) {
        this.addr = ip;
        this.port = pt;
        this.action = false;
        this.iscon = true;
        this.ingame = false;
        if (socket != null && !socket.isClosed() && socket.isConnected()) {
            Log.d("socket", "socket close\n");
            try {
                socket.close();
            } catch (IOException e) {
                Log.d("socket", e.toString());
            }
        }
        new Thread(new Runnable() {  
            @Override // java.lang.Runnable
            public void run() {
                try {
                    Socket unused = mudsocketd.socket = new Socket(mudsocketd.this.addr, mudsocketd.this.port);
                    mudsocketd.socket.setKeepAlive(true);
                    mudsocketd.this.in = new BufferedReader(new InputStreamReader(mudsocketd.socket.getInputStream(), MUD_CHARSET));
                    PrintWriter unused2 = mudsocketd.out = new PrintWriter((Writer) new BufferedWriter(new OutputStreamWriter(mudsocketd.socket.getOutputStream(), MUD_CHARSET)), true);
                    mudsocketd.this.action = true;
                    mudsocketd.this.iscon = false;
                    Log.d("socket", "newsock...\n");
                } catch (Exception ee) {
                    Log.d("socket", ee.toString());
                    mudsocketd.this.action = false;
                    mudsocketd.this.iscon = false;
                }
            }
        }).start();
    }

    public String mudin() {
        if (this.iscon) {
            return null;
        }
        if (socket != null && !socket.isClosed() && socket.isConnected()) {
            if (!socket.isInputShutdown()) {
                if (!Objects.equals(this.sencelong, "无")) {
                    return null;
                }
                try {
                    this.content = this.in.readLine();
                } catch (IOException e) {
                    this.content = "连接已断开！";
                }
            } else {
                this.content = "连接已断开！";
            }
        } else {
            this.content = "连接已断开！";
        }
        return this.content;
    }

    public void shut() {
        if (socket != null && !socket.isClosed() && socket.isConnected()) {
            socket = null;
            this.in = null;
            out = null;
        }
    }

    public boolean isconing() {
        return this.iscon;
    }

    public void setpay(boolean pay) {
        this.canpay = pay;
    }

    public boolean canpay() {
        return this.canpay;
    }

    public String recon() {
        this.iscon = true;
        new_sock(this.addr, this.port);
        return "连接错误，正在重新连接...";
    }

    public static void mudout(final String mudout) {
         
// java.lang.Runnable
        new Thread(() -> {
            if (!mudsocketd.socket.isClosed() && !mudsocketd.socket.isInputShutdown()) {
                Log.d("socket", mudsocketd.socket.toString() + "run: " + mudout);
                if (mudsocketd.cmdss) {
                    mudsocketd.out.println(mudout);
                } else {
                    mudsocketd.out.println(mudout.replace("\n", ";"));
                }
            }
        }).start();
    }

    public void set_cmdss(Boolean y) {
        cmdss = y;
    }

    public boolean get_cmdss() {
        return cmdss;
    }

    public void set_ucenter_key(String id) {
        this.ucenter_key = id;
    }

    public String get_ucenter_key() {
        return this.ucenter_key;
    }

    public void set_ucenter_id(String id) {
        this.ucenter_id = id;
    }

    public String get_ucenter_id() {
        return this.ucenter_id;
    }

    public void set_ucenter_pass(String pass) {
        this.ucenter_pass = pass;
    }

    public String get_ucenter_pass() {
        return this.ucenter_pass;
    }

    public void set_ucenter_phone(String phone) {
        this.ucenter_phone = phone;
    }

    public String get_ucenter_phone() {
        return this.ucenter_phone;
    }

    public void set_ucenter_email(String email) {
        this.ucenter_email = email;
    }

    public String get_ucenter_email() {
        return this.ucenter_email;
    }

    public void setgamepart(String part) {
        this.gamepart = part;
    }

    public String getgamepart() {
        return this.gamepart;
    }

    public void setgameip(String ip) {
        this.gameip = ip;
    }

    public String getgameip() {
        return this.gameip;
    }

    public void setgameport(String port) {
        this.gameport = port;
    }

    public String getgameport() {
        return this.gameport;
    }

    public void setAddr(String ip) {
        this.addr = ip;
        this.gameip = ip;
    }

    public String getAddr() {
        return this.addr;
    }

    public void setPort(int pt) {
        this.port = pt;
        this.gameport = String.valueOf(pt);
    }

    public int getPort() {
        return this.port;
    }

    public void setgamekey(String key) {
        this.gamekey = key;
    }

    public String getgamekey() {
        return this.gamekey;
    }

    public void setmubiaoimg(String mubiao) {
        this.mubiaoimg = mubiao;
    }

    public String getmubiaoimg() {
        return this.mubiaoimg;
    }

    public void setmubiaoinfo(String mubiao) {
        this.mubiaoinfo = mubiao;
    }

    public String getmubiaoinfo() {
        return this.mubiaoinfo;
    }

    public boolean isbbact() {
        return this.bbact;
    }

    public void setbbact(boolean act) {
        this.bbact = act;
    }

    public boolean isgxact() {
        return this.gxact;
    }

    public void setgxact(boolean act) {
        this.gxact = act;
    }

    public boolean isjobact() {
        return this.jobact;
    }

    public void setjobact(boolean act) {
        this.jobact = act;
    }

    public boolean ismailact() {
        return this.mailact;
    }

    public void setmailact(boolean act) {
        this.mailact = act;
    }

    public boolean islookact() {
        return this.lookact;
    }

    public void setlookact(boolean act) {
        this.lookact = act;
    }

    public boolean isaction() {
        return this.action;
    }

    public void setguanxi(String mubiao) {
        this.guanxi = mubiao;
    }

    public String getguanxi() {
        return this.guanxi;
    }

    public void setmubiaoid(String mubiao) {
        this.mubiaoid = mubiao;
    }

    public String getmubiaoid() {
        return this.mubiaoid;
    }

    public void setmubiaoname(String mubiao) {
        this.mubiaoname = mubiao;
    }

    public String getmubiaoname() {
        return this.mubiaoname;
    }

    public void setjobs(String mubiao) {
        this.jobs = mubiao;
    }

    public String getjobs() {
        return this.jobs;
    }

    public void setmubiaojinengs(String jinengs) {
        this.mubiaojinengs = jinengs;
    }

    public String getmubiaojinengs() {
        return this.mubiaojinengs;
    }

    public void setmailtexts(String wupins) {
        this.mailtexts = wupins;
    }

    public String getmailtexts() {
        return this.mailtexts;
    }

    public void setmubiaowupins(String wupins) {
        this.mubiaowupins = wupins;
    }

    public String getmubiaowupins() {
        return this.mubiaowupins;
    }

    public void setsenceshort(String senshort) {
        this.senceshort = senshort;
    }

    public String getsenceshort() {
        return this.senceshort;
    }

    public void setsencelong(String senlong) {
        this.sencelong = senlong;
    }

    public String getsencelong() {
        return this.sencelong;
    }

    public void setmyshort(String my) {
        this.myshort = my;
        this.myname = this.myshort.split("/")[0];
        this.myid = this.myshort.split("/")[1];
        this.mylevel = this.myshort.split("/")[2];
        this.myimg = this.myshort.split("/")[3];
    }

    public String getmyid() {
        return this.myid;
    }

    public String getmylevel() {
        return this.mylevel;
    }

    public String getmyname() {
        return this.myname;
    }

    public String getmyimg() {
        return this.myimg;
    }

    public String getmyshort() {
        return this.myshort;
    }

    public void setsenceexit(String senexit) {
        this.senceexit = senexit;
    }

    public String getsenceexit() {
        return this.senceexit;
    }

    public void setsencenpc(String sennpc) {
        this.sencenpc = sennpc;
    }

    public String getsencenpc() {
        return this.sencenpc;
    }

    public void setpindaoa(String pd1) {
        this.pindaoa = pd1;
    }

    public void setpindaob(String pd2) {
        this.pindaob = pd2;
    }

    public String getpindaoa() {
        return this.pindaoa;
    }

    public String getpindaob() {
        return this.pindaob;
    }

    public void setzhuzaowu(String pd1) {
        this.zhuzaowu = pd1;
    }

    public String getzhuzaowu() {
        return this.zhuzaowu;
    }

    public void setmails(String mail) {
        this.mails = mail;
    }

    public String getmails() {
        return this.mails;
    }
}