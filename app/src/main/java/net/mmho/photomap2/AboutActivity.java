package net.mmho.photomap2;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ScrollView;
import android.widget.TextView;

public class AboutActivity extends ActionBarActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        showAbout();
        showLicense();
    }

    private void showLicense() {
        ScrollView scroll = (ScrollView) findViewById(R.id.license);
        String[] list = getResources().getStringArray(R.array.oss);
        for(String oss:list){
            LicenseLayout layout = new LicenseLayout(this,oss);
            scroll.addView(layout);
        }
    }

    private void showAbout() {
        TextView about = (TextView)findViewById(R.id.about);
        StringBuilder b = new StringBuilder();
        b.append(getString(R.string.about,getString(R.string.app_name)));
        b.append("\n\n");
        String[] list = getResources().getStringArray(R.array.oss);
        for(String oss:list){
            b.append("\t").append("・").append(oss);
        }
        about.setText(b.toString());
    }
}
