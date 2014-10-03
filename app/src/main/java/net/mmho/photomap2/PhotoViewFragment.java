package net.mmho.photomap2;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PhotoViewFragment extends Fragment {

    public static final String EXTRA_IMAGE_ID = "image_id";
    private long image_id;
    private LoadableImageView image = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle b = getArguments();
        image_id = b.getLong(EXTRA_IMAGE_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.photo_view,container,false);
        image = (LoadableImageView)v.findViewById(R.id.photo_view);
        image.startLoading(getActivity().getSupportLoaderManager(),(int)image_id,image_id,null);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
        if(drawable!=null) {
            Bitmap bmp = drawable.getBitmap();
            if (bmp != null) bmp.recycle();
        }
    }
}
