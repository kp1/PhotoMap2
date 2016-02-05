package net.mmho.photomap2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        showAbout();
        LicenseAdapter adapter = new LicenseAdapter(this,R.layout.layout_license,
                getResources().getStringArray(R.array.oss));
        ((ListView)findViewById(R.id.list)).setAdapter(adapter);
    }

    private void showAbout() {
        TextView about = (TextView)findViewById(R.id.about);
        StringBuilder b = new StringBuilder();
        b.append(getString(R.string.about_software,getString(R.string.app_name)));
        b.append("\n\n");
        String[] list = getResources().getStringArray(R.array.oss);
        for(String oss:list){
            b.append("\t").append("・").append(oss).append("\n");
        }
        about.setText(b.toString());
    }
}
