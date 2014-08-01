package net.mmho.photomap2;

import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SeekBar;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class PhotoListFragment extends Fragment {

    private static final String TAG = "PhotoListFragment";
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
        getLoaderManager().initLoader(0,null,photoCursorCallbacks);
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
                    getLoaderManager().destroyLoader(1);
                    getLoaderManager().restartLoader(1, null, photoGroupListLoaderCallbacks);
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

    private final Handler handle = new Handler(){
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

    private final LoaderManager.LoaderCallbacks<Cursor> photoCursorCallbacks =
    new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String q = QueryBuilder.createQuery();  // all list
            String o = QueryBuilder.sortDate();
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            return new CursorLoader(getActivity().getApplicationContext(),uri,PhotoCursor.projection,q,null,o);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mCursor = new PhotoCursor(data);
            getLoaderManager().destroyLoader(1);
            getLoaderManager().restartLoader(1, null, photoGroupListLoaderCallbacks);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    private final LoaderManager.LoaderCallbacks<PhotoGroupList> photoGroupListLoaderCallbacks =
    new LoaderManager.LoaderCallbacks<PhotoGroupList>() {
        @Override
        public Loader<PhotoGroupList> onCreateLoader(int id, Bundle args) {
            mGroup = new PhotoGroupList(mCursor);
            return new PhotoGroupListLoader(getActivity().getApplicationContext(),mGroup,distance,handle);
        }

        @Override
        public void onLoadFinished(Loader<PhotoGroupList> loader, PhotoGroupList data) {
                getLoaderManager().restartLoader(100, null, geocodeLoaderCallbacks);
        }

        @Override
        public void onLoaderReset(Loader<PhotoGroupList> loader) {

        }
    };

    private final LoaderManager.LoaderCallbacks<Integer> geocodeLoaderCallbacks =
    new LoaderManager.LoaderCallbacks<Integer>() {
        @Override
        public Loader<Integer> onCreateLoader(int i, Bundle bundle) {
            Log.d(TAG,"geocodeLoaderCallbacks.onCreateLoader()");
            return new GeocodeLoader(getActivity().getApplicationContext(),mGroup);
        }

        @Override
        public void onLoadFinished(Loader<Integer> listLoader, Integer success) {
            Log.d(TAG,"geocodeLoaderCallbacks.onLoadFinished()");
            if(success>0) adapter.notifyDataSetChanged();
        }

        @Override
        public void onLoaderReset(Loader<Integer> listLoader) {

        }
    };

}
