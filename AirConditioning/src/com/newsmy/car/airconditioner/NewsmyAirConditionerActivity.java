package com.newsmy.car.airconditioner;

import com.android.internal.car.can.AirConditioning;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by zybo on 8/1/14.
 */
public class NewsmyAirConditionerActivity extends Activity {
    private static final String TAG = "NewsmyAirConditionerActivity";
    private static final boolean D = false;
    private static final int DISPLAY_DURATION = 2 * 1000;
    private static final String AIR_CONDITION_SP_NAME = "air_condition_sp";
    private AirConditionUIController mViewController;
    private Handler mHandler;
    private Runnable mExitRunnable;
    private SharedPreferences mSp;
    private AirConditioning mCurrentCondition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (D)
            Log.d(TAG, "onCreate!");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AirConditioning initAirConditioning;
        if (savedInstanceState != null) {
            final Bundle bundle = savedInstanceState.getBundle(AirConditioning.BUNDLE_NAME);
            initAirConditioning = AirConditioning.bundle2AirCondition(bundle);
        } else {
            mSp = getSharedPreferences(AIR_CONDITION_SP_NAME, MODE_PRIVATE);
            initAirConditioning = AirConditioning.restoreAirCondition(mSp);
        }
        setContentView(R.layout.main);
        mCurrentCondition = initAirConditioning;
        mViewController = new AirConditionUIController(findViewById(R.id.view_container));
        mViewController.initViews(initAirConditioning);
        updateByIntent(getIntent());
        mHandler = new Handler();
        mExitRunnable = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };
        // animation
    }

    @Override
    protected void onResume() {
        if (D)
            Log.d(TAG, "onResume");
        super.onResume();
        mHandler.removeCallbacks(mExitRunnable);
        mHandler.postDelayed(mExitRunnable, DISPLAY_DURATION);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (D)
            Log.d(TAG, "onNewIntent");
        super.onNewIntent(intent);
        updateByIntent(intent);
        mHandler.removeCallbacks(mExitRunnable);
        if (!mCurrentCondition.getAirConditioningDisplaySiwtch()){
            //Log.d(TAG, "Will shut down the air condition right now!");
            mHandler.post(mExitRunnable);
        }else{
            //Log.d(TAG, "Will shut down the air condition after 3 seconds!");
            mHandler.postDelayed(mExitRunnable, DISPLAY_DURATION);
        }
    }

    private void updateByIntent(final Intent airConditionIntent) {
        if (airConditionIntent == null)
            return;
        final Bundle bundle = airConditionIntent.getBundleExtra(AirConditioning.BUNDLE_NAME);

        final AirConditioning airConditioning = AirConditioning.bundle2AirCondition(bundle);
        // test
//        AirConditioning airConditioning = AirConditioningUtil.bundle2AirCondition(bundle);
//        airConditioning = new AirConditioning();
//        airConditioning.setREARSwitch(AirConditioning.SWITCH_ON);
//        airConditioning.setMAXFORNTSwitch(AirConditioning.SWITCH_ON);
//        airConditioning.setAUTOSoftWindSiwtch(AirConditioning.SWITCH_ON);
//        airConditioning.setLeftSeatHeatingLevel(1);
//        airConditioning.setRightSeatHeatingLevel(2);
//        airConditioning.setUpWindSwitch(true);
//        airConditioning.setHorizontalWindSwitch(true);
//        airConditioning.setDownWindSwitch(true);
        // test
        if (airConditioning == null)
            return;
        mViewController.updateViews(airConditioning);
        mCurrentCondition = airConditioning;
    }

    @Override
    protected void onDestroy() {
        if (D)
            Log.d(TAG, "onDestroy");
        super.onDestroy();
        AirConditioning.saveAirCondition(mSp, mCurrentCondition);
    }
}
