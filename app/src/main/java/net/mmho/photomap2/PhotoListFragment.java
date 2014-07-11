package net.mmho.photomap2;

import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

public class PhotoListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "PhotoListFragment";
    private PhotoCursor mCursor;
    private PhotoGroup mGroup;
    private  PhotoListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        mGroup = new PhotoGroup(null);
        adapter= new PhotoListAdapter(getActivity(), R.layout.fragment_photo_list,mGroup);
        getLoaderManager().initLoader(0,null,this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.activity_photo_list,container,false);
        AbsListView list = (AbsListView)parent.findViewById(R.id.list);
        list.setAdapter(adapter);
        return parent;

    }

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
        mGroup = new PhotoGroup(mCursor);
        mGroup.exec(4000);
        if(BuildConfig.DEBUG) Log.d(TAG,"group:"+mGroup.size());
        adapter.addAll(mGroup);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.clear();
    }
}
