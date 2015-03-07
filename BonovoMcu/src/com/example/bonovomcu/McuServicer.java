package com.example.bonovomcu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;


import android.R.bool;
import android.R.string;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

public class McuServicer extends Service {

	private static final String TAG = "BonovoMcuServicer";
	public String pathName = "/mnt/external_sd/updatemcu.bin";
	File path = new File(pathName);
	private static final boolean DEBUG = false;
	public static final String action = "broadcast"; 

	public byte mbuff[] = null;							//存放整个升级文件的内容
	public byte[] rebuff = new byte[128];				//存放检验返回的128字节数据；
	public byte[] newbuff = null;						//声明一个数组用于存放可以被128整除的数据
	public byte[] partBuff = new byte[128];				//存放往JNI里传送的128字节数据
	private static final int Update_Fail = 1;					//更新MCU失败
	private static final int Update_OK = 2;						//更新MCU成功
	private static final int MUTE = 1;					//倒车静音标示
	private static final int NO_MUTE = 0;						//不静音标示
	private static final int CAMERA = 1;					//倒车摄像头标示
	private static final int NO_CAMERA = 0;						//无倒车摄像头标示
	public int fileLen = 0;
	public int nfilelen = 0;
	public int lightState;								//大灯状态标志
	public int progessBrigth;								//大灯开启背光值
	public int progessVolume;								//自动音量百分比值
	public boolean mCheckLight;								//大灯BOX选择标志
	public boolean mCheckMute;								//倒车静音BOX选择标志
	public boolean mCheckCamera;								//倒车后视BOX选择标志
	public boolean mCheckVolume;								//自动音量BOX选择标志
	private boolean mReverseStatus = false;                     // ����״̬
	public static int sysBrightness = -1;						//system brigthness
	public static int loop;								//128字节循环次数
	private Context mContext;

	 private native final byte[] jniCheckMcuVersion() throws
	 IllegalStateException;
	 private native final int jniWipeMcuAPP() throws IllegalStateException;
	 private native final int jniUpdateMcu(byte[] buf,int offset) throws
	 IllegalStateException;
	 private native final int jniResetMcu() throws IllegalStateException;
	 private native final byte[] jnirequestMcuFlash(int offset) throws
	 IllegalStateException;
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
	
	 static {
		 System.loadLibrary("bonovomcu");
	 }

	private ServiceBinder serviceBinder = new ServiceBinder();

