package com.bonovo.mcuupdate_and_setting;

import android.os.Binder;
import android.app.Service;
import java.io.File;
import android.util.Log;
import android.os.Environment;
import java.util.Arrays;
import android.content.Intent;
import android.os.Bundle;
import java.io.FileInputStream;
import android.os.IBinder;
import android.content.SharedPreferences;

public class FragmentService extends Service {
    private static final int CAMERA = 1;
    private static final boolean DEBUG = false;
    private static final int MUTE = 1;
    private static final int NO_CAMERA = 0;
    private static final int NO_MUTE = 0;
    private int PRESSHOST;
    private int PRESSSLAVE;
    private final String TAG;
	private static final int Update_Fail = 1;
    private static final int Update_OK = 2;
    public static final String action = "broadcast";
    private FragmentService.ServiceBinder binder;
    public static int carType;
    public int fileLen;
    public int lightState;
    public static int loop;
    public boolean mCheckCamera;
    public boolean mCheckLight;
    public boolean mCheckMute;
    public int mCheckOTG;
    public boolean mCheckVolume;
    public byte[] mbuff;
    public byte[] newbuff;
    public int nfilelen;
    public byte[] partBuff;
    File path;
    public String pathName;
    public int progessBrigth;
    public int progessVolume;
    public byte[] rebuff;
    public static int serialType;
    public static int volumeS8;
    
	// New in 1.1.4
    private final native int jniPickColor(int i, int j, int k) throws IllegalStateException;
    private final native int jniswitchOTG(int i) throws IllegalStateException;
	
	// Exist in 1.0.9
	 private native final byte[] jniCheckMcuVersion() throws IllegalStateException;
	 private native final int jniWipeMcuAPP() throws IllegalStateException;
	 private native final int jniUpdateMcu(byte[] buf,int offset) throws IllegalStateException;
	 private native final int jniResetMcu() throws IllegalStateException;
	 private native final byte[] jnirequestMcuFlash(int offset) throws IllegalStateException;
	 private native final int jniRebootMcu() throws IllegalStateException;
	 private native final int jniAsternMute(int onOff) throws IllegalStateException;
	 private native final int jniqueryHeadlight() throws IllegalStateException;
     private native final int jniQueryReverse() throws IllegalStateException;
	 private native final int jnirearviewCamera(int onOff) throws IllegalStateException;
	 private native final int jnigetBrightness() throws IllegalStateException;
	 private native final int jnisetBrightness(int brightness) throws IllegalStateException;
	 private native final int jnilowBrigthness(int brightness) throws IllegalStateException;
	 private native final int jniautoVolume(int percent) throws IllegalStateException;
     private native final int jniControlScreen(boolean isShowAndroid) throws IllegalStateException;
	
	public static class ServiceBinder extends Binder {
        public FragmentService getServicer() {
            return FragmentService.this;
        }
    }
    
    public FragmentService() {
        TAG = "com.example.fragment.service";
        pathName = "/mnt/external_sd/updatemcu.bin";
        path = new File(pathName);
        PRESSHOST = 1;
        PRESSSLAVE = 2;
        mbuff = null;
        fileLen = 0;
        nfilelen = 0;
        newbuff = null;
        partBuff = new byte[128];
        rebuff = new byte[128];
        binder = new ServiceBinder();
    }
    
    static {
        System.loadLibrary("bonovomcu");
    }
    
