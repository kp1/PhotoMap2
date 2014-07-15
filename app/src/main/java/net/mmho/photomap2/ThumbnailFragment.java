package net.mmho.photomap2;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

public class ThumbnailFragment extends Fragment {

    private ThumbnailAdapter adapter;
    private PhotoGroup group;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle bundle = getArguments();
        group = bundle.getParcelable(ThumbnailActivity.EXTRA_GROUP);
        adapter = new ThumbnailAdapter(getActivity(),R.layout.fragment_thumbnail,group.getIDList());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.activity_thumbnail,container,false);
        AbsListView list = (AbsListView)parent.findViewById(R.id.thumbnail_grid);
        list.setAdapter(adapter);
        return parent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
