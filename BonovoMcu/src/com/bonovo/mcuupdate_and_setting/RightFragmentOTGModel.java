package com.bonovo.mcuupdate_and_setting;

import android.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.widget.RadioButton;
import android.content.SharedPreferences;
import android.widget.RadioGroup;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

public class RightFragmentOTGModel extends Fragment {
    private int PRESSHOST = 1;
    private int PRESSSLAVE = 2;
    private CallBackOTG backOTG;
    private Activity context;
    private RadioButton hostBtn;
    private int otgFlag;
    private SharedPreferences preferences;
    private RadioGroup radioGroup;
    private RadioButton slaveBtn;
	
	public static interface CallBackOTG {
        public abstract void switchOTG(int i);
    }
    
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        try {
            backOTG = (CallBackOTG)activity;
        } catch(Exception e) {
        }
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.right_switch_otg, null);
        radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup3);
        hostBtn = (RadioButton)view.findViewById(R.id.radioHost);
        slaveBtn = (RadioButton)view.findViewById(R.id.radioSlave);
        readSharePreConfig();
        checkRadioButton();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            
			@Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == hostBtn.getId()) {
                    otgFlag = PRESSHOST;
                    preferences = context.getSharedPreferences("otg model", Context.MODE_WORLD_READABLE);
                    preferences.edit().putInt("otg checked", otgFlag).commit();
                    backOTG.switchOTG(otgFlag);
                    return;
                }
                if(checkedId == slaveBtn.getId()) {
                    otgFlag = PRESSSLAVE;
                    preferences = context.getSharedPreferences("otg model", Context.MODE_WORLD_READABLE);
                    preferences.edit().putInt("otg checked", otgFlag).commit();
                    backOTG.switchOTG(otgFlag);
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
        preferences = context.getSharedPreferences("otg model", Context.MODE_WORLD_READABLE);
        otgFlag = preferences.getInt("otg checked", PRESSHOST);
    }
    
    private void checkRadioButton() {
        if(otgFlag == PRESSHOST) {
            hostBtn.setChecked(true);
        } else if(otgFlag == PRESSSLAVE) {
            slaveBtn.setChecked(true);
        }
    }
}
