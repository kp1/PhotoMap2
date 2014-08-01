package net.mmho.photomap2;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SeekBar;

public class PhotoListFragment extends Fragment {

    private static final String TAG = "PhotoListFragment";
    private static final int CURSOR_LOADER_ID = 0;
    private static final int GROUPING_LOADER_ID = 1;
    private static final int GEOCODE_LOADER_ID = 2;

    private static final int ADAPTER_LOADER_ID = 1000;

    private PhotoCursor mCursor;
    private PhotoGroupList mGroup;
    private  PhotoListAdapter adapter;
    private float distance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mGroup = new PhotoGroupList(null);
        adapter= new PhotoListAdapter(getActivity(), R.layout.adapter_photo_list,mGroup,getLoaderManager(),ADAPTER_LOADER_ID);
        getLoaderManager().initLoader(CURSOR_LOADER_ID,null,photoCursorCallbacks);
        distance = DistanceUtil.toDistance(DistanceUtil.initialIndex());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_photo_list,container,false);
        GridView list = (GridView)parent.findViewById(R.id.list);
        SeekBar bar = (SeekBar)parent.findViewById(R.id.distance);
        bar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        list.setAdapter(adapter);
        list.setOnItemClickListener(onItemClickListener);
        return parent;

    }

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    distance = DistanceUtil.toDistance(progress);
                    getLoaderManager().destroyLoader(GEOCODE_LOADER_ID);
                    getLoaderManager().destroyLoader(GROUPING_LOADER_ID);
                    getLoaderManager().restartLoader(GROUPING_LOADER_ID, null, photoGroupListLoaderCallbacks);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };

    AdapterView.OnItemClickListener onItemClickListener=
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(getActivity(),ThumbnailActivity.class);
                    i.putExtra(ThumbnailActivity.EXTRA_GROUP,mGroup.get(position));
                    startActivity(i);
                }
            };

    private final Handler groupingHandler = new Handler(){
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
            }
        }
    };

    private final Handler geocodeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyDataSetChanged();
        }
    };


    private void showProgress(boolean show){
        ((ActionBarActivity)getActivity()).setSupportProgressBarIndeterminate(show);
    }

    private final LoaderManager.LoaderCallbacks<Cursor> photoCursorCallbacks =
    new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            showProgress(true);
            String q = QueryBuilder.createQuery();  // all list
            String o = QueryBuilder.sortDate();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            return new CursorLoader(getActivity().getApplicationContext(),uri,PhotoCursor.projection,q,null,o);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mCursor = new PhotoCursor(data);
            getLoaderManager().destroyLoader(GROUPING_LOADER_ID);
            getLoaderManager().restartLoader(GROUPING_LOADER_ID, null, photoGroupListLoaderCallbacks);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            showProgress(false);
        }
    };

    private final LoaderManager.LoaderCallbacks<PhotoGroupList> photoGroupListLoaderCallbacks =
    new LoaderManager.LoaderCallbacks<PhotoGroupList>() {
        @Override
        public Loader<PhotoGroupList> onCreateLoader(int id, Bundle args) {
            showProgress(true);
            mGroup = new PhotoGroupList(mCursor);
            return new PhotoGroupListLoader(getActivity().getApplicationContext(),mGroup,distance, groupingHandler);
        }

        @Override
        public void onLoadFinished(Loader<PhotoGroupList> loader, PhotoGroupList data) {
                getLoaderManager().restartLoader(GEOCODE_LOADER_ID, null, geocodeLoaderCallbacks);
        }

        @Override
        public void onLoaderReset(Loader<PhotoGroupList> loader) {
            showProgress(false);
        }
    };

    private final LoaderManager.LoaderCallbacks<Integer> geocodeLoaderCallbacks =
    new LoaderManager.LoaderCallbacks<Integer>() {
        @Override
        public Loader<Integer> onCreateLoader(int i, Bundle bundle) {
            return new GeocodeLoader(getActivity().getApplicationContext(),mGroup,geocodeHandler);
        }

        @Override
        public void onLoadFinished(Loader<Integer> listLoader, Integer success) {
            if(success>0) adapter.notifyDataSetChanged();
            showProgress(false);
        }

        @Override
        public void onLoaderReset(Loader<Integer> listLoader) {
            showProgress(false);
        }
    };

}
