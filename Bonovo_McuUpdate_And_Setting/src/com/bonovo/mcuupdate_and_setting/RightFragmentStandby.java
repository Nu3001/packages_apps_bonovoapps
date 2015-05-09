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
	
	private final int HALF_HOUR_TIME= 30;
	private final int ONE_HOUR_TIME= 60;
	private final int TWO_HOUR_TIME= 120;
	private final int HALF_DAY_TIME= 720;
	private final int ONE_DAY_TIME= 1440;
	private final int TWO_DAY_TIME= 2880;
	private final int NO_STANDBY_TIME= 0;
	private final int INFINITE_STANDBY= 65535;
	
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
				preferences = context.getSharedPreferences("standby model", Context.MODE_WORLD_READABLE);
				if(checkedId == halfHourBtn.getId()){
					checkFlag = HALF_HOUR_TIME;
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
				}else if(checkedId == oneHourBtn.getId()){
					checkFlag = ONE_HOUR_TIME;
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
				}else if (checkedId == twoHourBtn.getId()) {
					checkFlag = TWO_HOUR_TIME;
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
				}else if (checkedId == halfDayBtn.getId()) {
					checkFlag = HALF_DAY_TIME;
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
				}else if (checkedId == oneDayBtn.getId()) {
					checkFlag = ONE_DAY_TIME;
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
				}else if (checkedId == noStandbyBtn.getId()) {
					checkFlag = NO_STANDBY_TIME;
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
				}else if (checkedId == twoDayBtn.getId()) {
					checkFlag = TWO_DAY_TIME;
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
				}else if (checkedId == infiniteStandbyBtn.getId()) {
					checkFlag = INFINITE_STANDBY;
					preferences.edit()
							   .putInt("standby checked", checkFlag)
							   .commit();
				}
				backStandby.setStandby(checkFlag);

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
		checkFlag = preferences.getInt("standby checked", TWO_HOUR_TIME);	//默认全局变量为PRESSHOST
		switchCheckFlag = preferences.getBoolean("switch_checked", false);
	}
	
	/**
	 * 根据全局变量，选中保存的相应的RadioButton
	 */
	private void checkRadioButton() {
		// TODO Auto-generated method stub
		if(checkFlag == NO_STANDBY_TIME){
			noStandbyBtn.setChecked(true);
		}else if (checkFlag == HALF_HOUR_TIME) {
			halfHourBtn.setChecked(true);
		}else if (checkFlag == ONE_HOUR_TIME) {
			oneHourBtn.setChecked(true);
		}else if (checkFlag == TWO_HOUR_TIME){
			twoHourBtn.setChecked(true);
		}else if (checkFlag == HALF_DAY_TIME){
			halfDayBtn.setChecked(true);
		}else if(checkFlag == ONE_DAY_TIME){
			oneDayBtn.setChecked(true);
		}else if (checkFlag == TWO_DAY_TIME) {
			twoDayBtn.setChecked(true);
		}else if (checkFlag == INFINITE_STANDBY) {
			infiniteStandbyBtn.setChecked(true);
		}
		
	}
	
	public interface CallBackStandby{
		public void setStandby(int time);
	}
}
