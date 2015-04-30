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

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;


import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;

import btmsg.Btmsg;

public class BTUpdateFragment extends Fragment {
	private final static String TAG = "BTUpdateFragment";

	
	private Button upBtn;
	
	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;
	private CallbackMcuOP callbackMcuOp;
	private ProgressDialog progressDialog=null;
	private ProgressDialog progressDialogLoad=null;
	private ProgressDialog progressDialogcomplete=null;

///////////////////////////////////////////////////////////////////////////////
     private static final java.lang.String DESCRIPTOR = "sample.hello";
     private static final int FUNC_CALLFUNCTION = 1;
     private IBinder bBinderService;
///////////////////////////////////////////////////////////////////////////////

	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		activity.registerReceiver(mBroadcastReveiver, getIntentFilter());

		try{
			callbackMcuOp = (CallbackMcuOP)activity;
		}catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		fragmentManager = getFragmentManager();

		//bBinderService=ServiceManager.getService(DESCRIPTOR);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.btupdate, null);
		
		upBtn = (Button)view.findViewById(R.id.upbtn);
		upBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				
				if (checkSDCard() && checkFile()) {
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
					alertDialog.setMessage(R.string.found)
					.setCancelable(false)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							BTUpdateTast();
						}
					})
					.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					})
					.show();
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

	  private void setDialogText(View v) { 

          if (v instanceof ViewGroup) { 
                  ViewGroup parent = (ViewGroup) v; 
                  int count = parent.getChildCount(); 
                  for (int i = 0; i < count; i++) { 
                          View child = parent.getChildAt(i); 
                          setDialogText(child); 
                  } 
          } else if (v instanceof TextView) { 
                  ((TextView) v).setTextSize(20); 
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

	////////////////////////////////////////////////////////////////////////

	private String pathName = "/mnt/external_sd/BTHUpdate.dfu";
	File path = new File(pathName);
	
	private boolean checkSDCard() {

		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
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


       private String progressDialogInfo;
	   
	public String do_exec(String cmd) {
		Process process=null;
		
	        try {
	               process = Runtime.getRuntime().exec(cmd);

			  BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));  
            		  String line = null;
					  
	                while ((line = in.readLine()) != null) {  

					//progressDialogInfo=line;
					//Message m = handler.obtainMessage(1, 1, 1, line);
					//handler.sendMessage(m);
					//handler.sendEmptyMessage(1);

					Log.d(TAG, "do_exec info="+line);	
	                } 			
				
	        } catch (IOException e) {
	        
	            // TODO Auto-generated catch block  
	            e.printStackTrace();
				
	        }  

		 if(process != null)
		 {
			 try {
			     process.waitFor();
		        } catch (InterruptedException e) {
		            // TODO Auto-generated catch block  
		            e.printStackTrace();	
		        }  	
		 }
 
	        return cmd;       
    	}


	
	public void BTUpdateTast() {
		setbtUpdateStatus(BTUPDATESTATUS.LOADFILE);
		executeTast();
		
		new Thread(new UpdateTread()).start();
	}

	public class UpdateTread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub  
			
			new Thread(new SocketServerThread()).start();
			
			do_exec("/system/bin/btfw");
			
                     handler.sendEmptyMessage(0);

		        try {
		            Thread.sleep(1000);
			     
		        } catch (InterruptedException e) {
		            e.printStackTrace(); 
		        }

			    callbackMcuOp.rebootMcu();
		}
		
	}


	Handler handler = new Handler() {  
	        @Override  
	        public void handleMessage(Message msg) {

			int what = msg.what;
			switch (what) 
			{
			case 0:
				{
					closeALLProgressDialog();
				}
				break;
			case 2:
				{
					executeTast();
				}

				break;
			}

	            
	            }  
	};
       
	///////////////////////////////////////////////////////////////////////////////////////
	public enum BTUPDATESTATUS{
		SNULL,
		LOADFILE,
		PROGRESS,
		COMPLETE,
	} 

	BTUPDATESTATUS btUpdateStatus=BTUPDATESTATUS.SNULL;

	private void setbtUpdateStatus(BTUPDATESTATUS status)
	{
		btUpdateStatus=status;
	}

	
		
	private void closeALLProgressDialog()
	{  
		if(progressDialog!=null)
		{
			progressDialog.dismiss();
			progressDialog=null;
		}

		if(progressDialogLoad!=null)
		{
			progressDialogLoad.dismiss();
			progressDialogLoad=null;	
		}

		if(progressDialogcomplete!=null)
		{
			progressDialogcomplete.dismiss();
			progressDialogcomplete=null;				
		}
		
	}

	private void executeTast()
	{
		switch(btUpdateStatus)
		{
			case LOADFILE:
				{
					closeALLProgressDialog();
					progressDialogLoad = ProgressDialog.show(getActivity(),getResources().getString(R.string.btprogress)  , getResources().getString(R.string.btloadfile)); 
					setbtUpdateStatus(BTUPDATESTATUS.PROGRESS);
				}
				break;
			case PROGRESS:
				{
					if(progressDialog==null)
					{
						progressDialog = new ProgressDialog(getActivity());
						progressDialog.setMax(progressDialogMax);
						progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						progressDialog.setTitle(R.string.btprogress);
						progressDialog.setCanceledOnTouchOutside(false);
						progressDialog.setCancelable(false);
						progressDialog.show();

						progressDialogLoad.dismiss();
					}
					else if(progressDialogProgress>1 && progressDialogProgress==progressDialogMax)
					{

						setbtUpdateStatus(BTUPDATESTATUS.COMPLETE);
						
					}
					else
					{
						progressDialog.setProgress(progressDialogProgress);
					}					
				}
				break;
			case COMPLETE:
				{
						progressDialog.dismiss();

						if(progressDialogcomplete==null)
						       progressDialogcomplete = ProgressDialog.show(getActivity(),getResources().getString(R.string.btprogress) , getResources().getString(R.string.btunzip)); 
				}
				break;
		}

	}

	

	/////////////////////////////////////////////////////////////////////////

	private int progressDialogMax=0;
	private int progressDialogProgress=0;
	
	public class SocketServerThread implements Runnable{

		@Override
		public void run() 
		{
			try {
				
				ServerSocket serverSocket = new ServerSocket(12345);
				Log.d(TAG, "SocketServerThread start  serverSocket");
				
				while (true) {

					Log.d(TAG, "SocketServerThread wait  client........................");
					Socket client = serverSocket.accept();
					DataOutputStream dataOutputStream;
                			DataInputStream dataInputStream;

							try {

								InputStream inputstream = client.getInputStream();


					                        while (true) {
												
									byte len[] = new byte[1024];
									
					                            int byteRead = inputstream.read(len);
												
					                            if (byteRead < 0) {
					                                break;  // EOF.
					                            }
												
									byte[] temp = new byte[byteRead];
									for (int i = 0; i < byteRead; i++) {   
	                        
	                            					temp[i] = len[i];                              
	                    						}

									
									btmsg.Btmsg.msg myBtmsg= Btmsg.msg.parseFrom(temp);
									Log.d(TAG, "SocketServerThread info="+myBtmsg.getName()+"total:"+myBtmsg.getPackagetotal()+"ID:"+myBtmsg.getPackageId());
									progressDialogMax=myBtmsg.getPackagetotal();
									progressDialogProgress=myBtmsg.getPackageId();


								       handler.sendEmptyMessage(2);
								
					                        }

								//btmsg.Btmsg.msg myBtmsg= Btmsg.msg.parseFrom(inputstream);
								inputstream.close();
								//handler.sendEmptyMessage(0);
								
							} catch (IOException e) {
			
            			 				e.printStackTrace();
										
        						}finally {
        						
                  						client.close();
								Log.d(TAG, "SocketServerThread close  client........................");
								handler.sendEmptyMessage(0);		
                					}
					
				}
				
			} catch (IOException e) {
			
            			 e.printStackTrace();
        		}
			
		}
		
	}


	public interface CallbackMcuOP{
		public void rebootMcu();

	}

}
