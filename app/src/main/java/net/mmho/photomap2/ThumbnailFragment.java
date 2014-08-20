package net.mmho.photomap2;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        group = bundle.getParcelable(ThumbnailActivity.EXTRA_GROUP);
        adapter = new ThumbnailAdapter(getActivity(),R.layout.adapter_thumbnail,group.getIDList(),getLoaderManager(),0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_thumbnail,container,false);
        list = (GridView)parent.findViewById(R.id.thumbnail_grid);
        list.setAdapter(adapter);
        list.setOnItemClickListener(clickListener);
        return parent;
    }

    public void setPosition(int position){
        list.setSelection(position);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.thumbnail_manu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.map:
                Intent i = new Intent(getActivity(),PhotoMapActivity.class);
                i.putExtra(PhotoMapActivity.EXTRA_GROUP,group);
                startActivity(i);
                break;
        }
        return true;
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