    public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onBind()!!!");
		return serviceBinder;
    }
    
    public void onCreate() {
        Log.v(TAG, "-->Service-->onCreate()");
        super.onCreate();
        readSharePre();
        SharedPreferences preferences = getSharedPreferences("CAR_CONFIG", 0);
        mCheckMute = preferences.getBoolean("mute", true);
        mCheckCamera = preferences.getBoolean("camera", true);
        mCheckLight = preferences.getBoolean("brigthconfig", false);
        progessBrigth = preferences.getInt("Progress", 0);
        mCheckVolume = preferences.getBoolean("volume", false);
        progessVolume = preferences.getInt("ProgressVolume", 0);
        SharedPreferences preferencesOTG = getSharedPreferences("otg model", 0);
        mCheckOTG = preferencesOTG.getInt("otg checked", PRESSHOST);
        if(mCheckMute) {
            jniAsternMute(MUTE);
        } else {
            jniAsternMute(NO_MUTE);
        }
        if(mCheckCamera) {
            jnirearviewCamera(CAMERA);
        } else {
            jnirearviewCamera(NO_CAMERA);
        }
        if(mCheckLight) {
            Log.d(TAG, "mCheckLight is true");
            jnilowBrigthness(progessBrigth + 10);
        } else {
            Log.d(TAG, "mCheckLight is false");
            jnilowBrigthness(0);
        }
        if(mCheckVolume) {
            jniautoVolume(progessVolume + 10);
        } else {
            jniautoVolume(0);
        }
        if(mCheckOTG == PRESSHOST) {
            switchOTG(PRESSHOST);
        } else {
			switchOTG(PRESSSLAVE);
		}
    }
    
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.v(TAG, "-->Service-->onStart()");
    }
    
    public void onDestroy() {
        Log.v(TAG, "-->Service-->onDestroy()");
        super.onDestroy();
    }
    
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    
    public void setAsternMute(boolean Mute) {
		if(Mute){
			Log.d(TAG, "Mutecheckbox is true");
			jniAsternMute(MUTE);
		}else{
			Log.d(TAG, "Mutecheckbox is false");
			jniAsternMute(NO_MUTE);
		}
    }
    
    public void setRearviewCamera(boolean Camera) {
		if(Camera){
			Log.d(TAG, "Cameracheckbox is true");
			jnirearviewCamera(CAMERA);
		}else{
			Log.d(TAG, "Cameracheckbox is false");
			jnirearviewCamera(NO_CAMERA);
		}
    }
    
    public void getBrightness() {
		int getBrightness;
		getBrightness = jnigetBrightness();
		Log.d(TAG, "getBrightness is " + getBrightness);
    }
    
    public void setBrightness(int brightness) {
        jnisetBrightness(brightness);
    }
    
    public void lowBrightness(int low) {
        jnilowBrigthness(low);
    }
    
    public void volumePercent(int percent) {
        jniautoVolume(percent);
    }
    
    public int version() {
		byte[] date = new byte[4];
		date = jniCheckMcuVersion();
		for(int i=0;i<4;i++){										
			Log.d(TAG, "date["+i+"]="+date[i]);
		}
		int a = date[1] + date[0];
		return a;
    }
    
    public boolean checkSDCard() {
        String sdStatus = Environment.getExternalStorageState();
        if(!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "SD card is not avaiable/writeable right now.");
            return false;
        } else {
	        Log.d(TAG, "SD card is mounted!!!");
            return true;
		}
    }
    
    public boolean checkFile() {
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
    
    public void cpyfile() {
		try {
			FileInputStream inStream = new FileInputStream(path);
			mbuff = new byte[inStream.available()]; 				// mbuff????????
			fileLen = inStream.available();
			//Log.d(TAG, "fileLen = " + fileLen);
			inStream.read(mbuff);									// ???,???byte??
			//?????????128????
			//?????????????,??????????????????????0XFF?????128??
			if(fileLen%128 != 0){
				nfilelen = fileLen + (128-fileLen%128);
				newbuff = new byte[nfilelen];
				Log.d(TAG, "newfileLen = " + nfilelen);
				System.arraycopy(mbuff, 0, newbuff, 0, mbuff.length);
				for(int idx = fileLen; idx < nfilelen; idx++){
					newbuff[idx] = (byte)0xFF;
				}
			}else{
				nfilelen = fileLen;
				newbuff = new byte[nfilelen];
				System.arraycopy(mbuff, 0, newbuff, 0, mbuff.length);
				Log.d(TAG, "newfileLen = " + nfilelen);
			}
			
			inStream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    }
    
    public boolean checkMcu() {
		byte[] verBf = new byte[4];
		Log.d(TAG, "checkMcu()!!!");
		verBf = jniCheckMcuVersion();
		for(int i=0;i<4;i++){										//??mcu??
			Log.d(TAG, "verBf["+i+"]="+verBf[i]);
		}
		Log.d(TAG, "file-version="+(mbuff[401]|(mbuff[400]<<8))+"mcu-version="+(verBf[0]|(verBf[1]<<8)));
		if((verBf[0]|(verBf[1]<<8)) != (mbuff[401]|(mbuff[400]<<8)) && (verBf[2]|(verBf[3]<<8)) == (mbuff[403]|(mbuff[402]<<8))){
			return true;
		}
		return false;
    }
    
    public int wipeMcuAPP() {
        int cmd = jniWipeMcuAPP();
        return cmd;
    }
    
    public boolean checkdBuffer() {
		Log.d(TAG, "checkBuffer()");
		loop = newbuff.length / 128;		//????
		for (int i = 0; i < loop; i++) {
				System.arraycopy(newbuff, 128 * i, partBuff, 0, 128);
			if (DEBUG)
				Log.d(TAG, "partBuff" + i + " :  w_datelen = "
						+ (i * 128 + 128) + " partBuff.lenght = " + partBuff.length);
			if (jniUpdateMcu(partBuff, i) == Update_OK) { // ????????????
				if (DEBUG)
					Log.d(TAG, "Update_OK!!!");

				rebuff = jnirequestMcuFlash(i); // ??????MCU?????
				int len = rebuff.length; // ???????
				if (DEBUG)
					Log.d(TAG, " rebuff.length = " + len);
				boolean flag = Arrays.equals(partBuff, rebuff);// ?????????????
				if (flag) {
					Intent intent = new Intent(action);  
					Bundle bundle = new Bundle();
					bundle.putInt("loop", i);
					intent.putExtras(bundle);
					sendBroadcast(intent); 
					continue;
				} else {
					return false;
				}
			} else {
				return false;
			}

		}
		Log.d(TAG, "update and checkd Buffer over true!!!");
		return true;
    }
    
    public void delMcuFile() {
        if(path.exists()) {
            path.delete();
            Log.d(TAG, "The updatemcu.bin is del!!!");
        }
    }
    
    public void rebootMcu() {
        jniRebootMcu();
    }
    
    public void readSharePre() {
        SharedPreferences preferences = getSharedPreferences("serial_checked_result", 0x1);
        SharedPreferences preferences2 = getSharedPreferences("car_checked_result", 0x1);
        carType = preferences2.getInt("radioButton_Checked_Flag", 0x0);
        serialType = preferences.getInt("radioButton_Checked_Flag", 0x0);
        Intent intentSerialType = new Intent("com.android.internal.car.can.action.SERIAL_TYPE_CHANGED");
        intentSerialType.putExtra("serial_type", serialType);
        sendBroadcast(intentSerialType);
        Intent intentCarType = new Intent("com.android.internal.car.can.action.CAR_TYPE_CHANGED");
        intentCarType.putExtra("car_type", carType);
        sendBroadcast(intentCarType);
    }
    
    public void setColor(int red, int green, int blue) {
        jniPickColor(red, green, blue);
    }
    
    public void switchOTG(int model) {
        jniswitchOTG(model);
    }
}
