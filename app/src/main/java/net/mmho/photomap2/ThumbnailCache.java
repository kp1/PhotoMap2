package net.mmho.photomap2;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class ThumbnailCache extends LruCache<Long,Bitmap> {

    static private ThumbnailCache self;
    static final int maxMemory = (int)(Runtime.getRuntime().maxMemory()/1024);
    static final int cacheSize = maxMemory/8;

    private ThumbnailCache(int maxSize) {
        super(maxSize);
    }

    static public ThumbnailCache getInstance(){
        if(self==null){
            self = new ThumbnailCache(cacheSize);
        }
        return self;
    }

    @Override
    protected int sizeOf(Long key, Bitmap bitmap) {
        return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
    }
}
