package net.mmho.photomap2;

import android.graphics.Bitmap;
import android.location.Address;

import java.util.ArrayList;
import java.util.List;

public interface LoaderCallbacks {
    public void GeocodeCallback(List<Address> data);
    public void ThumbnailCallback(Bitmap data);
}