	public class ServiceBinder extends Binder {
		public McuServicer getServicer() {
			return McuServicer.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onBind()!!!");
		return serviceBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
//		if(checkSDCard()&&checkFile()){										//SD卡存在且有指定的升级文件则发送Intent启动DialogActivtiy
//			Log.d(TAG, "checkSDCard()&&checkFile() is true");
//			Intent it = new Intent(McuServicer.this, DialogActivity.class);
//			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			McuServicer.this.startActivity(it);
//		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**     获取MCU软件版本号
	 * @return 返回软件版本号
	 */
	public int version(){
		byte[] date = new byte[4];
		date = jniCheckMcuVersion();
		for(int i=0;i<4;i++){										
			Log.d(TAG, "date["+i+"]="+date[i]);
		}
		int a = date[1] + date[0];
		return a;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d(TAG, "onCreate()!!!");
        mContext = this;
		//remove by myu 2014-7-25:
		this.registerReceiver(myReceiver, getIntentFilter());
		
		try {
			sysBrightness = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//when service start , get Mute and Camera Box Flag and send cmd to setting them;
		SharedPreferences setting6 = getSharedPreferences("MUTE_STATE", 0);
		mCheckMute = setting6.getBoolean("Check", true);
		mCheckCamera = setting6.getBoolean("Check3", true);
		//***add by myu 2014-7-24
		mCheckLight = setting6.getBoolean("Check2", false);
		progessBrigth = setting6.getInt("Progress", 0);
		//******
		
		mCheckVolume = setting6.getBoolean("checkVolume", false);
		progessVolume = setting6.getInt("ProgressVolume", 0);
		if (mCheckMute) {
			jniAsternMute(MUTE);
		} else {
			jniAsternMute(NO_MUTE);
		}

		if (mCheckCamera) {
			jnirearviewCamera(CAMERA);
		} else {
			jnirearviewCamera(NO_CAMERA);
		}
		//***add by myu 2014-7-24
		if(mCheckLight){
			Log.d(TAG, "mCheckLight is true");
			jnilowBrigthness(progessBrigth + 10);
		}else {
			Log.d(TAG, "mCheckLight is false");
			jnilowBrigthness(0);
		}
		//******
		
		if(mCheckVolume){
			jniautoVolume(progessVolume + 10);
		}else{
			jniautoVolume(0);
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onDestroy()!!!");
        this.unregisterReceiver(myReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onUnbind()!!!");
		return super.onUnbind(intent);
	}

	/********************* 判断SD是否存在 **********************/
	public boolean checkSDCard() {

		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
			Log.d(TAG, "SD card is not avaiable/writeable right now.");
			return false;
		} else {
			Log.d(TAG, "SD card is mounted!!!");
			return true;
		}
	}

	/********************* 检查是否有指定升级文件 **********************/
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
	
	/********************* 检查MCU版本是否相同 **********************/
	public boolean checkMcu() {
		byte[] verBf = new byte[4];
		Log.d(TAG, "checkMcu()!!!");
		verBf = jniCheckMcuVersion();
		for(int i=0;i<4;i++){										//打印mcu版本
			Log.d(TAG, "verBf["+i+"]="+verBf[i]);
		}
		Log.d(TAG, "file-version="+(mbuff[401]|(mbuff[400]<<8))+"mcu-version="+(verBf[0]|(verBf[1]<<8)));
		if((verBf[0]|(verBf[1]<<8)) != (mbuff[401]|(mbuff[400]<<8)) && (verBf[2]|(verBf[3]<<8)) == (mbuff[403]|(mbuff[402]<<8))){
			return true;
		}
		return false;
	}

	/********************* 擦除MCU数据 **********************/
	public int wipeMcuAPP() {
		int cmd ;
		cmd = jniWipeMcuAPP();	
		return cmd;
	}

	/********************* 将升级文件拷贝到内存buffer中 **********************/
	public void cpyfile() {
		try {
			FileInputStream inStream = new FileInputStream(path);
			mbuff = new byte[inStream.available()]; 				// mbuff长度为文件的大小
			fileLen = inStream.available();
			//Log.d(TAG, "fileLen = " + fileLen);
			inStream.read(mbuff);									// 读入流,保存在byte数组
			//判断文件大小是否为128的整数倍
			//若是则将其拷贝给另一个数组，若不是则将其拷贝给另一个数组并将剩下的位补为0XFF直至可以被128整除
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
	
	/********************* 将Buffer传送给JNI 每次128字节 更新MCU数据 并校验 **********************/
	public boolean checkdBuffer() {
		Log.d(TAG, "checkBuffer()");
		loop = newbuff.length / 128;		//循环次数
		for (int i = 0; i < loop; i++) {
				System.arraycopy(newbuff, 128 * i, partBuff, 0, 128);
			if (DEBUG)
				Log.d(TAG, "partBuff" + i + " :  w_datelen = "
						+ (i * 128 + 128) + " partBuff.lenght = " + partBuff.length);
			if (jniUpdateMcu(partBuff, i) == Update_OK) { // 判断每次数据写入是否成功
				if (DEBUG)
					Log.d(TAG, "Update_OK!!!");

				rebuff = jnirequestMcuFlash(i); // 接收请求获取MCU返回的数据
				int len = rebuff.length; // 返回数据的长度
				if (DEBUG)
					Log.d(TAG, " rebuff.length = " + len);
				boolean flag = Arrays.equals(partBuff, rebuff);// 校验写入的数据和返回的数据
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
	
	/******************************************重启MCU**********************************************/
	public void rebootMcu() {
		jniRebootMcu();
	}
	
	/******************************************删除升级文件******************************************/
	public void delMcuFile() {
		if (path.exists()) {			
			path.delete();
			Log.d(TAG, "The updatemcu.bin is del!!!");
		}
	}
	
	/******************************************倒车是否静音******************************************/
	public void setAsternMute(boolean Mute){
		if(Mute){
			Log.d(TAG, "Mutecheckbox is true");
			jniAsternMute(MUTE);
		}else{
			Log.d(TAG, "Mutecheckbox is false");
			jniAsternMute(NO_MUTE);
		}
	}
	
	/******************************************倒车摄像头******************************************/
	public void setRearviewCamera(boolean Camera){
		if(Camera){
			Log.d(TAG, "Cameracheckbox is true");
			jnirearviewCamera(CAMERA);
		}else{
			Log.d(TAG, "Cameracheckbox is false");
			jnirearviewCamera(NO_CAMERA);
		}
	}
	
	/******************************************获取背光值******************************************/
	public void getBrightness(){
		int getBrightness;
		getBrightness = jnigetBrightness();
		Log.d(TAG, "getBrightness is " + getBrightness);
	}
	
	/******************************************设置背光值******************************************/
	public void setBrightness(int brightness){
		jnisetBrightness(brightness);
	}
	
	/******************************************设置背光值百分比******************************************/
	public void lowBrightness(int low){
		jnilowBrigthness(low);
	}
	
	/******************************************设置自动音量百分比******************************************/
	public void volumePercent(int percent){
		jniautoVolume(percent);
	}

    public void showAndroid(boolean isShowAndroid){
        jniControlScreen(isShowAndroid);
    }
	
	//remove by myu 2014-7-25:
	//intent-filter BroadcastReceiver
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("android.intent.action.KEYCODE_BONOVO_LIGHTSTATE")){            	
            	int reverseStatus = jniQueryReverse();
                Log.d(TAG, "====== reverseStatus:" + reverseStatus + "  mCheckCamera:" + mCheckCamera);
                mReverseStatus = (reverseStatus == 1);
                Intent i = new Intent("android.intent.action.KEYCODE_BONOVO_REVERSE_STATUS");
                i.putExtra("reverse_status", mReverseStatus);
                i.putExtra("camer_flag", mCheckCamera);
                mContext.sendBroadcast(i);
            }else if(intent.getAction().equals("android.intent.action.CONTROL_SCREEN")){
                boolean isShowAndroid = intent.getBooleanExtra("show_andriod", true);
                showAndroid(isShowAndroid);
            }
        }
    };
	
	  //remove by myu 2014-7-25:
    private IntentFilter getIntentFilter(){
        IntentFilter myIntentFilter = new IntentFilter("android.intent.action.KEYCODE_BONOVO_LIGHTSTATE");
        myIntentFilter.addAction("android.intent.action.CONTROL_SCREEN");
		return myIntentFilter;
	};

}
