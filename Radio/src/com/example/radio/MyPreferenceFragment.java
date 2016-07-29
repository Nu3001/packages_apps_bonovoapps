package com.example.radio;

import com.example.radio.VolumeSeekBarPreferences.seekBarCallBack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MyPreferenceFragment extends PreferenceFragment implements
		OnPreferenceClickListener, OnPreferenceChangeListener {
	private Activity context;
	private CallbackSetting callbackSetting;

	private VolumeSeekBarPreferences seekBarPreferences;
	private CheckBoxPreference checkBoxRemote;
	private ListPreference countryListPre;
	private ListPreference layoutList;
	private ListPreference scanDelayPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_setting);
		seekBarPreferences = (VolumeSeekBarPreferences) findPreference("seekBarPreference");
		seekBarPreferences.setSeekbarListener(new seekBarCallBack() {

			@Override
			public void setSeekBarVolume(int volume) {
				// TODO Auto-generated method stub
				callbackSetting.setVolume(volume);
			}

			@Override
			public int getSeekBarVolume() {
				// TODO Auto-generated method stub
				return callbackSetting.getVolume();
			}
		});

		checkBoxRemote = (CheckBoxPreference) findPreference("checkbox_remote_preference");
		checkBoxRemote.setOnPreferenceChangeListener(this);
		checkBoxRemote.setOnPreferenceClickListener(this);

		countryListPre = (ListPreference) findPreference("countries_list_preference");
		countryListPre.setOnPreferenceChangeListener(this);
		countryListPre.setOnPreferenceClickListener(this);

		layoutList = (ListPreference) findPreference("layout_list_preference");
		layoutList.setOnPreferenceChangeListener(this);
		layoutList.setOnPreferenceClickListener(this);

		scanDelayPref = (ListPreference) findPreference("scan_delay_list_preference");
		scanDelayPref.setOnPreferenceChangeListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		context = activity;
		try {
			callbackSetting = (CallbackSetting) context;
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	/*
	 * 利用回调函数,让IntentActivity调用Service的方法来实现Fragment间接调用Service的方法
	 */
	public interface CallbackSetting {
		public int getVolume();

		public void setVolume(int volume);

		public void setRemoteModel(boolean flag);

		public void readModelInfo();

		public void readLayoutInfo();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		SharedPreferences settings = context.getSharedPreferences("RadioPreferences", 0);
		if(preference == checkBoxRemote){
			if((Boolean)newValue){
				callbackSetting.setRemoteModel(true);
			}else {
				callbackSetting.setRemoteModel(false);
			}

		}else if(preference == countryListPre){
			if(newValue.equals("1")){
                settings.edit().putInt("radioModel", RadioService.JAPAN_MODEL).commit();
				callbackSetting.readModelInfo();
				Intent intent = new Intent();
				intent.setAction("updateFreqView");
				context.sendBroadcast(intent);
			}else if(newValue.equals("2")){
                settings.edit().putInt("radioModel", RadioService.CHINA_MODEL).commit();
				callbackSetting.readModelInfo();
				Intent intent = new Intent();
				intent.setAction("updateFreqView");
				context.sendBroadcast(intent);
			}else if(newValue.equals("3")){
                settings.edit().putInt("radioModel", RadioService.EUR_MODEL).commit();
				callbackSetting.readModelInfo();
				Intent intent = new Intent();
				intent.setAction("updateFreqView");
				context.sendBroadcast(intent);
			}else if(newValue.equals("4")){
                settings.edit().putInt("radioModel", RadioService.ITUREGION1_MODEL).commit();
				callbackSetting.readModelInfo();
				Intent intent = new Intent();
				intent.setAction("updateFreqView");
				context.sendBroadcast(intent);
			}else if(newValue.equals("5")){
                settings.edit().putInt("radioModel", RadioService.ITUREGION2_MODEL).commit();
				callbackSetting.readModelInfo();
				Intent intent = new Intent();
				intent.setAction("updateFreqView");
				context.sendBroadcast(intent);
			}else if(newValue.equals("6")){
                settings.edit().putInt("radioModel", RadioService.ITUREGION2_MODEL).commit();
				callbackSetting.readModelInfo();
				Intent intent = new Intent();
				intent.setAction("updateFreqView");
				context.sendBroadcast(intent);
			}

		} else if(preference == layoutList){
			if(newValue.equals("1")){
                settings.edit().putInt("radioLayout", RadioService.NEW_LAYOUT).commit();
				callbackSetting.readLayoutInfo();
				Intent intent = new Intent();
				intent.setAction("updateLayoutView");
				context.sendBroadcast(intent);
			}else if(newValue.equals("2")) {
                settings.edit().putInt("radioLayout", RadioService.OLD_LAYOUT).commit();
				callbackSetting.readLayoutInfo();
				Intent intent = new Intent();
				intent.setAction("updateLayoutView");
				context.sendBroadcast(intent);
			}else if(newValue.equals("3")){
                settings.edit().putInt("radioLayout", RadioService.CUSTOM_LAYOUT).commit();
				callbackSetting.readLayoutInfo();
				Intent intent = new Intent(this.getActivity().getBaseContext(), RadioCustomColorActivity.class);
				intent.setAction("updateLayoutView");
				context.sendBroadcast(intent);
				startActivity(intent);
			}
		} else if (preference == scanDelayPref) {
			int secs;

			try {
				secs = Integer.parseInt(String.valueOf(newValue), 10);
			} catch (NumberFormatException nfe) {
				return false;
			}

			settings.edit().putInt("scanDelayMsecs", (int) (DateUtils.SECOND_IN_MILLIS * secs)).apply();
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		if(preference == seekBarPreferences){

		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

}
