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


import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;

import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;


import com.bonovo.musicplayer.view.DefaultLrcBuilder;
import com.bonovo.musicplayer.view.ILrcBuilder;
import com.bonovo.musicplayer.view.ILrcView;
import com.bonovo.musicplayer.view.ILrcView.LrcViewListener;
import com.bonovo.musicplayer.view.LrcRow;
import com.bonovo.musicplayer.view.LrcView;

import com.bonovo.musicplayer.IMusicPlayerService.MusicStatus;
import com.bonovo.musicplayer.IMusicPlayerService.MusicStatusChangeListener;

import java.io.UnsupportedEncodingException; 
import java.net.URLDecoder;  
import java.net.URLEncoder; 




public class PlayerLrcViewFragment extends PlayerControl  {
	private final static String TAG = "PlayerLrcViewFragment";



    public String getFromAssetsCode(String fileName,String code){
        try {

	      Log.d(TAG, "lrcfileName:" + fileName);
	      FileInputStream fileStream=new FileInputStream(fileName);
      	      InputStreamReader inputReader = new InputStreamReader( fileStream ,code);
             BufferedReader bufReader = new BufferedReader(inputReader);

            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null){
            	if(line.trim().equals(""))
            		continue;
            	Result += line + "\r\n";
            }
	    fileStream.close();
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getFromAssets(String fileName){
        try {

	      Log.d(TAG, "lrcfileName:" + fileName);
	      FileInputStream fileStream=new FileInputStream(fileName);
      	      InputStreamReader inputReader = new InputStreamReader( fileStream ,"gbk");
             BufferedReader bufReader = new BufferedReader(inputReader);
			 
            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null){
            	if(line.trim().equals(""))
            		continue;
            	Result += line + "\r\n";
            }
	    fileStream.close();
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


     private String getLRCPath(String path) {
        int index = path.lastIndexOf(".");
        String strPath = path;
        if (index >= 0) {
            strPath = path.substring(0, index);
        }
        return strPath + ".lrc";
    }


	public static String getCharEncode(String str){  
	      
		String charEncode = "GB2312";  
		try {  
		    if(str.equals(new String(str.getBytes(charEncode),charEncode))){  
		          
		        return charEncode;  
		    }  
		} catch (UnsupportedEncodingException e) {  
		      
		}  
		  
		charEncode = "GBK";  
		try {  
		    if(str.equals(new String(str.getBytes(charEncode),charEncode))){  
		          
		        return charEncode;  
		    }  
		} catch (UnsupportedEncodingException e) {  
		      
		}  
		  
		charEncode = "ISO-8859-1";  
		try {  
		    if(str.equals(new String(str.getBytes(charEncode),charEncode))){  
		          
		        return charEncode;  
		    }  
		} catch (UnsupportedEncodingException e) {  
		      
		}  
		  
		charEncode = "UTF-8";  
		try {  
		    if(str.equals(new String(str.getBytes(charEncode),charEncode))){  
		      
		        return charEncode;  
		    }  
		} catch (UnsupportedEncodingException e) {  
		  
		}  
		  
		return "";  
	      
	}
		

	ILrcView mLrcView;

	private List<LrcRow> GetLrcRowList(String lrcPath)
	{
	       Log.d(TAG, "lrcfileName:" + lrcPath);
		String lrcOrg = getFromAssets(lrcPath);
		
	    String code=getCharEncode(lrcOrg);

	    if(code=="UTF-8")
	    {
		   lrcOrg=getFromAssetsCode(lrcPath,code);
	    }
			

		
		ILrcBuilder builder = new DefaultLrcBuilder();
		List<LrcRow> rows = builder.getLrcRows(lrcOrg);
		return rows;
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onCreateView");

		mLrcView = new LrcView(getActivity(), null);        
		
	        super.bindService();
		
		return (View) mLrcView;
	}


	@Override
	public void onDestroy() {
	
	    Log.d(TAG, "onDestroy");

	    super.unbindService();
		
	    super.onDestroy();
	}


	@Override
       public void onUpdatePositionUI(long position)
       {
	   Log.d(TAG, "onUpdatePositionUI  position="+position+"isShow="+isVisible());
	   if(isVisible()==true){
		mLrcView.seekLrcToTime(position);
	   }
       }


	@Override
       public void onUpdateUI(MusicStatus status)
       {
       	 Log.d(TAG, "onUpdateUI  file="+status.currMusicFile+"isShow="+isVisible());
	 mLrcView.setLrc(GetLrcRowList(getLRCPath(status.currMusicFile)));
       }
	
		

}
