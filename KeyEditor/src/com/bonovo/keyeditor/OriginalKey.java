package com.bonovo.keyeditor;

public class OriginalKey {
	
	private int mAddrCode; // address code
	private int mScanCode; // key's code
	
	OriginalKey(int addrCode, int scanCode) {
		// TODO Auto-generated constructor stub
		mAddrCode = addrCode;
		mScanCode = scanCode;
	}
	
	public int getAddrCode(){
		return mAddrCode;
	}
	
	public int getScanCode(){
		return mScanCode;
	}
}
