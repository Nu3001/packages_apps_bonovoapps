/*@
 * *************************************************************************
 *  The method is an config of keys
 *  ------------------------------------------------------------------------
 *  @filename: MappingTable.java
 *  @author: zbiao
 *  @date:2014-05-07
 * *************************************************************************
 */
package com.bonovo.keyeditor;

public class MappingTable {
	
	private static final int OFFSET = 0; // default offset
	private int mType; // table type
	private int mKeysCnt; // keys number
	private int mId; // table's id
	private int mOffSet; // offset
	public Key[] mTable; // table of keys
    
	MappingTable(int tabType){
		this(tabType, 40);
    }
    
	MappingTable(int tabType, int keysCnt){
		this(tabType, keysCnt, 0);
    }
	
	MappingTable(int tabType, int keysCnt, int tabId){
		this(tabType, keysCnt, tabId, OFFSET);
	}

	MappingTable(int tabType, int keysCnt, int tabId, int offset){
        mType = tabType;
        mId = tabId;
        mKeysCnt = keysCnt;
        mOffSet = offset;
        mTable = new Key[mKeysCnt];
        for(int i=0; i< mKeysCnt; i++){
            mTable[i] = new Key();
        }
    }
    
	MappingTable(int tabType, int keysCnt, int tabId, int tab[][]){
		this(tabType, keysCnt, tabId, tab, OFFSET);
	}
	
    /*@
     * mTable's max lenght is keysCnt.
     * tab array should be tab[n][2]
     * if keysCnt > tab.length, mTable[i(>tab.length)]{mPhysCode = mFuncCode = 0}
     * if keysCnt < tab.length, tab[i(>keysCnt)][] will be unused.
     */
	MappingTable(int tabType, int keysCnt, int tabId, int tab[][], int offset){
        mType = tabType;
        mId = tabId;
        mKeysCnt = keysCnt;
        mOffSet = offset;
        mTable = new Key[mKeysCnt];
        
        int maxLen = (mKeysCnt <= tab.length) ? mKeysCnt : tab.length;
        for(int i=0; i< maxLen; i++){
            mTable[i] = new Key(tab[i][0], tab[i][1]);
        }
        
        if(maxLen < mKeysCnt){
            for(int i=maxLen; i<mKeysCnt; i++){
                mTable[i] = new Key();
            }
        }
    }
	
	MappingTable(int tabType, int keysCnt, int tabId, Key[] keys){
		this(tabType, keysCnt, tabId, keys, OFFSET);
	}
    
    /*@
     * mTable's max lenght is keysCnt.
     * tab array should be tab[n][2]
     * if keysCnt > keys.length, mTable[i(>keys.length)]{mPhysCode = mFuncCode = 0}
     * if keysCnt < keys.length, keys[i(>keysCnt)][] will be unused.
     */
	MappingTable(int tabType, int keysCnt, int tabId, Key[] keys, int offset){
        mType = tabType;
        mId = tabId;
        mKeysCnt = keysCnt;
        mOffSet = offset;
        mTable = new Key[mKeysCnt];
        
        int maxLen = 0;
        if(keys != null){
        	maxLen = (mKeysCnt <= keys.length) ? mKeysCnt : keys.length;
        }
        
        for(int i=0; i< maxLen; i++){
            mTable[i] = new Key(keys[i].getFuncCode(), keys[i].getPhysCode());
        }
        
        if(maxLen < mKeysCnt){
            for(int i=maxLen; i<mKeysCnt; i++){
                mTable[i] = new Key();
            }
        }
    }
	
	public int rebuildMapping(int tab[][]){
		return rebuildMapping(mType, mKeysCnt, mId, tab);
	}
	
	public int rebuildMapping(int tabType, int keysCnt, int tabId, int tab[][]){
		mType = tabType;
        mId = tabId;
        mKeysCnt = keysCnt;
        
        int maxLen = (mKeysCnt <= tab.length) ? mKeysCnt : tab.length;
        for(int i=0; i< maxLen; i++){
            mTable[i].setKey(tab[i][0], tab[i][1]);
        }
        
        if(maxLen < mKeysCnt){
            for(int i=maxLen; i<mKeysCnt; i++){
                mTable[i] = new Key();
            }
        }
		return 0;
	}
    
