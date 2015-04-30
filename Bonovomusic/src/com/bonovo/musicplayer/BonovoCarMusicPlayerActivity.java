package com.bonovo.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.LevelListDrawable;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bonovo.musicplayer.IMusicPlayerService.MusicStatus;
import com.bonovo.musicplayer.IMusicPlayerService.MusicStatusChangeListener;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class BonovoCarMusicPlayerActivity extends Activity implements
        OnClickListener, ServiceConnection, SeekBar.OnSeekBarChangeListener {
    /**
     * for debug.
     */
    private static final String TAG = "BonovoMusicPlayerActivity";
    private static final boolean D = true;
    private static final int UPDATE_UI = 1;
    private static final int NOTIFICATION_ID = 101;
    private static final int UPDATE_POSITION = 2;
    private static final int DISMISS_VOL_CTRL_DIALOG = 3;
    private static final int SEEK_FORWARD = 4;
    private final MusicStatusChangeListener mMusicStatusListener = new MusicStatusChangeListener() {
        @Override
        public void onStatusChange(MusicStatus status) {
            if (mHandler != null) {
                Message msg = mHandler.obtainMessage(UPDATE_UI, status);
                mHandler.sendMessage(msg);
            }
        }
    };
    private static final int SEEK_BACKWARD = 5;
    private static final int SEEK = 6;
    private static final int PLAY_FILE = 7;
    private static final int INIT = 8;
    private static final int SEEK_UNIT = 10000; // ms
    private static final int PROGRESS_MAX = 100;
    /**
     * views.
     */
    private Button mTitleExplorerButton;
    private Button mTitleCloseButton;
    private Button mLikeButton;
   // private ImageView mDetailAlbumImage;
    //private TextView mDetailSongText;
    //private TextView mDetailAlbumText;
    //private TextView mDetailGenreText;
    //private TextView mDetailArtistText;
    private Button mActionLrcButton;
    private Button mActionPreButton;
    private Button mActionPlayPauseButton;
    private Button mActionNextButton;
    private Button mActionModeButton;
    private Button mActionScanButton;
    private TextView mProgressDurationText;
    private TextView mProgressDurationTotalText;
    private SeekBar mProgressSeekBar;
    // @ TODO
    // private NotificationManager mNm;
    private IMusicPlayerService mService;
    private boolean mServiceConnected = false;
    private boolean mLrcToggle = true;
    private boolean isFileFound = true;
    private LevelListDrawable mModeDrawable;
    private Button mScanButton;

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private int currentFragment=R.id.PlayerDetailFragment;
	
    private IMusicPlayerService.MusicScanListener mMusicScanListener = new IMusicPlayerService.MusicScanListener() {
        @Override
        public void onStart() {
            mScanButton.setText(R.string.player_stop_scanning_text);
            mScanningBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onStop() {
            mScanButton.setText(R.string.player_scan_text);
            mScanningBar.setVisibility(View.GONE);
        }
    };
    private boolean mHasNotification;
    private ProgressBar mScanningBar;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (D)
            Log.d(TAG, "BonovoMusicPlayerActivity onCreate");
        super.onCreate(savedInstanceState);
        mHasNotification = true;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.music_player_main_layout);
        setupViews();
        bindService();
	 fragmentManager = getFragmentManager();
	 SetFragmentShow(currentFragment);
    }

    private void init() {
        mHandler.sendEmptyMessage(INIT);
    }

    @Override
    protected void onStart() {
        if (D)
            Log.d(TAG, "onStart");
        super.onStart();
    }

    private void SetFragmentShow(int fragmentId)
    {
		transaction = fragmentManager.beginTransaction();
		
		Fragment mPlayerDetailFragment = (Fragment) fragmentManager.findFragmentById(R.id.PlayerDetailFragment);
		Fragment mPlayerLrcViewFragment = (Fragment) fragmentManager.findFragmentById(R.id.PlayerLrcViewFragment);

		 switch (fragmentId) 
		 {
			 case R.id.PlayerLrcViewFragment:
			 	{
					transaction.show(mPlayerLrcViewFragment);
					transaction.hide(mPlayerDetailFragment);
					transaction.commit();
					
					currentFragment=fragmentId;
				}
			 break;
			 case R.id.PlayerDetailFragment:
			 {
			 	transaction.show(mPlayerDetailFragment);
				transaction.hide(mPlayerLrcViewFragment);
				transaction.commit();
				currentFragment=fragmentId;	
			 }
			 break;
			 
			 default:
			 	{
				 	transaction.show(mPlayerLrcViewFragment);
					transaction.hide(mPlayerDetailFragment);
					transaction.commit();
					currentFragment=R.id.PlayerDetailFragment;
				}
		 
		 }	
    }

    private void SwitchFragment()
    {

    	   if(currentFragment==R.id.PlayerLrcViewFragment)
	   {
	         SetFragmentShow(R.id.PlayerDetailFragment);
	   }	
          else if(currentFragment==R.id.PlayerDetailFragment)
	   {
	    	  SetFragmentShow(R.id.PlayerLrcViewFragment);	
	   }
    }

    private Handler mHandler = new Handler() {
        private long mDuration;

        // private long mPosition;
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;

	    Log.d(TAG, "handleMessage="+what);
            switch (what) {
                case R.id.player_title_action_close:
                    mService.pause();
                    mService.stopService();
                    mHasNotification = false;
                    finish();
                    break;
                case R.id.player_title_action_explorer:
                    BonovoMusicPlayerUtil
                            .startListActivity(BonovoCarMusicPlayerActivity.this);
                    break;
                case R.id.player_action_pre_button:
                    mService.prev();
                    break;
                case R.id.player_action_play_pause_button:
                    if (mService.isPlaying()) {
                        mActionPlayPauseButton
                                .setBackgroundResource(R.drawable.player_action_play_drawable);
                        mService.pause();
                    } else if (mService.play()) {
                        mActionPlayPauseButton
                                .setBackgroundResource(R.drawable.player_action_pause_drawable);
                    }
                    break;
                case R.id.player_action_next_button:
                    mService.next();
                    break;
                case R.id.player_scan_action:
                    if (mServiceConnected) {
                        mService.scan();
                    }
                    break;
                case R.id.player_action_like_button:
                    if (mServiceConnected) {
                        mService.likeCurrent();
                    }
                    break;
                case R.id.player_action_mode_button:
                    int playMode = mService.getPlayMode();
                    switch (playMode) {
                        case BonovoAudioPlayerService.MODE_ALL:
                            mService.setPlayMode(BonovoAudioPlayerService.MODE_SINGLE);
                            mActionModeButton.setBackgroundResource(R.drawable.player_action_mode_repeat_drawable);
                            break;
                        case BonovoAudioPlayerService.MODE_SINGLE:
                            mService.setPlayMode(BonovoAudioPlayerService.MODE_RANDOM);
                            mActionModeButton.setBackgroundResource(R.drawable.player_action_mode_random_drawable);
                            break;
                        case BonovoAudioPlayerService.MODE_RANDOM:
                            mService.setPlayMode(BonovoAudioPlayerService.MODE_ALL);
                            mActionModeButton.setBackgroundResource(R.drawable.player_action_mode_cycle_drawable);
                            break;
                    }
                    break;
		  case R.id.player_lrc_action:
		  	{
				SwitchFragment();	
			}
		  	break;

                case UPDATE_UI:
                    if (D)
                        Log.d(TAG, "update UI!");
                    MusicStatus ms = (MusicStatus) msg.obj;
                    if (ms != null) {
                        mDuration = ms.duration;
                        mProgressDurationTotalText.setText(BonovoMusicPlayerUtil
                                .makeTimeString(BonovoCarMusicPlayerActivity.this,
                                        mDuration / 1000));
                        final LevelListDrawable likedDrawable = (LevelListDrawable) mLikeButton.getBackground();
                        if (ms.like) {
                            likedDrawable.setLevel(1);
                        } else {
                            likedDrawable.setLevel(0);
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
                            Log.d(TAG, "ms.like : " + ms.like);
                        }
                    }
					
                    if (mService.isPlaying()) {
                        mActionPlayPauseButton
                                .setBackgroundResource(R.drawable.player_action_pause_drawable);
                    } else {
                        mActionPlayPauseButton
                                .setBackgroundResource(R.drawable.player_action_play_drawable);
                    }
                    sendMessageDelayed(obtainMessage(UPDATE_POSITION), 1000);
                    break;

                case UPDATE_POSITION:
                    if (mService != null) {
                        removeMessages(UPDATE_POSITION);
                        long position = mService.position();
                        mProgressDurationText.setText(BonovoMusicPlayerUtil
                                .makeTimeString(BonovoCarMusicPlayerActivity.this,
                                        position / 1000));
                        if (mDuration > 0) {
                            double percent = (double) position / (double) mDuration;
                            int progress = (int) (percent * PROGRESS_MAX);
                            mProgressSeekBar
                                    .setProgress(progress > PROGRESS_MAX ? PROGRESS_MAX
                                            : progress);
                        }
                        sendMessageDelayed(obtainMessage(UPDATE_POSITION), 1000);
                    }
                    break;
                case SEEK_FORWARD:
                    if (mService != null) {
                        long position = mService.position();
                        if (D)
                            Log.d(TAG, " position : " + position + ", duration : "
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
                        double percent = mDuration / PROGRESS_MAX;
                        long position = (long) (percent * progress);
                        mService.seek(position);
                    }
                    break;
                case PLAY_FILE:
                    String filePath = (String) msg.obj;
                    File file = new File(filePath);
                    if (filePath != null) {
                        String folder = file.getParent().toString();
                        ArrayList<String> fileNames = new ArrayList<String>();
                        fileNames.add(file.getName());
                        List<Long> ids = BonovoMusicPlayerUtil.getAudioIdsByName(
                                BonovoCarMusicPlayerActivity.this, folder, fileNames);
                        mService.open(ids, 0);
                        mService.play();
                        sendEmptyMessage(UPDATE_POSITION);
                    }
                    break;
                case INIT:
                    if (mServiceConnected) {
                        mService.registerStatusListener(mMusicStatusListener);
                        int mode = mService.getPlayMode();
                        if (D)
                            Log.d(TAG, "service mode : " + mode);
                        switch (mode) {
                            case BonovoAudioPlayerService.MODE_ALL:
                                mActionModeButton.setBackgroundResource(
                                        R.drawable.player_action_mode_cycle_drawable);
                                break;
                            case BonovoAudioPlayerService.MODE_SINGLE:
                                mActionModeButton.setBackgroundResource(R.drawable.player_action_mode_repeat_drawable);
                                break;
                            case BonovoAudioPlayerService.MODE_RANDOM:
                                mActionModeButton.setBackgroundResource(R.drawable.player_action_mode_random_drawable);
                                break;
                        }
                        Intent intent = getIntent();
                        String action = intent.getAction();
                        mHandler.obtainMessage(UPDATE_UI, mService.getCurrentStatus()).sendToTarget();
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
        if (mHasNotification)
            mService.showNotification();
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
        if (mHandler != null) {
            mHandler.removeMessages(UPDATE_UI);
            mHandler = null;
        }
        BonovoMusicPlayerUtil.unbindPlayBackService(this, this);
        mService = null;
        super.onDestroy();
    }

    private void setupViews() {
        if (D)
            Log.d(TAG, "setupViews");
        mTitleExplorerButton = (Button) findViewById(R.id.player_title_action_explorer);
        mTitleExplorerButton.setOnClickListener(this);
        mScanButton = (Button) findViewById(R.id.player_scan_action);
        mScanButton.setOnClickListener(this);
        mTitleCloseButton = (Button) findViewById(R.id.player_title_action_close);
        mTitleCloseButton.setOnClickListener(this);
		
        mLikeButton = (Button) findViewById(R.id.player_action_like_button);
        mLikeButton.setOnClickListener(this);
        mActionScanButton = (Button) findViewById(R.id.player_scan_action);
        mActionScanButton.setOnClickListener(this);
        mScanningBar = (ProgressBar) findViewById(R.id.player_scan_progress_bar);
		
	 mActionLrcButton=(Button) findViewById(R.id.player_lrc_action);
        mActionLrcButton.setOnClickListener(this);
	 
        mActionPreButton = (Button) findViewById(R.id.player_action_pre_button);
        mActionPreButton.setOnClickListener(this);
        mActionPlayPauseButton = (Button) findViewById(R.id.player_action_play_pause_button);
        mActionPlayPauseButton.setOnClickListener(this);
        mActionNextButton = (Button) findViewById(R.id.player_action_next_button);
        mActionNextButton.setOnClickListener(this);
        mActionModeButton = (Button) findViewById(R.id.player_action_mode_button);
        mActionModeButton.setOnClickListener(this);
        mModeDrawable = (LevelListDrawable) mActionModeButton.getBackground();
        mProgressSeekBar = (SeekBar) findViewById(R.id.player_progress_progressbar);
        mProgressSeekBar.setOnSeekBarChangeListener(this);
        mProgressDurationText = (TextView) findViewById(R.id.player_progress_duration_current_text);
        mProgressDurationTotalText = (TextView) findViewById(R.id.player_progress_duration_total_text);
    }

    private void bindService() {
        if (D)
            Log.d(TAG, "bind service");
        BonovoMusicPlayerUtil.bindPlayBackService(this, this);
    }

    @Override
    public void onClick(View v) {
        //if (D)
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
        mService.cancelNotification();
        mService.setScanListener(mMusicScanListener);
        init();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService.unregisterStatusListener(mMusicStatusListener);
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