package com.bonovo.mcuupdate_and_setting;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

public class RightFragmentStandby extends Fragment {

	private Activity context;
	private CallBackStandby backStandby;
	private SharedPreferences preferences;
	
	private int checkFlag;
	private Boolean switchCheckFlag;
	private int NO_STANDBY = 0;
	private int HALF_HOUR = 1;
	private int ONE_HOUR = 2;
	private int TWO_HOUR = 3;
	private int HALF_DAY = 4;
	private int ONE_DAY = 5;
	private int TWO_DAY = 6;
	private int INFINITE_STANDBY = 7;
	
	private final int HALF_HOUR_TIME= 30;
	private final int ONE_HOUR_TIME= 60;
	private final int TWO_HOUR_TIME= 120;
	private final int HALF_DAY_TIME= 720;
	private final int ONE_DAY_TIME= 1440;
	private final int TWO_DAY_TIME= 2880;
	private final int NO_STANDBY_TIME= 0;
	private final int OPEN_STANDBY= 65535;
	
	private RadioGroup radioGroup;
	private RadioButton halfHourBtn;
	private RadioButton oneHourBtn;
	private RadioButton twoHourBtn;
	private RadioButton halfDayBtn;
	private RadioButton oneDayBtn;
	private RadioButton twoDayBtn;
	private RadioButton noStandbyBtn;
	private RadioButton infiniteStandbyBtn;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		context = activity;
		try {
			backStandby = (CallBackStandby)activity;
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.right_standby, null);
		radioGroup = (RadioGroup)view.findViewById(R.id.standbyGroup);
		halfHourBtn = (RadioButton)view.findViewById(R.id.half_hour);
		oneHourBtn = (RadioButton)view.findViewById(R.id.one_hour);
		twoHourBtn = (RadioButton)view.findViewById(R.id.two_hour);
		halfDayBtn = (RadioButton)view.findViewById(R.id.half_day);
		oneDayBtn = (RadioButton)view.findViewById(R.id.one_day);
		twoDayBtn = (RadioButton)view.findViewById(R.id.two_day);
		noStandbyBtn = (RadioButton)view.findViewById(R.id.no_hour);
		infiniteStandbyBtn = (RadioButton)view.findViewById(R.id.infinite);
		
		readSharePreConfig();
		checkRadioButton();
		
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId == halfHourBtn.getId()){
					checkFlag = HALF_HOUR;
					preferences = context.getSharedPreferences("standby model", Context.MODE_WORLD_READABLE);
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
					backStandby.setStandby(HALF_HOUR_TIME);
				}else if(checkedId == oneHourBtn.getId()){
					checkFlag = ONE_HOUR;
					preferences = context.getSharedPreferences("standby model", Context.MODE_WORLD_READABLE);
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
					backStandby.setStandby(ONE_HOUR_TIME);
				}else if (checkedId == twoHourBtn.getId()) {
					checkFlag = TWO_HOUR;
					preferences = context.getSharedPreferences("standby model", Context.MODE_WORLD_READABLE);
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
					backStandby.setStandby(TWO_HOUR_TIME);
				}else if (checkedId == halfDayBtn.getId()) {
					checkFlag = HALF_DAY;
					preferences = context.getSharedPreferences("standby model", Context.MODE_WORLD_READABLE);
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
					backStandby.setStandby(HALF_DAY_TIME);
				}else if (checkedId == oneDayBtn.getId()) {
					checkFlag = ONE_DAY;
					preferences = context.getSharedPreferences("standby model", Context.MODE_WORLD_READABLE);
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
					backStandby.setStandby(ONE_DAY_TIME);
				}else if (checkedId == noStandbyBtn.getId()) {
					checkFlag = NO_STANDBY;
					preferences = context.getSharedPreferences("standby model", Context.MODE_WORLD_READABLE);
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
					backStandby.setStandby(NO_STANDBY_TIME);
				}else if (checkedId == twoDayBtn.getId()) {
					checkFlag = TWO_DAY;
					preferences = context.getSharedPreferences("standby model", Context.MODE_WORLD_READABLE);
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
					backStandby.setStandby(TWO_DAY_TIME);
				}else if (checkedId == infiniteStandbyBtn.getId()) {
					checkFlag = INFINITE_STANDBY;
					preferences = context.getSharedPreferences("standby model", Context.MODE_WORLD_READABLE);
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
					backStandby.setStandby(OPEN_STANDBY);
				}
			}
		});
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	/**
	 * 读取SharedPreferences中的信息
	 */
	private void readSharePreConfig() {
		// TODO Auto-generated method stub
		preferences = context.getSharedPreferences("standby model", Context.MODE_WORLD_READABLE);
		checkFlag = preferences.getInt("standby checked", NO_STANDBY);	//默认全局变量为PRESSHOST
		switchCheckFlag = preferences.getBoolean("switch_checked", false);
	}
	
	/**
	 * 根据全局变量，选中保存的相应的RadioButton
	 */
	private void checkRadioButton() {
		// TODO Auto-generated method stub
		if(checkFlag == NO_STANDBY){
			noStandbyBtn.setChecked(true);
		}else if (checkFlag == HALF_HOUR) {
			halfHourBtn.setChecked(true);
		}else if (checkFlag == ONE_HOUR) {
			oneHourBtn.setChecked(true);
		}else if (checkFlag == TWO_HOUR){
			twoHourBtn.setChecked(true);
		}else if (checkFlag == HALF_DAY){
			halfDayBtn.setChecked(true);
		}else if(checkFlag == ONE_DAY){
			oneDayBtn.setChecked(true);
		}else if (checkFlag == TWO_DAY) {
			twoDayBtn.setChecked(true);
		}
		
	}
	
	public interface CallBackStandby{
		public void setStandby(int time);
	}
}
