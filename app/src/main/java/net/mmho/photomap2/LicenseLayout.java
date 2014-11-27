package net.mmho.photomap2;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class LicenseLayout extends LinearLayout{
    private String oss;
    private String TAG = "LicenseLayout";

    public LicenseLayout(Context context,String oss_name){
        super(context);
        oss = oss_name;
    }

    public LicenseLayout(Context context) {
        super(context);
    }

    public LicenseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
