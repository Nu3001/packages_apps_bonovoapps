package com.bonovo.mcuupdate_and_setting;



import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RigthFragmentVersion extends Fragment {
	private final String TAG = "com.example.fragment.rigthFragment";
	
	private TextView isAndroidVersion;
	private TextView isMcuVersion;
	private TextView isSystemVersion;
	private CallbackFragment1 callbackFragment1;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		Log.v(TAG, "-->RigthFragment-->onAttach()");
		try{
			callbackFragment1 = (CallbackFragment1)activity;
		}catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.v(TAG, "-->RigthFragment-->onActivityCreated()");
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.v(TAG, "-->RigthFragment-->onCreate()");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.v(TAG, "-->RigthFragment-->onCreateView()");
		View view = inflater.inflate(R.layout.right_version, null);
		isAndroidVersion = (TextView)view.findViewById(R.id.is_android_version);
		isAndroidVersion.setText(android.os.Build.VERSION.RELEASE);
//		String item = getArguments().getString("item");
		isMcuVersion = (TextView)view.findViewById(R.id.is_mcu_version);
		isMcuVersion.setText("V" + Integer.toString(callbackFragment1.getMcuVersion()) + getResources().getString(R.string.msg_1));
		isSystemVersion = (TextView)view.findViewById(R.id.is_system_version);
		isSystemVersion.setText(callbackFragment1.getSystemVersion());
		
		return view;
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.v(TAG, "-->RigthFragment-->onStart()");
		
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.v(TAG, "-->RigthFragment-->onResume()");
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.v(TAG, "-->RigthFragment-->onPause()");
	}
	
	public interface CallbackFragment1 {
		public int getMcuVersion();
		public String getSystemVersion();
	}
	
}
