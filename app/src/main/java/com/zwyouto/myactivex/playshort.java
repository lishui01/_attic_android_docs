package com.zwyouto.myactivex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.zwyouto.R;
import java.io.IOException;

public class playshort extends LinearLayout {
    private LinearLayout fskills;
    private TextView mname;
    private attrbar neili;
    private attrbar qixue;
    private ImageView short_img;
    private RelativeLayout short_img_zt;

    public String getname() {
        return this.mname.getText().toString();
    }

    protected void inflaterLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.playershortx, (ViewGroup) this, true);
    }

    public void set_img(String str) {
        try {
            this.short_img.setImageBitmap(BitmapFactory.decodeStream(getContext().getAssets().open("img/" + str + ".jpg")));
        } catch (IOException e) {
            try {
                this.short_img.setImageBitmap(BitmapFactory.decodeStream(getContext().getAssets().open("npc/" + str + ".png")));
            } catch (IOException e2) {
                this.short_img.setImageResource(R.drawable.icon);
            }
        }
    }

    public void set_img_zt_v(int i) {
        this.short_img_zt.setVisibility(i);
    }

    public void setbkcolor(String str) {
        setBackgroundColor(Color.parseColor(str));
    }

    public void setfskill(String str) {
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));
        animationSet.addAnimation(new TranslateAnimation(0.0f, 0.0f, 0.0f, -15.0f));
        animationSet.setDuration(2000L);
        animationSet.setFillEnabled(true);
        animationSet.setFillAfter(true);
        TextView textView = new TextView(getContext());
        textView.setGravity(17);
        textView.setTextColor(-65536);
        textView.setTextSize(2, 11.0f);
        textView.setBackgroundColor(Color.parseColor("#99ffffff"));
        textView.setText(str);
        this.fskills.addView(textView);
        textView.startAnimation(animationSet);
        if (this.fskills.getChildCount() > 2) {
            this.fskills.removeViewAt(0);
        }
    }

    public void setname(String str) {
        this.mname.setText(str);
    }

    public void setnamesize(int i) {
        this.mname.setTextSize(0, i);
    }

    public void setneili(String str) {
        String[] split = str.split("：")[1].split("/");
        this.neili.setTextss(str);
        int parseInt = Integer.parseInt(split[0]);
        int parseInt2 = Integer.parseInt(split[1]);
        this.neili.setWB(this.neili.getWidth());
        this.neili.setWA((this.neili.getWidth() * parseInt) / (parseInt2 + 1));
    }

    public void setqineisize(int i) {
        this.qixue.setsize(i);
        this.neili.setsize(i);
    }

    public void setqixue(String str) {
        String[] split = str.split("：")[1].split("/");
        this.qixue.setTextss(str);
        int parseInt = Integer.parseInt(split[0]);
        int parseInt2 = Integer.parseInt(split[1]);
        int parseInt3 = Integer.parseInt(split[2]);
        this.qixue.setWB((this.qixue.getWidth() * parseInt2) / parseInt3);
        this.qixue.setWA((this.qixue.getWidth() * parseInt) / parseInt3);
    }

    public void setwh(int i, int i2) {
        getLayoutParams().width = i;
        getLayoutParams().height = i2;
    }

    public playshort(Context context) {
        this(context, null);
    }

    @SuppressLint({"NewApi"})
    public playshort(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflaterLayout(context);
        this.mname = (TextView) findViewById(R.id.mytextimgname);
        this.fskills = (LinearLayout) findViewById(R.id.fskills);
        this.qixue = (attrbar) findViewById(R.id.short_qi);
        this.neili = (attrbar) findViewById(R.id.short_nei);
        this.short_img_zt = (RelativeLayout) findViewById(R.id.short_img_zt);
        this.short_img = (ImageView) findViewById(R.id.short_img);
        this.qixue.setColorA(-65536);
        this.neili.setColorA(-16776961);
    }
}