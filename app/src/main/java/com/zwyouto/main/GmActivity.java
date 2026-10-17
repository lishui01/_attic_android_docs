package com.zwyouto.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zwyouto.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GM 管理面板（安卓端）。
 * ------------------------------------------------------------------
 * 数据来自 GmSession 直连服务端后执行的 gm 子命令（cmds/adm/gm.c），
 * 与网页管理台同源。每个写操作在 MUD 侧都会自动备份档案到 /temp/
 * 并写入 log/static/gm 审计日志。
 */
public class GmActivity extends Activity {

    /* 常用字段分组（对应 dbase 键，"a/b" 表示嵌套） */
    private static final String[][] GROUPS = {
            {"基础属性", "str,con,dex,int,kar,per,age,age_modify,gender,race,class"},
            {"生命与内力",
                    "max_qi,qi,eff_qi,max_jing,jing,eff_jing,max_jingli,jingli,eff_jingli,"
                            + "max_neili,neili,max_mana,mana,shen,food,water"},
            {"成长与数值",
                    "combat_exp,potential,level,score,weiwang,honors,jungong,learned_points,"
                            + "experience,quest_count,tianfu_point,skill_point,mud_age"},
            {"金钱", "balance,xy_money,yuanbao,rmb"},
            {"门派与社交", "family/family_name,family/generation,title,party/party_name,out_family"},
    };

    private static final Map<String, String> LABELS = new HashMap<String, String>();

    static {
        String[][] pairs = {
                {"str", "臂力"}, {"con", "根骨"}, {"dex", "身法"}, {"int", "悟性"},
                {"kar", "福缘"}, {"per", "容貌"}, {"age", "年龄"}, {"age_modify", "年龄修正"},
                {"gender", "性别"}, {"race", "种族"}, {"class", "职业"},
                {"max_qi", "最大气血"}, {"qi", "气血"}, {"eff_qi", "有效气血"},
                {"max_jing", "最大精神"}, {"jing", "精神"}, {"eff_jing", "有效精神"},
                {"max_jingli", "最大精力"}, {"jingli", "精力"}, {"eff_jingli", "有效精力"},
                {"max_neili", "最大内力"}, {"neili", "内力"},
                {"max_mana", "最大法力"}, {"mana", "法力"}, {"shen", "神"},
                {"food", "食物"}, {"water", "饮水"},
                {"combat_exp", "实战经验"}, {"potential", "潜能"}, {"level", "等级"},
                {"score", "评分"}, {"weiwang", "威望"}, {"honors", "荣誉"},
                {"jungong", "军功"}, {"learned_points", "学识点"}, {"experience", "经验"},
                {"quest_count", "任务数"}, {"tianfu_point", "天赋点"},
                {"skill_point", "技能点"}, {"mud_age", "江湖年龄"},
                {"balance", "白银"}, {"xy_money", "信用"}, {"yuanbao", "元宝"}, {"rmb", "RMB"},
                {"family/family_name", "门派"}, {"family/generation", "辈分"},
                {"title", "称号"}, {"party/party_name", "帮派"}, {"out_family", "师门"},
        };
        for (String[] p : pairs) LABELS.put(p[0], p[1]);
    }

    private static final int C_TITLE_BG = 0xFF1E88E5;
    private static final int C_TITLE_FG = 0xFFFFFFFF;
    private static final int C_ROW_BG = 0xFF18202B;
    private static final int C_TEXT = 0xFFD8E4F0;
    private static final int C_DIM = 0xFF8A97A6;

    /** 上次会话留下的诊断信息（连接阶段 / 失败原因 / 面板异常关闭），下次进入时显示。 */
    private static final String KEY_LAST_DIAG = "gm_last_diag";
    /** 该记录是否已在游戏登录页提示过。 */
    private static final String KEY_DIAG_SEEN = "gm_diag_seen";

    /** 面板构建标记：装在手机上能一眼看出是不是这一版。 */
    public static final String BUILD_TAG = "GM-2026.10.18-4";

