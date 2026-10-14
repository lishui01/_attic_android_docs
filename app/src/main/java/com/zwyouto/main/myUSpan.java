package com.zwyouto.main;

import android.annotation.SuppressLint;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;
import com.zwyouto.mud.BuildConfig;

@SuppressLint({"ParcelCreator"})
public class myUSpan extends URLSpan {

    @Override
    public void onClick(View view) {
        if (getURL().indexOf("cmds:") == 0) {
            mudsocketd.mudout(getURL().replace("cmds:", BuildConfig.FLAVOR));
            mudmaind.hudong_lay.setVisibility(View.GONE);
        } else if (getURL().indexOf("pops:") == 0) {
            mudmaind.takepopmenu(getURL().replace("pops:", BuildConfig.FLAVOR));
        } else if (getURL().indexOf("voice:") == 0) {
            mudmaind.takevoice(getURL().replace("voice:", BuildConfig.FLAVOR));
        } else {
            super.onClick(view);
        }
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        textPaint.setUnderlineText(true);
    }

    public myUSpan(String url) {
        super(url);
    }
}