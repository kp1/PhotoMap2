package net.mmho.photomap2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;

import net.mmho.photomap2.geohash.GeoHash;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class PhotoListFragment extends Fragment implements BackPressedListener{

    private static final int CURSOR_LOADER_ID = 0;
    private static final String TAG = "PhotoListFragment";

    private ArrayList<PhotoGroup> groupList;
    private PhotoListAdapter adapter;
    private ArrayList<HashedPhoto> photoList;
    private boolean newest = true;
    private MenuItem search;
    private GridView list;
    private boolean loaded = false;
    private boolean filtered;
    private String query="";
    private int distance_index;

    // progress
    private ProgressChangeListener listener;
    private int progress;
    private int group_count;

    // rxAndroid
    private Context context;
    Subscription subscription;
    PublishSubject<Void> subject;


    public void onBackPressed() {
        if(filtered) resetFilter(true);
        else getActivity().finish();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        groupList = new ArrayList<>();
        photoList = new ArrayList<>();
        adapter= new PhotoListAdapter(getActivity(), R.layout.layout_photo_card,groupList);
        if(savedInstanceState!=null) {
            distance_index = savedInstanceState.getInt("DISTANCE");
            getActivity().setTitle(savedInstanceState.getString("title"));
        }
        else{
            distance_index = DistanceActionProvider.initialIndex();
        }
        subject = PublishSubject.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(subscription==null){
            subscription =
                subject
                    .onBackpressureDrop()
                    .doOnNext(aVoid ->{
                        groupList.clear();
                        listener.showProgress(0);
                        progress = group_count = 0;
                    })
                    .concatMap(aVoid -> groupObservable())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        g -> {
                            if(group_count!=0) {
                                listener.showProgress(++progress * 10000 / group_count);
                                groupList.add(g);
                                adapter.notifyDataSetChanged();
                            }
                        });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(subscription!=null){
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.photo_list_menu, menu);

        MenuItem distance = menu.findItem(R.id.distance);
        DistanceActionProvider distanceActionProvider
                = (DistanceActionProvider) MenuItemCompat.getActionProvider(distance);
        distanceActionProvider.setDistanceIndex(distance_index);
        distanceActionProvider.setOnDistanceChangeListener(onDistanceChangeListener);


        search = menu.findItem(R.id.search);
        MenuItemCompat.setOnActionExpandListener(search, onActionExpandListener);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setQueryHint(getString(R.string.search_list));
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchView.setOnQueryTextFocusChangeListener(onFocusChangeListener);

    }

    final private MenuItemCompat.OnActionExpandListener onActionExpandListener =
            new MenuItemCompat.OnActionExpandListener() {
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
                    MenuItemCompat.collapseActionView(search);
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
                    if(!hasFocus) MenuItemCompat.collapseActionView(search);
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
        case R.id.about:
            Intent i = new Intent(getActivity(),AboutActivity.class);
            startActivity(i);
            return true;
        default:
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
        outState.putInt("DISTANCE", distance_index);
        outState.putString("title", getActivity().getTitle().toString());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, photoCursorCallbacks);
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
                        intent.putExtra(PhotoViewActivity.EXTRA_GROUP, (Parcelable) group);
                    }
                    else {
                        intent = new Intent(getActivity(), ThumbnailActivity.class);
                        intent.putExtra(ThumbnailActivity.EXTRA_GROUP, (Parcelable) group);
                    }
                    startActivity(intent);
                }
            };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        Activity activity = getActivity();
        if(!(activity instanceof ProgressChangeListener)){
            throw new RuntimeException(activity.getLocalClassName()+" must implement ProgressChangeListener");
        }
        listener = (ProgressChangeListener) activity;
    }

    private final LoaderManager.LoaderCallbacks<Cursor> photoCursorCallbacks =
    new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String q = QueryBuilder.createQuery();  // all list
            String o = newest?QueryBuilder.sortDateNewest():QueryBuilder.sortDateOldest();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            return new CursorLoader(getActivity(),uri,PhotoCursor.projection,q,null,o);
        }

        private Cursor cursor = null;
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(cursor==null || !cursor.equals(data)) {
                cursor = data;
                photoList = new PhotoCursor(data).getHashedPhotoList();
                subject.onNext(null);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    private Observable<PhotoGroup> groupObservable(){
        return Observable.from(photoList)
            .subscribeOn(Schedulers.newThread())
            .groupBy(hash -> GeoHash.createFromLong(hash.getHash().getLong(),
                DistanceActionProvider.getDistance(distance_index)).toBase32())
            .doOnNext(g -> group_count++)
            .concatMap(group -> group.map(PhotoGroup::new)
                .reduce(PhotoGroup::append))
            .takeUntil(subject)
            .map(g -> g.resolveAddress(context))
            .observeOn(AndroidSchedulers.mainThread())
            .doOnCompleted(listener::endProgress);
    }

    private final DistanceActionProvider.OnDistanceChangeListener onDistanceChangeListener =
            new DistanceActionProvider.OnDistanceChangeListener() {
                @Override
                public void onDistanceChange(int index)
                {
                    distance_index = index;
                    subject.onNext(null);
                }
            };

}
