package net.mmho.photomap2;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import java.text.NumberFormat;

public class DistanceActionProvider extends ActionProvider
    implements MenuItem.OnMenuItemClickListener{
    private OnDistanceChangeListener onDistanceChangeListener;
    private int selected;

    private static final int INITIAL_INDEX = 1;

    private static final float[] distance={
            50,
            500,
            5*1000,
            50*1000,
            500*1000,
            5000*1000,
    };

    static private String pretty(int position) {
        StringBuilder pretty = new StringBuilder();
        int d = (int) (distance[position]) * 2;
        String prefix ="";
        if(d>=1000){
            d/=1000;
            prefix = "k";
        }
        pretty.append(NumberFormat.getNumberInstance().format(d)).append(prefix).append("m");
        return pretty.toString();
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

    public static float getDistance(int index){
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
        public void onDistanceChange(int index);
    }

    public void setOnDistanceChangeListener(OnDistanceChangeListener changeListener){
        onDistanceChangeListener = changeListener;
    }

}
