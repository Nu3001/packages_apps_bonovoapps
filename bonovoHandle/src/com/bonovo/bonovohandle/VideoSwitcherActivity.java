package com.bonovo.bonovohandle;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;
import android.util.Log;
import android.content.Intent;

public class VideoSwitcherActivity extends Activity {
	private final static String TAG = "VideoSwitcher";
	private static Boolean videoStop = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videoswitcher);

		Intent intent = getIntent();

		videoStop = intent.getExtras().getBoolean("stop");
		if (videoStop == true) {
			finish();
		}

	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		videoStop = intent.getExtras().getBoolean("stop");
		if (videoStop == true) {
			finish();
		}
	}

	@Override
	public void onPause() {
		if (videoStop == false) {
			// Switch display to Android display (0) whenever home/back is pressed
			Intent intent = new Intent("android.intent.action.BONOVO_SET_VIDEO_CHANNEL");
			intent.putExtra("channel", 0);
			intent.putExtra("videoswitcher", false);
			sendBroadcast(intent);
		}
		super.onPause();
	}
}
