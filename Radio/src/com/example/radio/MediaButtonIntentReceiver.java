package com.example.radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import com.radio.widget.RadioPlayerStatusStore;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

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
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				command = "KEYCODE_MEDIA_PLAY_PAUSE";
				break;
			case KeyEvent.KEYCODE_MEDIA_PLAY:
					command = "KEYCODE_MEDIA_PLAY";
			break;
			
			case KeyEvent.KEYCODE_MEDIA_PAUSE:
					command = "KEYCODE_MEDIA_PAUSE";
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
					}else if (command.equals("KEYCODE_MEDIA_PLAY_PAUSE")) {
							//Intent it = new Intent("Radio.Media_Broadcast_PLAY_PAUSE");
							//context.sendBroadcast(it);
							  ComponentName 	mRadioServiceComponent = new ComponentName("com.example.radio", "com.example.radio.RadioService");

						         RadioPlayerStatusStore mRadioPlayerStatusStore = RadioPlayerStatusStore.getInstance();
								 
						            if (mRadioPlayerStatusStore.getContext() == null)
						                  mRadioPlayerStatusStore.setContext(context);
									
							     String playStop = mRadioPlayerStatusStore.get(RadioPlayerStatusStore.KEY_PLAY_STOP);


							      if (RadioPlayerStatusStore.VALUE_PLAY.equals(playStop)) {
						                final Intent serviceIntent = new Intent();
						                serviceIntent.setComponent(mRadioServiceComponent);
						                context.stopService(serviceIntent);
						            }
						            if (RadioPlayerStatusStore.VALUE_STOP.equals(playStop) ||
						                    playStop.equals("")) {
						                final Intent serviceIntent = new Intent();
						                serviceIntent.setComponent(mRadioServiceComponent);
						                context.startService(serviceIntent);
						            }

					}
					else if (command.equals("KEYCODE_MEDIA_PLAY")){
						
							  ComponentName 	mRadioServiceComponent = new ComponentName("com.example.radio", "com.example.radio.RadioService");

						         RadioPlayerStatusStore mRadioPlayerStatusStore = RadioPlayerStatusStore.getInstance();
								 
						            if (mRadioPlayerStatusStore.getContext() == null)
						                  mRadioPlayerStatusStore.setContext(context);
									
							     String playStop = mRadioPlayerStatusStore.get(RadioPlayerStatusStore.KEY_PLAY_STOP);


						            if (RadioPlayerStatusStore.VALUE_STOP.equals(playStop) ||
						                    playStop.equals("")) {
						                final Intent serviceIntent = new Intent();
						                serviceIntent.setComponent(mRadioServiceComponent);
						                context.startService(serviceIntent);
						            }						
						
					}
					else if (command.equals("KEYCODE_MEDIA_PAUSE")){
						
							  ComponentName 	mRadioServiceComponent = new ComponentName("com.example.radio", "com.example.radio.RadioService");

						         RadioPlayerStatusStore mRadioPlayerStatusStore = RadioPlayerStatusStore.getInstance();
								 
						            if (mRadioPlayerStatusStore.getContext() == null)
						                  mRadioPlayerStatusStore.setContext(context);
									
							     String playStop = mRadioPlayerStatusStore.get(RadioPlayerStatusStore.KEY_PLAY_STOP);


							      if (RadioPlayerStatusStore.VALUE_PLAY.equals(playStop)) {
						                final Intent serviceIntent = new Intent();
						                serviceIntent.setComponent(mRadioServiceComponent);
						                context.stopService(serviceIntent);
						            }					
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
