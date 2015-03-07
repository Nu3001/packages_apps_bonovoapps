package com.bonovo.keyeditor;

/*@
 * Key class
 */
public class Key {
    private int mPhysCode; // key's physical value
    private int mFuncCode; // key's function code in android
    
    Key(){
        mPhysCode = -1;
        mFuncCode = -1;
    }
    
    Key(int funcCode, int physCode){
        mPhysCode = physCode;
        mFuncCode = funcCode;
    }
    
    public void setKey(int funcCode, int physCode){
    	mPhysCode = physCode;
        mFuncCode = funcCode;
    }
    
    public void setPhysCode(int physCode){
        mPhysCode = physCode;
    }
    
    public int getPhysCode(){
        return mPhysCode;
    }
    
    public void setFuncCode(int funcCode){
        mFuncCode = funcCode;
    }
    
    public int getFuncCode(){
        return mFuncCode;
    }
}
