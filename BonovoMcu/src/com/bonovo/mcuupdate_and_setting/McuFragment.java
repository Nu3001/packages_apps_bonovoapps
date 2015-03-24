package com.bonovo.mcuupdate_and_setting;

import android.util.Log;
import android.os.Handler;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.app.ProgressDialog;
import android.app.FragmentTransaction;
import android.widget.Button;
import android.content.IntentFilter;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.LayoutInflater;

public class McuFragment extends Fragment {
    private static final String TAG = "McuFragment";
    private final int WHAT_UPDATE_OK = 0x1;
    private final int WHAT_VERSION_SAME = 0x2;
	private final int WHAT_UPDATE_FAIL = 0x3;
    private final int WHAT_WIPE_FAIL = 0x4;
    private final int WHAT_PROGRESS = 0x5;
    private static final int WIPE_SUCCEED = 0x2;
    private CallbackMcu callbackMcu;
    private FragmentManager fragmentManager;
    private Handler handler;
    private BroadcastReceiver mBroadcastReveiver;
    private ProgressDialog progressDialog;
    private FragmentTransaction transaction;
    private Button upBtn;
	
	public static interface CallbackMcu {

        public abstract boolean checkFile();
        public abstract boolean checkMcu();
        public abstract boolean checkSDCard();
        public abstract boolean checkdBuffer();
        public abstract void cpyfile();
        public abstract void delMcuFile();
        public abstract int getLoopNum();
        public abstract void rebootMcu();
        public abstract int wipeMcuAPP();
    }
	
	public class MyTread implements Runnable {
        public void run() {
            callbackMcu.cpyfile();
            if (callbackMcu.checkMcu()) {
                if (callbackMcu.wipeMcuAPP() == WIPE_SUCCEED) {
                    Log.d(TAG "DialogActivity wipeMcuAPP() OK!!!");
                    handler.sendEmptyMessage(WHAT_PROGRESS);
                    if (callbackMcu.checkdBuffer()) {
                        handler.sendEmptyMessage(WHAT_UPDATE_OK);
                        try {
                            Thread.sleep(4000L);
                        } catch (InterruptedException interruptedexception) {
                            interruptedexception.printStackTrace();
                        }
                        callbackMcu.delMcuFile();
                        callbackMcu.rebootMcu();
                        getActivity().finish();
                    } else {
                        handler.sendEmptyMessage(WHAT_UPDATE_FAIL);
                    }
                } else {
                    handler.sendEmptyMessage(WHAT_WIPE_FAIL);
                }
            } else {
                handler.sendEmptyMessage(WHAT_VERSION_SAME);
            }
        }
    }
    
    public McuFragment() {

        handler = new Handler() {
            
            public void handleMessage(Message msg) {
                int what = msg.what;
                switch(what) {
                    case WHAT_UPDATE_OK:
                    {
                        progressDialog.dismiss();
                        ProgressDialog dialog = ProgressDialog.show(getActivity(), getResources().getString(R.string.update_ok), getResources().getString(R.string.update_ok_msg));
                        View v = dialog.getWindow().getDecorView();
                        dialog.show();
                        break;
                    }
                    case WHAT_VERSION_SAME:
                    {
                        Toast.makeText(getActivity(), R.string.version_same, 0).show();
                        break;
                    }
                    case WHAT_UPDATE_FAIL:
                    {
                        Toast.makeText(getActivity(), R.string.update_fail, 1).show();
                        break;
                    }
                    case WHAT_WIPE_FAIL:
                    {
                        Toast.makeText(getActivity(), R.string.wipe_fail, 1).show();
                        break;
                    }
                    case WHAT_PROGRESS:
                    {
                        ProgressDialog progressDialog = this$0new ProgressDialog(getActivity());
                        progressDialog = localint1;
                        progressDialog.setMax(callbackMcu.getLoopNum());
                        progressDialog.setProgressStyle(1);
                        progressDialog.setTitle(R.string.progress);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        break;
                    }
                }
            }
        };
        mBroadcastReveiver = new BroadcastReceiver(this) {
            
            public void onReceive(Context context, Intent intent) {
                int date = intent.getIntExtra("loop", 0);
                progressDialog.setProgress(date);
            }
        };
    }
    
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.registerReceiver(mBroadcastReveiver, getIntentFilter());
        try {
            callbackMcu = activity;
        } catch(Exception e) {
        }
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getFragmentManager();
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mcu, null);
        upBtn = (Button)view.findViewById(R.id.upbtn);
        upBtn.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                if((callbackMcu.checkSDCard()) && (callbackMcu.checkFile())) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setMessage(R.string.found).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        
                        public void onClick(DialogInterface dialog, int which) {
                            (new Thread(new MyTread())).start();
                        }
                    }).setNeutralButton("Cancel", DialogInterface.OnClickListener() {
					
						public void onClick(DialogInterface dialog, int which) {
						}
					
					}).show();
                } else {
					Toast.makeText(getActivity(), R.string.not_found, 0).show();
				}
            }
        });
        return view;
    }
    
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReveiver);
    }
    
    private void setDialogText(View v) {
        if (view instanceof ViewGroup) {
            ViewGroup viewgroup = (ViewGroup)view;
            int i = viewgroup.getChildCount();
            for (int j = 0; j < i; j++) {
                setDialogText(viewgroup.getChildAt(j));
            }

        } else if (view instanceof TextView) {
            ((TextView)view).setTextSize(20F);
        }
    }
    
    private IntentFilter getIntentFilter() {
        IntentFilter myIntentFilter = new IntentFilter("broadcast");
        return myIntentFilter;
    }
}
