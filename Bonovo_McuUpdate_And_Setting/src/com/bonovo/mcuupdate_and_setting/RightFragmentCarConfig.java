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

public class RightFragmentCarConfig extends Fragment {
	private final String TAG = "com.example.fragment.right_car_config";
	
	private Activity context;
	private RadioGroup radioGroup;
	private RadioButton radioButton_Volkswagen;
	private RadioButton radioButton_Sonata8;
	private RadioButton radioButton_Other;
	
	private SharedPreferences preferences;
	private static int Car_RadioButton_Flag;		//保存RadioButton信息的全局变量
	private static int Car_Flag = 0;					//全局变量:保存从CanBusService传过来的车型值 -->大众：0 ,索纳塔8代:1 其他:3
	private final int Volkswagen = 0;		//大众
	private final int Sonata8 = 1;		//索纳塔8代
	private final int OTHER= 2;		//索纳塔8代
	private final int Volkswagen_CHECKED = 1;		//Volkswagen RadioButton被按下
	private final int Sonata8_CHECKED = 2;			//Sonata8 RadioButton被按下
	private final int OTHER_CHECKED = 3;			//Other RadioButton被按下
	

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
		 View view = inflater.inflate(R.layout.right_car_configui, null);
		 radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup2);
		 radioButton_Volkswagen = (RadioButton)view.findViewById(R.id.radioVolkswagen);
		 radioButton_Sonata8 = (RadioButton)view.findViewById(R.id.radioSonata8);
		 radioButton_Other = (RadioButton)view.findViewById(R.id.radioOther);
		 readSharePreConfig();
		 checkRadioButton();
//		 Intent sendIntent = new Intent("com.android.internal.car.can.action.CAR_TYPE_REQUEST");
//		 context.sendBroadcast(sendIntent);
//		 readCarFlag();
		 
		 radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId == radioButton_Volkswagen.getId()){
					Car_RadioButton_Flag = Volkswagen;		//将全局变量赋为Volkswagen
					FragmentService.carType = Volkswagen;
					preferences = context.getSharedPreferences("car_checked_result", Context.MODE_WORLD_READABLE);	//保存全局变量的信息
					preferences.edit()
									  .putInt("radioButton_Checked_Flag", Car_RadioButton_Flag)
									  .commit();
					//把改变的值发送给CanBusService
					Intent intent = new Intent("com.android.internal.car.can.action.CAR_TYPE_CHANGED");
					intent.putExtra("car_type", Car_RadioButton_Flag);
					context.sendBroadcast(intent);
				}else if (checkedId == radioButton_Sonata8.getId()) {
					Car_RadioButton_Flag = Sonata8;			//将全局变量赋为Sonata8
					FragmentService.carType = Sonata8;
					preferences = context.getApplicationContext().getSharedPreferences("car_checked_result", Context.MODE_WORLD_READABLE);	//保存全局变量的信息
					preferences.edit()
									  .putInt("radioButton_Checked_Flag", Car_RadioButton_Flag)
									  .commit();
					Intent intent = new Intent("com.android.internal.car.can.action.CAR_TYPE_CHANGED");
					intent.putExtra("car_type", Car_RadioButton_Flag);
					context.sendBroadcast(intent);
				}else if (checkedId == radioButton_Other.getId()) {
					Car_RadioButton_Flag = OTHER;	
					FragmentService.carType = OTHER;
					preferences = context.getApplicationContext().getSharedPreferences("car_checked_result", Context.MODE_WORLD_READABLE);	//保存全局变量的信息
					preferences.edit()
									  .putInt("radioButton_Checked_Flag", Car_RadioButton_Flag)
									  .commit();
					Intent intent = new Intent("com.android.internal.car.can.action.CAR_TYPE_CHANGED");
					intent.putExtra("car_type", Car_RadioButton_Flag);
					context.sendBroadcast(intent);
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
		Car_RadioButton_Flag = FragmentService.carType;
		Log.v(TAG, "serial_RadioButton_Flag ="+Car_RadioButton_Flag);
//		preferences = context.getSharedPreferences("car_checked_result", Context.MODE_WORLD_READABLE);
//		Car_RadioButton_Flag = preferences.getInt("radioButton_Checked_Flag", Volkswagen);	//默认全局变量为Volkswagen
	}
	
	/**
	 * 根据全局变量，选中保存的相应的RadioButton
	 */
	private void checkRadioButton() {
		// TODO Auto-generated method stub
		if(Car_RadioButton_Flag == Volkswagen){
			radioButton_Volkswagen.setChecked(true);
		}else if (Car_RadioButton_Flag == Sonata8) {
			radioButton_Sonata8.setChecked(true);
		}else if (Car_RadioButton_Flag == OTHER) {
			radioButton_Other.setChecked(true);
		}
	}
	
//	/**
//	 * 读取全局变量Car_Flag,根据Car_Flag改变RadioButton的选中状态
//	 */
//	private void readCarFlag() {
//		// TODO Auto-generated method stub
//		if(Car_Flag == Volkswagen){
//			 Log.v(TAG, "3333333");
//			radioButton_Volkswagen.setChecked(true);
//		}else if (Car_Flag == Sonata8) {
//			Log.v(TAG, "44444444");
//			radioButton_Sonata8.setChecked(true);
//		}
//	}
	
	private IntentFilter getIntentFilter(){		
		IntentFilter myIntentFilter = new IntentFilter("com.android.internal.car.can.action.CAR_TYPE_RESPONSE");	//获取从CanBusService传来的车型值
		myIntentFilter.addCategory("com.android.internal.car.can.Car");
		return myIntentFilter;
		
	};
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(
					"com.android.internal.car.can.action.CAR_TYPE_RESPONSE")) {
				Car_Flag = intent.getIntExtra("car_type", OTHER);
				if (Car_Flag == Volkswagen) {
					radioButton_Volkswagen.setChecked(true);
				} else if (Car_Flag == Sonata8) {
					radioButton_Sonata8.setChecked(true);
				} else if (Car_Flag == OTHER) {
					radioButton_Other.setChecked(true);
				}
			}
		}
	};
}
