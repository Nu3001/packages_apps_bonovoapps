package com.bonovo.mcuupdate_and_setting;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class McuFragment extends Fragment {
	private final static String TAG = "McuFragment";

	
	private Button upBtn;
	
	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;
	private CallbackMcu callbackMcu;
	private ProgressDialog progressDialog;
	
	private static final int WIPE_SUCCEED = 2;					//擦除成功
	private final int WHAT_UPDATE_OK = 1;
	private final int WHAT_VERSION_SAME = 2;
	private final int WHAT_UPDATE_FAIL = 3;
	private final int WHAT_WIPE_FAIL = 4;
	private final int WHAT_PROGRESS = 5;
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case WHAT_UPDATE_OK:
				progressDialog.dismiss();
				final ProgressDialog dialog = ProgressDialog.show(
						getActivity(),
						getResources().getString(R.string.update_ok),
						getResources().getString(R.string.update_ok_msg));
				/******* 修改Dialog内文字大小 *******/
				View v = dialog.getWindow().getDecorView();// 取到dialog的整个view
				setDialogText(v);

				dialog.show();
				break;
			case WHAT_VERSION_SAME:
				Toast.makeText(getActivity(), R.string.version_same,
						Toast.LENGTH_SHORT).show();
				break;
			case WHAT_UPDATE_FAIL:
				Toast.makeText(getActivity(), R.string.update_fail,
						Toast.LENGTH_LONG).show();
				break;
			case WHAT_WIPE_FAIL:
				Toast.makeText(getActivity(), R.string.wipe_fail,
						Toast.LENGTH_LONG).show();
				break;
			case WHAT_PROGRESS:
				progressDialog = new ProgressDialog(getActivity());
				progressDialog.setMax(callbackMcu.getLoopNum());
				progressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setTitle(R.string.progress);
				progressDialog.setCanceledOnTouchOutside(false);
				progressDialog.setCancelable(false);
				progressDialog.show();
				break;
			default:
				break;
			}

		}
	};
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		activity.registerReceiver(mBroadcastReveiver, getIntentFilter());
		try{
			callbackMcu = (CallbackMcu)activity;
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		fragmentManager = getFragmentManager();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.mcu, null);
		upBtn = (Button)view.findViewById(R.id.upbtn);
		upBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if ((callbackMcu.checkSDCard() && callbackMcu.checkFile()) || callbackMcu.checkInterSDCard()) {
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
					alertDialog.setMessage(R.string.found)
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							//Start a child thread
							new Thread(new MyTread()).start();
						}
					})
					.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					})
					.show();
/****************give up init McuDialogFragment*******************/
//					McuDialogFragment mcuDialogFragment = new McuDialogFragment();
//					transaction = fragmentManager.beginTransaction();
//					transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);	//Standard of animation
//					mcuDialogFragment.show(transaction, "df");
/*****************************************************************/
				}else {
					Toast.makeText(getActivity(), R.string.not_found, Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		return view;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		getActivity().unregisterReceiver(mBroadcastReveiver);
	}
	
	public class MyTread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			callbackMcu.cpyfile(); // 将文件拷贝到内存
			if (callbackMcu.checkMcu()) { // 检查MCU版本
				if (callbackMcu.wipeMcuAPP() == WIPE_SUCCEED) {
					Log.d(TAG, "DialogActivity wipeMcuAPP() OK!!!");
						handler.sendEmptyMessage(WHAT_PROGRESS);
					if (callbackMcu.checkdBuffer()) { // 更新MCU 并检验
						handler.sendEmptyMessage(WHAT_UPDATE_OK);
						try {
							Thread.sleep(4000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						callbackMcu.delMcuFile();		//删除升级文件				
						callbackMcu.rebootMcu(); // 发送重启Mcu命令
						
						getActivity().finish();

					}else {
						handler.sendEmptyMessage(WHAT_UPDATE_FAIL);
					}
				}else {
					handler.sendEmptyMessage(WHAT_WIPE_FAIL);
				}
			}else {
				handler.sendEmptyMessage(WHAT_VERSION_SAME);
			}
		}
		
	}
	
	/*****修改升级成功dialog字体大小***/
	  private void setDialogText(View v) { 

          if (v instanceof ViewGroup) { 
                  ViewGroup parent = (ViewGroup) v; 
                  int count = parent.getChildCount(); 
                  for (int i = 0; i < count; i++) { 
                          View child = parent.getChildAt(i); 
                          setDialogText(child); 
                  } 
          } else if (v instanceof TextView) { 
                  ((TextView) v).setTextSize(20); //是textview，设置字号 
          } 
  }
	
	private IntentFilter getIntentFilter(){		
		IntentFilter myIntentFilter = new IntentFilter("broadcast");
		return myIntentFilter;
		
	};
	
	private BroadcastReceiver mBroadcastReveiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int date = intent.getIntExtra("loop", 0);
			progressDialog.setProgress(date);
		}
	};
	
	public interface CallbackMcu{
		public boolean checkSDCard();
		public boolean checkInterSDCard();
		public boolean checkFile();
		public void cpyfile();
		public boolean checkMcu();
		public int 	wipeMcuAPP();
		public boolean checkdBuffer();
		public void delMcuFile();
		public void rebootMcu();
		public int getLoopNum();

	}

}
