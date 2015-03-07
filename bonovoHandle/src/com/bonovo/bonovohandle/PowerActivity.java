package com.bonovo.bonovohandle;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;
import android.util.Log;
import android.content.Intent;

public class PowerActivity extends Activity {

	private final static String TAG = "BonovoPower";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_power);

		Intent intent = new Intent("android.intent.action.BONOVO_SEND_POWER_KEY");
	    sendBroadcast(intent);
        finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.power, menu);
		return true;
	}
}
