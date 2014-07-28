package net.mmho.photomap2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PhotoViewFragment extends Fragment {

    public static final String EXTRA_IMAGE_ID = "image_id";
    private long image_id;

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
        PhotoImageView image = (PhotoImageView)v.findViewById(R.id.photo_view);
        image.startLoading(getActivity().getSupportLoaderManager(),(int)image_id,image_id);
        return v;
    }
}
