package com.bonovo.keyeditor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class KeyService extends Service {
	
	private static final boolean DEBUG = false;
	private static final String TAG = "KeyService";
	
	private static final String USING_CONFIG_NAME = "UsingConfigs";
	private static final String TITLE_PANEL = "panel";
	private static final String TITLE_IR = "ir";
	private static final String TITLE_EXT = "external";
	public String mPath;
	public int mAddrCode; 
	public int mNewAddrCode = -1;
	
	public final int KEY_TYPE_PANEL = 0;
	public final int KEY_TYPE_IR = 1;
	public final int KEY_TYPE_EXTERNAL = 2;

	public static final int KEY_VALUE_PAIRS_NUM = 40; // pairs number of keys
	public static final int DEFAULT_IR_ADDRCODE = 255; // default address code of ir
	public static final int TAB_IR_OFFSET_DEFAULT = 40;
	public final String USING_CONFIG = "UsingCfg"; // config file
	public MappingTable mPanelTable = null;
	public MappingTable mIrTable = null;
	public MappingTable mExtTable = null;
	
	//*---------------------------------------------------------------------------*//
	
	private ServerBinder serverBinder = new ServerBinder();

	public class ServerBinder extends Binder{
		public KeyService getService(){
			return KeyService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		return serverBinder;
	}
	
	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		super.onRebind(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		if(DEBUG) Log.d(TAG, "=========== onCreate() ============");
		String dPanel = getDir("panel_configs", MODE_PRIVATE).toString();
		String dIr = getDir("ir_configs", MODE_PRIVATE).toString();
		String dExt = getDir("ext_configs", MODE_PRIVATE).toString();
		
		SharedPreferences sp = getSharedPreferences(USING_CONFIG_NAME, MODE_PRIVATE);
		String pPanel = dPanel + "/" + sp.getString(TITLE_PANEL, "panel.cfg");
		String pIr = dIr + "/" + sp.getString(TITLE_IR, "ir.cfg");
		String pExt = dExt + "/" + sp.getString(TITLE_EXT, "external.cfg");
		mPanelTable = importConfig(pPanel);
		mIrTable = importConfig(pIr);
		mExtTable = importConfig(pExt);
		
		if(mPanelTable != null){
			if(writeConfig(mPanelTable) < 0){
				Log.e(TAG, "write panel config err.");
			}
		}
		
		if(mIrTable != null){
			if(writeConfig(mIrTable) < 0){
				Log.e(TAG, "write ir config err.");
			}
		}
		
		if(mExtTable != null){
			if(writeConfig(mExtTable) < 0){
				Log.e(TAG, "write external config err.");
			}
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	/*@
	 * about file operation
	 */
	
	public void exportDefaultFile(){
		String dPanel = getDir("panel_configs", MODE_PRIVATE).toString();
		String dIr = getDir("ir_configs", MODE_PRIVATE).toString();
		String dExt = getDir("ext_configs", MODE_PRIVATE).toString();
		
		SharedPreferences sp = getSharedPreferences(USING_CONFIG_NAME, MODE_PRIVATE);
		String pPanel = dPanel + "/" + sp.getString(TITLE_PANEL, "panel.cfg");
		String pIr = dIr + "/" + sp.getString(TITLE_IR, "ir.cfg");
		String pExt = dExt + "/" + sp.getString(TITLE_EXT, "external.cfg");
		
		exportFile(pPanel, mPanelTable);
		exportFile(pIr, mIrTable);
		exportFile(pExt, mExtTable);
	}
	
	public void exportFile(String filePath, MappingTable table){
		try {
			RandomAccessFile file = new RandomAccessFile(filePath, "rw");
			file.seek(0);
			file.writeInt(table.getTableType());
			file.writeInt(table.getTableCnt());
			file.writeInt(table.getTableId());
			file.writeInt(table.getTableOffset());
			if(DEBUG) Log.d(TAG, "============ exportFile type:" + table.getTableType());
			for(int i=0; i < table.getTableCnt(); i++){
				if(DEBUG) Log.d(TAG, "funcCode:" + table.getFuncCode(i) + "    physCode:" + table.getPhysCode(i));
				file.writeInt(table.getFuncCode(i));
				file.writeInt(table.getPhysCode(i));
			}
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public MappingTable importConfig(String filePath) {
		
		File cfg = new File(filePath);
		if(!cfg.exists()){
			return null;
		}
		
		MappingTable mappingTab = null;
		try {
			RandomAccessFile file = new RandomAccessFile(cfg, "rw");
			file.seek(0);
			int type = file.readInt();
			int num = file.readInt();
			int id = file.readInt();
			int offset = file.readInt();
			Key[] keys = new Key[num];
			if(DEBUG) Log.d(TAG, "============ importConfig type:" + type);
			for(int i=0; i<num; i++){
				keys[i] = new Key(file.readInt(), file.readInt());
				if(DEBUG) Log.d(TAG, "funcCode:" + keys[i].getFuncCode() + "    physCode:" + keys[i].getPhysCode());
			}
			file.close();
			mappingTab = new MappingTable(type, num, id, keys, offset);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mappingTab;
	}
	
	/*@
	 *  about native method 
	 */
	
	public int setMode(boolean mode){
		return nativeSetMode(mode);
	}

	public OriginalKey readOriginalKey(){
		int[] buff = new int[2];
		int err = nativeReadKey(buff);
		if(err < 0){
			return null;
		}
		return (new OriginalKey(buff[0], buff[1])); 
	}
	
	public int writeKey(MappingTable table, int keyIdx){
		int[] tempBuf = new int[4];
		tempBuf[0] = table.getTableId();
		tempBuf[1] = table.getTableType();
		tempBuf[2] = table.mTable[keyIdx].getPhysCode();
		tempBuf[3] = table.mTable[keyIdx].getFuncCode();
		return nativeWriteKey(tempBuf);
	}

	public int readAddrCode(int type){
		int key_type = type;
		return nativeReadAddrCode(key_type);
	}

	public int writeAddrCode(int type, int addrCode){
		int[] type_addr = new int[2];
		type_addr[0] = type;
		type_addr[1] = addrCode;
		return nativeWriteAddrCode(type_addr);
	}
	
	public int readOffset(int type){
		int key_type = type;
		return nativeReadOffset(key_type);
	}

	public int writeOffset(int type, int offset){
		int[] type_addr = new int[2];
		type_addr[0] = type;
		type_addr[1] = offset;
		return nativeWriteOffset(type_addr);
	}

	public int clearKeys(int type){
		MappingTable table;
		int err = 0;
		switch(type){
		case KEY_TYPE_PANEL:
			table = mPanelTable;
			break;
		case KEY_TYPE_IR:
			table = mIrTable;
			break;
		case KEY_TYPE_EXTERNAL:
			table = mExtTable;
			break;
		default:
			return -1;
		}
		err = nativeClearKeys(type);
		if(err == 0){
			table.clearMapping();
		}
		return err;
	}
	
	public int readConfig(MappingTable table){
		int err = -1;
		int[] keyValPairs = new int[table.getTableCnt()*2];
		keyValPairs[0] = table.getTableType();
		if((err = nativeSetMode(true)) < 0){
			return err;
		}
		
		if((err = nativeReadConfig(keyValPairs)) < 0){
			return err;
		}
		nativeSetMode(false);
		
		int[][] tmp = new int[table.getTableCnt()][2];
		for(int i=0; i<table.getTableCnt(); i++){
			tmp[i][0] = keyValPairs[2*i];
			tmp[i][1] = keyValPairs[2*i + 1];
		}
		table.rebuildMapping(tmp);
		return err;
	}

	public int writeConfig(MappingTable table){
		int err = -1;
		int offset = 0;

		if(table == null){
			return -1;
		}
		int[] tmpBuf = new int[table.getTableCnt()*2 + 2];
		tmpBuf[0] = table.getTableType();
		tmpBuf[1] = table.getTableId();
		offset = table.getTableOffset();
		for(int i=0; i<table.getTableCnt(); i++){
			tmpBuf[2*i+2] = table.mTable[i].getPhysCode();
			tmpBuf[2*i+3] = table.mTable[i].getFuncCode();
		}
		
		if((err = nativeSetMode(true)) < 0){
			nativeSetMode(false);
			Log.e(TAG, "set mode failed in writeConfig function. err:" + err);
			return err;
		}
		if((err = nativeWriteConfig(tmpBuf)) < 0){
			nativeSetMode(false);
			Log.e(TAG, "write config failed in writeConfig function. err:" + err);
			return err;
		}
		
		if((err = writeOffset(table.getTableType(), offset)) < 0){
			nativeSetMode(false);
			Log.e(TAG, "write offset error in writeConfig function. err:" + err);
			return err;
		}
		nativeSetMode(false);
		return err;
	}
	
	static {
		System.loadLibrary("bonovokeyeditor");
	}
	private native final static int nativeSetMode(boolean mode) throws IllegalStateException;
	private native final static int nativeReadKey(int[] buff) throws IllegalStateException;
	private native final static int nativeWriteKey(int[] buff) throws IllegalStateException;
	private native final static int nativeReadAddrCode(int key_type) throws IllegalStateException;
	private native final static int nativeWriteAddrCode(int[] addrCode) throws IllegalStateException;
	private native final static int nativeClearKeys(int key_type) throws IllegalStateException;
	private native final static int nativeReadConfig(int[] keyValPairs) throws IllegalStateException;
	private native final static int nativeWriteConfig(int[] keyValPairs) throws IllegalStateException;
	private native final static int nativeReadOffset(int key_type) throws IllegalStateException;
	private native final static int nativeWriteOffset(int[] offset) throws IllegalStateException;
}
