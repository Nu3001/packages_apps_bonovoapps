package com.example.radio;


public interface RadioInterface {
	public static final int RADIO_FM1 = 0;
	public static final int RADIO_FM2 = 1;
	public static final int RADIO_AM = 2;
	public static final int RADIO_COLLECT = 3;
    public static final int RADIO_PAGE_COUNT = 48;
    public static final int RADIO_FM_START = 0;
    public static final int RADIO_AM_START = 48;
    public static final int RADIO_COLLECT_START = 96;
    public static final int RADIO_CHANNEL_COUNT = 144;

	public void clearAllContent();
	public int fineLeft(int freq);
	public int fineRight(int freq);
	public int stepLeft(int freq);
	public int stepRight(int freq);
	public void setFreq(int freq);
    public void onAutoSearch();
    
	int getCurChannelId();
    void setCurChannelId(int id);
	void setFunctionId(int id);
	int getFunctionId();
	void setCurrentFreq(int freq);
	int getCurrentFreq();

	interface RadioStatusChangeListener {
	      public void onStatusChange(int type);
	}
	void registStatusListener(final RadioStatusChangeListener listener);

	void unRegistStatusListener();

}
