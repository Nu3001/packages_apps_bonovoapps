package com.bonovo.mcuupdate_and_setting;

import android.app.Fragment;
import android.widget.TextView;
import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.os.Build;
import android.content.res.Resources;

public class RigthFragmentVersion extends Fragment {
    private final String TAG = "com.example.fragment.rigthFragment";
    private CallbackFragment1 callbackFragment1;
    private TextView isAndroidVersion;
    private TextView isMcuVersion;
    private TextView isSystemVersion;
	
	public static interface CallbackFragment1 {
        public abstract int getMcuVersion();
        public abstract String getSystemVersion();
    }
    
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.v(TAG, "-->RigthFragment-->onAttach()");
        try {
            callbackFragment1 = (CallbackFragment1)activity;
        } catch(Exception e) {
        }
    }
    
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v(TAG, "-->RigthFragment-->onActivityCreated()");
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "-->RigthFragment-->onCreate()");
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "-->RigthFragment-->onCreateView()");
        View view = inflater.inflate(0x7f03000c, null);
        isAndroidVersion = (TextView)view.findViewById(0x7f080039);
        isAndroidVersion.setText(Build.VERSION.RELEASE);
        isMcuVersion = (TextView)view.findViewById(0x7f08003b);
        isMcuVersion.setText("V" + Integer.toString(callbackFragment1.getMcuVersion()) + getResources().getString(0x7f050025));
        isSystemVersion = (TextView)view.findViewById(0x7f08003d);
        isSystemVersion.setText(callbackFragment1.getSystemVersion());
        return view;
    }
    
    public void onStart() {
        super.onStart();
        Log.v(TAG, "-->RigthFragment-->onStart()");
    }
    
    public void onResume() {
        super.onResume();
        Log.v(TAG, "-->RigthFragment-->onResume()");
    }
    
    public void onPause() {
        super.onPause();
        Log.v(TAG, "-->RigthFragment-->onPause()");
    }
}
