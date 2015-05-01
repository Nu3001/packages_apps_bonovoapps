package com.bonovo.screensaver;

import android.os.Bundle;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.view.Menu;

public class ScreenSaverActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen_saver);
		
		ComponentName componetName = new ComponentName( "com.android.deskclock",  
                "com.android.deskclock.ScreensaverActivity");  
         
	    Intent intent = new Intent();  
	    intent.setComponent(componetName);  
	    startActivity(intent);  
	    
	    finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.screen_saver, menu);
		return true;
	}

}
