package net.mmho.photomap2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

public class ThumbnailFragment extends Fragment {

    private ThumbnailAdapter adapter;
    private PhotoGroup group;
    private GridView list;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle bundle = getArguments();
        group = bundle.getParcelable(ThumbnailActivity.EXTRA_GROUP);
        adapter = new ThumbnailAdapter(getActivity(),R.layout.fragment_thumbnail,group.getIDList(),getLoaderManager(),0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.activity_thumbnail,container,false);
        list = (GridView)parent.findViewById(R.id.thumbnail_grid);
        list.setAdapter(adapter);
        list.setOnItemClickListener(clickListener);
        return parent;
    }

    public void setPosition(int position){
        list.setSelection(position);

    }

    AdapterView.OnItemClickListener clickListener =
        new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(),PhotoViewActivity.class);
                i.putExtra(PhotoViewActivity.EXTRA_GROUP,group);
                i.putExtra(PhotoViewActivity.EXTRA_POSITION,position);
                startActivityForResult(i,0);
            }
        };
}
