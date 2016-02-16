package net.mmho.photomap2;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;


public class PhotoListActivity extends AppCompatActivity implements ProgressChangeListener{

    public static final int PERMISSIONS_REQUEST = 1;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        progressBar = (ProgressBar) findViewById(R.id.progress);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        switch (requestCode){
        case PERMISSIONS_REQUEST:
            boolean granted = grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(fragment!=null){
                ((PhotoListFragment)fragment).grantedPermission(granted);
            }
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
        fadeout.setDuration(300);
        fadeout.setFillAfter(true);
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                progressBar.setVisibility(View.GONE);
                progressBar.setAnimation(null);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        progressBar.startAnimation(fadeout);

    }


}
