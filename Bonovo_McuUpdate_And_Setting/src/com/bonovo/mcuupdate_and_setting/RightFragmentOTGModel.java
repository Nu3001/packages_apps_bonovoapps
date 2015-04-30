package com.bonovo.mcuupdate_and_setting;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class RightFragmentOTGModel extends Fragment {
	private Activity context;
	private RadioGroup radioGroup;
	private RadioButton hostBtn;
	private RadioButton slaveBtn;
	
	private SharedPreferences preferences;
	private int otgFlag;	//save the otg state flag
	private int PRESSHOST = 1;
	private int PRESSSLAVE = 2;
	
	private CallBackOTG backOTG;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		context = activity;
		try {
			backOTG = (CallBackOTG)activity;
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.right_switch_otg, null);
		radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup3);
		hostBtn = (RadioButton)view.findViewById(R.id.radioHost);
		slaveBtn = (RadioButton)view.findViewById(R.id.radioSlave);
		
		readSharePreConfig();
		checkRadioButton();
		
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId == hostBtn.getId()){
					otgFlag = PRESSHOST;
					preferences = context.getSharedPreferences("otg model", Context.MODE_WORLD_READABLE);
					preferences.edit()
							   .putInt("otg checked", otgFlag)
							   .commit();
					backOTG.switchOTG(otgFlag);
				}else if(checkedId == slaveBtn.getId()){
					otgFlag = PRESSSLAVE;
					preferences = context.getSharedPreferences("otg model", Context.MODE_WORLD_READABLE);
					preferences.edit()
							   .putInt("otg checked", otgFlag)
							   .commit();
					backOTG.switchOTG(otgFlag);
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
		preferences = context.getSharedPreferences("otg model", Context.MODE_WORLD_READABLE);
		otgFlag = preferences.getInt("otg checked", PRESSHOST);	//默认全局变量为PRESSHOST
	}
	
	/**
	 * 根据全局变量，选中保存的相应的RadioButton
	 */
	private void checkRadioButton() {
		// TODO Auto-generated method stub
		if(otgFlag == PRESSHOST){
			hostBtn.setChecked(true);
		}else if (otgFlag == PRESSSLAVE) {
			slaveBtn.setChecked(true);
		}
	}
	
	public interface CallBackOTG{
		public void switchOTG(int model);
	}

}