	public void setTableType(int type){
		mType = type;
	}
	
    public int getTableType(){
        return mType;
    }
    
    public void setTableCnt(int cnt){
    	mKeysCnt = cnt;
    }
    
    public int getTableCnt(){
        return mKeysCnt;
    }
    
    public void setTableId(int id){
    	mId = id;
    }
    
    public int getTableId(){
        return mId;
    }
    
    public void setTableOffset(int offset){
    	mOffSet = offset;
    }
    
    public int getTableOffset(){
    	return mOffSet;
    }
    
    public int[] getMappingTable(){
    	int[] mapping = new int[mKeysCnt * 2];
    	for(int i=0; i<mKeysCnt; i++){
    		mapping[i+i] = mTable[i].getFuncCode();
    		mapping[i+i+1] =  mTable[i].getPhysCode();
    	}
    	return mapping;
    }
    
    public int[] getPhysArray(){
    	int[] temp = new int[mKeysCnt];
    	for(int i=0; i<mKeysCnt; i++){
    		temp[i] = mTable[i].getPhysCode();
    	}
    	return temp;
    }
    
    public int[] getFuncArray(){
    	int[] temp = new int[mKeysCnt];
    	for(int i=0; i<mKeysCnt; i++){
    		temp[i] = mTable[i].getFuncCode();
    	}
    	return temp;
    }
    
    public boolean setPhysCodeAccordFuncCode(int funcCode, int physCode){
    	boolean ret = false;
    	for(int i=0; i<mKeysCnt; i++){
    		if(mTable[i].getFuncCode() == funcCode){
    			mTable[i].setPhysCode(physCode);
    			ret = true;
    		}
    	}
    	return ret;
    }
    
    public boolean setFuncCodeAccordPhysCode(int funcCode, int physCode){
    	boolean ret = false;
    	for(int i=0; i<mKeysCnt; i++){
    		if(mTable[i].getPhysCode() == physCode){
    			mTable[i].setFuncCode(funcCode);
    			ret = true;
    		}
    	}
    	return ret;
    }
    
    public boolean setPhysCode(int index, int physCode){
    	boolean ret = false;
    	if((index < mKeysCnt) && (index >= 0)){
    		mTable[index].setPhysCode(physCode);
    		ret = true;
    	}
    	return ret;
    }
    
    public boolean setFuncCode(int index, int funcCode){
    	boolean ret = false;
    	if((index < mKeysCnt) && (index >= 0)){
    		mTable[index].setFuncCode(funcCode);
    		ret = true;
    	}
    	return ret;
    }
    
    public int getFuncCode(int index){
    	int ret = -1;
    	if((index < mKeysCnt) && (index >= 0)){
    		ret = mTable[index].getFuncCode();
    	}
    	return ret;
    }
    
    public int getPhysCode(int index){
    	int ret = -1;
    	if((index < mKeysCnt) && (index >= 0)){
    		ret = mTable[index].getPhysCode();
    	}
    	return ret;
    }
    
    public int searchByPhysCode(int physCode){
    	int ret = -1;
    	for(int i=0; i<mKeysCnt; i++){
    		if((physCode >= mTable[i].getPhysCode() - mOffSet) &&
    				(physCode <= mTable[i].getPhysCode() + mOffSet)){
    			ret = i;
    			break;
    		}
    	}
    	return ret;
    }
    
    public int searchByFuncCode(int funcCode){
    	int ret = -1;
    	for(int i=0; i<mKeysCnt; i++){
    		if(mTable[i].getFuncCode() == funcCode){
    			ret = i;
    			break;
    		}
    	}
    	return ret;
    }
    
    public boolean clearTable(){
    	mType = 0;
        mId = 0;
        mKeysCnt = 0;
    	for(int i=0; i<mKeysCnt; i++){
    		//mTable[i].setFuncCode(-1);
    		mTable[i].setPhysCode(-1);
    	}
    	return true;
    }
    
    public boolean clearMapping(){
    	for(int i=0; i<mKeysCnt; i++){
    		//mTable[i].setFuncCode(-1);
    		mTable[i].setPhysCode(-1);
    	}
    	return true;
    }
}
