package com.zwyouto.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.zwyouto.R;
import com.zwyouto.mud.BuildConfig;
import com.zwyouto.myactivex.JCrypt;
import com.zwyouto.myactivex.XRTextView;
import com.zwyouto.myactivex.longitemd;
import wuxie.mktoast;

import java.security.MessageDigest;

public class logind extends Activity {
    private Button denglub;
    protected String email;
    private String key;
    private longitemd last_part;
    private Thread loginT;
    private RelativeLayout logind_lay;
    private SharedPreferences mdate;
    protected SharedPreferences.Editor mdateedit;
    private FrameLayout mengban1;
    private mudsocketd mudsock;
    private EditText nameedit;
    private longitemd new_part;
    private Button nicok;
    private Button nicqx;
    private int nn;
    private Button noenter;
    private Button noreg;
    private EditText passedit;
    private EditText regemail;
    private EditText regid;
    private RelativeLayout reglay;
    private RelativeLayout reglayx;
    private EditText regname;
    private Button regok;
    private EditText regpass;
    private EditText regpass2;
    private EditText regphone;
    private RadioGroup regsexs;
    private String result;
    private int scrh;
    private int scrw;
    private TableLayout server_ls;
    private RelativeLayout slist;
    private Button slistdown;
    private Button slistup;
    private EditText serveredit;
    private String slserver;
    private Button tuichub;
    private Button user_center;
    private Button zhuceb;
    private String sex = "男";
    private String content = "...";
    private boolean turn_page = true;
    private boolean loop = true;
    private int slistpage = 1;
    /* GM 后台隐藏入口：连点“你的账号：”标签的计数 */
    private int gmTapCount = 0;
    private long gmLastTap = 0L;
    private final DisplayMetrics pingmu = new DisplayMetrics();
    protected boolean verify = false;
    protected String register = BuildConfig.FLAVOR;
    public Handler mHandler = new AnonymousClass5();

