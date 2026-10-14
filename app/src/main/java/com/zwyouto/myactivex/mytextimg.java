package com.zwyouto.myactivex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.zwyouto.R;

public class mytextimg extends LinearLayout {
    private ImageView mImg;
    private TextView mname;
    private FrameLayout mytttitle;
    private attrbar npcqi;

    public String getimgname() {
        return this.mname.getText().toString();
    }

    protected void inflaterLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.mytextimgx, (ViewGroup) this, true);
    }

    public void setimgimg(Bitmap bitmap) {
        this.mImg.setImageBitmap(bitmap);
    }

    public void setimgname(String str) {
        this.mname.setText(str);
    }

    public void setmyttyy(int i) {
    }

    public void setqixue(String str) {
        String[] split = str.split("：")[1].split("/");
        this.npcqi.setVisibility(View.VISIBLE);
        this.npcqi.setColorA(-65536);
        int parseInt = Integer.parseInt(split[0]);
        int parseInt2 = Integer.parseInt(split[1]);
        int parseInt3 = Integer.parseInt(split[2]);
        this.npcqi.setWB((this.npcqi.getWidth() * parseInt2) / parseInt3);
        this.npcqi.setWA((this.npcqi.getWidth() * parseInt) / parseInt3);
    }

    public void settextcolor(int i) {
        this.mytttitle.setBackgroundColor(i);
    }

    public void settextsize(int i) {
        this.mname.setTextSize(0, i);
    }

    public void settouxiang(int i) {
        this.mImg.setImageResource(i);
    }

    public mytextimg(Context context) {
        this(context, null);
    }

    @SuppressLint({"NewApi"})
    public mytextimg(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflaterLayout(context);
        this.mytttitle = (FrameLayout) findViewById(R.id.mytttitle);
        this.mImg = (ImageView) findViewById(R.id.mytextimgimg);
        this.mname = (TextView) findViewById(R.id.mytextimgname);
        this.npcqi = (attrbar) findViewById(R.id.npcqi);
        this.mImg.setClickable(false);
    }
}