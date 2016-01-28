package net.mmho.photomap2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class PhotoListFragment extends Fragment{

    private static final int CURSOR_LOADER_ID = 0;

    private PhotoListAdapter adapter;
    private ArrayList<HashedPhoto> photoList;
    private boolean newest = true;
    private int distance_index;

    // progress
    private ProgressChangeListener listener;

    // rxAndroid
    private Subscription subscription;
    private PublishSubject<Integer> subject;
    private boolean permission_granted;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        photoList = new ArrayList<>();
        adapter= new PhotoListAdapter(getActivity(), R.layout.layout_photo_card, new ArrayList<>());
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
    public void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= 23
            && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PhotoListActivity.PERMISSIONS_REQUEST);
            }
            else{
                View v = getView();
                if(v!=null)PermissionUtils.requestPermission(v,getContext());
            }
        }
        else {
            grantedPermission(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(subscription==null){
            subscription = subject.switchMap(this::groupObservable).subscribe();
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
        distanceActionProvider.setOnDistanceChangeListener(index -> {
            distance_index = index;
            subject.onNext(index);
        });

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
            getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, photoCursorCallbacks);
            break;
        case R.id.about:
            Intent i = new Intent(getActivity(),AboutActivity.class);
            startActivity(i);
            break;
        default:
            break;
        }
        return true;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(!permission_granted){
            menu.findItem(R.id.newest).setEnabled(false);
            menu.findItem(R.id.oldest).setEnabled(false);
            menu.findItem(R.id.distance).setEnabled(false);
        }
        else {
            menu.findItem(R.id.newest).setEnabled(!newest);
            menu.findItem(R.id.oldest).setEnabled(newest);
            menu.findItem(R.id.distance).setEnabled(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thumbnail,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // photo list
        GridView list = (GridView) view.findViewById(R.id.thumbnail_grid);
        list.setAdapter(adapter);
        list.setOnItemClickListener((p, v, position, id) -> {
            PhotoGroup group = adapter.getItem(position);
            Intent intent;
            if (group.size() == 1) {
                intent = new Intent(getActivity(), PhotoViewActivity.class);
                intent.putExtra(PhotoViewActivity.EXTRA_GROUP, (Parcelable) group);
            } else {
                intent = new Intent(getActivity(), ThumbnailActivity.class);
                intent.putExtra(ThumbnailActivity.EXTRA_GROUP, (Parcelable) group);
            }
            startActivity(intent);
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("DISTANCE", distance_index);
        outState.putString("title", getActivity().getTitle().toString());
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if(!(activity instanceof ProgressChangeListener)){
            throw new RuntimeException(activity.getLocalClassName()+" must implement ProgressChangeListener");
        }
        listener = (ProgressChangeListener) activity;
    }

    private final static String TAG = "PhotoListFragment";
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
            if(cursor==null || photoList.size() != data.getCount() ) {
                Log.d(TAG, "reload cursor!!");
                cursor = data;
                photoList = new PhotoCursor(data).getHashedPhotoList();
                subject.onNext(distance_index);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    private int progress;
    private int group_count;

    private Observable<PhotoGroup> groupObservable(int distance){
        return Observable.from(photoList)
            .subscribeOn(Schedulers.newThread())
            .groupBy(hash -> hash.getHash().toBase32()
                    .substring(0, DistanceActionProvider.getDistance(distance)))
            .doOnNext(g -> group_count++)
            .concatMap(group -> group.map(PhotoGroup::new)
                    .reduce(PhotoGroup::append))
//            .map(g -> g.resolveAddress(getContext()))
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(() -> {
                adapter.clear();
                listener.showProgress(0);
                progress = group_count = 0;
            })
            .doOnNext(g -> {
                listener.showProgress(++progress * 10000 / group_count);
                adapter.add(g);
            })
            .doOnCompleted(listener::endProgress);
    }

    public void grantedPermission(boolean granted) {
        if(granted){
            getLoaderManager().initLoader(CURSOR_LOADER_ID,null,photoCursorCallbacks);
        }
        else{
            View v = getView();
            if(v!=null)PermissionUtils.requestPermission(v,getContext());
        }
        permission_granted = granted;
    }
}
