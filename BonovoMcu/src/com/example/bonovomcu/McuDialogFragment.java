package com.example.bonovomcu;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class McuDialogFragment extends DialogFragment {
	
	private final static String TAG = "McuFragment";
	
	private Activity context;
	private CallbackMcu callbackMcu;
	private ProgressDialog progressDialog;
	
	private static final int WIPE_SUCCEED = 2;					//wipe ok flag
	private final int WHAT_UPDATE_OK = 1;						//update ok flag
	private final int WHAT_VERSION_SAME = 2;					//version same flag
	private final int WHAT_UPDATE_FAIL = 3;						//update failed flag
	private final int WHAT_WIPE_FAIL = 4;						//wipe failed flag
	private final int WHAT_PROGRESS = 5;						//create MCU_Update progressDialog flag
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setCancelable(false);// Settings Dialog click screen don't dismiss
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Get the layout inflater  
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.mcudialog, null);  
        // Inflate and set the layout for the dialog  
        // Pass null as the parent view because its going in the dialog layout  
        builder.setView(view)
                // Add action buttons  
                .setPositiveButton("Update",  
                        new DialogInterface.OnClickListener()  
                        {  
                            @Override  
                            public void onClick(DialogInterface dialog, int id)  
                            {  
                            	new Thread(new MyTread()).start();
                            } 
                        }).setNegativeButton("Cancel", null);
        return builder.create();
		
	}
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		context = activity;
		activity.registerReceiver(mBroadcastReveiver, getIntentFilter());
		try{
			callbackMcu = (CallbackMcu)activity;
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case WHAT_UPDATE_OK:
				progressDialog.dismiss();
				final ProgressDialog dialog = ProgressDialog.show(context, "升级成功", "正在重启,请稍后...");
//				/*******修改Dialog内文字大小*******/
//				View v = dialog.getWindow().getDecorView();//取到dialog的整个view 
//				setDialogText(v);
				
				dialog.show();
				break;
			case WHAT_VERSION_SAME:
				Toast.makeText(context, "MCU版本与文件版本相同...", Toast.LENGTH_SHORT).show();
				break;
			case WHAT_UPDATE_FAIL:
				Toast.makeText(context, "更新失败", Toast.LENGTH_LONG).show();
				break;
			case WHAT_WIPE_FAIL:
				Toast.makeText(context, "MCU擦除失败", Toast.LENGTH_LONG).show();
				break;
			case WHAT_PROGRESS:
				progressDialog = new ProgressDialog(context);
				progressDialog.setMax(callbackMcu.getLoopNum());
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setTitle("MCU升级进度条");
				progressDialog.setCanceledOnTouchOutside(false);
				progressDialog.setCancelable(false);
				progressDialog.show();
				break;
			default:
				break;
			}
			
		}
	};
	
	public class MyTread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int times = 5 ;//重复发重启命令的次数
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
						do{
							times--;
							callbackMcu.rebootMcu(); // 发送重启Mcu命令	
						}while(times > 0);
						
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
