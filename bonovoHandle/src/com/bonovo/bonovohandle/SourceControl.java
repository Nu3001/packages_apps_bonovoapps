package com.bonovo.bonovohandle;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RadioGroup;
import android.content.Intent;

public class SourceControl extends Activity implements RadioGroup.OnCheckedChangeListener {

    private final static String TAG = "SourceControl";
    private RadioGroup mAudioGroup;
    private RadioGroup mVideoGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sourcecontrol);
        mAudioGroup = (RadioGroup)findViewById(R.id.audioGroup);
        mVideoGroup = (RadioGroup)findViewById(R.id.videoGroup);
        mAudioGroup.setOnCheckedChangeListener(this);
        mVideoGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int source = 0;
        int viewId = group.getId();
        Intent sourceIntent = null;
        switch (viewId) {
            case R.id.audioGroup:
                source = mAudioGroup.indexOfChild(findViewById(checkedId));
                sourceIntent = new Intent("android.intent.action.BONOVO_SET_AUDIO_CHANNEL");
                sourceIntent.putExtra("channel", source);
                break;
            case R.id.videoGroup:
                source = mVideoGroup.indexOfChild(findViewById(checkedId));
                sourceIntent = new Intent("android.intent.action.BONOVO_SET_VIDEO_CHANNEL");
                sourceIntent.putExtra("channel", source);
                break;
        }
        if (sourceIntent != null) {
            sendBroadcast(sourceIntent);
        }
    }
}