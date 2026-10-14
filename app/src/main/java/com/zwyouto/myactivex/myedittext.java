package com.zwyouto.myactivex;

import android.content.Context;
import android.text.Layout;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class myedittext extends androidx.appcompat.widget.AppCompatEditText {
    private int off;

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        Layout layout = getLayout();
        switch (action) {
            case 0:
                this.off = layout.getOffsetForHorizontal(layout.getLineForVertical(getScrollY() + ((int) motionEvent.getY())), (int) motionEvent.getX());
                Selection.setSelection(getEditableText(), this.off);
                return true;
            case 1:
            case 2:
                Selection.setSelection(getEditableText(), this.off, layout.getOffsetForHorizontal(layout.getLineForVertical(getScrollY() + ((int) motionEvent.getY())), (int) motionEvent.getX()));
                return true;
            default:
                return true;
        }
    }

    public myedittext(Context context) {
        super(context);
    }

    public myedittext(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}