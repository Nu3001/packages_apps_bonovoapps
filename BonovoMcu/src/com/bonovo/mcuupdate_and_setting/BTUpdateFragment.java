package com.bonovo.mcuupdate_and_setting;

import java.io.IOException;
import java.net.ServerSocket;
import android.util.Log;
import java.net.Socket;
import java.io.InputStream;
import btmsg.Btmsg;
import android.os.Handler;
import android.app.Fragment;
import android.os.IBinder;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import java.io.File;
import android.app.ProgressDialog;
import android.app.FragmentTransaction;
import android.widget.Button;
import android.os.Environment;
import android.app.Activity;
import android.content.res.Resources;
import android.content.Context;
import android.content.IntentFilter;
import android.view.View;
import android.view.ViewGroup;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import android.os.Bundle;
import android.view.LayoutInflater;

public class BTUpdateFragment extends Fragment {
    private static final String DESCRIPTOR = "sample.hello";
    private static final int FUNC_CALLFUNCTION = 1;
    private static final String TAG = "BTUpdateFragment";
    private IBinder bBinderService;
    BTUPDATESTATUS btUpdateStatus = BTUPDATESTATUS.SNULL;
    private CallbackMcuOP callbackMcuOp;
    private FragmentManager fragmentManager;
    Handler handler;
    private BroadcastReceiver mBroadcastReveiver;
    private String pathName = "/mnt/external_sd/BTHUpdate.dfu";
    File path = new File(pathName);
    private ProgressDialog progressDialog = null;
    private String progressDialogInfo;
    private ProgressDialog progressDialogLoad = null;
    private int progressDialogMax = 0;
    private int progressDialogProgress = 0;
    private ProgressDialog progressDialogcomplete = null;
    private FragmentTransaction transaction;
    private Button upBtn;
	
	public enum BTUPDATESTATUS {
        SNULL,
        LOADFILE,
        PROGRESS,
        COMPLETE;
    }
	
	public static interface CallbackMcuOP {
        public abstract void rebootMcu();
    }
	
	public class SocketServerThread implements Runnable {

		public void run() {

			try {
				ServerSocket serverSocket = new ServerSocket(12345);
				Log.d(TAG, "SocketServerThread start  serverSocket");

				while (true) {
					Log.d(TAG, "SocketServerThread wait  client........................");
					Socket client = serverSocket.accept();

					try {
						InputStream inputstream = client.getInputStream();
						while (true) {
							byte[] len = new byte[0x400];
							int byteRead = inputstream.read(len);
							if (byteRead > 0) {

								byte[] temp = new byte[byteRead];
								for (int i = 0; i < byteRead; i++) {
									temp[i] = len[i];
								}

								Btmsg.msg myBtmsg = Btmsg.msg.parseFrom(temp);
								Log.d(TAG, "SocketServerThread info=" + myBtmsg.getName() + "total:" + myBtmsg.getPackagetotal() + "ID:" + myBtmsg.getPackageId());
								progressDialogMax = myBtmsg.getPackagetotal();
								progressDialogProgress = myBtmsg.getPackageId();
								handler.sendEmptyMessage(0x2);

							}
						}
						inputstream.close();
					} finally {
						client.close();
						Log.d(TAG, "SocketServerThread close  client........................");
						handler.sendEmptyMessage(0x0);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class UpdateTread implements Runnable {
	
		@Override
        public void run() {
            (new Thread(new SocketServerThread())).start();
            do_exec("/system/bin/btfw");
            handler.sendEmptyMessage(0);
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException interruptedexception) {
                interruptedexception.printStackTrace();
            }
            callbackMcuOp.rebootMcu();
        }
    }
    
    public BTUpdateFragment() {
        mBroadcastReveiver = new BroadcastReceiver() {
            
			@Override
            public void onReceive(Context context, Intent intent) {
                int date = intent.getIntExtra("loop", 0);
                progressDialog.setProgress(date);
            }
        };

        handler = new Handler() {
            
            public void handleMessage(Message msg) {
                int what = msg.what;
                switch(what) {
                    case 0:
                    {
						closeALLProgressDialog();
                        break;
                    }
                    case 2:
                    {
						executeTast();
                        break;
                    }
                    case 1:
                    {
                        break;
                    }
                }
            }
        };
    }
    
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.registerReceiver(mBroadcastReveiver, getIntentFilter());
        try {
            callbackMcuOp = (CallbackMcuOP)activity;
        } catch(Exception e) {
        }
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getFragmentManager();
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.btupdate, null);
        upBtn = (Button)view.findViewById(R.id.upbtn);
        upBtn.setOnClickListener(new View.OnClickListener() {
            
			@Override
            public void onClick(View v) {
                if(checkSDCard() && checkFile()) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setMessage(R.string.found).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        
						@Override
                        public void onClick(DialogInterface dialog, int which) {
                            BTUpdateTast();
                        }
                    }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
					
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
                } else {
					Toast.makeText(getActivity(), R.string.not_found, 0x0).show();
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
    
    private boolean checkSDCard() {
        String sdStatus = Environment.getExternalStorageState();
        if(!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "SD card is not avaiable/writeable right now.");
            return false;
        } else {
	        Log.d(TAG, "SD card is mounted!!!");
            return true;
		}
    }
    
    private boolean checkFile() {
        try {
			if (!path.exists()) {
				Log.d(TAG, "Couldn't find the file!!!");
				return false;
				// path.createNewFile();
			} else {
				Log.d(TAG, "Has been the file!!!");
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
    }
    
	public String do_exec(String cmd) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null) {
				Log.d(TAG, "do_exec info=" + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (process != null) {
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return cmd;
	}
    
    public void BTUpdateTast() {
        setbtUpdateStatus(BTUPDATESTATUS.LOADFILE);
        executeTast();
        (new Thread(new UpdateTread())).start();
    }
    
    private void setbtUpdateStatus(BTUPDATESTATUS status) {
        btUpdateStatus = status;
    }
    
    private void closeALLProgressDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        if(progressDialogLoad != null) {
            progressDialogLoad.dismiss();
            progressDialogLoad = null;
        }
        if(progressDialogcomplete != null) {
            progressDialogcomplete.dismiss();
            progressDialogcomplete = null;
        }
    }
    
    private void executeTast() {
        switch(btUpdateStatus) {
            case BTUPDATESTATUS.LOADFILE:
                closeALLProgressDialog();
                progressDialogLoad = ProgressDialog.show(getActivity(), getResources().getString(R.string.btprogress), getResources().getString(R.string.btloadfile));
                setbtUpdateStatus(BTUpdateFragment.BTUPDATESTATUS.PROGRESS);
                break;
            case BTUPDATESTATUS.PROGRESS:
                if(progressDialog == null) {
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMax(progressDialogMax);
                    progressDialog.setProgressStyle(0x1);
                    progressDialog.setTitle(R.string.btprogress);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    progressDialogLoad.dismiss();
                } else if ((progressDialogProgress > 1) && (progressDialogProgress == progressDialogMax)) {
                    setbtUpdateStatus(BTUpdateFragment.BTUPDATESTATUS.COMPLETE);
                } else {
					progressDialog.setProgress(progressDialogProgress);
				}
                break;
            case BTUPDATESTATUS.COMPLETE:
                progressDialog.dismiss();
                if(progressDialogcomplete == null) {
                    progressDialogcomplete = ProgressDialog.show(getActivity(), getResources().getString(R.string.btprogress), getResources().getString(R.string.btunzip));
                }
				break;
        }
    }
}
