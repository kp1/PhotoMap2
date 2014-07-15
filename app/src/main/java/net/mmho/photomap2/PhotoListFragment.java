package net.mmho.photomap2;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

public class PhotoListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "PhotoListFragment";
    private PhotoCursor mCursor;
    private PhotoGroupList mGroup;
    private  PhotoListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        mGroup = new PhotoGroupList(null);
        adapter= new PhotoListAdapter(getActivity(), R.layout.fragment_photo_list,mGroup);
        getLoaderManager().initLoader(0,null,this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.activity_photo_list,container,false);
        AbsListView list = (AbsListView)parent.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(onItemClickListener);
        return parent;

    }

    AdapterView.OnItemClickListener onItemClickListener=
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(BuildConfig.DEBUG) Log.d(TAG,"onItemClick:"+position);
                    Intent i = new Intent(getActivity(),ThumbnailActivity.class);
                    i.putExtra(ThumbnailActivity.EXTRA_GROUP,mGroup.get(position));
                    startActivity(i);
                }
            };
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String q = QueryBuilder.createQuery();  // all list
        String o = QueryBuilder.sortDate();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        return new CursorLoader(getActivity().getApplicationContext(),uri,PhotoCursor.projection,q,null,o);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(BuildConfig.DEBUG) Log.d(TAG,"onLoadFinished()");
        adapter.clear();
        mCursor = new PhotoCursor(data);
        mGroup = new PhotoGroupList(mCursor);
        mGroup.exec(4000);
        if(BuildConfig.DEBUG) Log.d(TAG,"group:"+mGroup.size());
        adapter.addAll(mGroup);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.clear();
    }
}
