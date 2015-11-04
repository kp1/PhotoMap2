package net.mmho.photomap2;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class LicenseLayout extends LinearLayout{

    public LicenseLayout(Context context) {
        this(context,null);
    }

    public LicenseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(String oss){
        ((TextView)findViewById(R.id.title)).setText(oss+"\n");
        try{
            int id = getResources().getIdentifier("license_" + oss.toLowerCase(Locale.US),"raw", getContext().getPackageName());
            ((TextView)findViewById(R.id.license)).setText(getStringResource(id));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getStringResource(int id) throws IOException {
        InputStream is = getResources().openRawResource(id);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024*4]; // 4kB

        int i;
        try{
            do{
                i=is.read(buffer,0,buffer.length);
                if(i>0) os.write(buffer,0,i);
            }while(i!=-1);

        }
        finally {
            is.close();
        }
        return os.toString();
    }

}
