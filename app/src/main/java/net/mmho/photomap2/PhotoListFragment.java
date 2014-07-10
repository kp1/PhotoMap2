package net.mmho.photomap2;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;

public class PhotoListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private PhotoCursor mCursor;
    private PhotoGroup mGroup;
    private  ArrayAdapter<PhotoGroup.Group> adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGroup = new PhotoGroup(null);
        adapter= new ArrayAdapter<PhotoGroup.Group>(getActivity(), R.layout.fragment_photo_list,mGroup);
        setListAdapter(adapter);
        getLoaderManager().initLoader(0,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String q = QueryBuilder.createQuery();  // all list
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        return new CursorLoader(getActivity().getApplicationContext(),uri,PhotoCursor.projection,q,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.clear();
        mCursor = new PhotoCursor(data);
        mGroup = new PhotoGroup(mCursor);
        mGroup.exec(4000);
        adapter.addAll(mGroup);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.clear();
    }
}
