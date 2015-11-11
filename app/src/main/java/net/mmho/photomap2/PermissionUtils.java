package net.mmho.photomap2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

class PermissionUtils {
    static void requestPermission(View rootView,Context c){
        if(rootView!=null){
            Snackbar snackbar =
                Snackbar.make(rootView,R.string.request_permission,Snackbar.LENGTH_LONG);

            TextView text = (TextView) snackbar.getView()
                .findViewById(android.support.design.R.id.snackbar_text);
            int color,action_color;
            if(Build.VERSION.SDK_INT>=23) {
                color = c.getResources().getColor(R.color.textPrimary, null);
                action_color = c.getResources().getColor(R.color.primary,null);
            }
            else{
                //noinspection deprecation
                color = c.getResources().getColor(R.color.textPrimary);
                //noinspection deprecation
                action_color = c.getResources().getColor(R.color.primary);
            }
            snackbar.setActionTextColor(action_color);
            text.setTextColor(color);
            snackbar.setAction(R.string.setting,v -> {
                Uri uri =
                    Uri.parse("package:"+c.getApplicationContext().getPackageName());
                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,uri);
                c.startActivity(i);
            });
            snackbar.show();
        }

    }
}
