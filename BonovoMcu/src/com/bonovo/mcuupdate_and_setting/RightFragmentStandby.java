package com.bonovo.mcuupdate_and_setting;

import android.app.Fragment;
import android.app.Activity;
import android.widget.RadioButton;
import android.content.SharedPreferences;
import android.widget.RadioGroup;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

public class RightFragmentStandby extends Fragment {
    private int NO_STANDBY = 0;
    private int HALF_HOUR = 1;
    private int ONE_HOUR = 2;
    private int TWO_HOUR = 3;
    private int HALF_DAY = 4;
    private int ONE_DAY = 5;
    private CallBackStandby backStandby;
    private int checkFlag;
    private Activity context;
    private RadioButton halfDayBtn;
    private RadioButton halfHourBtn;
    private RadioButton noStandbyBtn;
    private RadioButton oneDayBtn;
    private RadioButton oneHourBtn;
    private SharedPreferences preferences;
    private RadioGroup radioGroup;
    private RadioButton twoHourBtn;
	
	public static interface CallBackStandby {
        public abstract void setStandby(int i);
    }
 
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.right_standby, null);
        radioGroup = (RadioGroup)view.findViewById(R.id.standbyGroup);
        halfHourBtn = (RadioButton)view.findViewById(R.id.half_hour);
        oneHourBtn = (RadioButton)view.findViewById(R.id.one_hour);
        twoHourBtn = (RadioButton)view.findViewById(R.id.two_hour);
        halfDayBtn = (RadioButton)view.findViewById(R.id.half_day);
        oneDayBtn = (RadioButton)view.findViewById(R.id.one_day);
        noStandbyBtn = (RadioButton)view.findViewById(R.id.no_hour);
        readSharePreConfig();
        checkRadioButton();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == halfHourBtn.getId()) {
                    checkFlag = HALF_HOUR;
                    preferences = context.getSharedPreferences("standby model", 0x1);
                    preferences.edit().putInt("standby checked", checkFlag).commit();
                } else if (checkedId == oneHourBtn.getId()) {
                    checkFlag = ONE_HOUR;
                    preferences = context.getSharedPreferences("standby model", 0x1);
                    preferences.edit().putInt("standby checked", checkFlag).commit();
                } else if (checkedId == twoHourBtn.getId()) {
                    checkFlag = TWO_HOUR;
                    preferences = context.getSharedPreferences("standby model", 0x1);
                    preferences.edit().putInt("standby checked", checkFlag).commit();
                } else if (checkedId == halfDayBtn.getId()) {
                    checkFlag = HALF_DAY;
                    preferences = context.getSharedPreferences("standby model", 0x1);
                    preferences.edit().putInt("standby checked", checkFlag).commit();
                } else if (checkedId == oneDayBtn.getId()) {
                    checkFlag = ONE_DAY;
                    preferences = context.getSharedPreferences("standby model", 0x1);
                    preferences.edit().putInt("standby checked", checkFlag).commit();
                } else if (checkedId == noStandbyBtn.getId()) {
                    checkFlag = NO_STANDBY;
                    preferences = context.getSharedPreferences("standby model", 0x1);
                    preferences.edit().putInt("standby checked", checkFlag).commit();
                }
            }
        });
        return view;
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public void onDestroy() {
        super.onDestroy();
    }
    
    private void readSharePreConfig() {
        preferences = context.getSharedPreferences("standby model", 0x1);
        checkFlag = preferences.getInt("standby checked", NO_STANDBY);
    }
    
    private void checkRadioButton() {
        if(checkFlag == NO_STANDBY) {
            noStandbyBtn.setChecked(true);
        } else if (checkFlag == HALF_HOUR) {
            halfHourBtn.setChecked(true);
        } else if (checkFlag == ONE_HOUR) {
            oneHourBtn.setChecked(true);
        } else if (checkFlag == TWO_HOUR) {
            twoHourBtn.setChecked(true);
        } else if (checkFlag == HALF_DAY) {
            halfDayBtn.setChecked(true);
        } else if (checkFlag == ONE_DAY) {
            oneDayBtn.setChecked(true);
        }
    }
}
