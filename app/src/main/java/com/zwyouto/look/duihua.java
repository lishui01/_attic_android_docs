package com.zwyouto.look;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.zwyouto.main.mudmaind;
import com.zwyouto.main.mudsocketd;
import java.io.IOException;

import static com.zwyouto.R.*;

public class duihua extends Activity {
    private EditText number;
    private String okmudout = "";
    private String nomudout = "";
    private final DisplayMetrics pingmu = new DisplayMetrics();

    protected int makebklv(String str) {
        switch (Integer.parseInt(str)) {
            case 1:
                return drawable.wht;
            case 2:
                return drawable.grn;
            case 3:
                return drawable.blu;
            case 4:
                return drawable.zis;
            case 5:
                return drawable.god;
            case 6:
                return drawable.red;
            default:
                return drawable.cry;
        }
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        setContentView(layout.duihuax);
        mudsocketd mudsock = (mudsocketd) getApplication();
        getWindowManager().getDefaultDisplay().getMetrics(this.pingmu);
        LinearLayout duihuatexts = findViewById(id.duihuatexts);
        LinearLayout give_items = findViewById(id.give_items);
        this.number = findViewById(id.numbers);
        this.number.getLayoutParams().height = this.pingmu.widthPixels / 10;
        this.number.setTextSize(0, (float) this.pingmu.widthPixels / 26);
        TextView give_exp = findViewById(id.give_exp);
        give_exp.setTextSize(0, (float) this.pingmu.widthPixels / 26);
        TextView give_money = findViewById(id.give_money);
        give_money.setTextSize(0, (float) this.pingmu.widthPixels / 26);
        Button gbdh = findViewById(id.gbdh);
        gbdh.getLayoutParams().height = this.pingmu.widthPixels / 9;
        gbdh.getLayoutParams().width = this.pingmu.widthPixels / 4;
        gbdh.setTextSize(0, (float) this.pingmu.widthPixels / 26);
        Button qxdh = findViewById(id.qxdh);
        qxdh.getLayoutParams().height = this.pingmu.widthPixels / 9;
        qxdh.getLayoutParams().width = this.pingmu.widthPixels / 4;
        qxdh.setTextSize(0, (float) this.pingmu.widthPixels / 26);
        String[] duihuass = mudsock.getsencelong().split("\\$dh#");
        for (String s : duihuass) {
            if (s.length() > 5 && s.startsWith("ok11.")) {
                this.okmudout += s.substring(5) + "$sock#";
            } else if (s.length() > 5 && s.startsWith("no11.")) {
                this.nomudout = s.substring(5);
                qxdh.setVisibility(View.VISIBLE);
            } else if (s.length() <= 5 || !s.startsWith("numb.")) {
                String[] duihuasss = s.split("\\$br#");
                for (int i2 = 0; i2 < duihuasss.length; i2++) {
                    if (duihuasss[i2].length() > 5 && duihuasss[i2].startsWith("$exp#")) {
                        give_exp.setVisibility(View.VISIBLE);
                        give_exp.setText(duihuasss[i2].substring(5));
                    } else if (duihuasss[i2].length() > 5 && duihuasss[i2].startsWith("$obj#")) {
                        ImageView imageView = new ImageView(this);
                        imageView.setTag(duihuasss[i2].substring(5).split(",")[0]);
                        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        int i3 = this.pingmu.densityDpi / 160;
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
                        layoutParams.setMargins(i3, 0, i3 * 5, i3 * 2);
                        layoutParams.height = this.pingmu.widthPixels / 6;
                        layoutParams.width = this.pingmu.widthPixels / 6;
                         
// android.view.View.OnClickListener
                        imageView.setOnClickListener(view -> mudsocketd.mudout("litem " + view.getTag().toString()));
                        imageView.setBackgroundResource(makebklv(duihuasss[i2].substring(5).split(",")[2]));
                        try {
                            imageView.setImageBitmap(BitmapFactory.decodeStream(getAssets().open("item/" + duihuasss[i2].substring(5).split(",")[1] + ".png")));
                        } catch (IOException e) {
                            imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), drawable.icon));
                        }
                        give_items.setVisibility(View.VISIBLE);
                        give_items.addView(imageView, layoutParams);
                    } else if (duihuasss[i2].length() <= 5 || !duihuasss[i2].startsWith("$god#")) {
                        TextView textView = new TextView(this);
                        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
                        layoutParams2.setMargins(0, 0, 0, (this.pingmu.densityDpi * 4) / 160);
                        textView.setTextSize(0, (float) this.pingmu.widthPixels / 26);
                        if (duihuasss[i2].length() > 1 && duihuasss[i2].charAt(0) == '#') {
                            textView.setTextColor(Color.parseColor(duihuasss[i2].substring(0, 7)));
                            duihuasss[i2] = duihuasss[i2].substring(7);
                        }
                        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                        if (duihuasss[i2].contains("\u001b")) {
                            for (String str : duihuasss[i2].split("\u001b")) {
                                CharSequence takespan = mudmaind.takespan(str);
                                if (takespan != null) {
                                    spannableStringBuilder.append(takespan);
                                }
                            }
                        } else {
                            CharSequence takespan2 = mudmaind.takespan(duihuasss[i2]);
                            if (takespan2 != null) {
                                spannableStringBuilder.append(takespan2);
                            }
                        }
                        textView.setText(spannableStringBuilder);
                        duihuatexts.addView(textView, layoutParams2);
                    } else {
                        give_money.setVisibility(View.VISIBLE);
                        give_money.setText(duihuasss[i2].substring(5));
                    }
                }
            } else {
                this.number.setVisibility(View.VISIBLE);
            }
        }
        setFinishOnTouchOutside(false);
         
// android.view.View.OnClickListener
        View.OnClickListener onClickListener = view -> {
            switch (view.getId()) {
                case id.gbdh /* 2131230751 */:
                    if (duihua.this.number.getText().toString().equals("") && duihua.this.number.getVisibility() == View.VISIBLE) {
                        Toast makeText = Toast.makeText(duihua.this.getBaseContext(), "锟斤拷锟斤拷锟斤拷锟斤拷为锟秸ｏ拷", Toast.LENGTH_SHORT);
                        makeText.setGravity(17, 0, 0);
                        makeText.show();
                        return;
                    }
                    duihua.this.finish();
                    if (duihua.this.number.getVisibility() == View.VISIBLE && !duihua.this.number.getText().toString().equals("")) {
                        duihua.this.okmudout = duihua.this.okmudout.replace("$N", duihua.this.number.getText().toString());
                    }
                    if (duihua.this.okmudout.equals("")) {
                        return;
                    }
                    for (int i4 = 0; i4 < duihua.this.okmudout.split("\\$sock#").length; i4++) {
                        mudsocketd.mudout(duihua.this.okmudout.split("\\$sock#")[i4]);
                    }
                    return;
                case id.qxdh /* 2131230752 */:
                    if (!duihua.this.nomudout.equals("")) {
                        mudsocketd.mudout(duihua.this.nomudout);
                    }
                    duihua.this.finish();
                    return;
                default:
            }
        };
        gbdh.setOnClickListener(onClickListener);
        qxdh.setOnClickListener(onClickListener);
        mudsock.setsencelong("锟斤拷");
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return false;
    }
}