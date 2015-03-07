package com.bonovo.switchce;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

public class SwitchCEActivity extends Activity {

	static {
        System.loadLibrary("bonovoswitch");
  	}
	
	private native final void jniopenserial() throws IllegalStateException;
	private native final void jnigoWinCE() throws IllegalStateException;
	private native final void jnicloseserial() throws IllegalStateException;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_ce);
        
        jniopenserial();
        try
	    {
	    	Thread.sleep(5);//ms
	    }
	    catch(Exception e){}
        jnigoWinCE();
        try
	    {
	    	Thread.sleep(5);//ms
	    }
	    catch(Exception e){}
		jnicloseserial();
	    finish();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_switch_ce, menu);
        return true;
    }

    
}
