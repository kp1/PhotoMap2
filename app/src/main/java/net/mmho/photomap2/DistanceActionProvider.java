package net.mmho.photomap2;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public class DistanceActionProvider extends ActionProvider
    implements MenuItem.OnMenuItemClickListener{
    private OnDistanceChangeListener onDistanceChangeListener;

    public DistanceActionProvider(Context context) {
        super(context);
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

        for(int i=0;i<DistanceUtils.size();i++){
            subMenu.add(0,i,i,DistanceUtils.pretty(i)).setOnMenuItemClickListener(this);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(onDistanceChangeListener !=null) onDistanceChangeListener.onDistanceChange(DistanceUtils.getDistance(item.getItemId()));
        return false;
    }
    
    public interface OnDistanceChangeListener {
        public void onDistanceChange(float distance);
    }

    public void setOnDistanceChangeListener(OnDistanceChangeListener changeListener){
        onDistanceChangeListener = changeListener;
    }

}
