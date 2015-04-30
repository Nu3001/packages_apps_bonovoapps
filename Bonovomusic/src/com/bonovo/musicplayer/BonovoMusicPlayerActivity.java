package com.bonovo.musicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bonovo.musicplayer.IMusicPlayerService.MusicStatus;
import com.bonovo.musicplayer.IMusicPlayerService.MusicStatusChangeListener;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class BonovoMusicPlayerActivity extends Activity implements
        OnClickListener, ServiceConnection, SeekBar.OnSeekBarChangeListener {
    /**
     * for debug.
     */
    private static final String TAG = "BonovoMusicPlayerActivity";
    private static final boolean D = true;

    /**
     * views.
     */
    private ImageButton mTitleButtonHome;
    private Button mTitleButtonList;
    private ImageButton mTitleButtonClose;
    private TextView mDetailTextPreMusic;
    private TextView mDetailTextNextMusic;
    private ImageView mDetailMusicImage;
    private TextView mDetailTextTitle;
    private TextView mDetailTextAlbum;
    private TextView mDetailTextSchool;
    private TextView mDetailTextArtist;
    private TextView mDetailProgressbarPosition;
    private TextView mDetailProgressbarDuration;
    private SeekBar mDetailProgressbar;
    private ImageButton mBottomButtonPrevious;
    private ImageButton mBottomButtonPlayPause;
    private ImageButton mBottomButtonNext;
    private Button mBottomButtonPlayMode;
    private Button mDetailButtonLrcToggle;
    private ImageButton mBottomButtonSetting;
    private ImageButton mBottomButtonVolumeCtl;
    private AlertDialog mVolCtrlDialog;
    private IMusicPlayerService mService;
    private String filePath;
    // 歌词显示组建
    private TextView mLrcText1;
    private TextView mLrcText2;
    private TextView mLrcText3;
    private TextView mLrcText4;
    private TextView mLrcText5;

    // @ TODO
    // private NotificationManager mNm;
    /**
     * handler message id...
     */
    private static final int UPDATE_UI = 1;
    private static final int UPDATE_POSITION = 2;
    private static final int DISMISS_VOL_CTRL_DIALOG = 3;
    private static final int SEEK_FORWARD = 4;
    private static final int SEEK_BACKWARD = 5;
    private static final int SEEK = 6;
    private static final int PLAY_FILE = 7;
    private static final int INIT = 8;
    private static final int SEEK_UNIT = 10000; // ms
    private static final int PROGRESS_MAX = 100;

    private boolean mServiceConnected = false;
    private boolean mLrcToggle = true;
    private boolean isFileFound = true;
    private Handler mHandler = new Handler() {
        private long mDuration;

        // private long mPosition;
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case R.id.player_lrc_toggle:
                    if (mLrcToggle) {
                        findViewById(R.id.player_detail_layout).setVisibility(
                                View.GONE);
                        findViewById(R.id.player_lrc).setVisibility(View.VISIBLE);
                        isFileFound = mService.parseLRC();
                        mLrcToggle = false;
                    } else {
                        findViewById(R.id.player_detail_layout).setVisibility(
                                View.VISIBLE);
                        findViewById(R.id.player_lrc).setVisibility(View.GONE);
                        mLrcToggle = true;
                    }
                    break;
                case R.id.player_title_button_home:
                    // @ TODO
                    // showNotification();
                    finish();
                    break;
                case R.id.player_title_button_close:
                    mService.stopService();
                    finish();
                    break;
                case R.id.player_title_button_list:
                    BonovoMusicPlayerUtil
                            .startListActivity(BonovoMusicPlayerActivity.this);
                    break;
                case R.id.player_bottom_button_previous:
                    mService.prev();
                    if (!mLrcToggle) {
                        isFileFound = mService.parseLRC();
                    }
                    break;
                case R.id.player_bottom_button_play_pause:
                    if (mService.isPlaying()) {
                        mBottomButtonPlayPause
                                .setBackgroundResource(R.drawable.player_bottom_button_play_selector);
                        mService.pause();
                    } else if (mService.play()) {
                        mBottomButtonPlayPause
                                .setBackgroundResource(R.drawable.player_bottom_button_pause_selector);
                    }
                    break;
                case R.id.player_bottom_button_next:
                    mService.next();
                    if (!mLrcToggle) {
                        isFileFound = mService.parseLRC();
                    }
                    break;
                case R.id.player_bottom_button_play_mode:
                    int playMode = mService.getPlayMode();
                    switch (playMode) {
                        case BonovoAudioPlayerService.MODE_ALL:
                            mService.setPlayMode(BonovoAudioPlayerService.MODE_SINGLE);
//                            mBottomButtonPlayMode
//                                    .setText(R.string.player_button_mode_single);
                            break;
                        case BonovoAudioPlayerService.MODE_SINGLE:
                            mService.setPlayMode(BonovoAudioPlayerService.MODE_RANDOM);
//                            mBottomButtonPlayMode
//                                    .setText(R.string.player_button_mode_random);
                            break;
                        case BonovoAudioPlayerService.MODE_RANDOM:
                            mService.setPlayMode(BonovoAudioPlayerService.MODE_ALL);
//                            mBottomButtonPlayMode
//                                    .setText(R.string.player_button_mode_all);
                            break;
                    }
                    break;
                case R.id.player_bottom_button_setting:
                    BonovoMusicPlayerUtil
                            .startMusicSettingActivity(BonovoMusicPlayerActivity.this);
                    break;
                case R.id.player_bottom_button_volume_ctl:
                    // @ TODO
                    // View view =
                    // LayoutInflater.from(BonovoMusicPlayerActivity.this)
                    // .inflate(R.layout.vol_ctrl_layout, null);
                    // mVolCtrlDialog = new AlertDialog.Builder(
                    // BonovoMusicPlayerActivity.this).setTitle(null).setView(
                    // view).create();
                    // mVolCtrlDialog.show();
                    // sendEmptyMessageDelayed(DISMISS_VOL_CTRL_DIALOG, 3000);
                    break;
                case UPDATE_UI:
                    if (D)
                        Log.d(TAG, "uodate UI!");
                    isFileFound = mService.parseLRC();
                    MusicStatus ms = (MusicStatus) msg.obj;
                    if (ms != null) {
                        mDetailTextPreMusic.setText(ms.preMusic);
                        mDetailTextNextMusic.setText(ms.nextMusic);
                        mDetailTextTitle.setText(ms.currMusic);
                        mDetailTextAlbum.setText(ms.currentAlbum);
                        mDetailTextSchool.setText(ms.currentGenre);
                        mDetailTextArtist.setText(ms.currentArtist);
                        mDuration = ms.duration;
                        mDetailProgressbarDuration.setText(BonovoMusicPlayerUtil
                                .makeTimeString(BonovoMusicPlayerActivity.this,
                                        mDuration / 1000));
                        if (ms.albumArt != null) {
                            Uri uri = Uri.fromFile(new File(ms.albumArt));
                            ParcelFileDescriptor pfd = null;
                            Bitmap bm = null;
                            try {
                                pfd = BonovoMusicPlayerActivity.this
                                        .getContentResolver().openFileDescriptor(
                                                uri, "r");
                                if (pfd != null) {
                                    FileDescriptor fd = pfd.getFileDescriptor();
                                    bm = BitmapFactory.decodeFileDescriptor(fd);
                                }
                                BonovoMusicPlayerUtil.setBackground(
                                        mDetailMusicImage, bm);
                            } catch (FileNotFoundException e) {
                                Log.e(TAG, "File not found : " + e);
                            }
                        } else {
                            mDetailMusicImage
                                    .setBackgroundResource(R.drawable.player_slt);
                        }
                        if (D) {
                            Log.d(TAG, "update UI!");
                            Log.d(TAG, "ms.preMusic :" + ms.preMusic);
                            Log.d(TAG, "ms.nextMusic :" + ms.nextMusic);
                            Log.d(TAG, "ms.currentAlbum :" + ms.currentAlbum);
                            Log.d(TAG, "ms.currentGenre :" + ms.currentGenre);
                            Log.d(TAG, "ms.currentArtist :" + ms.currentArtist);
                            Log.d(TAG, "ms.duration : " + ms.duration);
                            Log.d(TAG, "ms.album art : " + ms.albumArt);
                        }
                    }
                    if (mService.isPlaying()) {
                        mBottomButtonPlayPause
                                .setBackgroundResource(R.drawable.player_bottom_button_pause_selector);
                    } else {
                        mBottomButtonPlayPause
                                .setBackgroundResource(R.drawable.player_bottom_button_play_selector);
                    }
                    sendMessageDelayed(obtainMessage(UPDATE_POSITION), 1000);
                    break;
                case DISMISS_VOL_CTRL_DIALOG:
                    if (mVolCtrlDialog != null) {
                        mVolCtrlDialog.dismiss();
                        mVolCtrlDialog = null;
                    }
                    break;
                case UPDATE_POSITION:
                    if (mService != null) {
                        removeMessages(UPDATE_POSITION);
                        long position = mService.position();
                        mDetailProgressbarPosition.setText(BonovoMusicPlayerUtil
                                .makeTimeString(BonovoMusicPlayerActivity.this,
                                        position / 1000));
                        if (mDuration > 0) {
                            double persent = (double) position / (double) mDuration;
                            int progress = (int) (persent * PROGRESS_MAX);
                            mDetailProgressbar
                                    .setProgress(progress > PROGRESS_MAX ? PROGRESS_MAX
                                            : progress);
                            if (!mLrcToggle)
                                setupLRC(mService.getLRCResult());
                        }
                        sendMessageDelayed(obtainMessage(UPDATE_POSITION), 1000);
                    }
                    break;
                case SEEK_FORWARD:
                    if (mService != null) {
                        long position = mService.position();
                        if (D)
                            Log.d(TAG, " positon : " + position + ", duration : "
                                    + mDuration);
                        if (position + SEEK_UNIT > mDuration) {
                            mService.next();
                        } else {
                            mService.seek(position + SEEK_UNIT);
                        }
                    }
                    break;
                case SEEK_BACKWARD:
                    if (mService != null) {
                        long position = mService.position();
                        if (position - SEEK_UNIT < 0) {
                            mService.prev();
                        } else {
                            mService.seek(position - SEEK_UNIT);
                        }
                    }
                    break;
                case SEEK:
                    if (mService != null) {
                        int progress = msg.arg1;
                        double persent = mDuration / PROGRESS_MAX;
                        long position = (long) (persent * progress);
                        mService.seek(position);
                    }
                    break;
                case PLAY_FILE:
                    filePath = (String) msg.obj;
                    File file = new File(filePath);
                    if (filePath != null) {
                        String folder = file.getParent().toString();
                        ArrayList<String> fileNames = new ArrayList<String>();
                        fileNames.add(file.getName());
                        List<Long> ids = BonovoMusicPlayerUtil.getAudioIdsByName(
                                BonovoMusicPlayerActivity.this, folder, fileNames);
                        mService.open(ids, 0);
                        mService.play();
                        sendEmptyMessage(UPDATE_POSITION);
                    }
                    break;
                case INIT:
                    if (mServiceConnected) {
                        mService.registerStatusListener(mMusciStatusListener);
                        int mode = mService.getPlayMode();
                        if (D)
                            Log.d(TAG, "service mode : " + mode);
                        switch (mode) {
                            case BonovoAudioPlayerService.MODE_ALL:
//                                mBottomButtonPlayMode
//                                        .setText(R.string.player_button_mode_all);
                                break;
                            case BonovoAudioPlayerService.MODE_SINGLE:
//                                mBottomButtonPlayMode
//                                        .setText(R.string.player_button_mode_single);
                                break;
                            case BonovoAudioPlayerService.MODE_RANDOM:
//                                mBottomButtonPlayMode
//                                        .setText(R.string.player_button_mode_random);
                                break;
                        }
                        Intent intent = getIntent();
                        String action = intent.getAction();
                        if (Intent.ACTION_VIEW.equals(action)) {
                            Uri data = intent.getData();
                            Message playMsg = mHandler.obtainMessage(PLAY_FILE,
                                    data.getPath());
                            mHandler.sendMessage(playMsg);
                        }
                    } else {
                        sendEmptyMessageDelayed(INIT, 1000);
                    }
                    break;
                default:
            }
        }
    };

    private void setupLRC(List<String> result) {
        if (isFileFound) {
            String str = "~~~~~~~~~";
            mLrcText1.setText((result.get(0).equals("")) ? str : result.get(0));
            mLrcText2.setText((result.get(1).equals("")) ? str : result.get(1));
            mLrcText3.setText((result.get(2).equals("")) ? str : result.get(2));
            mLrcText4.setText((result.get(3).equals("")) ? str : result.get(3));
            mLrcText5.setText((result.get(4).equals("")) ? str : result.get(4));
        } else {
            mLrcText1.setText("");
            mLrcText2.setText("");
            mLrcText4.setText("");
            mLrcText5.setText("");
            mLrcText3.setText(R.string.player_lrc_file_not_found);
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (D)
            Log.d(TAG, "BonovoMusicPlayerActivity onCreate");
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.music_player_layout);
        setContentView(R.layout.music_player_main_layout);
        setupViews();
        bindService();

    }

    private void init() {
        // mNm = (NotificationManager)
        // getSystemService(Context.NOTIFICATION_SERVICE);
        mHandler.sendEmptyMessage(INIT);
    }

    @Override
    protected void onStart() {
        if (D)
            Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (D)
            Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (D)
            Log.d(TAG, "onPause");
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (D)
            Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        if (!mLrcToggle) {
            isFileFound = mService.parseLRC();
        }
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        if (D)
            Log.d(TAG, "onDestroy");
        // mNm = null;
        mService = null;
        if (mHandler != null) {
            mHandler.removeMessages(UPDATE_UI);
            mHandler = null;
        }
        BonovoMusicPlayerUtil.unbindPlayBackService(this, this);
        super.onDestroy();
    }

    private void setupViews() {
        if (D)
            Log.d(TAG, "setupViews");
        Button button = (Button) findViewById(R.id.player_action_play_pause_button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
//        findViewById(R.id.view_container).setSystemUiVisibility(8);
//        mDetailButtonLrcToggle = (Button) findViewById(R.id.player_lrc_toggle);
//        mDetailButtonLrcToggle.setOnClickListener(this);
//        mTitleButtonHome = (ImageButton) findViewById(R.id.player_title_button_home);
//        mTitleButtonHome.setOnClickListener(this);
//        mTitleButtonList = (Button) findViewById(R.id.player_title_button_list);
//        mTitleButtonList.setOnClickListener(this);
//        mTitleButtonClose = (ImageButton) findViewById(R.id.player_title_button_close);
//        mTitleButtonClose.setOnClickListener(this);
//        mDetailTextPreMusic = (TextView) findViewById(R.id.player_detail_text_pre_music_name);
//        mDetailTextNextMusic = (TextView) findViewById(R.id.player_detail_text_next_music_name);
//        mDetailMusicImage = (ImageView) findViewById(R.id.player_detail_music_image);
//        mDetailTextTitle = (TextView) findViewById(R.id.player_detail_text_music_title);
//        mDetailTextTitle.requestFocus();
//        mDetailTextAlbum = (TextView) findViewById(R.id.player_detail_text_music_album_name);
//        mDetailTextSchool = (TextView) findViewById(R.id.player_detail_text_music_school_name);
//        mDetailTextArtist = (TextView) findViewById(R.id.player_detail_text_music_artist_name);
//        mDetailProgressbarPosition = (TextView) findViewById(R.id.player_detail_progressbar_position);
//        mDetailProgressbarDuration = (TextView) findViewById(R.id.player_detail_progressbar_duration);
//        mDetailProgressbar = (SeekBar) findViewById(R.id.player_detail_progressbar);
//        mDetailProgressbar.setMax(PROGRESS_MAX);
//        mDetailProgressbar.setOnSeekBarChangeListener(this);
//        mBottomButtonPrevious = (ImageButton) findViewById(R.id.player_bottom_button_previous);
//        mBottomButtonPrevious.setOnClickListener(this);
//        mBottomButtonPlayPause = (ImageButton) findViewById(R.id.player_bottom_button_play_pause);
//        mBottomButtonPlayPause.setOnClickListener(this);
//        mBottomButtonNext = (ImageButton) findViewById(R.id.player_bottom_button_next);
//        mBottomButtonNext.setOnClickListener(this);
//        mBottomButtonPlayMode = (Button) findViewById(R.id.player_bottom_button_play_mode);
//        mBottomButtonPlayMode.setOnClickListener(this);
//        mBottomButtonSetting = (ImageButton) findViewById(R.id.player_bottom_button_setting);
//        mBottomButtonSetting.setOnClickListener(this);
//        mBottomButtonVolumeCtl = (ImageButton) findViewById(R.id.player_bottom_button_volume_ctl);
//        mBottomButtonVolumeCtl.setOnClickListener(this);
//        /********** 歌词显示组建 *********/
//        mLrcText1 = (TextView) findViewById(R.id.textView1);
//        mLrcText2 = (TextView) findViewById(R.id.textView2);
//        mLrcText3 = (TextView) findViewById(R.id.textView3);
//        mLrcText4 = (TextView) findViewById(R.id.textView4);
//        mLrcText5 = (TextView) findViewById(R.id.textView5);

    }

    private final MusicStatusChangeListener mMusciStatusListener = new MusicStatusChangeListener() {
        @Override
        public void onStatusChange(MusicStatus status) {
            Message msg = mHandler.obtainMessage(UPDATE_UI, status);
            mHandler.sendMessage(msg);
        }
    };

    private void bindService() {
        if (D)
            Log.d(TAG, "bind service");
        BonovoMusicPlayerUtil.bindPlayBackService(this, this);
    }

    @Override
    public void onClick(View v) {
        if (D)
            Log.d(TAG, "onClick : " + v.getId());
        int viewId = v.getId();
        Message msg = mHandler.obtainMessage(viewId);
        if (msg != null) {
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (D)
            Log.d(TAG, "onServiceConnected");
        mService = ((BonovoMusicPlayerUtil.ServiceBinder) service).getService();
        mServiceConnected = true;
        init();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        //mService.unregisterStatusListener();
        mServiceConnected = false;
    }

    /**
     * @author zybo 蹇繘
     */
    public void seekForward() {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(SEEK_FORWARD);
            mHandler.sendEmptyMessage(UPDATE_POSITION);
        }
    }

    /**
     * @author zybo 蹇�
     */
    public void seekBackward() {
        if (mHandler != null) {
            mHandler.sendEmptyMessage(SEEK_BACKWARD);
            mHandler.sendEmptyMessage(UPDATE_POSITION);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android
     * .widget.SeekBar, int, boolean)
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (fromUser) {
            if (progress < 0 || progress > 100) {
                return;
            }
            Log.d("ethan_log", "seek to : " + progress);
            Message msg = mHandler.obtainMessage(SEEK, progress, 0);
            mHandler.sendMessage(msg);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.widget.SeekBar.OnSeekBarChangeListener#onStartTrackingTouch(android
     * .widget.SeekBar)
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.widget.SeekBar.OnSeekBarChangeListener#onStopTrackingTouch(android
     * .widget.SeekBar)
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}