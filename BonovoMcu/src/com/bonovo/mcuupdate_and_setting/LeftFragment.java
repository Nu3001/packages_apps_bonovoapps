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
    private ArrayAdapter<String> adArrayAdapter<String>;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adArrayAdapter = new ArrayAdapter(getActivity(), 0x1090003, getData());
        fragmentManager = getFragmentManager();
    }
    
    public List getData() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(getResources().getString(0x7f050026));
        list.add(getResources().getString(0x7f050027));
        list.add(getResources().getString(0x7f050028));
        list.add(getResources().getString(0x7f050029));
        list.add(getResources().getString(0x7f05002a));
        list.add(getResources().getString(0x7f05002c));
        list.add(getResources().getString(0x7f050035));
        list.add(getResources().getString(0x7f05002b));
        return list;
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(0x7f030002, null);
        setListAdapter(adArrayAdapter);
        adArrayAdapter.notifyDataSetChanged();
        return view;
    }
    
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id, id);
        Log.v("leftfragment", "position is " + position);
        transaction = fragmentManager.beginTransaction();
        switch(position) {
            case 0:
            {
                RigthFragmentVersion rigthFragmentVersion = new RigthFragmentVersion();
                transaction.replace(0x7f080001, rigthFragmentVersion, "rigthFragmentVersion");
                break;
            }
            case 1:
            {
                McuFragment mcuFragment = new McuFragment();
                transaction.replace(0x7f080001, mcuFragment, "mchFragment");
                break;
            }
            case 2:
            {
                RightFragmentSetting rightFragmentSetting = new RightFragmentSetting();
                transaction.replace(0x7f080001, rightFragmentSetting, "rightFragmentSetting");
                break;
            }
            case 3:
            {
                RightFragmentSerialConfig rightFragmentSerialConfig = new RightFragmentSerialConfig();
                transaction.replace(0x7f080001, rightFragmentSerialConfig, "rightFragmentSerialConfig");
                break;
            }
            case 4:
            {
                RightFragmentCarConfig rightFragmentCarConfig = new RightFragmentCarConfig();
                transaction.replace(0x7f080001, rightFragmentCarConfig, "rightFragmentCarConfig");
                break;
            }
            case 5:
            {
                RightFragmentKeysBackLight rightFragmentKeysBackLight = new RightFragmentKeysBackLight();
                transaction.replace(0x7f080001, rightFragmentKeysBackLight, "rightFragmentCarConfig");
                break;
            }
            case 6:
            {
                RightFragmentOTGModel rightFragmentOTGModel = new RightFragmentOTGModel();
                transaction.replace(0x7f080001, rightFragmentOTGModel, "rightFragmentOTGModel");
                break;
            }
            case 7:
            {
                BTUpdateFragment fragmentBTUpdate = new BTUpdateFragment();
                transaction.replace(0x7f080001, fragmentBTUpdate, "fragmentBTUpdate");
                break;
            }
            case 8:
            {
                RightFragmentStandby fragmentStandby = new RightFragmentStandby();
                transaction.replace(0x7f080001, fragmentStandby, "fragmentStandby");
                break;
            }
            case 10:
            {
                RightFragmentScrollView fragmentScrollView = new RightFragmentScrollView();
                transaction.replace(0x7f080001, fragmentScrollView, "fragmentScrollView");
                break;
            }
            case 9:
            {
                transaction.commit();
                break;
            }
        }
    }
    
    public void onPause() {
        super.onPause();
    }
}
