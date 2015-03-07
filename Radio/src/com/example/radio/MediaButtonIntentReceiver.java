package com.example.radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class MediaButtonIntentReceiver extends BroadcastReceiver {
	

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)){
			KeyEvent keyEvent = (KeyEvent) intent
					.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			int keyCode = keyEvent.getKeyCode();
			// 按下 / 松开 按钮
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
			default:
				break;
			}
			
			if (command != null) {
				if (keyAction == KeyEvent.ACTION_DOWN) {
					if (keyEvent.getRepeatCount() == 0) {
						if(command.equals("KEYCODE_MEDIA_NEXT")){
							Intent it = new Intent("Radio.Media_Broadcast_Next");
							context.sendBroadcast(it);
						} else if (command.equals("KEYCODE_MEDIA_PREVIOUS")) {
							Intent it = new Intent("Radio.Media_Broadcast_Last");
							context.sendBroadcast(it);
					}
				} else {
	
					}
				}
			}
			if (isOrderedBroadcast()) {
                abortBroadcast();
            }
		}else if (intent.getAction().equals("android.intent.action.BONOVO_RADIO_KEY")) {
			//through the steering wheel control to start the BonovoRadio
			Intent intent2 = new Intent();
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent2.setAction("com.example.radio.setup");
			context.startActivity(intent2);
		}
	}

}
