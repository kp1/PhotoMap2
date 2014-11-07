package net.mmho.photomap2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;

public class PhotoListActivity extends ActionBarActivity implements ProgressChangeListener{

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);
        progressBar = (ProgressBar) findViewById(R.id.progress);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.list);
        if(fragment!=null && fragment instanceof BackPressedListener){
            ((BackPressedListener) fragment).onBackPressed();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void showProgress(int progress){
        progressBar.setProgress(progress);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void endProgress(){
        progressBar.setProgress(progressBar.getMax());
        AlphaAnimation fadeout;
        fadeout = new AlphaAnimation(1,0);
        fadeout.setDuration(1000);
        fadeout.setFillAfter(true);
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        progressBar.startAnimation(fadeout);
    }


}
