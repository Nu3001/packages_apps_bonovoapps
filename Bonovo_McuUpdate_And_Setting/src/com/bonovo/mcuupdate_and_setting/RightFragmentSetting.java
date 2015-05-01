package com.bonovo.mcuupdate_and_setting;



import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class RightFragmentSetting extends Fragment {
	private final String TAG = "com.example.fragment.right2";
	
	private SeekBar seekBarBrigthness;
	private SeekBar seekBarVolume;
	private CheckBox checkMute;
	private CheckBox checkCamera;
	private CheckBox checkBrigthness;
	private CheckBox checkVolume;
	
	public static boolean isCheckLight;			// 大灯背光调节标志
	public static boolean isCheckMute;			// 倒车静音标志־
	public static boolean isCheckCamera;		// 背后视标志
	public static boolean isCheckVolume;		// 自动音量调节标志
	
	public static int progessBrigthness;					// 保存大灯背光调节滑动条数值
	public static int progessVolume;						// 保存自动音量滑动条数值
	
	private CallbackInterface callbackInterface;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			callbackInterface = (CallbackInterface) activity;
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.right_setting, null);
		seekBarBrigthness = (SeekBar) view.findViewById(R.id.seekBar1);
		seekBarBrigthness.setMax(80);
		
		SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0);
        progessBrigthness = sharedPreferences.getInt("Progress", 0);
        seekBarBrigthness.setProgress(progessBrigthness);
		
		seekBarBrigthness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
						"CAR_CONFIG", 0);
				sharedPreferences.edit().putInt("Progress", progress).commit();
				
				if (isCheckLight == true ) {
					callbackInterface.lowBrigthness(progress+10);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}
		});//end of seekBarBrigthness
		
		seekBarVolume = (SeekBar)view.findViewById(R.id.seekBar2);
		seekBarVolume.setMax(40);
		
		progessVolume = sharedPreferences.getInt("ProgressVolume", 0);
		
		seekBarVolume.setProgress(progessVolume);
		seekBarVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
						"CAR_CONFIG", 0);
				sharedPreferences.edit().putInt("ProgressVolume", progress).commit();
				callbackInterface.autoVolume(progress + 10);
			}
		});//end of seekBarVolume
		
		checkVolume = (CheckBox)view.findViewById(R.id.checkBoxVolume);
		isCheckVolume = sharedPreferences.getBoolean("volume", false);
		
		checkVolume.setChecked(isCheckVolume);
		if (isCheckVolume) {
			seekBarVolume.setVisibility(View.VISIBLE);
		}else{
			seekBarVolume.setVisibility(View.GONE);
		}
		checkVolume.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					isCheckVolume = isChecked;
					
					SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0);
					sharedPreferences.edit().putBoolean("volume", isChecked).commit();
					
					seekBarVolume.setVisibility(View.VISIBLE);
					callbackInterface.autoVolume(progessVolume + 10);
				}else{
					isCheckVolume = isChecked;
					
					SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0);
					sharedPreferences.edit().putBoolean("volume", isChecked).commit();
					
					seekBarVolume.setVisibility(View.GONE);
					callbackInterface.autoVolume(0);
				}
			}
		});
		
		checkMute = (CheckBox)view.findViewById(R.id.checkBoxMute);
		isCheckMute = sharedPreferences.getBoolean("mute", true);
		checkMute.setChecked(isCheckMute);
		
		checkMute.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					isCheckMute = isChecked;
					
					SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0);
					sharedPreferences.edit().putBoolean("mute", isChecked).commit();
					
					callbackInterface.setMute(true);
				}else {
					isCheckMute = isChecked;
					
					SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0);
					sharedPreferences.edit().putBoolean("mute", isChecked).commit();
					
					callbackInterface.setMute(false);
				}
			}
		});//end of checkMute
		
		checkCamera = (CheckBox)view.findViewById(R.id.checkBoxCamera);
		isCheckCamera = sharedPreferences.getBoolean("camera", true);
		
		checkCamera.setChecked(isCheckCamera);
		checkCamera.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					isCheckCamera = isChecked;
					
					SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0);
					sharedPreferences.edit().putBoolean("camera", isChecked).commit();
					
					callbackInterface.setCamera(true);
				}else {
					isCheckCamera = isChecked;
					
					SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0);
					sharedPreferences.edit().putBoolean("camera", isChecked).commit();
					
					callbackInterface.setCamera(false);
				}
			}
		});//end of checkCamera
		
		checkBrigthness = (CheckBox)view.findViewById(R.id.checkBoxBrigth);
		
		//SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0);
        isCheckLight = sharedPreferences.getBoolean("brigthconfig", false);
        
		checkBrigthness.setChecked(isCheckLight);
		
		if (isCheckLight) {
			seekBarBrigthness.setVisibility(View.VISIBLE);
		}else{
			seekBarBrigthness.setVisibility(View.GONE);
		}
		
		checkBrigthness.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					isCheckLight = isChecked;
					
					SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0);
					sharedPreferences.edit().putBoolean("brigthconfig", isChecked).commit();
					seekBarBrigthness.setVisibility(View.VISIBLE);
					callbackInterface.lowBrigthness(progessBrigthness + 10);
				}else {
					isCheckLight = isChecked;
					
					SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0);
					sharedPreferences.edit().putBoolean("brigthconfig", isChecked).commit();
					seekBarBrigthness.setVisibility(View.GONE);
					callbackInterface.lowBrigthness(0);
				}
			}
		});//end of checkBrigthness
		return view;
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	public interface CallbackInterface {
		public void setMute(boolean flag);
		public void setCamera(boolean flag);
		public void lowBrigthness(int progress);
		public void autoVolume(int Progress);
	}

}
