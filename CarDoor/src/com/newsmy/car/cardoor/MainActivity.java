
package com.newsmy.car.cardoor;

import com.android.internal.car.can.CarDoor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
    private CarDoorController mController;
    private BroadcastReceiver receiver;
    
    public MainActivity() {
        receiver = new BroadcastReceiver(this) {
			@Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("android.intent.action.BONOVO_SLEEP_KEY")) {
                    finish();
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mController = new CarDoorController();
        mController.init(findViewById(R.id.view_container));
        updateByBundle(getIntent().getBundleExtra(CarDoor.BUNDLE_NAME));
		registerReceiver(receiver, getIntentFilter());
    }

    //@Override
    //protected void onResume() {
    //    super.onResume();
        //mController.init(findViewById(R.id.view_container));
    //}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateByBundle(intent.getBundleExtra(CarDoor.BUNDLE_NAME));
    }
		
	@Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void updateByBundle(final Bundle bundle) {
        if (bundle == null) {
            finish();
            return;
        }
        final CarDoor carDoor = CarDoor.bundleToCarDoor(bundle);
        mController.updateStatus(carDoor);
        if (mController.isAllClosed())
            finish();
        else
            mController.updateViews();
    }

    private IntentFilter getIntentFilter() {
        IntentFilter myIntentFilter = new IntentFilter("android.intent.action.BONOVO_SLEEP_KEY");
        return myIntentFilter;
    }
}
