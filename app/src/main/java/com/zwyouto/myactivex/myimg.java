package com.zwyouto.myactivex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.zwyouto.R;

public class myimg extends RelativeLayout {
    private ImageView bk2;

    protected void inflaterLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.myimg, (ViewGroup) this, true);
    }

    public void setbk2(int i) {
        this.bk2.setBackgroundResource(i);
    }

    public void setimgbmp(Bitmap bitmap) {
        this.bk2.setImageBitmap(bitmap);
    }

    public void setimgres(int i) {
        this.bk2.setImageResource(i);
    }

    public myimg(Context context) {
        this(context, null);
    }

    @SuppressLint({"NewApi"})
    public myimg(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflaterLayout(context);
        this.bk2 = (ImageView) findViewById(R.id.myimg_bk2);
        this.bk2.setClickable(false);
    }
}