package net.mmho.photomap2;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.SupportMapFragment;

public class CustomMapFragment extends SupportMapFragment {
	final static String TAG="CustomMapFragment";
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(savedInstanceState==null){
			setRetainInstance(true);
		}
	}
}
