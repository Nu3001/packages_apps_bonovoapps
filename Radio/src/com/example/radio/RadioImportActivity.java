package com.example.radio;

import java.util.ArrayList;
import java.util.List;


import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.view.LayoutInflater;


public class RadioImportActivity extends Activity implements OnClickListener,
		OnItemClickListener, ServiceConnection {

	private final static String TAG = "RadioImportActivity";
	private static final boolean DEBUG = false;

	private static final int LIST_ITEM = 1;

	private ImageButton mCloseButton;
	private Button mAreaButton;
	private Button mProvinceButton;
	private Button mCityButton;
	private Button mPresetButton;
	private Button mDefineButton;
	private Button mImportButton;

	private ListView mListView;
	private RadioService mService;
	private RadioListAdapter mAdapter;

	private int mProvinceSel = -1;
	private int mCitySel = -1;

	static class ListItem {
		int id;
		String text;
	}

	private Handler mViewEventHandler = new Handler() {
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case R.id.import_button_close:
				if (DEBUG) Log.d(TAG, "import_button_close has been worked");
				finish();
				break;
			case R.id.import_button_area:
				if (DEBUG) Log.d(TAG, "import_button_area has been worked");
				mProvinceSel = -1;
				mCitySel = -1;
				showPlayList();
				mProvinceButton.setVisibility(View.INVISIBLE);
				mCityButton.setVisibility(View.INVISIBLE);
				mImportButton.setVisibility(View.INVISIBLE);
				break;
			case R.id.import_button_province:
				if (DEBUG) Log.d(TAG, "<myu>RadioImportActivity button province");
				mCitySel = -1;
				showPlayList();
				mCityButton.setVisibility(View.INVISIBLE); // �������Ϊinvisible���ɼ�
				mImportButton.setVisibility(View.INVISIBLE);
				break;
			case R.id.import_button_city:
					if (DEBUG) Log.d(TAG, "<myu>RadioImportActivity button city");
				break;
			case R.id.import_button_import:
				mService.importChannelList(mProvinceSel, mCitySel, true);
				mService.setFreq(mService.getCurrentFreq());
				if (DEBUG) Log.d(TAG, "RadioImportActivity finish()");
				finish();
				break;
			case LIST_ITEM:
				if (mProvinceSel < 0) {
					mProvinceSel = msg.arg1;
					mProvinceButton.setVisibility(View.VISIBLE);
					mProvinceButton.setText(mService.radioGetProvince().get(
							mProvinceSel));
				} else if (mCitySel < 0) {
					mCitySel = msg.arg1;
					mImportButton.setVisibility(View.VISIBLE);
					mCityButton.setVisibility(View.VISIBLE);
					mCityButton.setText(mService.radioGetCity(mProvinceSel)
							.get(mCitySel));
				}
				showPlayList();
				break;
			}
		}

	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.radio_list_layout);
		setupViews();

		// bind a service with RadioService
		Intent serviceIntent = new Intent("com.example.radio.RadioService");
		this.bindService(serviceIntent, this, BIND_AUTO_CREATE);
	}

	private void setupViews() {
		mCloseButton = (ImageButton) findViewById(R.id.import_button_close);
		mCloseButton.setOnClickListener(this);
		mAreaButton = (Button) findViewById(R.id.import_button_area);
		mAreaButton.setOnClickListener(this);
		mProvinceButton = (Button) findViewById(R.id.import_button_province);
		mProvinceButton.setOnClickListener(this);
		mCityButton = (Button) findViewById(R.id.import_button_city);
		mCityButton.setOnClickListener(this);
		mImportButton = (Button) findViewById(R.id.import_button_import);
		mImportButton.setOnClickListener(this);
//		mPresetButton = (Button) findViewById(R.id.import_button_preset);
//		mPresetButton.setOnClickListener(this);
//		mDefineButton = (Button) findViewById(R.id.import_button_define);
//		mDefineButton.setOnClickListener(this);

		mAdapter = new RadioListAdapter(this);
		mListView = (ListView) findViewById(R.id.import_list_view);
		mListView.setOnItemClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (mViewEventHandler != null) {
			mViewEventHandler.sendEmptyMessage(id);
		}

	}

	private LayoutInflater mInflater;

	// ��ʾ�����б�
	private void showPlayList() {

		ArrayList<ListItem> viewList = null;
		List<String> importList = null;

		if (mService != null) {
			viewList = new ArrayList<ListItem>();

			// city list of the province,province list of the area
			if (mCitySel >= 0) {
				importList = mService.radioGetChannel(mProvinceSel, mCitySel);
			} else if (mProvinceSel >= 0) {
				importList = mService.radioGetCity(mProvinceSel);
			} else {
				importList = mService.radioGetProvince();
			}
			for (String title : importList) {
				ListItem item = new ListItem();
				if (mCitySel >= 0) {
					item.id = R.drawable.import_channel;
				} else if (mProvinceSel >= 0) {
					item.id = R.drawable.import_city_icon;
				} else {
					item.id = R.drawable.import_province_icon;
				}
				item.text = title;
				viewList.add(item);
			}
		}
		refreshList(viewList); // ˢ���б�

	}

	private void refreshList(List<ListItem> list) {
		mAdapter.setList(list);
		mListView.setAdapter(mAdapter);
		mListView.invalidate();
	}

	private BroadcastReceiver myReceiver = new BroadcastReceiver() { /*
																	 * ����
																	 * BroadcastReceiver���myReceiver
																	 * ��������㲥��Ϣ
																	 * ���ֵ�ʱ��ᱻAndroid��������
																	 */
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Message msg = mViewEventHandler
					.obtainMessage(R.id.import_button_close);
			if (msg != null) {
				mViewEventHandler.sendMessage(msg);
			}
		}
	};

	private IntentFilter getIntentFilter() { /* ע�� BroadcastReceiver */

		IntentFilter myIntentFilter = new IntentFilter(RadioService.MSG_CLOSE);
		return myIntentFilter;
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		 if (mViewEventHandler != null) {
	            Message msg = mViewEventHandler.obtainMessage(LIST_ITEM);
	            if (msg != null) {
	                msg.arg1 = position;
	                msg.obj = view;
	                mViewEventHandler.sendMessage(msg);
	            }
	        }
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		long lstart1, lstart2 = 0, lstart3;
		mService = ((RadioService.ServiceBinder) service).getService();
		lstart1 = System.currentTimeMillis();
		if (mService.radioReadXML()) {
			lstart2 = System.currentTimeMillis(); // ָ��Ҫ������ʾ
			showPlayList();
		} else {
			if (DEBUG) Log.v(TAG, "radioReadXML  is null");
		}
		this.registerReceiver(myReceiver, getIntentFilter());
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		mService = null;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mListView.setAdapter(null);
		mService = null;
		this.unregisterReceiver(myReceiver);
		this.unbindService(this);
	}

}