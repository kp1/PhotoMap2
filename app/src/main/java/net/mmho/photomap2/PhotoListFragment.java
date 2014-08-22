package net.mmho.photomap2;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

public class PhotoListFragment extends Fragment {

    private static final int CURSOR_LOADER_ID = 0;
    private static final int GROUPING_LOADER_ID = 1;

    private static final int ADAPTER_LOADER_ID = 1000;

    private Cursor mCursor;
    private  PhotoListAdapter adapter;
    private int distance_index;
    private boolean newest = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        adapter= new PhotoListAdapter(getActivity(), R.layout.adapter_photo_list,new PhotoGroupList(null),getLoaderManager(),ADAPTER_LOADER_ID);
        if(savedInstanceState!=null) {
            distance_index = savedInstanceState.getInt("DISTANCE");
        }
        else{
            distance_index = DistanceAdapter.initial();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.photo_list_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case R.id.oldest:
            newest = false;
            getLoaderManager().restartLoader(CURSOR_LOADER_ID,null,photoCursorCallbacks);
            break;
        case R.id.newest:
            newest = true;
            getLoaderManager().restartLoader(CURSOR_LOADER_ID,null,photoCursorCallbacks);
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(newest){
            menu.getItem(0).setEnabled(false);
            menu.getItem(1).setEnabled(true);
        }
        else{
            menu.getItem(0).setEnabled(true);
            menu.getItem(1).setEnabled(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_photo_list,container,false);

        // photo list
        GridView list = (GridView)parent.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(onItemClickListener);

        DistanceAdapter distanceAdapter = new DistanceAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
        ActionBar bar = getActivity().getActionBar();
        bar.setListNavigationCallbacks(distanceAdapter, onNavigationListener);
        bar.setSelectedNavigationItem(distance_index);

        return parent;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("DISTANCE",distance_index);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, photoCursorCallbacks);
        if(getLoaderManager().getLoader(GROUPING_LOADER_ID)!=null)
            getLoaderManager().initLoader(GROUPING_LOADER_ID, null, photoGroupListLoaderCallbacks);
    }

    ActionBar.OnNavigationListener onNavigationListener =
            new ActionBar.OnNavigationListener() {
                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                    if(itemPosition!=distance_index) {
                        distance_index = itemPosition;
                        if(mCursor==null || mCursor.isClosed()) {
                            getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, photoCursorCallbacks);
                        }
                        else {
                            getLoaderManager().destroyLoader(GROUPING_LOADER_ID);
                            getLoaderManager().restartLoader(GROUPING_LOADER_ID, null, photoGroupListLoaderCallbacks);
                        }
                    }
                    return true;
                }
            };

    AdapterView.OnItemClickListener onItemClickListener=
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(getActivity(),ThumbnailActivity.class);
                    i.putExtra(ThumbnailActivity.EXTRA_GROUP,adapter.getItem(position));
                    startActivity(i);
                }
            };

    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
            case PhotoGroupList.MESSAGE_RESTART:
                adapter.clear();
                break;
            case PhotoGroupList.MESSAGE_ADD:
                Bundle b = msg.getData();
                PhotoGroup g = b.getParcelable(PhotoGroupList.EXTRA_GROUP);
                adapter.add(g);
                adapter.notifyDataSetChanged();
                break;
            case PhotoGroupList.MESSAGE_ADDRESS:
                adapter.notifyDataSetChanged();
                break;
            }
        }
    };

    private final LoaderManager.LoaderCallbacks<Cursor> photoCursorCallbacks =
    new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String q = QueryBuilder.createQuery();  // all list
            String o = newest?QueryBuilder.sortDateNewest():QueryBuilder.sortDateOldest();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            return new CursorLoader(getActivity(),uri,PhotoCursor.projection,q,null,o);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(mCursor==null || !mCursor.equals(data)) {
                mCursor = data;
                getLoaderManager().destroyLoader(GROUPING_LOADER_ID);
                getLoaderManager().restartLoader(GROUPING_LOADER_ID, null, photoGroupListLoaderCallbacks);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    private final LoaderManager.LoaderCallbacks<PhotoGroupList> photoGroupListLoaderCallbacks =
    new LoaderManager.LoaderCallbacks<PhotoGroupList>() {
        @Override
        public Loader<PhotoGroupList> onCreateLoader(int id, Bundle args) {
            return new PhotoGroupListLoader(getActivity(),mCursor,
                    DistanceAdapter.getDistance(distance_index),true, handler);
        }

        @Override
        public void onLoadFinished(Loader<PhotoGroupList> loader, PhotoGroupList data) {
        }

        @Override
        public void onLoaderReset(Loader<PhotoGroupList> loader) {
        }
    };
}
