package com.example.radio;


public interface RadioInterface {
	public static final int RADIO_FM1 = 0;
	public static final int RADIO_FM2 = 1;
	public static final int RADIO_AM = 2;
	public static final int RADIO_COLLECT = 3;

	int FUNCTION_FINE_TUNE = 0;
	int FUNCTION_STEP_SEEK = 1;
	int FUNCTION_SCAN = 2;

	//��JNI�йصĺ���
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
	void clearAllContent();

	//����ӿ�
	interface RadioStatusChangeListener {
	      public void onStatusChange(int type);
	}
	void registStatusListener(final RadioStatusChangeListener listener);

	void unRegistStatusListener();

}