    public String MD5(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            char[] charArray = str.toCharArray();
            byte[] bArr = new byte[charArray.length];
            for (int i = 0; i < charArray.length; i++) {
                bArr[i] = (byte) charArray[i];
            }
            byte[] digest = messageDigest.digest(bArr);
            StringBuilder stringBuffer = new StringBuilder();
            for (byte b : digest) {
                int i2 = b & 255;
                if (i2 < 16) {
                    stringBuffer.append("0");
                }
                stringBuffer.append(Integer.toHexString(i2));
            }
            return stringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return BuildConfig.FLAVOR;
        }
    }

    /**
     * zj 转义协议握手应答。
     * 服务端 logon() 发 "ver1.0,&lt;str&gt;"，客户端需回 crypt(ZJKEY, str[2..3])。
     * 本 lib 的 jiance() 其实不校验内容（str 恒等于 ZJKEY），这里按协议本地计算，
     * 不再请求 hugongzi.top 的 check.php 网关。
     */
    public static String zjAnswer(String str) {
        try {
            if (str == null || str.length() < 4) {
                return JCrypt.crypt("zj", mudsocketd.ZJKEY);
            }
            return JCrypt.crypt(str.substring(2, 4), mudsocketd.ZJKEY);
        } catch (Exception e) {
            return "zj";
        }
    }

    /** "账号║密码║密文║email" 里的密文：crypt(ZJKEY,账号)+crypt(ZJKEY,密码)。 */
    public static String zjCipher(String id, String pass) {
        try {
            return JCrypt.crypt(id, mudsocketd.ZJKEY) + JCrypt.crypt(pass, mudsocketd.ZJKEY);
        } catch (Exception e) {
            return "zj";
        }
    }

    public String getDiskCacheDir() {
        return ("mounted".equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) ? getExternalCacheDir().getAbsolutePath() : getCacheDir().getAbsolutePath();
    }

    public String getVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override // android.app.Activity
    @SuppressLint({"SetJavaScriptEnabled"})
    protected void onCreate(Bundle bundle) {
        mktoast.huluxia(this);
        super.onCreate(bundle);
        setContentView(R.layout.loginx);
        getWindowManager().getDefaultDisplay().getMetrics(this.pingmu);
        this.scrw = this.pingmu.widthPixels;
        this.scrh = this.pingmu.heightPixels;
        this.reglay = (RelativeLayout) findViewById(R.id.reg_id_lay);
        this.reglayx = (RelativeLayout) findViewById(R.id.reglayx);
        this.slist = (RelativeLayout) findViewById(R.id.slist);
        this.logind_lay = (RelativeLayout) findViewById(R.id.logind_lay);
        this.server_ls = (TableLayout) findViewById(R.id.server_ls);
        this.nameedit = (EditText) findViewById(R.id.nameedit);
        this.nameedit.getLayoutParams().height = this.scrw / 11;
        this.passedit = (EditText) findViewById(R.id.passedit);
        this.passedit.getLayoutParams().height = this.scrw / 11;
        this.regid = (EditText) findViewById(R.id.new_id);
        this.regid.getLayoutParams().height = this.scrw / 11;
        this.regname = (EditText) findViewById(R.id.newregname);
        this.regname.getLayoutParams().height = this.scrw / 11;
        this.regpass = (EditText) findViewById(R.id.new_pass);
        this.regpass.getLayoutParams().height = this.scrw / 11;
        this.regpass2 = (EditText) findViewById(R.id.new_pass2);
        this.regpass2.getLayoutParams().height = this.scrw / 11;
        this.regphone = (EditText) findViewById(R.id.new_phone);
        this.regphone.getLayoutParams().height = this.scrw / 11;
        this.regemail = (EditText) findViewById(R.id.new_email);
        this.regemail.getLayoutParams().height = this.scrw / 11;
        this.denglub = (Button) findViewById(R.id.denglubt);
        this.denglub.getLayoutParams().height = this.scrw / 10;
        this.tuichub = (Button) findViewById(R.id.tuichubt);
        this.tuichub.getLayoutParams().height = this.scrw / 10;
        this.zhuceb = (Button) findViewById(R.id.zhucebt);
        this.zhuceb.getLayoutParams().height = this.scrw / 10;
        this.regok = (Button) findViewById(R.id.reg_ok);
        this.regok.getLayoutParams().height = this.scrw / 10;
        this.noreg = (Button) findViewById(R.id.qx_reg);
        this.noreg.getLayoutParams().height = this.scrw / 10;
        this.nn = ((this.scrh - ((this.pingmu.densityDpi * 180) / 160)) - (this.scrw / 9)) - (this.scrw / 10);
        this.new_part = (longitemd) findViewById(R.id.new_part);
        this.new_part.setimg(BitmapFactory.decodeResource(getResources(), R.drawable.licon), this.nn / 10);
        this.new_part.settextsize1(this.scrw / 28);
        this.new_part.settext2v(8);
        this.new_part.setlpad((this.pingmu.densityDpi * 5) / 160);
        this.last_part = (longitemd) findViewById(R.id.last_part);
        this.last_part.setimg(BitmapFactory.decodeResource(getResources(), R.drawable.licon), this.nn / 10);
        this.last_part.settextsize1(this.scrw / 28);
        this.last_part.settext2v(8);
        this.last_part.setlpad((this.pingmu.densityDpi * 5) / 160);
        this.noenter = (Button) findViewById(R.id.noenter);
        this.noenter.getLayoutParams().height = this.scrw / 9;
        this.slistup = (Button) findViewById(R.id.slistup);
        this.slistup.getLayoutParams().height = this.scrw / 9;
        this.slistdown = (Button) findViewById(R.id.slistdown);
        this.slistdown.getLayoutParams().height = this.scrw / 9;
        this.user_center = (Button) findViewById(R.id.user_center);
        this.user_center.getLayoutParams().height = this.scrw / 9;
        this.nicok = (Button) findViewById(R.id.nicok);
        this.nicok.getLayoutParams().height = this.scrw / 9;
        this.nicqx = (Button) findViewById(R.id.nicqx);
        this.nicqx.getLayoutParams().height = this.scrw / 9;
        this.regsexs = (RadioGroup) findViewById(R.id.newregsex);
        this.mengban1 = (FrameLayout) findViewById(R.id.loginmengban);
        this.mudsock = (mudsocketd) getApplication();
        this.mdate = getSharedPreferences("zwyjh", 0);
        this.mdateedit = this.mdate.edit();
        this.nameedit.setText(this.mdate.getString("name", BuildConfig.FLAVOR));
        this.passedit.setText(this.mdate.getString("pass", BuildConfig.FLAVOR));
        this.serveredit = (EditText) findViewById(R.id.serveredit);
        this.serveredit.getLayoutParams().height = this.scrw / 11;
        String savedServer = this.mdate.getString("server", BuildConfig.FLAVOR);
        this.serveredit.setText(savedServer.equals(BuildConfig.FLAVOR)
                ? mudsocketd.DEFAULT_HOST + ":" + mudsocketd.DEFAULT_PORT : savedServer);
        this.regsexs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override // android.widget.RadioGroup.OnCheckedChangeListener
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == 2131230835) {
                    logind.this.sex = "男";
                } else {
                    logind.this.sex = "女";
                }
            }
        });
        View.OnClickListener onClickListener = new View.OnClickListener() {
            /*
            * 注册页*/
            /* 本地注册：服务端 get_user() 对不存在的账号会自动建档，
               所以只要把账号密码填回登录框、直接登录即可，无需 hugongzi.top。 */
            private void checkreg() {
                String id = logind.this.regid.getText().toString().trim();
                String p1 = logind.this.regpass.getText().toString();
                String p2 = logind.this.regpass2.getText().toString();
                if (id.equals(BuildConfig.FLAVOR) || p1.equals(BuildConfig.FLAVOR)) {
                    Toast makeText = Toast.makeText(logind.this.getBaseContext(), "账号和密码不能为空！", Toast.LENGTH_SHORT);
                    makeText.setGravity(17, 0, 0);
                    makeText.show();
                    return;
                }
                if (!p1.equals(p2)) {
                    Toast makeText2 = Toast.makeText(logind.this.getBaseContext(), "两次输入密码不一致！", Toast.LENGTH_SHORT);
                    makeText2.setGravity(17, 0, 0);
                    makeText2.show();
                    return;
                }
                logind.this.nameedit.setText(id);
                logind.this.passedit.setText(p1);
                logind.this.reglay.setVisibility(View.GONE);
                Toast.makeText(logind.this.getBaseContext(), "已填入账号密码，点击登录即可（新账号会自动创建）。", Toast.LENGTH_LONG).show();
            }

            /* 已移除 hugongzi.top 的服务器列表：登录即直连反代地址 */
            private void enter_list() {
                logind.this.connect_server();
            }

            private void myinput(int i, String str) {
                final EditText editText = new EditText(logind.this);
                final EditText editText2 = (EditText) logind.this.findViewById(i);
                editText.setText(editText2.getText().toString());

                new AlertDialog.Builder(logind.this).setTitle(str).setIcon(R.drawable.command).setCancelable(false).setView(editText).setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        editText2.setText(editText.getText().toString());
                    }
                }).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.nameedit :
                        myinput(R.id.nameedit, "请输入账号：");
                        return;
                    case R.id.passedit /* 2131230789 */:
                        myinput(R.id.passedit, "请输入密码：");
                        return;
                    case R.id.serveredit /* 服务器地址 */:
                        myinput(R.id.serveredit, "请输入服务器地址(域名或IP:端口)：");
                        return;
                    case R.id.denglubt /* 2131230792 */:
                        logind.this.connect_server();
                        return;
                    case R.id.zhucebt /* 2131230793 */:
                        logind.this.reglay.setVisibility(View.VISIBLE);
                        return;
                    case R.id.tuichubt /* 2131230794 */:
                    case R.id.nicqx /* 2131230840 */:
                        System.exit(0);
                        return;
                    case R.id.slistup /* 2131230807 */:
                        if (logind.this.slistpage > 1) {
                            logind.this.slistpage--;
                            logind.this.turn_page = false;
                            enter_list();
                            return;
                        }
                        return;
                    case R.id.slistdown /* 2131230808 */:
                        logind.this.slistpage++;
                        logind.this.turn_page = false;
                        enter_list();
                        return;
                    case R.id.user_center /* 2131230809 */:
                        Toast.makeText(logind.this.getBaseContext(), "账号即游戏账号，直接登录即可；新账号会自动创建。", Toast.LENGTH_SHORT).show();
                        return;
                    case R.id.noenter /* 2131230810 */:
                        logind.this.slist.setVisibility(View.GONE);
                        logind.this.logind_lay.setVisibility(View.VISIBLE);
                        return;
                    case R.id.new_id /* 2131230815 */:
                        myinput(R.id.new_id, "请输4-12位的ID(字母开头可包含数字)：");
                        return;
                    case R.id.new_pass /* 2131230817 */:
                        myinput(R.id.new_pass, "请输入15位以内的密码：");
                        return;
                    case R.id.new_pass2 /* 2131230819 */:
                        myinput(R.id.new_pass2, "请再次输入密码：");
                        return;
                    case R.id.new_phone /* 2131230822 */:
                        myinput(R.id.new_phone, "请输入11位数字的手机号：");
                        return;
                    case R.id.new_email /* 2131230825 */:
                        myinput(R.id.new_email, "请输入你的邮箱号：");
                        return;
                    case R.id.reg_ok /* 2131230828 */:
                        checkreg();
                        return;
                    case R.id.qx_reg /* 2131230829 */:
                        logind.this.reglay.setVisibility(View.GONE);
                        logind.this.logind_lay.setVisibility(View.VISIBLE);
                        return;
                    case R.id.newregname /* 2131230838 */:
                        myinput(R.id.newregname, "请输你游戏中要用的名字(中文)：");
                        return;
                    case R.id.nicok /* 2131230839 */:
                        logind.this.mengban1.setVisibility(View.VISIBLE);
                        mudsocketd.mudout(logind.this.sex + "║║" + logind.this.regname.getText().toString());
                        return;
                    default:
                        return;
                }
            }
        };
        this.user_center.setOnClickListener(onClickListener);
        this.nameedit.setOnClickListener(onClickListener);
        this.passedit.setOnClickListener(onClickListener);
        this.serveredit.setOnClickListener(onClickListener);
        this.regid.setOnClickListener(onClickListener);
        this.regpass.setOnClickListener(onClickListener);
        this.regpass2.setOnClickListener(onClickListener);
        this.regname.setOnClickListener(onClickListener);
        this.regphone.setOnClickListener(onClickListener);
        this.regemail.setOnClickListener(onClickListener);
        this.denglub.setOnClickListener(onClickListener);
        this.tuichub.setOnClickListener(onClickListener);
        this.zhuceb.setOnClickListener(onClickListener);
        this.regok.setOnClickListener(onClickListener);
        this.noreg.setOnClickListener(onClickListener);
        this.new_part.setOnClickListener(onClickListener);
        this.last_part.setOnClickListener(onClickListener);
        this.noenter.setOnClickListener(onClickListener);
        this.nicok.setOnClickListener(onClickListener);
        this.nicqx.setOnClickListener(onClickListener);
        this.slistup.setOnClickListener(onClickListener);
        this.slistdown.setOnClickListener(onClickListener);
        this.loginT = new Thread(new Runnable() {
            @Override // java.lang.Runnable
            public void run() {
                while (logind.this.loop) {
                    logind.this.content = logind.this.mudsock.mudin();
                    if (logind.this.content != null && logind.this.content.length() > 3) {
                        logind.this.mHandler.sendMessage(logind.this.mHandler.obtainMessage(1));
                        try {
                            Thread.sleep(200L);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        Thread.sleep(200L);
                    } catch (InterruptedException e2) {
                        throw new RuntimeException(e2);
                    }
                }
            }
        });
        /* 已移除 hugongzi.top 的 vers.php 版本/公告拉取：直接进入登录界面 */
        this.logind_lay.setVisibility(View.VISIBLE);
        /* GM 后台隐藏入口：连点“你的账号：”标签 7 次进入 GM 管理面板 */
        findViewById(R.id.zhanghao).setOnClickListener(new View.OnClickListener() {
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                long now = System.currentTimeMillis();
                if (now - logind.this.gmLastTap > 1500L) {
                    logind.this.gmTapCount = 0;
                }
                logind.this.gmLastTap = now;
                logind.this.gmTapCount++;
                if (logind.this.gmTapCount >= 7) {
                    logind.this.gmTapCount = 0;
                    logind.this.gmLastTap = 0L;
                    logind.this.startActivity(new Intent(logind.this, GmActivity.class));
                }
            }
        });
    }

    /**
     * 直连反代服务器：解析地址框里的 "host:port"（可省略端口），
     * 填写账号密码后建立 socket 连接。
     */
    public void connect_server() {
        String id = this.nameedit.getText().toString().trim();
        String pass = this.passedit.getText().toString();
        if (id.equals(BuildConfig.FLAVOR) || pass.equals(BuildConfig.FLAVOR)) {
            Toast makeText = Toast.makeText(getBaseContext(), "请输入账号和密码！", Toast.LENGTH_SHORT);
            makeText.setGravity(17, 0, 0);
            makeText.show();
            return;
        }
        String addr = this.serveredit.getText().toString().trim();
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
        if (host.equals(BuildConfig.FLAVOR)) {
            host = mudsocketd.DEFAULT_HOST;
        }
        this.mdateedit.putString("server", addr);
        this.mdateedit.putString("name", id);
        this.mdateedit.putString("pass", pass);
        this.mdateedit.commit();
        this.mudsock.set_ucenter_id(id);
        this.mudsock.set_ucenter_pass(pass);
        this.mudsock.set_ucenter_email(id + "@zj");
        this.mudsock.setAddr(host);
        this.mudsock.setPort(port);
        this.mudsock.setgamepart(host + ":" + port);
        this.mudsock.setgameip(host);
        this.mudsock.setgameport(String.valueOf(port));
        this.mengban1.setVisibility(View.VISIBLE);
        this.mudsock.new_sock(host, port);
        new Thread(() -> {
            for (int i = 0; i < 30; i++) {
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                }
                if (logind.this.mudsock.isaction()) {
                    break;
                }
            }
            logind.this.mHandler.sendMessage(logind.this.mHandler.obtainMessage(3));
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showGmDiagIfAny();
        showCrashIfAny();
    }

    /** 若上次进程崩溃，把堆栈直接弹出来（可长按复制），读完即删。 */
    private void showCrashIfAny() {
        try {
            java.io.File f = new java.io.File(getFilesDir(), "gm_crash.txt");
            if (!f.exists()) return;
            byte[] buf = new byte[(int) f.length()];
            java.io.FileInputStream fis = new java.io.FileInputStream(f);
            int n = fis.read(buf);
            fis.close();
            // 先删再显示，避免每次启动都弹
            if (!f.delete()) f.renameTo(new java.io.File(getFilesDir(), "gm_crash.old"));
            if (n <= 0) return;
            TextView tv = new TextView(this);
            tv.setText(new String(buf, 0, n, mudsocketd.MUD_CHARSET));
            tv.setTextSize(11.0f);
            tv.setTextIsSelectable(true);
            ScrollView sv = new ScrollView(this);
            sv.addView(tv);
            new AlertDialog.Builder(this)
                    .setTitle("上次闪退原因")
                    .setView(sv)
                    .setPositiveButton("知道了", (DialogInterface.OnClickListener) null)
                    .show();
        } catch (Throwable ignore) {
            // 读取失败不影响登录
        }
    }

    /**
     * GM 面板若被异常终止（崩溃 / 进程被 System.exit 杀掉），会在这里
     * 把最后一次记录提示出来，方便判断是“点连接后就没了”还是“登录成功后才断”。
     */
    private void showGmDiagIfAny() {
        if (this.mdate == null) return;
        String diag = this.mdate.getString("gm_last_diag", "");
        if (diag.equals(BuildConfig.FLAVOR)) return;
        if (diag.equals(this.mdate.getString("gm_diag_seen", ""))) return;
        this.mdate.edit().putString("gm_diag_seen", diag).commit();
        Toast.makeText(getBaseContext(), "GM面板记录：" + diag, Toast.LENGTH_LONG).show();
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return false;
    }

    public void unzipok() {
    }



    class AnonymousClass5 extends Handler {

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            if (message.what == 2) {
                if (logind.this.result.equals("注册成功")) {
                    logind.this.nameedit.setText(logind.this.regid.getText().toString());
                    logind.this.passedit.setText(logind.this.regpass.getText().toString());
                    logind.this.reglay.setVisibility(View.GONE);
                    logind.this.mengban1.setVisibility(View.GONE);
                } else if (logind.this.result.equals("最后一页")) {
                    logind.this.slistpage--;
                    logind.this.mengban1.setVisibility(View.GONE);
                    Toast.makeText(logind.this.getBaseContext(), logind.this.result, Toast.LENGTH_SHORT).show();
                } else if (!logind.this.result.subSequence(0, 3).equals("$l#")) {
                    logind.this.mengban1.setVisibility(View.GONE);
                    Toast.makeText(logind.this.getBaseContext(), logind.this.result, Toast.LENGTH_SHORT).show();
                } else {
                    logind.this.slist.setVisibility(View.VISIBLE);
                    logind.this.mengban1.setVisibility(View.GONE);
                    logind.this.server_ls.removeAllViews();
                    String[] split = logind.this.result.substring(3).split("\\|");
                    if (logind.this.slistpage == 1 && logind.this.mudsock.get_ucenter_id().equals(BuildConfig.FLAVOR)) {
                        logind.this.mdateedit.putString("name", logind.this.nameedit.getText().toString());
                        logind.this.mdateedit.putString("pass", logind.this.passedit.getText().toString());
                        logind.this.mdateedit.commit();
                        logind.this.mudsock.set_ucenter_id(logind.this.nameedit.getText().toString());
                        logind.this.mudsock.set_ucenter_pass(logind.this.passedit.getText().toString());
                        logind.this.email = split[split.length - 1];
                        logind.this.mudsock.set_ucenter_email(logind.this.email);
                        logind.this.new_part.settext1(split[0].split("&")[0]);
                        logind.this.new_part.setTag(split[0]);

// android.view.View.OnClickListener
                        logind.this.new_part.setOnClickListener(view -> {
                            logind.this.mengban1.setVisibility(View.VISIBLE);
                            logind.this.slserver = view.getTag().toString();
                            logind.this.key = logind.this.slserver.split("&")[5];
                            logind.this.mudsock.new_sock(logind.this.slserver.split("&")[1], Integer.valueOf(logind.this.slserver.split("&")[2]).intValue());

// java.lang.Runnable
                            new Thread(() -> {
                                for (int i = 0; i < 30; i++) {
                                    try {
                                        Thread.sleep(200L);
                                    } catch (InterruptedException e) {
                                    }
                                    if (logind.this.mudsock.isaction()) {
                                        break;
                                    }
                                }
                                logind.this.mHandler.sendMessage(logind.this.mHandler.obtainMessage(3));
                            }).start();
                        });
                        String string = logind.this.mdate.getString("last_part", "无");
                        if (!string.equals("无")) {
                            logind.this.last_part.settext1(string.split("&")[0]);
                            logind.this.last_part.setTag(string);

// android.view.View.OnClickListener
                            logind.this.last_part.setOnClickListener(view -> {
                                logind.this.mengban1.setVisibility(View.VISIBLE);
                                logind.this.slserver = view.getTag().toString();
                                logind.this.key = logind.this.slserver.split("&")[5];
                                logind.this.mudsock.new_sock(logind.this.slserver.split("&")[1], Integer.valueOf(logind.this.slserver.split("&")[2]).intValue());

// java.lang.Runnable
                                new Thread(() -> {
                                    for (int i = 0; i < 30; i++) {
                                        try {
                                            Thread.sleep(200L);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                        if (logind.this.mudsock.isaction()) {
                                            break;
                                        }
                                    }
                                    logind.this.mHandler.sendMessage(logind.this.mHandler.obtainMessage(3));
                                }).start();
                            });
                        }
                    }
                    for (int i = 0; i < split.length - 1; i++) {
                        longitemd longitemdVar = new longitemd(logind.this);
                        longitemdVar.settext1(split[i].split("&")[0]);
                        longitemdVar.setTag(split[i]);

// android.view.View.OnClickListener
                        longitemdVar.setOnClickListener(view -> {
                            logind.this.mengban1.setVisibility(View.VISIBLE);
                            logind.this.slserver = view.getTag().toString();
                            logind.this.key = logind.this.slserver.split("&")[5];
                            logind.this.mudsock.new_sock(logind.this.slserver.split("&")[1], Integer.valueOf(logind.this.slserver.split("&")[2]).intValue());
                            new Thread(new Runnable() {


                                @Override // java.lang.Runnable
                                public void run() {
                                    for (int i2 = 0; i2 < 30; i2++) {
                                        try {
                                            Thread.sleep(200L);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                        if (logind.this.mudsock.isaction()) {
                                            break;
                                        }
                                    }
                                    logind.this.mHandler.sendMessage(logind.this.mHandler.obtainMessage(3));
                                }
                            }).start();
                        });
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
                        layoutParams.setMargins((logind.this.pingmu.densityDpi * 10) / 160, (logind.this.pingmu.densityDpi * 5) / 160, (logind.this.pingmu.densityDpi * 10) / 160, 0);
                        layoutParams.width = (logind.this.scrw - ((logind.this.pingmu.densityDpi * 62) / 160)) / 2;
                        longitemdVar.settextsize1(logind.this.scrw / 28);
                        longitemdVar.settext2v(8);
                        longitemdVar.setlpad((logind.this.pingmu.densityDpi * 5) / 160);
                        longitemdVar.setimg(BitmapFactory.decodeResource(logind.this.getResources(), R.drawable.licon), logind.this.nn / 10);
                        RelativeLayout relativeLayout = new RelativeLayout(logind.this);
                        relativeLayout.addView(longitemdVar, layoutParams);
                        relativeLayout.setGravity(17);
                        boolean z = false;
                        int i2 = 0;
                        while (true) {
                            if (i2 >= logind.this.server_ls.getChildCount()) {
                                break;
                            }
                            TableRow tableRow = (TableRow) logind.this.server_ls.getChildAt(i2);
                            if (tableRow.getChildCount() < 1) {
                                tableRow.addView(relativeLayout);
                                z = true;
                                break;
                            }
                            i2++;
                        }
                        if (!z) {
                            TableRow tableRow2 = new TableRow(logind.this);
                            tableRow2.addView(relativeLayout);
                            logind.this.server_ls.addView(tableRow2);
                        }
                    }
                }
            } else if (message.what == 3) {
                if (logind.this.mudsock.isaction()) {
                    logind.this.loginT.start();
                } else {
                    new AlertDialog.Builder(logind.this).setTitle("连接超时！").setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i3) {
                            logind.this.mengban1.setVisibility(View.GONE);
                        }
                    }).show();
                }
            } else if (message.what == 4) {
                mudsocketd.mudout(logind.this.result);
            } else if (message.what == 5) {
                logind.this.finish();
                System.exit(0);
            } else if (message.what == 8) {
                if (!logind.this.content.contains("|")) {
                    if (logind.this.content.equals(logind.this.getVersion())) {
                        logind.this.logind_lay.setVisibility(View.VISIBLE);
                        return;
                    }
                    XRTextView xRTextView = new XRTextView(logind.this, logind.this.scrw / 25.0f, -16777216, 5.0f, 2.0f, 2.0f, 2.0f);
                    xRTextView.setText("您当前使用的客户端版本不是最新版本，如果现有版本不能正常进行游戏，请进入官方网站或者游戏群里下载最新版本！");
                    xRTextView.setBackgroundColor(-1);
                    new AlertDialog.Builder(logind.this).setTitle("版本更新提示：").setView(xRTextView).setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i3) {
                            logind.this.logind_lay.setVisibility(View.VISIBLE);
                        }
                    }).show();
                    return;
                }
                String[] split2 = logind.this.content.split("\\|");
                TextView textView = new TextView(logind.this);
                textView.setPadding(15, 5, 15, 5);
                String str = split2[1];
                if (split2.length > 2) {
                    logind.this.mudsock.setpay(false);
                }
                if (!split2[0].equals(logind.this.getVersion())) {
                    str = split2[1] + "\n您当前使用的客户端版本不是最新版本，请进群下载最新版本";
                }
                textView.setText(String.format("%s！", str));
                textView.setBackgroundColor(-1);
                new AlertDialog.Builder(logind.this).setTitle("服务器提醒：").setView(textView).setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {


                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i3) {
                        logind.this.logind_lay.setVisibility(View.VISIBLE);
                    }
                }).show();
            } else {
                if (logind.this.content.equals("与服务器断开连接！")) {
                    logind.this.loop = false;
                    /* GM 面板在前台时不要弹框、更不能 System.exit(0)：
                       那会把整个进程连同 GM 面板一起杀掉，表现为“点登录就弹回登录页”。 */
                    if (GmActivity.isPanelActive()) {
                        return;
                    }
                    new AlertDialog.Builder(logind.this).setTitle("与服务器失去连接！").setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {


                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i3) {
                            System.exit(0);
                        }
                    }).show();
                }
                if (logind.this.content == null || logind.this.content.length() < 4) {
                    return;
                }
                String substring = logind.this.content.substring(1, 4);
                if (!substring.equals("000")) {
                    if (substring.equals("015")) {
                        Toast makeText = Toast.makeText(logind.this.getBaseContext(), logind.this.content.substring(4), Toast.LENGTH_SHORT);
                        makeText.setGravity(17, 0, 0);
                        makeText.show();
                        logind.this.mengban1.setVisibility(View.GONE);
                    } else if (logind.this.content.substring(0, 3).equals("ver")) {
                        final String[] split3 = logind.this.content.split(",");
                        // 本地生成 zj 握手应答，不再请求 hugongzi.top/mobi/check.php
                        mudsocketd.mudout(zjAnswer(split3.length > 1 ? split3[1] : BuildConfig.FLAVOR));
                    } else if (logind.this.content.equals("版本验证成功")) {
                        logind.this.verify = true;
                        String myid = logind.this.mudsock.get_ucenter_id();
                        String mypass = logind.this.mudsock.get_ucenter_pass();
                        mudsocketd.mudout(myid + "║" + mypass + "║" + zjCipher(myid, mypass) + "║" + logind.this.mudsock.get_ucenter_email());
                    } else if (logind.this.content.equals("客户端非法")) {
                        Toast.makeText(logind.this.getBaseContext(), logind.this.content, Toast.LENGTH_SHORT).show();
                    }
                } else if (!logind.this.verify) {
                    logind.this.loop = false;
                    if (GmActivity.isPanelActive()) {
                        return;
                    }
                    new AlertDialog.Builder(logind.this).setTitle("服务器验证失败！").setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {


                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i3) {
                            System.exit(0);
                        }
                    }).show();
                } else if (logind.this.content.substring(4).equals("0003")) {
                    mudsocketd.mudout(logind.this.passedit.getText().toString());
                } else if (logind.this.content.substring(4).equals("0008")) {
                    if (GmActivity.isPanelActive()) {
                        return;
                    }
                    logind.this.reglayx.setVisibility(View.VISIBLE);
                    logind.this.mengban1.setVisibility(View.GONE);
                } else if (logind.this.content.substring(4).equals("0007")) {
                    /* GM 面板在前台时不要把主场景顶上来把面板挤掉 */
                    if (GmActivity.isPanelActive()) {
                        return;
                    }
                    logind.this.startActivity(new Intent(logind.this, mudmaind.class));
                    logind.this.loop = false;
                    logind.this.finish();
                }
            }
        }

        AnonymousClass5() {
        }
    }
}