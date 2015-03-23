package com.bonovo.mcuupdate_and_setting;

import android.app.Fragment;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.content.SharedPreferences;
import android.widget.CompoundButton;

public class RightFragmentSetting extends Fragment {
    private final String TAG = "com.example.fragment.right2";
    private CallbackInterface callbackInterface;
    private CheckBox checkBrigthness;
    private CheckBox checkCamera;
    private CheckBox checkMute;
    private CheckBox checkVolume;
    public static boolean isCheckCamera;
    public static boolean isCheckLight;
    public static boolean isCheckMute;
    public static boolean isCheckVolume;
    public static int progessBrigthness;
    public static int progessVolume;
    private SeekBar seekBarBrigthness;
    private SeekBar seekBarVolume;

	public static interface CallbackInterface {
        public abstract void autoVolume(int i);
        public abstract void lowBrigthness(int i);
        public abstract void setCamera(boolean flag);
        public abstract void setMute(boolean flag);
    }
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callbackInterface = (CallbackInterface)activity;
        } catch(Exception e) {
        }
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(0x7f030009, null);
        seekBarBrigthness = (SeekBar)view.findViewById(0x7f08002b);
        seekBarBrigthness.setMax(80);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0x0);
        progessBrigthness = sharedPreferences.getInt("Progress", 0x0);
        seekBarBrigthness.setProgress(progessBrigthness);
        seekBarBrigthness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            
			@Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0x0);
                sharedPreferences.edit().putInt("Progress", progress).commit();
                if(RightFragmentSetting.isCheckLight == 0x1) {
                    callbackInterface.lowBrigthness(progress + 10);
                }
            }
            
			@Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            
			@Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBarVolume = (SeekBar)view.findViewById(0x7f08002d);
        seekBarVolume.setMax(40);
        progessVolume = sharedPreferences.getInt("ProgressVolume", 0x0);
        seekBarVolume.setProgress(progessVolume);
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            
			@Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
            
			@Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            
			@Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0x0);
                sharedPreferences.edit().putInt("ProgressVolume", progress).commit();
                callbackInterface.autoVolume(progress + 10);
            }
        });
        checkVolume = (CheckBox)view.findViewById(0x7f08002c);
        isCheckVolume = sharedPreferences.getBoolean("volume", false);
        checkVolume.setChecked(isCheckVolume);
        if(isCheckVolume) {
            seekBarVolume.setVisibility(0x0);
        } else {
            seekBarVolume.setVisibility(0x8);
        }
        checkVolume.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            
			@Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    RightFragmentSetting.isCheckVolume = isChecked;
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0x0);
                    sharedPreferences.edit().putBoolean("volume", isChecked).commit();
                    seekBarVolume.setVisibility(0x0);
                    callbackInterface.autoVolume(RightFragmentSetting.progessVolume + 10);
                } else {
                    RightFragmentSetting.isCheckVolume = isChecked;
                    sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0x0);
                    sharedPreferences.edit().putBoolean("volume", isChecked).commit();
                    seekBarVolume.setVisibility(0x8);
                    callbackInterface.autoVolume(0);
    			}
            }
        });
        checkMute = (CheckBox)view.findViewById(0x7f080028);
        isCheckMute = sharedPreferences.getBoolean("mute", true);
        checkMute.setChecked(isCheckMute);
        checkMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            
			@Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    RightFragmentSetting.isCheckMute = isChecked;
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0x0);
                    sharedPreferences.edit().putBoolean("mute", isChecked).commit();
                    callbackInterface.setMute(true);
                } else {
					RightFragmentSetting.isCheckMute = isChecked;
					sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0x0);
					sharedPreferences.edit().putBoolean("mute", isChecked).commit();
					callbackInterface.setMute(false);
				}
            }
        });
        checkCamera = (CheckBox)view.findViewById(0x7f080029);
        isCheckCamera = sharedPreferences.getBoolean("camera", true);
        checkCamera.setChecked(isCheckCamera);
        checkCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            
			@Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    RightFragmentSetting.isCheckCamera = isChecked;
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0x0);
                    sharedPreferences.edit().putBoolean("camera", isChecked).commit();
                    callbackInterface.setCamera(true);
                } else {
					RightFragmentSetting.isCheckCamera = isChecked;
					sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0x0);
					sharedPreferences.edit().putBoolean("camera", isChecked).commit();
					callbackInterface.setCamera(false);
				}
            }
        });
        checkBrigthness = (CheckBox)view.findViewById(0x7f08002a);
        isCheckLight = sharedPreferences.getBoolean("brigthconfig", false);
        checkBrigthness.setChecked(isCheckLight);
        if(isCheckLight) {
            seekBarBrigthness.setVisibility(0x0);
        } else {
            seekBarBrigthness.setVisibility(0x8);
        }
        checkBrigthness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            
			@Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    RightFragmentSetting.isCheckLight = isChecked;
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0x0);
                    sharedPreferences.edit().putBoolean("brigthconfig", isChecked).commit();
                    seekBarBrigthness.setVisibility(0x0);
                    callbackInterface.lowBrigthness(RightFragmentSetting.progessBrigthness + 10);
                } else {
					RightFragmentSetting.isCheckLight = isChecked;
					sharedPreferences = getActivity().getSharedPreferences("CAR_CONFIG", 0x0);
					sharedPreferences.edit().putBoolean("brigthconfig", isChecked).commit();
					seekBarBrigthness.setVisibility(0x8);
					callbackInterface.lowBrigthness(0x0);
				}
            }
        });
        return view;
    }
    
    public void onPause() {
        super.onPause();
    }
}
