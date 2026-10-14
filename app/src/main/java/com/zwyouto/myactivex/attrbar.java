package com.zwyouto.myactivex;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.zwyouto.R;

public class attrbar extends RelativeLayout {
    private final TextView mA;
    private final TextView mABC;
    private final TextView mB;
    private final TextView mC;


    public void clearC() {
        this.mC.setBackground(null);
    }

    public void setColorA(int i) {
        this.mA.setBackgroundColor(i);
    }

    public void setColorB(int i) {
        this.mB.setBackgroundColor(i);
    }

    public void setColorC(int i) {
        this.mC.setBackground(null);
        this.mC.setBackgroundColor(i);
    }

    public void setTextss(SpannableStringBuilder spannableStringBuilder) {
        this.mABC.setText(spannableStringBuilder);
    }

    public void setTextss(String str) {
        this.mABC.setText(str);
    }

    public void setWA(int i) {
        this.mA.setWidth(i);
    }

    public void setWB(int i) {
        this.mB.setWidth(i);
    }

    public void setWC(int i) {
        this.mC.setWidth(i);
    }

    public void setsize(int i) {
        this.mABC.setTextSize(0, i);
    }

    public attrbar(Context context) {
        this(context, null);
    }

    public attrbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.attrbarx,this, true);
        this.mA =  findViewById(R.id.TextViewA);
        this.mB =  findViewById(R.id.TextViewB);
        this.mC =  findViewById(R.id.TextViewC);
        this.mABC = findViewById(R.id.TextABC);
    }
}