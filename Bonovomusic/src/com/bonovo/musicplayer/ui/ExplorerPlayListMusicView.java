package com.bonovo.musicplayer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bonovo.musicplayer.R;

/**
 * Created by zybo on 9/25/14.
 */
public class ExplorerPlayListMusicView extends LinearLayout {
    private static final String TAG = "ExplorerPlayListMusicView";
    private static final boolean D = true;
    private static final int MESSAGE_SHOW_DELETE_DIALOG = 101;
    private OnMusicClickListener mOnMusicClickListener;
    private TextView mTitleText;
    private TextView mArtistText;
    private TextView mAlbumText;
    private ImageButton mDeleteButton;
    private OnClickListener mOnClickListener;
    private Handler mHandler;

    public ExplorerPlayListMusicView(Context context) {
        super(context);
    }

    public ExplorerPlayListMusicView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExplorerPlayListMusicView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDeleteButton = (ImageButton) findViewById(R.id.explorer_play_list_music_delete);
        mOnClickListener = new DeleteOnClickListener();
        mDeleteButton.setOnClickListener(mOnClickListener);
        mHandler = new H();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (D)
            Log.d(TAG, "onInterceptTouchEvent, " + ev.getActionMasked());
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            setBackgroundResource(R.drawable.explorer_play_list_item_d);
            Log.d(TAG, "action down");
        }
        if (action == MotionEvent.ACTION_UP) {
            setBackgroundResource(android.R.color.transparent);
            Log.d(TAG, "action up");
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            setBackgroundResource(android.R.color.transparent);
            Log.d(TAG, "action cancel");
        }
        return true;
    }

    public void setOnMusicClickListener(final OnMusicClickListener listener) {
        mOnMusicClickListener = listener;
    }

    public interface OnMusicClickListener {
        public void onMusicSelected(String title, int position);

        public void onMusicDeleted(String title, int position);
    }

    private class DeleteOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (D)
                Log.d(TAG, "delete button pressed!");
            mHandler.sendEmptyMessage(MESSAGE_SHOW_DELETE_DIALOG);
        }
    }

    private class H extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_DELETE_DIALOG:
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage("Delete?")
                            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (D)
                                        Log.d(TAG, "sure to delete!");
                                }
                            }).setNegativeButton("No,", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (D)
                                        Log.d(TAG, "cancel to delete!");
                                }
                    }).create().show();
                    break;
            }
        }
    }
}
