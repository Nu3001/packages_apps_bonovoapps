package com.bonovo.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class BonovoBlueToothReceiver extends BroadcastReceiver {
	private static final String TAG = "BonovoBlueToothReceiver";
	private static final boolean DEBUG = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(DEBUG) Log.d(TAG, "action=" + intent.getAction());
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
		    || intent.getAction().equals("android.intent.action.PRE_LAUNCHER")){
			Intent intent_service = new Intent(context, BonovoBlueToothService.class);
			intent_service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(intent_service);
		}else if(intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)){
            KeyEvent keyEvent = (KeyEvent) intent
                            .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            int keyCode = keyEvent.getKeyCode();// 按下 / 松开 按钮
            int keyAction = keyEvent.getAction() ;
            // 获得事件的时间
            // long downtime = keyEvent.getEventTime();
            String command = null;
            switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_NEXT: 
                    command = "KEYCODE_MEDIA_NEXT";
                    break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    command = "KEYCODE_MEDIA_PREVIOUS";
                    break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: 
	                command = "KEYCODE_MEDIA_PLAY_PAUSE";
	                break;
            default:
                    break;
            }
            if (command != null) {
                    if (keyAction == KeyEvent.ACTION_DOWN) {
                            if (keyEvent.getRepeatCount() == 0) {
                                    if(command.equals("KEYCODE_MEDIA_NEXT")){
                                            Intent it = new Intent("BlueTooth.Media_Broadcast_Next");
                                            context.sendBroadcast(it);
                                    } else if (command.equals("KEYCODE_MEDIA_PREVIOUS")) {
                                            Intent it = new Intent("BlueTooth.Media_Broadcast_Last");
                                            context.sendBroadcast(it);
                                    } else if (command.equals("KEYCODE_MEDIA_PLAY_PAUSE")) {
                                        Intent it = new Intent("BlueTooth.Media_Broadcast_Play_Pause");
                                        context.sendBroadcast(it);
                                    } 
                            } else {

                            }
                    }
            }
            if (isOrderedBroadcast()) {
            	abortBroadcast();
            }
    
		}
	}

}
