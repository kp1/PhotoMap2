package net.mmho.photomap2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;


public class PhotoListActivity extends AppCompatActivity implements ProgressChangeListener{

    public static final int PERMISSIONS_REQUEST = 1;
    private ProgressBar progressBar;


    private void showContents(){
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if(f==null) {
            setContentView(R.layout.activity_photo_list);
            setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
            progressBar = (ProgressBar) findViewById(R.id.progress);
        }
    }
    private void checkPermissions(){
        if(Build.VERSION.SDK_INT >= 23
            && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST);
        }
        else{
            showContents();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
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
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode){
        case PERMISSIONS_REQUEST:
            if(grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showContents();
            }
            else{
                finish();
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
