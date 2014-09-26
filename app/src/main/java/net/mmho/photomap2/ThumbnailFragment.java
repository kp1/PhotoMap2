package net.mmho.photomap2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.util.Log;
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

    private LruCache<Long,Bitmap> mBitMapCache = null;
    private String TAG="ThumbnailFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        final int maxMemory = (int)(Runtime.getRuntime().maxMemory()/1024);
        final int cacheSize = maxMemory/8;
        Log.d(TAG, "cache size:"+cacheSize);

        mBitMapCache = new LruCache<Long, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Long key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };

        Bundle bundle = getArguments();
        group = bundle.getParcelable(ThumbnailActivity.EXTRA_GROUP);
        adapter = new ThumbnailAdapter(getActivity(),R.layout.adapter_thumbnail,group.getIDList(),getLoaderManager(),0,mBitMapCache);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(group.address!=null){
            getActivity().setTitle(AddressUtil.getTitle(group.address, getActivity()));
        }
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
                i.putExtra(PhotoMapFragment.EXTRA_GROUP,group);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
