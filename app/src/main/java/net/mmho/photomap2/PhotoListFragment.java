package net.mmho.photomap2;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.SearchView;

public class PhotoListFragment extends Fragment implements BackPressedListener{

    private static final int CURSOR_LOADER_ID = 0;
    private static final int GROUPING_LOADER_ID = 1;
    private static final int ADAPTER_LOADER_ID = 1000;

    private PhotoCursor mCursor;
    private PhotoGroupList groupList;
    private PhotoListAdapter adapter;
    private int distance_index;
    private boolean newest = true;
    private int progress;
    private int geo_progress;
    private MenuItem search;
    private GridView list;
    private boolean loaded = false;
    private boolean filtered;
    private String query="";

    public void onBackPressed() {
        if(filtered) resetFilter(true);
        else getActivity().finish();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        final int maxMemory = (int)(Runtime.getRuntime().maxMemory()/1024);
        final int cacheSize = maxMemory/8;
        LruCache<Long, Bitmap> mBitmapCache = new LruCache<Long, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Long key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };

        groupList = new PhotoGroupList();
        adapter= new PhotoListAdapter(getActivity(), R.layout.adapter_photo_list,groupList,getLoaderManager(),ADAPTER_LOADER_ID, mBitmapCache);
        if(savedInstanceState!=null) {
            distance_index = savedInstanceState.getInt("DISTANCE");
            getActivity().setTitle(savedInstanceState.getString("title"));
        }
        else{
            distance_index = DistanceUtils.initial();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.photo_list_menu, menu);

        SubMenu sub = menu.findItem(R.id.distance).getSubMenu();

        for(int i=0,l=DistanceUtils.size();i<l;i++){
            sub.add(i+1,Menu.NONE,Menu.NONE,DistanceUtils.pretty(i));
        }

        search = menu.findItem(R.id.search);
        search.setOnActionExpandListener(onActionExpandListener);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setQueryHint(getString(R.string.search_list));
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchView.setOnQueryTextFocusChangeListener(onFocusChangeListener);

    }

    final private MenuItem.OnActionExpandListener onActionExpandListener =
            new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    if(!loaded) return false;
                    filtered = false;
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    if(!filtered) resetFilter(true);
                    return true;
                }
            };

    final private SearchView.OnQueryTextListener onQueryTextListener =
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filtered = true;
                    PhotoListFragment.this.query = query;
                    getActivity().setTitle(getString(R.string.filtered,query));
                    search.collapseActionView();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if(!filtered) {
                        Filter filter = ((Filterable) list.getAdapter()).getFilter();
                        filter.filter(newText);
                    }
                    return true;
                }
            };

    final private SearchView.OnFocusChangeListener onFocusChangeListener =
            new SearchView.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(!hasFocus) search.collapseActionView();
                }
            };



    private void resetFilter(boolean reQuery){
        query = "";
        getActivity().setTitle(getString(R.string.app_name));
        if(reQuery) {
            Filter filter = ((Filterable) list.getAdapter()).getFilter();
            filter.filter(query);
        }
        filtered = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case R.id.oldest:
            newest = false;
            getLoaderManager().restartLoader(CURSOR_LOADER_ID,null,photoCursorCallbacks);
            return true;
        case R.id.newest:
            newest = true;
            getLoaderManager().restartLoader(CURSOR_LOADER_ID,null,photoCursorCallbacks);
            return true;
        case R.id.distance:
            return true;
        default:
            int id = item.getGroupId();
            if(id==0) return super.onOptionsItemSelected(item);
            id--;
            if(id!=distance_index) {
                distance_index = id;
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
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.newest).setEnabled(!newest);
        menu.findItem(R.id.oldest).setEnabled(newest);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_photo_list,container,false);

        // photo list
        list = (GridView)parent.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(onItemClickListener);
        list.setTextFilterEnabled(true);

        return parent;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("DISTANCE",distance_index);
        outState.putString("title",getActivity().getTitle().toString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, photoCursorCallbacks);
        if(getLoaderManager().getLoader(GROUPING_LOADER_ID)!=null)
            getLoaderManager().initLoader(GROUPING_LOADER_ID, null, photoGroupListLoaderCallbacks);
        if(query.length()>0) getActivity().setTitle(getString(R.string.filtered, query));
    }

    AdapterView.OnItemClickListener onItemClickListener=
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    PhotoGroup group = adapter.getItem(position);
                    Intent intent;
                    if(group.size()==1){
                        intent = new Intent(getActivity(),PhotoViewActivity.class);
                        intent.putExtra(PhotoViewActivity.EXTRA_GROUP,group);
                    }
                    else {
                        intent = new Intent(getActivity(), ThumbnailActivity.class);
                        intent.putExtra(ThumbnailActivity.EXTRA_GROUP,group);
                    }
                    startActivity(intent);
                }
            };

    private void setProgress(int progress){
        getActivity().setProgress(progress);
        getActivity().setProgressBarVisibility(true);
    }

    private void endProgress(){
        getActivity().setProgress(Window.PROGRESS_END);
        getActivity().setProgressBarVisibility(false);
    }


    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(isRemoving()) return;
            final int PROGRESS_GROUPING_RATIO=8000;
            switch (msg.what){
            case PhotoGroupList.MESSAGE_RESTART:
                adapter.notifyDataSetChanged();
                setProgress(0);
                break;
            case PhotoGroupList.MESSAGE_ADD:
            case PhotoGroupList.MESSAGE_APPEND:
                adapter.notifyDataSetChanged();
                progress++;
                setProgress(progress * PROGRESS_GROUPING_RATIO/mCursor.getCount());
                break;
            case PhotoGroupList.MESSAGE_ADDRESS:
                geo_progress++;
                setProgress(PROGRESS_GROUPING_RATIO+
                        geo_progress*(Window.PROGRESS_END-PROGRESS_GROUPING_RATIO)/adapter.getCount());
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
                mCursor = new PhotoCursor(data);
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
            progress = geo_progress = 0;
            if(search!=null) {
                if (search.isActionViewExpanded()) search.collapseActionView();
                search.setEnabled(false);
            }
            resetFilter(false);
            loaded = false;
            return new PhotoGroupListLoader(getActivity(),groupList,mCursor,
                    DistanceUtils.getDistance(distance_index),true, handler);
        }

        @Override
        public void onLoadFinished(Loader<PhotoGroupList> loader, PhotoGroupList data) {
            search.setEnabled(true);
            loaded = true;
            endProgress();
        }

        @Override
        public void onLoaderReset(Loader<PhotoGroupList> loader) {

        }
    };
}
