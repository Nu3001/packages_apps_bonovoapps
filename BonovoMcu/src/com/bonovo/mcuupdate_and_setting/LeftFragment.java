package com.bonovo.mcuupdate_and_setting;

import android.app.ListFragment;
import android.widget.ArrayAdapter;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import java.util.List;
import java.util.ArrayList;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.util.Log;
import android.app.Fragment;

public class LeftFragment extends ListFragment {
    private ArrayAdapter<String> adArrayAdapter;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adArrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, getData());
        fragmentManager = getFragmentManager();
    }
    
    public List getData() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(getResources().getString(R.string.list_version));
        list.add(getResources().getString(R.string.list_update));
        list.add(getResources().getString(R.string.list_config));
        list.add(getResources().getString(R.string.list_serial_config));
        list.add(getResources().getString(R.string.list_car_config));
        list.add(getResources().getString(R.string.list_keysbacklight));
        list.add(getResources().getString(R.string.list_otg));
        list.add(getResources().getString(R.string.list_btupdate));
		// NOTE: Standby is available in the switch, but not in this list? Is it not fully functional yet?
        return list;
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.left, null);
        setListAdapter(adArrayAdapter);
        adArrayAdapter.notifyDataSetChanged();
        return view;
    }
    
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.v("leftfragment", "position is " + position);
        transaction = fragmentManager.beginTransaction();
        switch(position) {
            case 0:
            {
                RigthFragmentVersion rigthFragmentVersion = new RigthFragmentVersion();
                transaction.replace(R.id.rigth, rigthFragmentVersion, "rigthFragmentVersion");
                break;
            }
            case 1:
            {
                McuFragment mcuFragment = new McuFragment();
                transaction.replace(R.id.rigth, mcuFragment, "mchFragment");
                break;
            }
            case 2:
            {
                RightFragmentSetting rightFragmentSetting = new RightFragmentSetting();
                transaction.replace(R.id.rigth, rightFragmentSetting, "rightFragmentSetting");
                break;
            }
            case 3:
            {
                RightFragmentSerialConfig rightFragmentSerialConfig = new RightFragmentSerialConfig();
                transaction.replace(R.id.rigth, rightFragmentSerialConfig, "rightFragmentSerialConfig");
                break;
            }
            case 4:
            {
                RightFragmentCarConfig rightFragmentCarConfig = new RightFragmentCarConfig();
                transaction.replace(R.id.rigth, rightFragmentCarConfig, "rightFragmentCarConfig");
                break;
            }
            case 5:
            {
                RightFragmentKeysBackLight rightFragmentKeysBackLight = new RightFragmentKeysBackLight();
                transaction.replace(R.id.rigth, rightFragmentKeysBackLight, "rightFragmentCarConfig");
                break;
            }
            case 6:
            {
                RightFragmentOTGModel rightFragmentOTGModel = new RightFragmentOTGModel();
                transaction.replace(R.id.rigth, rightFragmentOTGModel, "rightFragmentOTGModel");
                break;
            }
            case 7:
            {
                BTUpdateFragment fragmentBTUpdate = new BTUpdateFragment();
                transaction.replace(R.id.rigth, fragmentBTUpdate, "fragmentBTUpdate");
                break;
            }
            case 8:
            {
                RightFragmentStandby fragmentStandby = new RightFragmentStandby();
                transaction.replace(R.id.rigth, fragmentStandby, "fragmentStandby");
                break;
            }
            case 10:
            {
                RightFragmentScrollView fragmentScrollView = new RightFragmentScrollView();
                transaction.replace(R.id.rigth, fragmentScrollView, "fragmentScrollView");
                break;
            }
            case 9:
			default:
            {
                break;
            }
        }
        transaction.commit();
    }
    
    public void onPause() {
        super.onPause();
    }
}
