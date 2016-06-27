package com.bonovo.mcuupdate_and_setting;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class LeftFragment extends ListFragment {

	private ArrayAdapter<String> adArrayAdapter;
	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;

	private static final int POS_VERSION = 0;
	private static final int POS_CAPABILITIES = 1;
	private static final int POS_MCU_UPDATE = 2;
	private static final int POS_CAR_CONFIG = 3;
	private static final int POS_SERIAL_CONFIG = 4;
	private static final int POS_CAR_TYPE = 5;
	private static final int POS_KEYS_BACKLIGHT = 6;
	private static final int POS_OTG = 7;
	private static final int POS_BT_UPDATE = 8;
	private static final int POS_STANDBY = 9;

	@SuppressWarnings("unused")
	private static final int POS_UNUSED_SCROLLVIEW = 11;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		adArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getData());
		fragmentManager = getFragmentManager();
	}

	public List<String> getData(){
		List<String> list = new ArrayList<String>();
		list.add(POS_VERSION, getResources().getString(R.string.list_version));
		list.add(POS_CAPABILITIES, getResources().getString(R.string.list_capabilities));
		list.add(POS_MCU_UPDATE, getResources().getString(R.string.list_update));
		list.add(POS_CAR_CONFIG, getResources().getString(R.string.list_config));
		list.add(POS_SERIAL_CONFIG, getResources().getString(R.string.list_serial_config));
		list.add(POS_CAR_TYPE, getResources().getString(R.string.list_car_config));
		list.add(POS_KEYS_BACKLIGHT, getResources().getString(R.string.list_keysbacklight));
		list.add(POS_OTG, getResources().getString(R.string.list_otg));
		list.add(POS_BT_UPDATE, getResources().getString(R.string.list_btupdate));
		list.add(POS_STANDBY, getResources().getString(R.string.list_standby));
		// list.add(POS_UNUSED_SCROLLVIEW, getResources().getString(R.string.list_scrollview));
		//list.add("滚动框架"); // scroll framework
		return list;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.left, null);
		setListAdapter(adArrayAdapter);
		adArrayAdapter.notifyDataSetChanged();
		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Log.v("leftfragment","position is "+position);
		transaction = fragmentManager.beginTransaction();

		switch (position) {
		case POS_VERSION:
			RigthFragmentVersion rigthFragmentVersion = new RigthFragmentVersion();
			transaction.replace(R.id.rigth, rigthFragmentVersion,"rigthFragmentVersion");
			//transaction.addToBackStack(null);		//把事务添加到一个后退栈中,同时在后退栈中保存被代替的fragment的状态
			break;
		case POS_CAPABILITIES:
			RightFragmentCapabilities capsFragment = new RightFragmentCapabilities();
			transaction.replace(R.id.rigth, capsFragment, "rightCapabilities");
			break;
		case POS_MCU_UPDATE:
			McuFragment mcuFragment = new McuFragment();
			transaction.replace(R.id.rigth, mcuFragment, "mchFragment");
			//transaction.addToBackStack(null);
			break;
		case POS_CAR_CONFIG:
			RightFragmentSetting rightFragmentSetting = new RightFragmentSetting();
			transaction.replace(R.id.rigth, rightFragmentSetting, "rightFragmentSetting");
			//transaction.addToBackStack(null);
			break;
		case POS_SERIAL_CONFIG:
			RightFragmentSerialConfig rightFragmentSerialConfig = new RightFragmentSerialConfig();
			transaction.replace(R.id.rigth, rightFragmentSerialConfig,"rightFragmentSerialConfig");
			break;
		case POS_CAR_TYPE:
			RightFragmentCarConfig rightFragmentCarConfig = new RightFragmentCarConfig();
			transaction.replace(R.id.rigth, rightFragmentCarConfig, "rightFragmentCarConfig");
			break;
		case POS_KEYS_BACKLIGHT:
			RightFragmentKeysBackLight rightFragmentKeysBackLight = new RightFragmentKeysBackLight();
			transaction.replace(R.id.rigth, rightFragmentKeysBackLight, "rightFragmentCarConfig");
			break;
		case POS_OTG:
			RightFragmentOTGModel rightFragmentOTGModel = new RightFragmentOTGModel();
			transaction.replace(R.id.rigth, rightFragmentOTGModel, "rightFragmentOTGModel");
			break;
		case POS_BT_UPDATE:
			BTUpdateFragment fragmentBTUpdate = new BTUpdateFragment();
			transaction.replace(R.id.rigth, fragmentBTUpdate, "fragmentBTUpdate");
			break;
		case POS_STANDBY:
			RightFragmentStandby fragmentStandby = new RightFragmentStandby();
			transaction.replace(R.id.rigth, fragmentStandby, "fragmentStandby");
			break;
		default:
            Log.w("leftfragment", "Unexpected item click: " + position);
            return;
		}
		transaction.commit();
	}
}
