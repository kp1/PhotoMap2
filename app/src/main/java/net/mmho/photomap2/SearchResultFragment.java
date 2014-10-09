package net.mmho.photomap2;

import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.lang.reflect.Field;
import java.util.List;

public class SearchResultFragment extends DialogFragment{
    List<Address> addresses = null;
    static SearchResultFragment newInstance(){
        SearchResultFragment fragment = new SearchResultFragment();

        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            Field mDismissedField = DialogFragment.class.getDeclaredField("mDismissed");
            mDismissedField.setAccessible(true);
            mDismissedField.set(this, true);
            Field mShownByMeField = DialogFragment.class.getDeclaredField("mShownByMe");
            mShownByMeField.setAccessible(true);
            mShownByMeField.set(this, true);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch(NoSuchFieldException e){
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }
}