    /** GM 面板是否在前台。游戏登录页在后台断线时会 System.exit(0)，
     *  那会把整个进程（连同 GM 面板）一起杀掉，所以要对它让路。 */
    private static volatile boolean panelActive;

    public static boolean isPanelActive() {
        return panelActive;
    }

    private interface InputHandler {
        void onInput(String text);
    }

    private GmSession session;
    private SharedPreferences prefs;

    private View loginLay;
    private View mainLay;
    private EditText serverEdit;
    private EditText idEdit;
    private EditText passEdit;
    private EditText searchEdit;
    private TextView statusView;
    private TextView whoView;
    private LinearLayout content;
    private Button loginBt;

    private String account = "";
    private String selectedId = "";
    private float density = 1f;
    private int scrw;
    private long lastBackTime;

    private final Handler ui = new Handler();

    /** 连接期间把服务端最近输出滚动显示在状态栏，卡在哪一步一目了然。 */
    private final Runnable progressTick = new Runnable() {
        @Override
        public void run() {
            GmSession s = session;
            if (s == null || s.isReady()) return;
            if (loginLay.getVisibility() != View.VISIBLE) return;

            String[] lines = s.getRecentOutput().split("\n");
            int from = Math.max(0, lines.length - 8);
            StringBuilder sb = new StringBuilder();
            for (int i = from; i < lines.length; i++) {
                sb.append(lines[i]).append('\n');
            }
            statusView.setText(sb.toString());
            ui.postDelayed(progressTick, 1500);
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.gmx);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        this.scrw = dm.widthPixels;
        this.density = dm.density;

        this.loginLay = findViewById(R.id.gm_login_lay);
        this.mainLay = findViewById(R.id.gm_main_lay);
        this.serverEdit = (EditText) findViewById(R.id.gm_server_edit);
        this.idEdit = (EditText) findViewById(R.id.gm_id_edit);
        this.passEdit = (EditText) findViewById(R.id.gm_pass_edit);
        this.searchEdit = (EditText) findViewById(R.id.gm_search_edit);
        this.statusView = (TextView) findViewById(R.id.gm_status);
        this.whoView = (TextView) findViewById(R.id.gm_who);
        this.content = (LinearLayout) findViewById(R.id.gm_content);

        this.prefs = getSharedPreferences("zwyjh", 0);
        String addr = prefs.getString("server", "");
        if (addr.length() == 0) {
            addr = mudsocketd.DEFAULT_HOST + ":" + mudsocketd.DEFAULT_PORT;
        }
        serverEdit.setText(addr);
        idEdit.setText(prefs.getString("gm_name", ""));
        passEdit.setText(prefs.getString("gm_pass", ""));

        int h = scrw / 11;
        if (h < dp(40)) h = dp(44);
        serverEdit.getLayoutParams().height = h;
        idEdit.getLayoutParams().height = h;
        passEdit.getLayoutParams().height = h;

        this.loginBt = (Button) findViewById(R.id.gm_login_bt);
        this.loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });
        findViewById(R.id.gm_close_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.gm_refresh_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadList();
            }
        });
        findViewById(R.id.gm_search_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pid = searchEdit.getText().toString().trim();
                if (pid.length() == 0) {
                    toast("请输入玩家ID");
                    return;
                }
                showPlayer(pid);
            }
        });
        findViewById(R.id.gm_logout_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogout();
            }
        });
        /* 上次会话留下的记录（连接阶段 / 失败原因 / 面板被异常关闭），进来就能看到 */
        String last = prefs.getString(KEY_LAST_DIAG, "");
        if (last.length() > 0) {
            statusView.setText("上次记录：" + last + "\n（" + BUILD_TAG + "）");
        } else {
            statusView.setText(BUILD_TAG);
        }
        /* 长按标题可随时查看服务端原始输出（排查用） */
        findViewById(R.id.gm_title).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDiag(session == null ? "（尚未连接）" : session.getRecentOutput());
                return true;
            }
        });
    }

    /**
     * 返回键分三种情况处理，避免「输入完密码一按返回就退回游戏登录页」：
     * 1. 已在管理界面：只断开 GM 连接，回到本面板登录页；
     * 2. 正在连接中：取消本次连接，仍留在本面板；
     * 3. 其余：两秒内连按两次才真正关闭面板。
     */
    @Override
    public void onBackPressed() {
        if (mainLay.getVisibility() == View.VISIBLE) {
            doLogout();
            return;
        }
        if (session != null) {
            session.close();
            session = null;
            setLoginBusy(false);
            ui.removeCallbacks(progressTick);
            statusView.setText("已取消连接");
            return;
        }
        long now = SystemClock.elapsedRealtime();
        if (now - lastBackTime > 2000L) {
            lastBackTime = now;
            toast("再按一次返回键退出 GM 面板");
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panelActive = true;
    }

    @Override
    protected void onPause() {
        panelActive = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        panelActive = false;
        ui.removeCallbacks(progressTick);
        if (session != null) {
            session.close();
            session = null;
        }
        /* 用户自己关掉就把记录清干净；被动销毁才留记号，
           这样“退回登录页”时能看出是主动退出还是被顶掉。 */
        if (prefs != null) {
            if (isFinishing()) {
                clearLastDiag();
            } else {
                writeLastDiag("面板被系统销毁（finishing=false）");
            }
        }
        super.onDestroy();
    }

    /* ==================== 连接 ==================== */

    private void doLogin() {
        final String addr = serverEdit.getText().toString().trim();
        final String uid = idEdit.getText().toString().trim();
        final String pwd = passEdit.getText().toString();

        if (uid.length() == 0 || pwd.length() == 0) {
            toast("请输入 GM 账号和密码");
            return;
        }
        if (addr.length() == 0) {
            toast("请输入服务器地址");
            return;
        }

        String host;
        int port;
        int idx = addr.lastIndexOf(':');
        if (idx > 0) {
            host = addr.substring(0, idx).trim();
            try {
                port = Integer.parseInt(addr.substring(idx + 1).trim());
            } catch (Exception e) {
                port = mudsocketd.DEFAULT_PORT;
            }
        } else {
            host = addr;
            port = mudsocketd.DEFAULT_PORT;
        }
        if (host.length() == 0) host = mudsocketd.DEFAULT_HOST;

        prefs.edit()
                .putString("gm_name", uid)
                .putString("gm_pass", pwd)
                .commit();

        account = uid.toLowerCase();
        selectedId = "";
        statusView.setText("正在连接 " + host + ":" + port + " ...\n" + BUILD_TAG);
        setLoginBusy(true);
        /* 先落盘，万一面板被进程级终止，回来后也知道走到了哪一步 */
        writeLastDiag("开始连接 " + host + ":" + port + "（账号 " + uid + "）");

        ui.removeCallbacks(progressTick);
        if (session != null) session.close();
        final GmSession s = new GmSession();
        session = s;
        /* 每个阶段立即落盘：即使进程被强行终止，也能知道卡在哪一步 */
        s.setStageListener(new GmSession.StageListener() {
            @Override
            public void onStage(String st) {
                writeLastDiag(st);
            }
        });
        try {
            s.connect(host, port, uid, pwd, new GmSession.ConnectCallback() {
                @Override
                public void onReady(String wizhood, int online) {
                    if (isFinishing() || isDestroyed()) return;
                    setLoginBusy(false);
                    ui.removeCallbacks(progressTick);
                    loginLay.setVisibility(View.GONE);
                    mainLay.setVisibility(View.VISIBLE);
                    whoView.setText(account + "  [" + wizhood + "]");
                    writeLastDiag("GM 登录成功（" + wizhood + "）");
                    loadList();
                }

                @Override
                public void onError(String message) {
                    if (isFinishing() || isDestroyed()) return;
                    setLoginBusy(false);
                    ui.removeCallbacks(progressTick);
                    loginLay.setVisibility(View.VISIBLE);
                    mainLay.setVisibility(View.GONE);
                    statusView.setText(summarize(message));
                    writeLastDiag(summarize(message));
                    showDiag(message);
                }
            });
        } catch (Throwable t) {
            setLoginBusy(false);
            statusView.setText("启动连接失败：" + t);
            writeLastDiag("启动连接失败：" + t);
            return;
        }
        ui.postDelayed(progressTick, 1200);
    }

    private void doLogout() {
        if (session != null) {
            session.close();
            session = null;
        }
        setLoginBusy(false);
        mainLay.setVisibility(View.GONE);
        loginLay.setVisibility(View.VISIBLE);
        statusView.setText("已断开连接");
    }

    /** 连接期间锁定按钮，避免重复点击开出多条 GM 会话。 */
    private void setLoginBusy(boolean busy) {
        if (loginBt == null) return;
        loginBt.setEnabled(!busy);
        loginBt.setText(busy ? "连接中..." : "连 接");
    }

    /* ==================== 上次会话记录 ==================== */

    /**
     * 把当前阶段写盘（commit 同步落盘），进程被 System.exit / 崩溃带走也不会丢。
     * 写新记录时重置“已提示”标记，方便游戏登录页再提示一次。
     */
    private void writeLastDiag(String text) {
        if (prefs == null || text == null || text.length() == 0) return;
        String t = new java.text.SimpleDateFormat("MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());
        prefs.edit()
                .putString(KEY_LAST_DIAG, t + "  " + text)
                .remove(KEY_DIAG_SEEN)
                .commit();
    }

    private void clearLastDiag() {
        if (prefs == null) return;
        prefs.edit().remove(KEY_LAST_DIAG).remove(KEY_DIAG_SEEN).commit();
    }

    /* ==================== 数据加载 ==================== */

    private void loadList() {
        if (session == null || !session.isReady()) {
            toast("会话未就绪");
            return;
        }
        whoView.setText(account + "  [" + session.getWizhood() + "]  正在读取在线玩家...");
        session.gm("list", new GmSession.CommandCallback() {
            @Override
            public void onResult(boolean ok, String msg, JSONObject data) {
                if (!ok) {
                    toast(msg);
                    whoView.setText(account + "  读取失败：" + msg);
                    return;
                }
                renderPlayers(data == null ? null : data.optJSONArray("players"));
            }
        });
    }

    private void renderPlayers(JSONArray arr) {
        content.removeAllViews();
        int n = arr == null ? 0 : arr.length();
        whoView.setText(account + "  [" + session.getWizhood() + "]  在线 " + n + " 人");

        addTitle("在线玩家（" + n + "）");
        if (n == 0) {
            addTip("暂无在线玩家。可在上方输入玩家ID直接查询离线档案。");
            return;
        }

        for (int i = 0; i < n; i++) {
            JSONObject p = arr.optJSONObject(i);
            if (p == null) continue;
            final String pid = p.optString("id", "");
            if (pid.length() == 0) continue;

            String family = p.optString("family", "");
            StringBuilder sb = new StringBuilder();
            sb.append(p.optString("name", "")).append("  [").append(pid).append("]");
            sb.append("  Lv").append(p.optInt("level", 0));
            sb.append("  经验").append(p.optInt("exp", 0));
            if (family.length() > 0) sb.append("  ").append(family);

            LinearLayout row = makeRow(sb.toString());
            addRowButton(row, "查看", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPlayer(pid);
                }
            });
            content.addView(row);
        }
    }

    private void showPlayer(final String pid) {
        if (session == null || !session.isReady()) {
            toast("会话未就绪");
            return;
        }
        selectedId = pid.toLowerCase();
        content.removeAllViews();
        addTip("正在读取 " + pid + " ...");
        session.gm("show " + selectedId, new GmSession.CommandCallback() {
            @Override
            public void onResult(boolean ok, String msg, JSONObject data) {
                if (!ok) {
                    toast(msg);
                    content.removeAllViews();
                    addTip(msg);
                    return;
                }
                renderDetail(data);
            }
        });
    }

    private void renderDetail(JSONObject data) {
        content.removeAllViews();
        if (data == null) {
            addTip("没有返回数据");
            return;
        }

        final String pid = data.optString("id", selectedId);
        String pname = data.optString("name", "");
        boolean online = data.optInt("online", 0) == 1;

        addTitle("玩家 " + pname + "（" + pid + "）" + (online ? "  ·共同在线" : "  ·离线"));

        JSONObject dbase = data.optJSONObject("dbase");
        Set<String> shown = new HashSet<String>();

        for (String[] g : GROUPS) {
            addTitle(g[0]);
            for (String k : g[1].split(",")) {
                shown.add(k);
                addValueRow(pid, dbase, k);
            }
        }

        if (dbase != null) {
            List<String> others = new ArrayList<String>();
            Iterator<String> it = dbase.keys();
            while (it.hasNext()) {
                String k = it.next();
                if (shown.contains(k)) continue;
                Object v = dbase.opt(k);
                if (v instanceof JSONObject || v instanceof JSONArray) continue;
                others.add(k);
            }
            if (others.size() > 0) {
                addTitle("其他字段");
                for (String k : others) {
                    addValueRow(pid, dbase, k);
                }
            }
        }

        renderSkills(pid, data.optJSONArray("skills"));
        renderItems(pid, data.optJSONArray("items"));
    }

    private void renderSkills(final String pid, JSONArray skills) {
        addTitle("技能（等级填 0 或负数视为删除）");
        int n = skills == null ? 0 : skills.length();
        if (n == 0) {
            addTip("（无技能）");
            return;
        }
        for (int i = 0; i < n; i++) {
            JSONObject s = skills.optJSONObject(i);
            if (s == null) continue;
            final String sk = s.optString("id", "");
            final String lv = String.valueOf(s.optInt("level", 0));
            if (sk.length() == 0) continue;

            LinearLayout row = makeRow(sk + "  Lv" + lv);
            addRowButton(row, "改", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    promptInput("设置技能等级：" + sk, lv, new InputHandler() {
                        @Override
                        public void onInput(String text) {
                            execRaw(pid, "skill " + pid + " " + sk + " " + text);
                        }
                    });
                }
            });
            content.addView(row);
        }
    }

    private void renderItems(final String pid, JSONArray items) {
        addTitle("身上物品");
        int n = items == null ? 0 : items.length();
        if (n == 0) {
            addTip("（无物品）");
        } else {
            for (int i = 0; i < n; i++) {
                JSONObject it = items.optJSONObject(i);
                if (it == null) continue;
                String nm = it.optString("name", "");
                String file = it.optString("file", "");
                addTip(nm + "    " + file);
            }
        }

        LinearLayout row = makeRow("发放物品（填写物品文件路径，如 /obj/food/xxx）");
        addRowButton(row, "发放", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptInput("发放物品给 " + pid, "/", new InputHandler() {
                    @Override
                    public void onInput(String text) {
                        if (text.length() == 0) return;
                        execRaw(pid, "give " + pid + " " + text);
                    }
                });
            }
        });
        content.addView(row);
    }

    /* ==================== 写操作 ==================== */

    private void addValueRow(final String pid, JSONObject dbase, final String key) {
        final String val = dbaseValue(dbase, key);

        LinearLayout row = makeRow(labelOf(key) + "：" + val + "   (" + key + ")");
        addRowButton(row, "改", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptInput("设置 " + labelOf(key), val, new InputHandler() {
                    @Override
                    public void onInput(String text) {
                        execRaw(pid, "set " + pid + " " + key + " " + text);
                    }
                });
            }
        });
        if (isNumber(val)) {
            addRowButton(row, "+", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    promptInput("增加 " + labelOf(key) + "（填负数即减少）", "0", new InputHandler() {
                        @Override
                        public void onInput(String text) {
                            execRaw(pid, "add " + pid + " " + key + " " + text);
                        }
                    });
                }
            });
        }
        content.addView(row);
    }

    /** 执行一条完整子命令，成功后刷新该玩家档案。 */
    private void execRaw(final String pid, final String fullSub) {
        if (session == null || !session.isReady()) {
            toast("会话未就绪");
            return;
        }
        session.gm(fullSub, new GmSession.CommandCallback() {
            @Override
            public void onResult(boolean ok, String msg, JSONObject data) {
                toast(msg);
                if (ok) showPlayer(pid);
            }
        });
    }

    /* ==================== 工具 ==================== */

    private void addTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(C_TITLE_FG);
        tv.setTextSize(14f);
        tv.setBackgroundColor(C_TITLE_BG);
        tv.setPadding(dp(8), dp(6), dp(8), dp(6));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(10);
        tv.setLayoutParams(lp);
        content.addView(tv);
    }

    private void addTip(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(C_DIM);
        tv.setTextSize(12.5f);
        tv.setPadding(dp(10), dp(5), dp(10), dp(5));
        content.addView(tv);
    }

    private LinearLayout makeRow(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(C_ROW_BG);
        row.setPadding(dp(10), dp(4), dp(6), dp(4));
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(C_TEXT);
        tv.setTextSize(13f);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tv);

        return row;
    }

    private void addRowButton(LinearLayout row, String text, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(12f);
        b.setPadding(dp(10), 0, dp(10), 0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(34));
        lp.leftMargin = dp(6);
        b.setLayoutParams(lp);
        row.addView(b);
    }

    private void promptInput(String title, String def, final InputHandler handler) {
        final EditText et = new EditText(this);
        et.setSingleLine(true);
        et.setText(def == null ? "" : def);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.onInput(et.getText().toString().trim());
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private String labelOf(String key) {
        String s = LABELS.get(key);
        return s == null ? key : s;
    }

    private String dbaseValue(JSONObject dbase, String key) {
        if (dbase == null) return "-";
        try {
            if (key.indexOf('/') > 0) {
                String[] parts = key.split("/");
                Object cur = dbase;
                for (String p : parts) {
                    if (!(cur instanceof JSONObject)) return "-";
                    cur = ((JSONObject) cur).opt(p);
                }
                return scalar(cur);
            }
            if (!dbase.has(key)) return "-";
            return scalar(dbase.opt(key));
        } catch (Exception e) {
            return "-";
        }
    }

    private String scalar(Object v) {
        if (v == null || v == JSONObject.NULL) return "-";
        if (v instanceof JSONObject || v instanceof JSONArray) return "(复合字段)";
        return String.valueOf(v);
    }

    private boolean isNumber(String s) {
        if (s == null || s.length() == 0) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i == 0 && (c == '-' || c == '+')) continue;
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    private int dp(float v) {
        return (int) (v * density + 0.5f);
    }

    private void toast(String msg) {
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /** 状态栏只显示第一行，详细内容走弹窗。 */
    private String summarize(String msg) {
        int i = msg.indexOf('\n');
        return i < 0 ? msg : msg.substring(0, i);
    }

    /** 显示连接诊断（可选中复制）。 */
    private void showDiag(String msg) {
        if (isFinishing() || isDestroyed()) return;
        try {
            TextView tv = new TextView(this);
            tv.setText(msg);
            tv.setTextSize(12.5f);
            tv.setTextColor(C_TEXT);
            tv.setTextIsSelectable(true);
            tv.setPadding(dp(14), dp(14), dp(14), dp(14));

            ScrollView sv = new ScrollView(this);
            sv.addView(tv);

            new AlertDialog.Builder(this)
                    .setTitle("GM 连接诊断")
                    .setView(sv)
                    .setPositiveButton("知道了", null)
                    .show();
        } catch (Throwable t) {
            /* 面板已不在前台时弹窗会抛 BadTokenException，这不该让整个进程退出 */
            toast("诊断信息：" + summarize(msg));
        }
    }
}
