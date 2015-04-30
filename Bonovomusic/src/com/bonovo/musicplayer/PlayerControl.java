package com.bonovo.musicplayer;

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
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;


import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;

import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import com.bonovo.musicplayer.IMusicPlayerService.MusicStatus;
import com.bonovo.musicplayer.IMusicPlayerService.MusicStatusChangeListener;

import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;


public class PlayerControl  extends Fragment implements
        ServiceConnection {
	private final static String TAG = "PlayerControl";


	private boolean mServiceConnected = false;

///////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onUpdateUI(MusicStatus status)
    {
    	
    }

    public void onUpdatePositionUI(long position)
    {
    	
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// PlayerDetailFragment  private function
	 private void initFragment() {
	        mHandler.sendEmptyMessage(INIT_FRAGMENT);
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Override  interface of ServiceConnection
	
	private IMusicPlayerService mService=null;

	public void bindService() {
        	BonovoMusicPlayerUtil.bindPlayBackService(getActivity(), this);
    	}
	
	public void unbindService() {
		 BonovoMusicPlayerUtil.unbindPlayBackService(getActivity(), this);
    	}
	
	 @Override
    	public void onServiceConnected(ComponentName name, IBinder service) {
        
        Log.d(TAG, "onServiceConnected");
		
        mService = ((BonovoMusicPlayerUtil.ServiceBinder) service).getService();

        mService.cancelNotification();
       
        mServiceConnected=true;
        initFragment();

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected");
        mService.unregisterStatusListener(mMusicStatusListener);
    }

    @Override
    public void onDestroy() {
        
        Log.d(TAG, "onDestroy");
        if (mHandler != null) {
            mHandler.removeMessages(UPDATE_FRAGMENT);
	    mHandler.removeMessages(UPDATE_POSITION);
            mHandler = null;
        }
        
        super.onDestroy();
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////
   // create MusicStatusChangeListener to listen chang of music status;
    private final MusicStatusChangeListener mMusicStatusListener = new MusicStatusChangeListener() {
        @Override
        public void onStatusChange(MusicStatus status) {
            if (mHandler != null) {
		  Log.d(TAG, "send UPDATE_FRAGMENT");
                Message msg = mHandler.obtainMessage(UPDATE_FRAGMENT, status);
                mHandler.sendMessage(msg);
            }
        }
    };
///////////////////////////////////////////////////////////////////////////////////////////////////////////
   // create  thread to  handle msg
   	private static final int INIT_FRAGMENT = 1;
	private static final int UPDATE_FRAGMENT = 2;
	private static final int UPDATE_POSITION = 3;
   
       private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;

	    Log.d(TAG, "handleMessage="+what);
		
            switch (what) {
                case INIT_FRAGMENT:
			{
				Log.d(TAG, "INIT_FRAGMENT");

				if (mServiceConnected) {
	                        mService.registerStatusListener(mMusicStatusListener);
	                        mHandler.obtainMessage(UPDATE_FRAGMENT, mService.getCurrentStatus()).sendToTarget();
				   mHandler.sendEmptyMessage(UPDATE_POSITION);
							
	                    } else {
	                        sendEmptyMessageDelayed(INIT_FRAGMENT, 1000);
	                    }	
			}
			break;
		case UPDATE_FRAGMENT:
			{
				Log.d(TAG, "UPDATE_FRAGMENT");
				MusicStatus ms = (MusicStatus) msg.obj;
				if(ms!=null)
				{
					onUpdateUI(ms);
				}

			}
			break;
		case UPDATE_POSITION:
			{
				if (mService != null) {
	                        removeMessages(UPDATE_POSITION);
	                        long position = mService.position();

				   onUpdatePositionUI(position);

	                        sendMessageDelayed(obtainMessage(UPDATE_POSITION), 1000);
	                    }
			}
			break;
                default:
            }
        }
    };

}
