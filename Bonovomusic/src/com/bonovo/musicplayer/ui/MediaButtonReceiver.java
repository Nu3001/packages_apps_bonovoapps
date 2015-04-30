package com.bonovo.musicplayer.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

import com.bonovo.musicplayer.BonovoAudioPlayerService;
import com.bonovo.musicplayer.IMusicPlayerService;

/**
 * Created by zybo on 11/13/14.
 */
public class MediaButtonReceiver extends BroadcastReceiver {
    private static final boolean D = true;
    private static final String TAG = "MediaButtonReceiver";
    private static long mLastClickTime = 0;
    private static boolean mDown = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (D)
            Log.d(TAG, "on receive media button!");
        final String action = intent.getAction();
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
            final Intent serviceIntent = new Intent(context, BonovoAudioPlayerService.class);
            serviceIntent.setAction(IMusicPlayerService.SERVICE_COMMAND_STOP);
            context.startService(serviceIntent);
        } else if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
            final KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (keyEvent == null)
                return;
            final int keyCode = keyEvent.getKeyCode();
            final int keyAction = keyEvent.getAction();
            final long eventTime = keyEvent.getEventTime();

            String command = null;
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    command = IMusicPlayerService.SERVICE_COMMAND_PLAY;
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    command = IMusicPlayerService.SERVICE_COMMAND_NEXT;
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    command = IMusicPlayerService.SERVICE_COMMAND_PRE;
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    command = IMusicPlayerService.SERVICE_COMMAND_STOP;
                    break;
            }

            if (command != null) {
                if (keyAction == KeyEvent.ACTION_DOWN) {
                    if (mDown) {
                        if (D)
                            Log.d(TAG, "pressed!");
                    } else if (keyEvent.getRepeatCount() == 0) {
                        Intent i = new Intent(context, BonovoAudioPlayerService.class);
                        i.setAction(command);
                        context.startService(i);
                        mDown = true;
                    }
                } else {
                    mDown = false;
                }
            }
        }
    }
}
