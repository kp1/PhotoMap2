package net.mmho.photomap2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class PhotoListAdapter extends ArrayAdapter<PhotoGroup> {

    private int resource;
    private LayoutInflater inflater;

    private ArrayList<PhotoGroup> objects;
    private ArrayList<PhotoGroup> original;

    public Subscription subscription;

    public PhotoListAdapter(Context context, int resource, ArrayList<PhotoGroup> objects) {
        super(context, resource, objects);
        this.resource = resource;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if(convertView!=null){
            v = convertView;
        }
        else {
            v = inflater.inflate(resource,null);
        }
        if(position < getCount()) {
            PhotoGroup g = getItem(position);
            ((PhotoCardLayout) v).setData(g);
        }
        return v;
    }

    public void filter(String query){
        if(original==null) original = new ArrayList<>(objects);
        if(subscription!=null) subscription.unsubscribe();
        subscription = filterObservable(query).subscribe();
    }

    @Override
    public void clear() {
        super.clear();
        original = null;
    }

    private Observable<PhotoGroup> filterObservable(String query){
        return Observable.from(original)
            .subscribeOn(Schedulers.newThread())
            .filter(g -> (query == null || query.isEmpty()) ||
                g.getDescription().toLowerCase(Locale.getDefault())
                    .contains(query.toLowerCase(Locale.getDefault())))
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(super::clear)
            .doOnNext(this::add);
    }

}
