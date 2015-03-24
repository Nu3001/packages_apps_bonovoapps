package com.bonovo.mcuupdate_and_setting;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.content.IntentFilter;
import android.util.Log;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

public class RightFragmentCarConfig extends Fragment {
    private static int Car_RadioButton_Flag;
    private final int Sonata8;
    private final int Sonata8_CHECKED;
    private final String TAG;
    private final int Volkswagen;
    private final int Volkswagen_CHECKED;
    private BroadcastReceiver broadcastReceiver;
    private Activity context;
    private SharedPreferences preferences;
    private RadioButton radioButton_Sonata8;
    private RadioButton radioButton_Volkswagen;
    private RadioGroup radioGroup;
    
    public RightFragmentCarConfig() {
        TAG = "com.example.fragment.right_car_config";
        Volkswagen = 0;
        Sonata8 = 1;
        Volkswagen_CHECKED = 1;
        Sonata8_CHECKED = 2;
        broadcastReceiver = new BroadcastReceiver(this) {
            
			@Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("com.android.internal.car.can.action.CAR_TYPE_RESPONSE")) {
                    Car_Flag = intent.getIntExtra("car_type", 0);
                    if(Car_Flag == Volkswagen) {
                        Log.v("com.example.fragment.right_car_config", "3333333");
                        radioButton_Volkswagen.setChecked(true);
                    } else if(Car_Flag == Sonata8) {
                        Log.v("com.example.fragment.right_car_config", "44444444");
                        radioButton_Sonata8.setChecked(true);
                    } else {
						Log.v("com.example.fragment.right_car_config", "1111111 --> Car_Flag = " + Car_Flag);
					}
                }
            }
        };
    }
    private static int Car_Flag = 0;
    
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
        View view = inflater.inflate(R.layout.right_car_configui, null);
        radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup2);
        radioButton_Volkswagen = (RadioButton)view.findViewById(R.id.radioVolkswagen);
        radioButton_Sonata8 = (RadioButton)view.findViewById(R.id.radioSonata8);
        readSharePreConfig();
        checkRadioButton();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            
			@Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == radioButton_Volkswagen.getId()) {
                    RightFragmentCarConfig.Car_RadioButton_Flag = Volkswagen;
                    FragmentService.carType = Volkswagen;
                    preferences = context.getSharedPreferences("car_checked_result", 1);
                    preferences.edit().putInt("radioButton_Checked_Flag", RightFragmentCarConfig.Car_RadioButton_Flag).commit();
                    Intent intent = new Intent("com.android.internal.car.can.action.CAR_TYPE_CHANGED");
                    intent.putExtra("car_type", RightFragmentCarConfig.Car_RadioButton_Flag);
                    context.sendBroadcast(intent);
                } else if (checkedId == radioButton_Sonata8.getId()) {
                    RightFragmentCarConfig.Car_RadioButton_Flag = Sonata8;
                    FragmentService.carType = Sonata8;
                    preferences = context.getApplicationContext().getSharedPreferences("car_checked_result", 1);
                    preferences.edit().putInt("radioButton_Checked_Flag", RightFragmentCarConfig.Car_RadioButton_Flag).commit();
                    Intent intent = new Intent("com.android.internal.car.can.action.CAR_TYPE_CHANGED");
                    intent.putExtra("car_type", RightFragmentCarConfig.Car_RadioButton_Flag);
                    context.sendBroadcast(intent);
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
        Car_RadioButton_Flag = FragmentService.carType;
        Log.v("com.example.fragment.right_car_config", "serial_RadioButton_Flag =" + Car_RadioButton_Flag);
    }
    
    private void checkRadioButton() {
        if(Car_RadioButton_Flag == Volkswagen) {
            radioButton_Volkswagen.setChecked(true);
        } else if (Car_RadioButton_Flag == Sonata8) {
            radioButton_Sonata8.setChecked(true);
        }
    }
    
    private IntentFilter getIntentFilter() {
        IntentFilter myIntentFilter = new IntentFilter("com.android.internal.car.can.action.CAR_TYPE_RESPONSE");
        myIntentFilter.addCategory("com.android.internal.car.can.Car");
        return myIntentFilter;
    }
}
