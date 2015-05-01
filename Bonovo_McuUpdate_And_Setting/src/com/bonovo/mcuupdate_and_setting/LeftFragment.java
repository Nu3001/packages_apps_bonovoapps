package com.bonovo.mcuupdate_and_setting;

import java.util.ArrayList;
import java.util.List;



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

public class LeftFragment extends ListFragment {
	
	private ArrayAdapter<String> adArrayAdapter;
	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		adArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,getData());
		fragmentManager = getFragmentManager();
	}
	
	public List<String> getData(){
		List<String> list = new ArrayList<String>();
		list.add(getResources().getString(R.string.list_version));
		list.add(getResources().getString(R.string.list_update));
		list.add(getResources().getString(R.string.list_config));
		//list.add("滚动框架");
		list.add(getResources().getString(R.string.list_serial_config));
		list.add(getResources().getString(R.string.list_car_config));
		list.add(getResources().getString(R.string.list_keysbacklight));
		list.add(getResources().getString(R.string.list_otg));
		list.add(getResources().getString(R.string.list_btupdate));
		list.add(getResources().getString(R.string.list_standby));
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
		case 0:
			RigthFragmentVersion rigthFragmentVersion = new RigthFragmentVersion();
			transaction.replace(R.id.rigth, rigthFragmentVersion,"rigthFragmentVersion");
			//transaction.addToBackStack(null);		//把事务添加到一个后退栈中,同时在后退栈中保存被代替的fragment的状态
			break;
		case 1:
			McuFragment mcuFragment = new McuFragment();
			transaction.replace(R.id.rigth, mcuFragment, "mchFragment");
			//transaction.addToBackStack(null);
			break;
		case 2:
			RightFragmentSetting rightFragmentSetting = new RightFragmentSetting();
			transaction.replace(R.id.rigth, rightFragmentSetting, "rightFragmentSetting");
			//transaction.addToBackStack(null);
			break;
		case 3:
			RightFragmentSerialConfig rightFragmentSerialConfig = new RightFragmentSerialConfig();
			transaction.replace(R.id.rigth, rightFragmentSerialConfig,"rightFragmentSerialConfig");
			break;
		case 4:
			RightFragmentCarConfig rightFragmentCarConfig = new RightFragmentCarConfig();
			transaction.replace(R.id.rigth, rightFragmentCarConfig, "rightFragmentCarConfig");
			break;
		case 5:
			RightFragmentKeysBackLight rightFragmentKeysBackLight = new RightFragmentKeysBackLight();
			transaction.replace(R.id.rigth, rightFragmentKeysBackLight, "rightFragmentCarConfig");
			break;
		case 6:
			RightFragmentOTGModel rightFragmentOTGModel = new RightFragmentOTGModel();
			transaction.replace(R.id.rigth, rightFragmentOTGModel, "rightFragmentOTGModel");
			break;
		case 7:
			BTUpdateFragment fragmentBTUpdate = new BTUpdateFragment();
			transaction.replace(R.id.rigth, fragmentBTUpdate, "fragmentBTUpdate");
			break;
		case 8:
			RightFragmentStandby fragmentStandby = new RightFragmentStandby();
			transaction.replace(R.id.rigth, fragmentStandby, "fragmentStandby");
			break;
		case 10:
			RightFragmentScrollView fragmentScrollView = new RightFragmentScrollView();
			transaction.replace(R.id.rigth, fragmentScrollView, "fragmentScrollView");
			//transaction.addToBackStack(null);
			break;
		default:
			break;
		}
//		String itemString = adArrayAdapter.getItem(position);
//		//����ߵ�Fragment �е�� �滻�ұߵ�Fragment
//		RigthFragment rigthFragment = new RigthFragment();
//		transaction = fragmentManager.beginTransaction();
//		transaction.replace(R.id.rigth, rigthFragment,"rigthFragment");
////		transaction.addToBackStack("rigthFragment");
//		Bundle bundle = new Bundle();
//		bundle.putString("item", itemString);
//		rigthFragment.setArguments(bundle);
		transaction.commit();
		
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

}
