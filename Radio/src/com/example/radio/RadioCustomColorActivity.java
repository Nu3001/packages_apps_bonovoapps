package com.example.radio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class RadioCustomColorActivity extends Activity implements View.OnClickListener, ServiceConnection {

    View topPanel, midPanel, bottomPanel;
    Button save;

    Bitmap bmap1, bmap2, bmap3;
    ColorFilter colorFilter;

    int alphaValue, redValue, greenValue, blueValue, brightness, contrast, saturation, hue;

    SeekBar redBar, greenBar, blueBar, alphaBar, brightBar, contrastBar, satBar, hueBar;
    TextView redText;
    TextView greenText;
    TextView blueText;
    TextView alphaText;
    TextView argbText;

    private final static String TAG = "RadioCustomColor";
    private static final boolean DEBUG = false;

    private RadioService mService;

    private Handler mViewEventHandler = new Handler() {
        public void handleMessage(final Message msg) {
            switch (msg.what) {

                case R.id.btnColorSave:
                    mService.setCustomColors(alphaValue, redValue, greenValue, blueValue,
                            brightness, contrast, saturation, hue);
                    if (DEBUG) Log.d(TAG, "RadioCustomColorActivity finish()");
                    finish();
                    break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.radio_custom_color);

        topPanel = findViewById(R.id.topPanel);
        midPanel = findViewById(R.id.midPanel);
        bottomPanel = findViewById(R.id.bottomPanel);
        bmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.top_bg_custom);
        bmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.buttonbar_bg_custom);
        bmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.bottom_bg_custom);

        redBar = (SeekBar)findViewById(R.id.redbar);
        greenBar = (SeekBar)findViewById(R.id.greenbar);
        blueBar = (SeekBar)findViewById(R.id.bluebar);
        alphaBar = (SeekBar)findViewById(R.id.alphabar);
        brightBar = (SeekBar)findViewById(R.id.brightness);
        contrastBar = (SeekBar)findViewById(R.id.contrast);
        satBar = (SeekBar)findViewById(R.id.saturation);
        hueBar = (SeekBar)findViewById(R.id.hue);

        //redText = (TextView)findViewById(R.id.red);
        //greenText = (TextView)findViewById(R.id.green);
        //blueText = (TextView)findViewById(R.id.blue);
        //alphaText = (TextView)findViewById(R.id.alpha);
        //argbText = (TextView)findViewById(R.id.argb);

        save = (Button) findViewById(R.id.btnColorSave);
        save.setOnClickListener(this);

        redBar.setOnSeekBarChangeListener(colorBarChangeListener);
        greenBar.setOnSeekBarChangeListener(colorBarChangeListener);
        blueBar.setOnSeekBarChangeListener(colorBarChangeListener);
        alphaBar.setOnSeekBarChangeListener(colorBarChangeListener);
        brightBar.setOnSeekBarChangeListener(colorBarChangeListener);
        contrastBar.setOnSeekBarChangeListener(colorBarChangeListener);
        satBar.setOnSeekBarChangeListener(colorBarChangeListener);
        hueBar.setOnSeekBarChangeListener(colorBarChangeListener);

        getSavedColors();
        setColorFilter();

        // bind a service with RadioService
        this.bindService(new Intent("com.example.radio.RadioService"), this, BIND_AUTO_CREATE);
    }

    SeekBar.OnSeekBarChangeListener colorBarChangeListener
            = new SeekBar.OnSeekBarChangeListener(){

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            setColorFilter();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void getSavedColors() {
        alphaBar.setProgress(mService.colors[0]);
        redBar.setProgress(mService.colors[1]);
        greenBar.setProgress(mService.colors[2]);
        blueBar.setProgress(mService.colors[3]);
        brightBar.setProgress(mService.colorFilters[0]);
        contrastBar.setProgress(mService.colorFilters[1]);
        satBar.setProgress(mService.colorFilters[2]);
        hueBar.setProgress(mService.colorFilters[3]);

        if (alphaBar.getProgress() == 0) {
            alphaBar.setProgress(255);
        }
    }

    private void setColorFilter(){

        alphaValue = (alphaBar.getProgress());
        redValue = (redBar.getProgress());
        greenValue = (greenBar.getProgress());
        blueValue = (blueBar.getProgress());
        int argb = Color.argb(alphaValue, redValue, greenValue, blueValue);

        //redText.setText(Integer.toString(redValue));
        //greenText.setText(Integer.toString(greenValue));
        //blueText.setText(Integer.toString(blueValue));
        //alphaText.setText(Integer.toString(alphaValue));
        //argbText.setText(Integer.toHexString(argb));

        brightness = (brightBar.getProgress());
        contrast = (contrastBar.getProgress());
        saturation = (satBar.getProgress());
        hue = (hueBar.getProgress());
        colorFilter = ColorFilterGenerator.adjustColor(brightness,contrast, saturation, hue);

        topPanel.setBackground(changeColor(bmap1, argb));
        midPanel.setBackground(changeColor(bmap2, argb));
        bottomPanel.setBackground(changeColor(bmap3, argb));
    }

    private Drawable changeColor(Bitmap bmap, int color) {

        Canvas canvas = new Canvas();
        Bitmap result = Bitmap.createBitmap(bmap.getWidth(), bmap.getHeight(), Bitmap.Config.ARGB_8888);

        canvas.setBitmap(result);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);

        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        canvas.drawBitmap(bmap, 0, 0, paint);
        paint.setColorFilter(null);

        paint.setColorFilter(colorFilter);
        canvas.drawBitmap(result, 0, 0, paint);
        paint.setColorFilter(null);

        return new BitmapDrawable(getResources(), result);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        if (mViewEventHandler != null) {
            mViewEventHandler.sendEmptyMessage(id);
        }

    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() { /*BroadcastReceiver*/
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Message msg = mViewEventHandler
                    .obtainMessage(R.id.import_button_close);
            if (msg != null) {
                mViewEventHandler.sendMessage(msg);
            }
        }
    };

    private IntentFilter getIntentFilter() { /* עBroadcastReceiver */
        return new IntentFilter(RadioService.MSG_CLOSE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // TODO Auto-generated method stub
        mService = ((RadioService.ServiceBinder) service).getService();
        this.registerReceiver(myReceiver, getIntentFilter());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // TODO Auto-generated method stub
        mService = null;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mService = null;
        this.unregisterReceiver(myReceiver);
        this.unbindService(this);
    }

}