package com.zwyouto.myactivex;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.zwyouto.R;

public class text3d extends RelativeLayout {
    private final TextView mA;
    private final TextView mB;
    private final TextView mC;

    public void set3dStep(int i) {
        this.mC.setTop(i * 2);
        this.mC.setLeft(i * 2);
        this.mB.setTop(i);
        this.mB.setLeft(i);
    }

    public void set3dcolor(int i) {
        this.mA.setTextColor(i);
    }

    public void set3dtext(CharSequence charSequence, int i) {
        this.mA.setText(charSequence);
        this.mB.setText(charSequence);
        this.mC.setText(charSequence);
        this.mA.setTextSize(0, i);
        this.mB.setTextSize(0, i);
        this.mC.setTextSize(0, i);
    }

    public text3d(Context context) {
        this(context, null);
    }

    public text3d(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.text3d, (ViewGroup) this, true);
        this.mA = (TextView) findViewById(R.id.top3d);
        this.mB = (TextView) findViewById(R.id.center3d);
        this.mC = (TextView) findViewById(R.id.bottom3d);
    }
}