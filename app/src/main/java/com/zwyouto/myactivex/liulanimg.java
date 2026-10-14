package com.zwyouto.myactivex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.zwyouto.R;
import java.io.IOException;

public class liulanimg extends LinearLayout {
    private String dirs;
    private final LinearLayout liulanimgs;
    private final ImageView liulannow;
    private final Context mContext;


    public String getimg() {
        return this.liulannow.getTag().toString();
    }

    protected void inflaterLayout(Context context) {
        LayoutInflater.from(context).inflate(R.layout.liulanimg,  this, true);
    }

    public void setdir(String str) {
        this.dirs = str;
        String[] imgfiles;
        try {
            imgfiles = this.mContext.getResources().getAssets().list(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String imgfile : imgfiles) {
            ImageView imageView = new ImageView(this.mContext);
            try {
                imageView.setImageBitmap(BitmapFactory.decodeStream(this.mContext.getResources().getAssets().open(this.dirs + "/" + imgfile)));
            } catch (IOException e2) {
                throw new RuntimeException(e2);
            }
            imageView.setTag(imgfile);
             
// android.view.View.OnClickListener
            imageView.setOnClickListener(view -> {
                try {
                    liulanimg.this.liulannow.setImageBitmap(BitmapFactory.decodeStream(liulanimg.this.mContext.getResources().getAssets().open(liulanimg.this.dirs + "/" + view.getTag())));
                } catch (IOException e3) {
                    throw new RuntimeException(e3);
                }
                liulanimg.this.liulannow.setTag(view.getTag());
            });
            this.liulanimgs.addView(imageView);
        }
    }

    public liulanimg(Context context) {
        this(context, null);
    }

    @SuppressLint({"NewApi"})
    public liulanimg(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflaterLayout(context);
        this.liulanimgs = findViewById(R.id.liulanimgs);
        this.liulannow =  findViewById(R.id.liulannow);
        this.mContext = context;
    }
}