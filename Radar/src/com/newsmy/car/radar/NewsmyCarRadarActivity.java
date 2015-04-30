package com.newsmy.car.radar;

import com.android.internal.car.can.Radar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.view.MotionEvent;
import android.os.Handler;
import android.util.Log;

public class NewsmyCarRadarActivity extends Activity {

    private final static String TAG = "Radar";
    private ViewGroup mViewContainer;
    private RadarViewController mViewController;
    private Button mBackButton;
    private static boolean mIsUserKnown = false;
    private static boolean mIsShowAndroid = false;

    private Handler mHandler;
    private Runnable mExitRunnable;
    private static final int DISPLAY_DURATION = 300;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mBackButton = (Button) findViewById(R.id.button_back);
        mBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish();
                if(mHandler != null && mExitRunnable != null){
                    if(mIsShowAndroid)
                        switchScreen(false);
                    mHandler.removeCallbacks(mExitRunnable);
                    mHandler.postDelayed(mExitRunnable, DISPLAY_DURATION);
                }else{
                    finish();
                }
            }
        });
        mViewContainer = (ViewGroup) findViewById(R.id.view_container);
        mViewController = new RadarViewController(mViewContainer);
        final Intent intent = getIntent();
        updateByIntent(intent);
		notifyShow(true);

        if(!mIsUserKnown && RadarReceiver.mReverseStatus
            && RadarReceiver.mIsCamerCheck){
            switchScreen(true);
            mIsUserKnown = true;
        }
        mHandler = new Handler();
        mExitRunnable = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };
    }
	    
    @Override
    public void onDestroy() {
        super.onDestroy();
        notifyShow(false);
        if(mIsShowAndroid)
            switchScreen(false);
    }
    
    private void notifyShow(final boolean showing) {
        final Intent intent = new Intent();
        intent.setAction(Radar.ACTION_SHOW);
        intent.putExtra(Radar.EXTRA_SHOW, showing);
        sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_UP)
            switchScreen(!mIsShowAndroid);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateByIntent(intent);
    }

    private void updateByIntent(final Intent intent) {
        if (intent == null)
            return;
        final Bundle bundle = intent.getBundleExtra(Radar.BUNDLE_NAME);
        final Radar radarModel = Radar.bundle2Radar(bundle);

        if (ifSafe(radarModel))
            finish();
        else
            mViewController.updateViews(radarModel);
    }

    private void switchScreen(boolean showAndroid){
        if(RadarReceiver.mReverseStatus && RadarReceiver.mIsCamerCheck){
            mIsShowAndroid = showAndroid;
            Intent intent = new Intent("android.intent.action.CONTROL_SCREEN");
            intent.putExtra("show_andriod", showAndroid);
            sendBroadcast(intent);
        }
    }

    private boolean ifSafe(final Radar radarModel) {
        return (radarModel.getDistanceHeadstockCentreLeft() == 0
                && radarModel.getDistanceHeadstockCentreRight() == 0
                && radarModel.getDistanceHeadstockLeft() == 0
                && radarModel.getDistanceHeadstockRight() == 0
                && radarModel.getDistanceTailstockCentreLeft() == 0
                && radarModel.getDistanceTailstockCentreRight() == 0
                && radarModel.getDistanceTailstockLeft() == 0
                && radarModel.getDistanceTailstockRight() == 0);
    }
}
