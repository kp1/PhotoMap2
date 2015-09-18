package net.mmho.photomap2;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public class DistanceActionProvider extends ActionProvider
    implements MenuItem.OnMenuItemClickListener{
    private OnDistanceChangeListener onDistanceChangeListener;
    private int selected;

    private static final int INITIAL_INDEX = 1;

    private static final int[] distance={
            7,6,5,4,3

    };
    private static final int[] meters={
            150,
            600,
            5000,
            20000,
            150000,
    };

    private String pretty(int position) {
        int meter = meters[position];
        String unit = "m";
        if(meter>=1000){
            unit = "km";
            meter /= 1000;
        }
        return getContext().getString(R.string.about)+String.valueOf(meter)+unit;
    }


    public DistanceActionProvider(Context context) {
        super(context);
        selected = INITIAL_INDEX;
    }

    public static int initialIndex(){
        return INITIAL_INDEX;
    }

    public void setDistanceIndex(int index){
        selected = index;
    }

    public static int getDistance(int index){
        return distance[index];
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    @Override
    public boolean hasSubMenu() {
        return true;
    }

    @Override
    public void onPrepareSubMenu(SubMenu subMenu) {
        subMenu.clear();

        for(int i=0;i<distance.length;i++){
            MenuItem s = subMenu.add(0, i, i, pretty(i));
            s.setOnMenuItemClickListener(this);
            if(i==selected) s.setEnabled(false);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        selected = item.getItemId();
        if(onDistanceChangeListener !=null) onDistanceChangeListener.onDistanceChange(selected);
        return false;
    }
    
    public interface OnDistanceChangeListener {
        void onDistanceChange(int index);
    }

    public void setOnDistanceChangeListener(OnDistanceChangeListener changeListener){
        onDistanceChangeListener = changeListener;
    }

}
