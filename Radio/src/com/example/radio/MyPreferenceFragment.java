package com.example.radio;

import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.app.Activity;
import android.preference.ListPreference;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.content.SharedPreferences;
import android.content.Intent;
import android.preference.PreferenceScreen;

public class MyPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
	private MyPreferenceFragment.CallbackSetting callbackSetting;
	private CheckBoxPreference checkBoxRemote;
	private Activity context;
	private ListPreference countryListPre;
	private VolumeSeekBarPreferences seekBarPreferences;
	
	public MyPreferenceFragment() {
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preference_setting);
		seekBarPreferences = (VolumeSeekBarPreferences)findPreference("seekBarPreference");
		seekBarPreferences.setSeekbarListener(new VolumeSeekBarPreferences.seekBarCallBack() {
			
			public void setSeekBarVolume(int volume) {
				callbackSetting.setVolume(volume);
			}
			
			public int getSeekBarVolume() {
				return callbackSetting.getVolume();
			}
		});
		checkBoxRemote = (CheckBoxPreference)findPreference("checkbox_remote_preference");
		checkBoxRemote.setOnPreferenceChangeListener(this);
		checkBoxRemote.setOnPreferenceClickListener(this);
		countryListPre = (ListPreference)findPreference("countries_list_preference");
		countryListPre.setOnPreferenceChangeListener(this);
		countryListPre.setOnPreferenceClickListener(this);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
		try {
			callbackSetting = (MyPreferenceFragment.CallbackSetting)context;
		} catch(Exception e) {
		}
	}
	
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		SharedPreferences modelpre = context.getSharedPreferences("CHECKED", 0x0);
		if(preference == checkBoxRemote) {
			if(newValue.booleanValue()) {
				callbackSetting.setRemoteModel(true);
			} else {
				callbackSetting.setRemoteModel(false);
			}
		} else if (preference == countryListPre) {
			if(newValue.equals("1")) {
				modelpre.edit().putInt("radioModel", 0x1).commit();
				callbackSetting.readModelInfo();
				Intent intent = new Intent();
				intent.setAction("updateFreqView");
				context.sendBroadcast(intent);
			} else if (newValue.equals("2")) {
				modelpre.edit().putInt("radioModel", 0x0).commit();
				callbackSetting.readModelInfo();
				Intent intent = new Intent();
				intent.setAction("updateFreqView");
				context.sendBroadcast(intent);
			} else if(newValue.equals("3")) {
				modelpre.edit().putInt("radioModel", 0x2).commit();
				callbackSetting.readModelInfo();
				Intent intent = new Intent();
				intent.setAction("updateFreqView");
				context.sendBroadcast(intent);
			}
		}
		return true;
	}
	
	public boolean onPreferenceClick(Preference preference) {
		return false;
	}
	
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if(preference == seekBarPreferences) {
			return seekBarPreferences;
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
}
