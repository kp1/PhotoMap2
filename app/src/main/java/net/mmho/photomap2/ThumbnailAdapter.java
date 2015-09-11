package net.mmho.photomap2;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.LoaderManager;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class ThumbnailAdapter extends ArrayAdapter<HashedPhoto>{

    private int resource;
    private LayoutInflater inflater;
    private LruCache<java.lang.Long,Bitmap> mBitmapCache;

    public ThumbnailAdapter(Context c, int resource, List<HashedPhoto> objects,LruCache<java.lang.Long,Bitmap> cache) {
        super(c, resource, objects);
        this.resource = resource;
        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBitmapCache = cache;
    }

    private class ViewHolder{
        ThumbnailImageView thumbnail;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        ViewHolder holder;
        if(convertView!=null && position!=0){
            v = convertView;
            holder = (ViewHolder) v.getTag();
        }
        else{
            v = inflater.inflate(resource,null);
            holder = new ViewHolder();
            holder.thumbnail = (ThumbnailImageView) v.findViewById(R.id.thumbnail);
            v.setTag(holder);
        }

        holder.thumbnail.startLoading(getItem(position).getPhotoId(),mBitmapCache);

        return v;
    }
}
