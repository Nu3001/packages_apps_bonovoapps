package com.bonovo.mcuupdate_and_setting;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class FragmentService extends Service {
	private final String TAG = "com.example.fragment.service";
	private static final boolean DEBUG = false;
	public static final String action = "broadcast";  
	
	public String pathName = "/mnt/external_sd/updatemcu.bin";	//外置SD卡路径
	public String pathName_inter = "/mnt/internal_sd/updatemcu.bin"; //内置储存路径
	File path = new File(pathName);
	File path_inter = new File(pathName_inter);
	
	private static final int MUTE = 1;					
	private static final int NO_MUTE = 0;						
	private static final int CAMERA = 1;					
	private static final int NO_CAMERA = 0;						
	private static final int Update_OK = 2;						//更新MCU成功
	public static int carType;									//用来保存从SharePre中读取的车型值
	public static int serialType;								//用来保存从SharePre中读取的串口选择值
	public static int volumeS8;									//用来保存从SharePre中读取的S8音量值
	private int PRESSHOST = 1;									//Host模式
	private int PRESSSLAVE = 2;									//SLAVE模式
	
	public byte mbuff[] = null;							//存放整个升级文件的内容
	public int fileLen = 0;
	public int nfilelen = 0;
	public byte[] newbuff = null;						//声明一个数组用于存放可以被128整除的数据
	public byte[] partBuff = new byte[128];				//存放往JNI里传送的128字节数据
	public byte[] rebuff = new byte[128];				//存放检验返回的128字节数据；
	public static int loop;								//128字节循环次数
	
	public int lightState;								//大灯状态标志
	public int progessBrigth;								//大灯开启背光值
	public int progessVolume;								//自动音量百分比值
	public boolean mCheckLight;								//大灯BOX选择标志
	public boolean mCheckMute;								//倒车静音BOX选择标志
	public boolean mCheckCamera;								//倒车后视BOX选择标志
	public boolean mCheckVolume;								//自动音量BOX选择标志
	public int mCheckOTG;									//OTG选择标志
	public int mCheckStandby;								// Standby time

	private native final byte[] jniCheckMcuVersion()
			throws IllegalStateException;
	private native final int jniWipeMcuAPP() throws IllegalStateException;
	private native final int jniUpdateMcu(byte[] buf, int offset)
			throws IllegalStateException;
	private native final int jniResetMcu() throws IllegalStateException;
	private native final byte[] jnirequestMcuFlash(int offset)
			throws IllegalStateException;
	private native final int jniRebootMcu() throws IllegalStateException;
	private native final int jniAsternMute(int onOff)
			throws IllegalStateException;
	private native final int jniqueryHeadlight() throws IllegalStateException;
	private native final int jnirearviewCamera(int onOff)
			throws IllegalStateException;
	private native final int jnigetBrightness() throws IllegalStateException;
	private native final int jnisetBrightness(int brightness)
			throws IllegalStateException;
	private native final int jnilowBrigthness(int brightness)
			throws IllegalStateException;
	private native final int jniautoVolume(int percent)
			throws IllegalStateException;
	private native final int jniControlScreen(boolean isShowAndroid) 
			throws IllegalStateException;
	private native final int jniQueryReverse() throws IllegalStateException;
	private native final int jniPickColor(int red, int green, int blue) 
			throws IllegalStateException;
	private native final int jniswitchOTG(int model) 
			throws IllegalStateException;
	private native final int jniSetStandby(int time) 
			throws IllegalStateException;

	static {
		System.loadLibrary("bonovomcu");
	}

	public class ServiceBinder extends Binder {
		public FragmentService getServicer() {
			return FragmentService.this;
		}
	}

	private ServiceBinder binder = new ServiceBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		Log.v(TAG, "-->Service-->onBind()");
		return binder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
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
		SharedPreferences preferencesStandby = getSharedPreferences("standby model", MODE_WORLD_READABLE);
		mCheckStandby = preferencesStandby.getInt("standby checked", 120);	// Default standby is 120 minutes (2 hours)
		
		try {
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
			if(mCheckLight){
				Log.d(TAG, "mCheckLight is true");
				jnilowBrigthness(progessBrigth + 10);
			}else {
				Log.d(TAG, "mCheckLight is false");
				jnilowBrigthness(0);
			}
			
			if(mCheckVolume){
				jniautoVolume(progessVolume + 10);
			}else{
				jniautoVolume(0);
			}
			
			if(mCheckOTG == PRESSHOST){
				switchOTG(PRESSHOST);
			}else {
				switchOTG(PRESSSLAVE);
			}
			jniSetStandby(mCheckStandby);
		
		} catch (Exception e) {
			Toast toast = Toast.makeText(this, "An error has occured.  The Android box must be connected to the headunit for this application to work.", Toast.LENGTH_LONG);
			toast.show();
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		Log.v(TAG, "-->Service-->onStart()");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.v(TAG, "-->Service-->onDestroy()");
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
	/******************************************倒车是否静音*****************************************/
	public void setAsternMute(boolean Mute){
		if(Mute){
			Log.d(TAG, "Mutecheckbox is true");
			jniAsternMute(MUTE);
		}else{
			Log.d(TAG, "Mutecheckbox is false");
			jniAsternMute(NO_MUTE);
		}
	}
	
	/******************************************倒车摄像头*******************************************/
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
	
	/******************************************设置背光值百分比************************************/
	public void lowBrightness(int low){
		jnilowBrigthness(low);
	}
	
	/******************************************设置自动音量百分比**********************************/
	public void volumePercent(int percent){
		jniautoVolume(percent);
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

	/********************* 检查SD卡是否有指定升级文件 **********************/
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
	
	/********************* 检查内置储存空间是否有指定升级文件 **********************/
	public boolean checkInterSDFile() {
		try {
			if (!path_inter.exists()) {
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
	
	/********************* 将升级文件拷贝到内存buffer中 **********************/
	public void cpyfile() {
		FileInputStream inStream = null;
		try {
			if(path.exists()){
				inStream = new FileInputStream(path);
			}else if (path_inter.exists()) {
				inStream = new FileInputStream(path_inter);
			}
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
	
	/********************* 将Buffer传送给JNI 每次128字节 更新MCU数据 并校验 并发送广播 更新进度条百分比 **********************/
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
	
	/******************************************删除升级文件******************************************/
	public void delMcuFile() {
		if (path.exists()) {			
			path.delete();
			Log.d(TAG, "The updatemcu.bin is del!!!");
		}else if (path_inter.exists()) {
			path_inter.delete();
			Log.d(TAG, "The updatemcu.bin is del!!!");
		}
	}
	
	/******************************************重启MCU**********************************************/
	public void rebootMcu() {
		jniRebootMcu();
	}
	
	/******************************************读取存放在SharePrefercen中的CarType和SerialType**********************************************/
	public void readSharePre() {
		SharedPreferences preferences = getSharedPreferences("serial_checked_result", Context.MODE_WORLD_READABLE);
		SharedPreferences preferences2 = getSharedPreferences("car_checked_result", Context.MODE_WORLD_READABLE);
		carType = preferences2.getInt("radioButton_Checked_Flag", 0);			//0:Non
		serialType = preferences.getInt("radioButton_Checked_Flag", 0);				//0:大众
//		Log.v(TAG, "11111111111111Service carType="+carType+" serialType="+serialType);
		
		Intent intentSerialType = new Intent("com.android.internal.car.can.action.SERIAL_TYPE_CHANGED");
		intentSerialType.putExtra("serial_type", serialType);
		this.sendBroadcast(intentSerialType);
		
		Intent intentCarType = new Intent("com.android.internal.car.can.action.CAR_TYPE_CHANGED");
		intentCarType.putExtra("car_type", carType);
		this.sendBroadcast(intentCarType);
	}
	
	/**选择按键背景灯颜色
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void setColor(int red, int green, int blue){
		jniPickColor(red, green, blue);
	}
	
	/**切换OTG模式
	 * @param model
	 */
	public void switchOTG(int model){
		jniswitchOTG(model);
	}
	
	/**设置待机时间
	 * @param time 分钟
	 */
	public void setStandy(int time){
		jniSetStandby(time);
	}
}
