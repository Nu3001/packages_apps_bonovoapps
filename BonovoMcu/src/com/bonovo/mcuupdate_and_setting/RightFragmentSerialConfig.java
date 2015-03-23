package com.bonovo.mcuupdate_and_setting;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.app.Activity;
import android.widget.RadioGroup;
import android.content.SharedPreferences;
import android.widget.RadioButton;
import android.content.IntentFilter;
import android.util.Log;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

public class RightFragmentSerialConfig extends Fragment {
    private final int CAN = 2;
    private final int CAN_CHECKED = 2;
    private final int NON = 0;
    private final int NON_CHECKED = 0;
    private final int OBD = 1;
    private final int OBD_CHECKED = 1;
    private final String TAG = "com.example.fragment.right_serial_config";
    private Activity context;
    private RadioGroup group;
    private SharedPreferences preferences;
    private RadioButton radioButton_CAN;
    private RadioButton radioButton_NON;
    private RadioButton radioButton_OBD;
    private static int serial_RadioButton_Flag;
    
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        
		@Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.android.internal.car.can.action.SERIAL_TYPE_RESPONSE")) {
                Serial_Flag = intent.getIntExtra("serial_type", 0x0);
                if(Serial_Flag == ODB) {
                    radioButton_OBD.setChecked(true);
                } else if(Serial_Flag == CAN) {
                    radioButton_CAN.setChecked(true);
                } else if(Serial_Flag == NON) {
                    radioButton_NON.setChecked(true);
                }
            }
        }
    };
	
    private static int Serial_Flag = 0;
    
    public void onAttach(Activity activity) {
        context = activity;
        context.registerReceiver(broadcastReceiver, getIntentFilter());
        super.onAttach(activity);
    }
    
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(0x7f030008, null);
        group = (RadioGroup)view.findViewById(0x7f080024);
        radioButton_OBD = (RadioButton)view.findViewById(0x7f080025);
        radioButton_CAN = (RadioButton)view.findViewById(0x7f080026);
        radioButton_NON = (RadioButton)view.findViewById(0x7f080027);
        readSharePreConfig();
        checkRadioButton();
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == radioButton_OBD.getId()) {
                    serial_RadioButton_Flag = ODB_CHECKED;
                    FragmentService.serialType = ODB;
                    Intent intent = new Intent("com.android.internal.car.can.action.SERIAL_TYPE_CHANGED");
                    intent.putExtra("serial_type", serial_RadioButton_Flag);
                    context.sendBroadcast(intent);
                    preferences = context.getSharedPreferences("serial_checked_result", 0x1);
                    preferences.edit().putInt("radioButton_Checked_Flag", serial_RadioButton_Flag).commit();
                    return;
                }
                if(checkedId == radioButton_CAN.getId()) {
                    serial_RadioButton_Flag = CAN_CHECKED;
                    FragmentService.serialType = CAN;
                    Intent intent = new Intent("com.android.internal.car.can.action.SERIAL_TYPE_CHANGED");
                    intent.putExtra("serial_type", serial_RadioButton_Flag);
                    context.sendBroadcast(intent);
                    preferences = context.getSharedPreferences("serial_checked_result", 0x1);
                    preferences.edit().putInt("radioButton_Checked_Flag", serial_RadioButton_Flag).commit();
                    return;
                }
                if(checkedId == radioButton_NON.getId()) {
                    serial_RadioButton_Flag = NON_CHECKED;
                    FragmentService.serialType = NON;
                    Intent intent = new Intent("com.android.internal.car.can.action.SERIAL_TYPE_CHANGED");
                    intent.putExtra("serial_type", serial_RadioButton_Flag);
                    context.sendBroadcast(intent);
                    preferences = context.getSharedPreferences("serial_checked_result", 0x1);
                    preferences.edit().putInt("radioButton_Checked_Flag", serial_RadioButton_Flag).commit();
                }
            }
        });
        return view;
    }
    
    public void onDestroy() {
        super.onDestroy();
        context.unregisterReceiver(broadcastReceiver);
    }
    
    private void readSharePreConfig() {
        serial_RadioButton_Flag = FragmentService.serialType;
        Log.v(TAG, "serial_RadioButton_Flag =" + serial_RadioButton_Flag);
    }
    
    private void checkRadioButton() {
        if(serial_RadioButton_Flag == ODB_CHECKED) {
            radioButton_OBD.setChecked(true);
        }else if(serial_RadioButton_Flag == CAN_CHECKED) {
            radioButton_CAN.setChecked(true);
        }else if(serial_RadioButton_Flag == NON_CHECKED) {
            radioButton_NON.setChecked(true);
        }
    }
    
    private IntentFilter getIntentFilter() {
        IntentFilter myIntentFilter = new IntentFilter("com.android.internal.car.can.action.SERIAL_TYPE_RESPONSE");
        myIntentFilter.addCategory("com.android.internal.car.can.Serial");
        return myIntentFilter;
    }
}
