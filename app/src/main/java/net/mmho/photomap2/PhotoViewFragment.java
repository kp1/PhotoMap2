package net.mmho.photomap2;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;

public class PhotoViewFragment extends Fragment {

    private PhotoGroup group;
    private int position;
    private PhotoViewAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle bundle = getArguments();
        group = bundle.getParcelable(PhotoViewActivity.EXTRA_GROUP);
        position = bundle.getInt(PhotoViewActivity.EXTRA_POSITION);
        adapter = new PhotoViewAdapter(getActivity().getApplicationContext(),
                R.layout.photo_view,group.getIDList(),getLoaderManager(),0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_photo_view,container,false);
        AdapterViewFlipper flipper = (AdapterViewFlipper)parent.findViewById(R.id.photo_flipper);
        flipper.setAdapter(adapter);
        flipper.setDisplayedChild(position);

        return parent;
    }
}
