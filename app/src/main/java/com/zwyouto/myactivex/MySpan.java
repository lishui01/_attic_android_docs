package com.zwyouto.myactivex;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import com.zwyouto.R;

public class MySpan {
    private static SpannableString addSpan(Context contexts, SpannableString spanString, int start, int end, int spanindex, int spanstyle, String imgorurl) {
        switch (spanindex) {
            case 1:
                spanString.setSpan(new URLSpan(imgorurl), start, end, 33);
                break;
            case 2:
                spanString.setSpan(new ForegroundColorSpan(spanstyle), start, end, 33);
                break;
            case 3:
                spanString.setSpan(new BackgroundColorSpan(spanstyle), start, end, 33);
                break;
            case 4:
                spanString.setSpan(new AbsoluteSizeSpan(spanstyle), start, end, 33);
                break;
            case 5:
                spanString.setSpan(new StyleSpan(spanstyle), start, end, 33);
                break;
            case 6:
                spanString.setSpan(new UnderlineSpan(), start, end, 33);
                break;
            case 7:
                Drawable d = contexts.getResources().getDrawable(Integer.parseInt(imgorurl));
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                spanString.setSpan(new ImageSpan(d, 1), start - 1, start, 33);
                break;
        }
        return spanString;
    }

    public static SpannableString toSpan(Context contexts, String myspantext) {
        String[] newtext = myspantext.split("#SP%");
        SpannableString spanString = new SpannableString(newtext[0]);
        if (newtext.length != 1) {
            String[] spans = newtext[1].split("@sp&");
            for (String str : spans) {
                String[] spanthis = str.split(",");
                addSpan(contexts, spanString, Integer.parseInt(spanthis[0]), Integer.parseInt(spanthis[1]), Integer.parseInt(spanthis[2]), Integer.parseInt(spanthis[3]), spanthis[4]);
            }
        }
        return spanString;
    }
}