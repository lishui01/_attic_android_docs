package com.zwyouto.myactivex;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class XRTextView extends androidx.appcompat.widget.AppCompatTextView {
    private float LineSpacing;
    private float Spacing;
    private float marginLeft;
    private float marginRight;
    private final String namespace;
    private float paddingBot;
    private float paddingLeft;
    private float paddingRight;
    private float paddingTop;
    private Paint paint1;
    private String text;
    private int textColor;
    private float textShowWidth;
    private float textSize;


    public float getMYLineSpacing() {
        return this.LineSpacing;
    }

    public float getMYTextSize() {
        return this.textSize;
    }

    public float getSpacing() {
        return this.Spacing;
    }

    @Override // android.widget.TextView, android.view.View
    protected void onDraw(Canvas canvas) {
        View view = (View) getParent();
        this.textShowWidth = (((((view.getMeasuredWidth() - view.getPaddingLeft()) - view.getPaddingRight()) - this.paddingLeft) - this.paddingRight) - this.marginLeft) - this.marginRight;
        int i = 0;
        this.text = getText().toString();
        if (this.text == null) {
            return;
        }
        char[] charArray = this.text.toCharArray();
        float f = 0.0f;
        for (int i2 = 0; i2 < charArray.length; i2++) {
            float measureText = this.paint1.measureText(charArray, i2, 1);
            if (charArray[i2] == '\n') {
                i++;
                f = 0.0f;
            } else {
                if (this.textShowWidth - f < measureText) {
                    i++;
                    f = 0.0f;
                }
                canvas.drawText(charArray, i2, 1, this.paddingLeft + f, this.paddingTop + ((i + 1) * (this.textSize + this.LineSpacing)) + view.getPaddingTop(), this.paint1);
                f += measureText;
            }
        }
        setHeight((int) (((i + 1) * (this.textSize + this.LineSpacing)) + view.getPaddingTop() + view.getPaddingBottom() + this.paddingTop + this.paddingBot));
    }

    public void setMYLineSpacing(float f) {
        this.LineSpacing = f;
    }

    public void setMYTextColor(int i) {
        this.textColor = i;
        this.paint1.setColor(i);
    }

    public void setMYTextSize(float f) {
        this.textSize = f;
        this.paint1.setTextSize(f);
    }

    public void setSpacing(float f) {
        this.Spacing = f;
    }

    public XRTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.namespace = "com.zwyouto.myactivex.XRTextView";
        this.paddingTop = 0.0f;
        this.paddingBot = 5.0f;
        this.paint1 = new Paint();
        this.Spacing = 3.0f;
        this.LineSpacing = 3.0f;
        this.text = attrs.getAttributeValue("com.zwyouto.myactivex.XRTextView", "text");
        this.textSize = attrs.getAttributeIntValue("com.zwyouto.myactivex.XRTextView", "textSize", 25);
        this.textColor = attrs.getAttributeIntValue("com.zwyouto.myactivex.XRTextView", "textColor", -16777216);
        this.paddingLeft = attrs.getAttributeIntValue("com.zwyouto.myactivex.XRTextView", "paddingLeft", 5);
        this.paddingRight = attrs.getAttributeIntValue("com.zwyouto.myactivex.XRTextView", "paddingRight", 5);
        this.marginLeft = attrs.getAttributeIntValue("com.zwyouto.myactivex.XRTextView", "marginLeft", 0);
        this.marginRight = attrs.getAttributeIntValue("com.zwyouto.myactivex.XRTextView", "marginRight", 0);
        this.paint1.setTextSize(this.textSize);
        this.paint1.setColor(this.textColor);
        this.paint1.setAntiAlias(true);
    }

    public XRTextView(Context context, float textSize, int textColor, float paddingLeft, float paddingRight, float marginLeft, float marginRight) {
        super(context);
        this.namespace = "com.zwyouto.myactivex.XRTextView";
        this.paddingTop = 0.0f;
        this.paddingBot = 5.0f;
        this.paint1 = new Paint();
        this.Spacing = 3.0f;
        this.LineSpacing = 3.0f;
        this.textSize = textSize;
        this.textColor = textColor;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.paint1.setTextSize(textSize);
        this.paint1.setColor(textColor);
        this.paint1.setAntiAlias(true);
    }
}