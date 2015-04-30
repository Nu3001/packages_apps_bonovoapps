package com.bonovo.mcuupdate_and_setting;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class RightFragmentSerialConfig extends Fragment {
	private final String TAG = "com.example.fragment.right_serial_config";
	
	private Activity context;
	private RadioGroup group;
	private RadioButton radioButton_OBD;
	private RadioButton radioButton_CAN;
	private RadioButton radioButton_NON;
	private SharedPreferences preferences;
	private static int serial_RadioButton_Flag; //保存RadioButton信息的全局变量
	private static int Serial_Flag = 0;					//全局变量:保存从CanBusService传过来的Serial值 -->OBD：1 ,CAN:2 ,Non:0
	private final int OBD = 1;			//OBD 
	private final int CAN = 2;			//CAN 
	private final int NON = 0;			//NON 
	private final int OBD_CHECKED = 1;			//OBD RadioButton被按下
	private final int CAN_CHECKED = 2;			//CAN RadioButton被按下
	private final int NON_CHECKED = 0;			//NON RadioButton被按下
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		context = activity;
		context.registerReceiver(broadcastReceiver, getIntentFilter());
		super.onAttach(activity);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.right_serial_configui, null);
		group = (RadioGroup)view.findViewById(R.id.radioGroup1);
		radioButton_OBD = (RadioButton)view.findViewById(R.id.radioOBD);
		radioButton_CAN = (RadioButton)view.findViewById(R.id.radioCAN);
		radioButton_NON = (RadioButton)view.findViewById(R.id.radioNON);
		
//		Intent sendIntent = new Intent("com.android.internal.car.can.action.SERIAL_TYPE_REQUEST");
//		 context.sendBroadcast(sendIntent);
		readSharePreConfig();
		checkRadioButton();
		
		group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId== radioButton_OBD.getId()){
					serial_RadioButton_Flag = OBD;		//将全局变量赋为OBD
					FragmentService.serialType = OBD;
					Intent intent = new Intent("com.android.internal.car.can.action.SERIAL_TYPE_CHANGED");
					intent.putExtra("serial_type", serial_RadioButton_Flag);
					context.sendBroadcast(intent);
					preferences = context.getSharedPreferences("serial_checked_result", Context.MODE_WORLD_READABLE);	//保存全局变量的信息
					preferences.edit()
									  .putInt("radioButton_Checked_Flag", serial_RadioButton_Flag)
									  .commit();
				}else if (checkedId== radioButton_CAN.getId()) {
					serial_RadioButton_Flag = CAN;		//将全局变量赋为CAN
					FragmentService.serialType = CAN;
					Intent intent = new Intent("com.android.internal.car.can.action.SERIAL_TYPE_CHANGED");
					intent.putExtra("serial_type", serial_RadioButton_Flag);
					context.sendBroadcast(intent);
					preferences = context.getSharedPreferences("serial_checked_result", Context.MODE_WORLD_READABLE);	//保存全局变量的信息
					preferences.edit()
									  .putInt("radioButton_Checked_Flag", serial_RadioButton_Flag)
									  .commit();
				}else if (checkedId== radioButton_NON.getId()) {
					serial_RadioButton_Flag = NON;		//将全局变量赋为NON
					FragmentService.serialType = NON;
					Intent intent = new Intent("com.android.internal.car.can.action.SERIAL_TYPE_CHANGED");
					intent.putExtra("serial_type", serial_RadioButton_Flag);
					context.sendBroadcast(intent);
					preferences = context.getSharedPreferences("serial_checked_result", Context.MODE_WORLD_READABLE);	//保存全局变量的信息
					preferences.edit()
									  .putInt("radioButton_Checked_Flag", serial_RadioButton_Flag)
									  .commit();
				}
			}
		});
		return view;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		context.unregisterReceiver(broadcastReceiver);
	}
	
	/**
	 * 读取SharedPreferences中的信息
	 */
	private void readSharePreConfig() {
		// TODO Auto-generated method stub
		serial_RadioButton_Flag = FragmentService.serialType;
		Log.v(TAG, "serial_RadioButton_Flag ="+serial_RadioButton_Flag);
//		preferences = context.getSharedPreferences("serial_checked_result", Context.MODE_WORLD_READABLE);
//		serial_RadioButton_Flag = preferences.getInt("radioButton_Checked_Flag", NON_CHECKED);	//默认全局变量为NON_CHECKED
	}
	
	/**
	 * 根据全局变量，选中保存的相应的RadioButton
	 */
	private void checkRadioButton() {
		// TODO Auto-generated method stub
		if(serial_RadioButton_Flag == OBD_CHECKED){
			radioButton_OBD.setChecked(true);
		}else if (serial_RadioButton_Flag == CAN_CHECKED) {
			radioButton_CAN.setChecked(true);
		}else if (serial_RadioButton_Flag == NON_CHECKED) {
			radioButton_NON.setChecked(true);
		}
	}
	
	private IntentFilter getIntentFilter(){		
		IntentFilter myIntentFilter = new IntentFilter("com.android.internal.car.can.action.SERIAL_TYPE_RESPONSE");	//获取从CanBusService传来的Serial值
		myIntentFilter.addCategory("com.android.internal.car.can.Serial");
		return myIntentFilter;
		
	};
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals("com.android.internal.car.can.action.SERIAL_TYPE_RESPONSE")){
				Serial_Flag = intent.getIntExtra("serial_type", NON);
				if(Serial_Flag == OBD){
					radioButton_OBD.setChecked(true);
				}else if (Serial_Flag == CAN) {
					radioButton_CAN.setChecked(true);
				}else if (Serial_Flag == NON) {
					radioButton_NON.setChecked(true);
				}
			}
		}
		
	};

}
