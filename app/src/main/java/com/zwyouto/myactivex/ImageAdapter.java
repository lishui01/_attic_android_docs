package com.zwyouto.myactivex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import java.io.IOException;

public class ImageAdapter extends BaseAdapter {
    private String dirs;
    private String[] imgfiles;
    private final Context mContext;
    private int w = 100;
    private int h = 100;
    /** 缩略图缓存：滚动时不再对每个 position 重复解码原图。 */
    private final LruCache<String, Bitmap> thumbCache = new LruCache<String, Bitmap>(4 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    @Override // android.widget.Adapter
    public int getCount() {
        return this.imgfiles.length;
    }

    @Override // android.widget.Adapter
    public Object getItem(int i) {
        return i;
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return i;
    }

    @Override // android.widget.Adapter
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView;
        if (view == null) {
            imageView = new ImageView(this.mContext);
            imageView.setLayoutParams(new AbsListView.LayoutParams(this.w, this.h));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imageView = (ImageView) view;
        }
        String name = this.imgfiles[i];
        String key = this.dirs + "/" + name;
        Bitmap thumb = this.thumbCache.get(key);
        if (thumb == null) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 2;
            try {
                Bitmap src = BitmapFactory.decodeStream(
                        this.mContext.getAssets().open(key), null, opts);
                if (src != null) {
                    thumb = ThumbnailUtils.extractThumbnail(src, 180, 130);
                    if (thumb != src) {
                        src.recycle();
                    }
                    this.thumbCache.put(key, thumb);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        imageView.setImageBitmap(thumb);
        imageView.setTag(name.substring(0, name.indexOf(".")));
        return imageView;
    }

    public String getdir() {
        return this.dirs;
    }

    public void setdir(String str) {
        this.dirs = str;
        try {
            this.imgfiles = this.mContext.getAssets().list(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setwh(int i, int i2) {
        this.w = i;
        this.h = i2;
    }

    public ImageAdapter(Context c) {
        this.mContext = c;
    }
}
