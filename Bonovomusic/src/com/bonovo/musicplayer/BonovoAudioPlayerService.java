package com.bonovo.musicplayer;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.bonovo.musicplayer.BonovoMusicPlayerUtil.ServiceBinder;
import com.bonovo.musicplayer.ui.MediaButtonReceiver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;

public class BonovoAudioPlayerService extends Service implements
        IMusicPlayerService {
    static final int NOTIFICATION_ID = 101;
    private static final String TAG = "BonovoAudioPlayerService";
    private static final boolean D = true;
    private static final int TRACK_ENDED = 1;
    private static final int RELEASE_WAKE_LOCK = 2;
    private static final int SERVER_DIED = 3;
    private static final int FADE_IN = 4;
    private static final int FOCUS_CHANGED = 5;
    private static final int NOTIFY_LISTENER = 6;
    private static final int PLAY_APE = 7;
    private static final int OPEN_CURRENT = 8;
    private static final int MAX_HISTORY_SIZE = 100;
    private final Vector<Integer> mHistory = new Vector<Integer>(
            MAX_HISTORY_SIZE);
    private static final int SCAN_MESSAGE_SCAN = 101;
    private static final int SCAN_MESSAGE_FINISHED = 102;
    private static final int SCAN_MESSAGE_CLEAR = 103;
    private static final int IDLE_DELAY = 60000;
    private static final String AUDIO_LIST_KEY = "audio_list";
    private static final String AUDIO_PLAY_MODE = "play_mode";
    private static final String AUDIO_POSITION = "audio_list_position";
    private static final String AUDIO_PLAY_PAUSE = "play_pause";
    private static final int AUDIO_PLAY = 1;
    private static final int AUDIO_PAUSE = 2;
    static boolean sRunning;
    static boolean sShowingNotification;
    private final ArrayList<TimeSpan> mTimeSpans = new ArrayList<TimeSpan>();
    private final Shuffler mShuffler = new Shuffler();
    private final LRCManager lrcManager = new LRCManager();
    private final ServiceBinder mBinder = new ServiceBinder() {
        @Override
        public IMusicPlayerService getService() {
            return BonovoAudioPlayerService.this;
        }
    };
    private final OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            mMediaPlayerHandler.obtainMessage(FOCUS_CHANGED, focusChange, 0)
                    .sendToTarget();
        }
    };
    private boolean mScanning;
    private AudioManager mAudioManager;
    private BroadcastReceiver mUnmountReceiver;
    private BroadcastReceiver mShutdownReceiver;
    private AudioPlayer mPlayer;
    private WakeLock mWakeLock;
    private List<Long> mListToPlay;
    private int mPos=0;
    private int mNextPos;
     // List of all  listeners.    
    private final CopyOnWriteArrayList<MusicStatusChangeListener> mStatusListeners =            new CopyOnWriteArrayList<MusicStatusChangeListener>();
    private Cursor mCurrentCursor;
    private boolean mIsPlaying = false;
    private long mCurrentAudioId;
    private String mCurrentAudioTitle;
    private String mPreAudioTitle;
    private String mCurrentMusicFile;
    private int mPlayMode;
    private boolean mPausedByTransientLossOfFocus = false;
    private final Handler mDelayedStopHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isPlaying() || mPausedByTransientLossOfFocus
                    || mMediaPlayerHandler.hasMessages(TRACK_ENDED)) {
                return;
            }
            saveQueue();
            stopSelf();
        }
    };
    private final Handler mMediaPlayerHandler = new Handler() {
        float mCurrentVolume = 1.0f;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADE_IN:
                    mCurrentVolume += 0.01f;
                    if (mCurrentVolume < 1.0f) {
                        mMediaPlayerHandler.sendEmptyMessageDelayed(FADE_IN, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;
                case SERVER_DIED:
                    if (mIsPlaying) {
                        next();
                    } else {
                        openCurrent();
                    }
                    break;
                case OPEN_CURRENT:
                    openCurrent();
                    break;
                case TRACK_ENDED:
                    if (mPlayMode == MODE_SINGLE) {
                        seek(0);
                        play();
                    } else {
                        next();
                    }
                    break;
                case RELEASE_WAKE_LOCK:
                    mWakeLock.release();
                    break;
                case FOCUS_CHANGED:
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                            Log.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS");
//                            if (isPlaying()) {
////                                mPausedByTransientLossOfFocus = false;
//                                mPausedByTransientLossOfFocus = true;
//                            }
//                            pause();
//                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            Log.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
                            if (isPlaying()) {
                                mPausedByTransientLossOfFocus = true;
                            }
                            pause();
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            Log.v(TAG, "AudioFocus: received AU--DIOFOCUS_GAIN");
                            if (!isPlaying() && mPausedByTransientLossOfFocus) {
                                mPausedByTransientLossOfFocus = false;
                                mCurrentVolume = 0f;
                                mPlayer.setVolume(mCurrentVolume);
                                play();
                            }
                            break;
                        default:
                            Log.e(TAG, "Unknown audio focus change code");
                    }
                    break;
                case NOTIFY_LISTENER:
		      
		      Log.d("PlayerDetailFragment", "send NOTIFY_LISTENER");
			  
                    MusicStatus ms = getCurrentMusicState();
                    if (ms != null) {
                           for (MusicStatusChangeListener listener : mStatusListeners){
					listener.onStatusChange(ms);       
			  }
                    }
                    
                    if (sShowingNotification) {
                        updateNotification();
                    }
                    break;
                case PLAY_APE:
                    long[] times = BonovoAudioPlayerService.this
                            .getTimeSpan(BonovoAudioPlayerService.this.mPlayer
                                    .position());
                    BonovoAudioPlayerService.this.seek(times[0], times[1]);
                    break;
                default:
                    break;
            }
        }
    };
    private SharedPreferences mSp;
    private boolean isAPE = false;
    private Handler mHandler;
    private Notification.Builder mNotificationBuilder;
    private List<String> mScanFolder;
    private FileExplorer.FileTraveller mFileTraveller;
    private MusicScanListener mMusicScanListener;
    private int mScanCompletedCount;
    private MediaScannerConnection.OnScanCompletedListener mScanCompletedListener =
            new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    if (D && path != null & uri != null)
                        Log.d(TAG, "scan completed, path : " + path + ", uri : " + uri.toString());
                    ++mScanCompletedCount;
                    mScanHandler.sendEmptyMessage(SCAN_MESSAGE_SCAN);
                }
            };
    private Handler mScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SCAN_MESSAGE_SCAN) {
//                removeMessages(SCAN_MESSAGE_FINISHED);
                final String path = mFileTraveller.nextFile();
                if (path == null) {
                    if (mScanFolder.size() > 0) {
                        mFileTraveller = new FileExplorer.FileTraveller(mScanFolder.remove(0));
                        sendEmptyMessage(SCAN_MESSAGE_SCAN);
                    } else {
                        stopScan();
                    }
                } else {
//                    sendEmptyMessageDelayed(SCAN_MESSAGE_FINISHED, 10000);
                    if (D)
                        Log.d(TAG, "check file : " + path);
                    if (MediaStoreUtil.isAudioFile(path)) {
                        MediaScannerConnection.scanFile(BonovoAudioPlayerService.this,
                                new String[]{path}, null, mScanCompletedListener);
                    } else {
                        sendEmptyMessage(SCAN_MESSAGE_SCAN);
                    }
                }
            }
            if (msg.what == SCAN_MESSAGE_CLEAR) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        if (D)
                            Log.d(TAG, "clear data");
                        MediaStoreUtil.clear(BonovoAudioPlayerService.this);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        mScanHandler.sendEmptyMessage(SCAN_MESSAGE_SCAN);
                    }
                }.execute();
            }
            if (msg.what == SCAN_MESSAGE_FINISHED) {
                stopScan();
            }
        }
    };
    private boolean mHasStorage;
    private boolean mPaused;
    private ComponentName mMediaButtonReceiver;

    static PendingIntent getAlarmSender(final Context context) {
        final Intent intent = new Intent(context, AlarmReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private void stopScan() {
        mScanning = false;
        mScanFolder.clear();
        mScanHandler.removeMessages(SCAN_MESSAGE_SCAN);
        final String message = getResources().getString(R.string.service_scan_completed, mScanCompletedCount);
        Toast.makeText(BonovoAudioPlayerService.this, message, Toast.LENGTH_SHORT).show();
        if (mMusicScanListener != null)
            mMusicScanListener.onStop();
    }

    public void cancelNotification() {
        final NotificationManager nm =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
        sShowingNotification = false;
    }

    private void setupNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        final MusicStatus status = getCurrentMusicState();
        if (status == null) {
            cancelNotification();
            return;
        }
        builder
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(status.currMusic)
                .setOngoing(true)
                .setContentText(status.currentArtist);
        final Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, BonovoCarMusicPlayerActivity.class));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(BonovoCarMusicPlayerActivity.class);
        stackBuilder.addNextIntent(intent);
        builder.setContentIntent(
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT));
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationBuilder = builder;
        nm.notify(NOTIFICATION_ID, builder.build());
    }

    private void updateNotification() {
        if (mNotificationBuilder == null)
            return;
        final MusicStatus status = getCurrentMusicState();
        if (status == null)
            return;
        mNotificationBuilder.setContentTitle(status.currMusic);
        mNotificationBuilder.setContentText(status.currentArtist);
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean parseLRC() {
        return lrcManager.parse();
    }

    @Override
    public List<String> getLRCResult() {
        String strTime = BonovoMusicPlayerUtil.makeTimeString(this,
                position() / 1000);
        String[] time = strTime.split(":");

        return lrcManager.getRows(Integer.parseInt(time[1]),
                Integer.parseInt(time[2]));
    }

    private String getLRCPath(String path) {
        int index = path.lastIndexOf(".");
        String strPath = path;
        if (index >= 0) {
            strPath = path.substring(0, index);
        }
        return strPath + ".lrc";
    }

    private void saveQueue() {
        if (mListToPlay != null) {
            StringBuilder sb = new StringBuilder();
            for (long id : mListToPlay) {
                sb.append(id);
                sb.append(";");
            }
            SharedPreferences.Editor editor = mSp.edit();
            if (D)
                Log.d(TAG, "save queue : " + sb.toString());
            editor.putString(AUDIO_LIST_KEY, sb.toString());
            editor.apply();
        }
    }

    private void restoreQueue() {
        synchronized (this) {
            String ids = mSp.getString(AUDIO_LIST_KEY, "");
            if (D)
                Log.d(TAG, "restore queue : " + ids);
            if (ids != null && !ids.equals("")) {
                String[] idArray = ids.split(";");
                if (mListToPlay == null) {
                    mListToPlay = new ArrayList<Long>();
                }
                for (String id : idArray) {
                    mListToPlay.add(Long.valueOf(id));
                }
            } else {
                mListToPlay = MediaStoreUtil.createDefaultList(this);
            }
        }
    }

    private void gotoIdleState() {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        stopForeground(true);
    }

    private void openCurrent() {
        openCurrent(false);
    }

    private void openCurrent(boolean playAfterOpened) {
        synchronized (this) {
            if (mCurrentCursor != null) {
                mCurrentCursor.close();
                mCurrentCursor = null;
            }

            if (mListToPlay == null || mListToPlay.size() == 0) {
                return;
            }

            if (mIsPlaying)
                stop();
            if (mPos > mListToPlay.size() - 1 || mPos<0)
                mPos = 0;
            long id = mListToPlay.get(mPos);
            mCurrentCursor = BonovoMusicPlayerUtil.getMusicById(this, id);
            if (D)
                Log.d(TAG, "open ... : " + String.valueOf(id));
            if (mCurrentCursor != null) {
                if (mCurrentCursor.moveToFirst()) {
                    mCurrentAudioId = id;
                    mCurrentAudioTitle = mCurrentCursor
                            .getString(mCurrentCursor
                                    .getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String strPath = mCurrentCursor.getString(mCurrentCursor
                            .getColumnIndex(MediaStore.Audio.Media.DATA));
                    open(strPath);
		      mCurrentMusicFile=strPath;
                } else {
                    mCurrentCursor.close();
                    mCurrentCursor = null;
                }
            }

            if (!mHasStorage)
                return;
            if (!mPlayer.isInitialized()) {
                mListToPlay.remove(mPos);
                if (mListToPlay.size() != 0) {
                    mPos = mPos % mListToPlay.size();
//                    openCurrent();
                    mMediaPlayerHandler.obtainMessage(OPEN_CURRENT).sendToTarget();
                }
            } else {
                if (playAfterOpened)
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            play();
                        }
                    });
            }
        }
    }

    //    @Override
    public void open(String path) {
        if (D)
            Log.d(TAG, "open , position : " + mPos + ", next position : " + mNextPos
                    + ", pre position : " +
                    (mHistory.size() == 0 ? "none" : String.valueOf(mHistory.get(mHistory.size() - 1))));
        mMediaPlayerHandler.removeMessages(PLAY_APE);
        isAPE = false;
        mPlayer.setDataSource(path);
//        lrcManager.setPath(getLRCPath(path));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (D)
            Log.d(TAG, "on create!");
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final ComponentName mediaButtonReceiver = new ComponentName(this, MediaButtonReceiver.class);
        mAudioManager.registerMediaButtonEventReceiver(mediaButtonReceiver);
        mMediaButtonReceiver = mediaButtonReceiver;
        registerExternalStorageListener();
        mPlayer = new AudioPlayer();
        mPlayer.setHandler(mMediaPlayerHandler);

        sRunning = true;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
                .getClass().getName());
        mWakeLock.setReferenceCounted(false);
        final File externalDir = Environment.getExternalStorageDirectory();
        mHasStorage = externalDir.exists() && externalDir.isDirectory() && (externalDir.list() != null);
        mHandler = new Handler();
        setupWatchDog();
        synchronized (this) {
            mSp = getSharedPreferences("service", MODE_PRIVATE);
            int playMode = mSp.getInt(AUDIO_PLAY_MODE, 0);
            mPos = mSp.getInt(AUDIO_POSITION, 0);
            if (playMode == 0) {
                playMode = MODE_ALL;
            }
            mPaused = mSp.getInt(AUDIO_PLAY_PAUSE, AUDIO_PLAY) == AUDIO_PAUSE ? true : false;
            if (D)
                Log.d(TAG, "pause : " + String.valueOf(mPaused));
            mPlayMode = playMode;
            restorePlay();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        if (action == null)
            return 0;
        if (SERVICE_COMMAND_PLAY.equals(action)) {
            play();
        }
        if (SERVICE_COMMAND_NEXT.equals(action)) {
            next();
        }
        if (SERVICE_COMMAND_PRE.equals(action)) {
            prev();
        }
        if (SERVICE_COMMAND_STOP.equals(action)) {
            stop();
        }
        return 0;
    }

    private void restorePlay() {
        Message msg = mDelayedStopHandler.obtainMessage();
        restoreQueue();
        if (mListToPlay != null) {
            openCurrent();
            if (mPlayer.isInitialized() && !mPaused) {
                if (D) {
                    Log.d(TAG, "restore to play, paused : " + String.valueOf(mPaused));
                }
//                play();
            }
        } else {
            mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        }
    }

    private void setupWatchDog() {
        final Context context = this;
        final AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);

//        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ 5000, sender);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                3000, getAlarmSender(BonovoAudioPlayerService.this));
    }

    // private static final Uri sArtworkUri = Uri
    // .parse("content://media/external/audio/albumart");

    @Override
    public void onDestroy() {
        synchronized (this) {
            mPlayer.release();
            mPlayer = null;
            mAudioManager.abandonAudioFocus(mAudioFocusListener);
            mAudioManager.unregisterMediaButtonEventReceiver(mMediaButtonReceiver);
            mDelayedStopHandler.removeCallbacksAndMessages(null);
            mMediaPlayerHandler.removeCallbacksAndMessages(null);

            SharedPreferences.Editor editor = mSp.edit();
            editor.putInt(AUDIO_PLAY_MODE, mPlayMode);
            editor.putInt(AUDIO_POSITION, mPos);
            editor.apply();

            saveQueue();
            if (mCurrentCursor != null) {
                mCurrentCursor.close();
                mCurrentCursor = null;
            }
        }
        if (sShowingNotification) {
            cancelNotification();
            sShowingNotification = false;
        }
        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;
        }
        if (mShutdownReceiver != null) {
            unregisterReceiver(mShutdownReceiver);
            mShutdownReceiver = null;
        }
        mWakeLock.release();

        super.onDestroy();
    }

    private void registerExternalStorageListener() {
        if (mUnmountReceiver == null) {
            mUnmountReceiver = new BroadcastReceiver() {
                private static final int TOTAL_MOUNTED_COUNT = 2;
                private int mMountCount;
                private int mUnMountCount;

                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (D)
                        Log.d(TAG, "media action : " + action);
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)
                            || action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)
                            || action.equals(Intent.ACTION_MEDIA_REMOVED)) {

                        if (++mUnMountCount == TOTAL_MOUNTED_COUNT) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    saveQueue();
                                    if (mIsPlaying)
                                        stop();
                                }
                            });
                            mUnMountCount = 0;
                        }
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        if (++mMountCount == TOTAL_MOUNTED_COUNT) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    restorePlay();
                                }
                            });
                            mMountCount = 0;
                        }
                    } else if (action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)
                            || action.equals(Intent.ACTION_MEDIA_REMOVED)) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                saveQueue();
                                if (mIsPlaying)
                                    stop();
                            }
                        });
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addDataScheme("file");
            registerReceiver(mUnmountReceiver, filter);
        }
        if (mShutdownReceiver == null) {
            mShutdownReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "mShutdownReceiver.onReceive()");
                    String action = intent.getAction();
                    if (action.equals(Intent.ACTION_SHUTDOWN)) {
                        saveQueue();
                        stop();
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_SHUTDOWN);
            registerReceiver(mShutdownReceiver, iFilter);
        }
    }

    @Override
    public void open(List<Long> list, int position) {
        mMediaPlayerHandler.removeMessages(PLAY_APE);
        isAPE = false;
        if (list == null)
            return;
        synchronized (this) {
            if (mListToPlay != null) {
                mListToPlay.clear();
                mListToPlay = null;
            }
            mListToPlay = new ArrayList<Long>();
            mListToPlay.addAll(list);
            mPos = position;
        }
        if (mListToPlay.size() == 0)
            return;
        openCurrent();
        // zyb
    }

    private MusicStatus getCurrentMusicState() {
        MusicStatus ms = null;
        synchronized (this) {
            if (mCurrentCursor != null) {
                Cursor c = mCurrentCursor;
//                String nextTitle = null;
                if (c == null || c.getCount() == 0)
                    return null;
                if (c.moveToFirst()) {
//                    if (mPlayMode == MODE_ALL) {
//                        mNextPos = (mPos + 1) % mListToPlay.size();
//                    } else if (mPlayMode == MODE_RANDOM) {
//                        mNextPos = mShuffler.nextInt(mListToPlay.size());
//                    } else if (mPlayMode == MODE_SINGLE) {
//                        mNextPos = mPos;
//                    }
//                    long nextId = mListToPlay.get(mNextPos);
//                    Cursor nextC = BonovoMusicPlayerUtil.getMusicById(this,
//                            nextId);
//                    if (nextC != null) {
//                        if (nextC.moveToFirst()) {
//                            nextTitle = nextC
//                                    .getString(nextC
//                                            .getColumnIndex(MediaStore.Audio.Media.TITLE));
//                        }
//                        nextC.close();
//                    }
                    String genreName = BonovoMusicPlayerUtil.getGenreByAudioId(
                            this, mCurrentAudioId);
                    String album = c.getString(c
                            .getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    Cursor albumC = this.getContentResolver().query(
                            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                            MediaStore.Audio.Albums.ALBUM + " =?",
                            new String[]{album},
                            MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
                    albumC.moveToFirst();
                    String albumArt = null;
                    if (albumC != null) {
                        if (albumC.getCount() != 0)
                            albumArt = albumC.getString(albumC
                                    .getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                        albumC.close();
                    }


					/*
                     * ms = new MusicStatus( mCurrentAudioTitle, mPreAudioTitle,
					 * nextTitle, album, genreName, c.getString(c
					 * .getColumnIndex(MediaStore.Audio.Media.ARTIST)),
					 * c.getLong(c
					 * .getColumnIndex(MediaStore.Audio.Media.DURATION)),
					 * albumArt);
					 */
                    ms = new MusicStatus(
                            mCurrentAudioTitle,
                            mPreAudioTitle,
                            null,
                            album,
                            genreName,
                            c.getString(c
                                    .getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                            this.mPlayer.duration(), albumArt, MediaStoreUtil.isMediaLiked(this, mCurrentAudioId),
                            mScanning,
                            mCurrentMusicFile);
                }
            }
        }
        return ms;
    }

    @Override
    public long position() {
        if (mPlayer != null && mPlayer.isInitialized())
            return mPlayer.position();
        else
            return 0;
    }

    @Override
    public boolean play() {
        mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (mListToPlay == null || mListToPlay.size() == 0) {
            open(MediaStoreUtil.createDefaultList(this), 0);
        }
        if (mPlayer.isInitialized()) {
            mPlayer.start();
            mMediaPlayerHandler.sendEmptyMessage(FADE_IN);
            if (!mIsPlaying) {
                mIsPlaying = true;
                if (D)
                    Log.d(TAG, "play....");
                mPaused = false;
                mSp.edit().putInt(AUDIO_PLAY_PAUSE, AUDIO_PLAY).commit();
            }
        }
        return mIsPlaying;
    }

    @Override
    public void prev() {
        synchronized (this) {
//            if (!mPlayer.isInitialized()) {
//                return;
//            }
            if (mPlayMode == MODE_ALL) {
                int histsize = mHistory.size();
                if (histsize == 0) {
                    return;
                }
                Integer pos = mHistory.remove(histsize - 1);
                mPos = pos.intValue();
                mPreAudioTitle = mCurrentAudioTitle;
            } else {
                if (mPos > 0) {
                    mPos--;
                } else {
                    mPos = mListToPlay.size() - 1;
                }
            }
            stop();
            openCurrent();
            if (mPlayer.isInitialized())
                play();
            else {
                if (mPlayMode != MODE_SINGLE)
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            prev();
                        }
                    });
            }
        }
    }

    private void computeNextPlayPosition() {
        if (mListToPlay == null || mListToPlay.size() == 0) {
            mNextPos = 0;
        } else {
            if (mPlayMode == MODE_ALL) {
                mNextPos = (mPos + 1) % mListToPlay.size();
            } else if (mPlayMode == MODE_RANDOM) {
                mNextPos = mShuffler.nextInt(mListToPlay.size());
            } else if (mPlayMode == MODE_SINGLE) {
                mNextPos = mPos;
            }
        }
    }

    private void updatePlayPosition() {
        mHistory.add(mPos);
        mPos = mNextPos;
    }

    @Override
    public void next() {
        if (D)
            Log.d(TAG, "NEXT ...");
        synchronized (this) {
            computeNextPlayPosition();
            updatePlayPosition();
            stop();
            openCurrent(true);
//            if (mPlayer.isInitialized()) {
//                play();
//            }
//            else {
//                if (mPlayMode != MODE_SINGLE) {
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            next();
//                        }
//                    });
//                }
//            }
        }
    }

    @Override
    public void showNotification() {
//        if (mIsPlaying) {
        sShowingNotification = true;
        setupNotification();
//        }
    }

    private void stop() {
        mIsPlaying = false;
        mPaused = false;
        mSp.edit().putInt(AUDIO_PLAY_PAUSE, AUDIO_PLAY).commit();
        synchronized (this) {
            if (mPlayer.isInitialized()) {
                mPlayer.stop();
            }
            if (mCurrentCursor != null) {
                mCurrentCursor.close();
                mCurrentCursor = null;
            }
        }
        stopForeground(false);
    }

    @Override
    public void stopService() {
        stop();
        stopSelf();
    }

    @Override
    public long seek(long pos) {
        if (mPlayer.isInitialized()) {
            if (!isAPE) {
                if (pos < 0)
                    pos = 0;
                if (pos > mPlayer.duration())
                    pos = mPlayer.duration();
                return mPlayer.seek(pos);
            } else {
                mMediaPlayerHandler.removeMessages(PLAY_APE);
                long[] times = BonovoAudioPlayerService.this.getTimeSpan(pos);
                return seek(times[0], times[1]);
            }
        }
        return -1;
    }

    private long seek(long pos, long timespan) {
        if (mPlayer.isInitialized()) {
            if (pos > mPlayer.duration())
                pos = mPlayer.duration();
            long result = -1;
            if (pos >= 0) {
                result = mPlayer.seek(pos);
            }
            if (timespan >= 0) {
                Message msg = mMediaPlayerHandler.obtainMessage(PLAY_APE);
                mMediaPlayerHandler.sendMessageDelayed(msg, timespan);
            }
            return result;
        }
        return -1;
    }

    @Override
    public boolean isPlaying() {
        return mIsPlaying;
    }

    @Override
    public MusicStatus getCurrentStatus() {
        return getCurrentMusicState();
    }

    @Override
    public void likeCurrent() {
        if (mPlayer.isInitialized()) {
            if (MediaStoreUtil.isMediaLiked(this, mCurrentAudioId)) {
                MediaStoreUtil.unlikeMedia(this, mCurrentAudioId);
            } else {
                MediaStoreUtil.likeMedia(this, mCurrentAudioId);
            }
            mMediaPlayerHandler.sendEmptyMessage(NOTIFY_LISTENER);
        }
    }

    @Override
    public void pause() {
        synchronized (this) {
            mMediaPlayerHandler.removeMessages(FADE_IN);
            if (isPlaying()) {
                mPlayer.pause();
                gotoIdleState();
                mIsPlaying = false;
                mPaused = true;
                // @ TODO
                // saveBookmark
            }
        }
        mSp.edit().putInt(AUDIO_PLAY_PAUSE, AUDIO_PAUSE).commit();
    }

    @Override
    public void scan() {
        boolean action = mScanning;
        if (!action) {
            mScanCompletedCount = 0;
            mScanFolder = new LinkedList<String>();
            Collections.addAll(mScanFolder, FileExplorer.getExternalRootFolders());
            mFileTraveller = new FileExplorer.FileTraveller(mScanFolder.remove(0));
            mScanHandler.sendEmptyMessage(SCAN_MESSAGE_CLEAR);

            mScanning = true;
            if (mMusicScanListener != null)
                mMusicScanListener.onStart();
        } else {
//            mFileTraveller = null;
            stopScan();
        }
    }

    @Override
    public void registerStatusListener(MusicStatusChangeListener listener) {
		if (listener != null) {
			mStatusListeners.add(listener);	
		}
    }

    @Override
    public void unregisterStatusListener(MusicStatusChangeListener listener) {
		if (listener == null) {
			mStatusListeners.remove(listener);
		}       
    }

    @Override
    public void setScanListener(MusicScanListener listener) {
        mMusicScanListener = listener;
        if (listener != null) {
            if (mScanning)
                listener.onStart();
            else
                listener.onStop();
        }
    }

    @Override
    public int getPlayMode() {
        return mPlayMode;
    }

    @Override
    public void setPlayMode(int playMode) {
        synchronized (this) {
            mPlayMode = playMode;
        }
    }

    @Override
    public void openAPE(String path, String selectedFileList) {
        // TODO Auto-generated method stub
        isAPE = true;
        mMediaPlayerHandler.removeMessages(PLAY_APE);
        mTimeSpans.clear();
        mPlayer.setDataSource(path);
        String[] timeSpans = selectedFileList.split("\\|");
        for (String timeSpan : timeSpans) {
            String[] times = timeSpan.split(",");
            TimeSpan span = new TimeSpan();
            span.StartMinute = Integer.parseInt(times[0].substring(0, 2));
            span.StartSecond = Integer.parseInt(times[0].substring(3, 5));
            span.EndMinute = (int) (mPlayer.duration() / 1000 / 60);
            span.EndSecond = (int) ((mPlayer.duration() / 1000) % 60);
            if (times.length == 2) {
                span.EndMinute = Integer.parseInt(times[1].substring(0, 2));
                span.EndSecond = Integer.parseInt(times[1].substring(3, 5));
            }
            mTimeSpans.add(span);
        }
        long startTime = convertTimeToMillSecond(mTimeSpans.get(0).StartMinute,
                mTimeSpans.get(0).StartSecond);
        long timespan = -1;
        if (mTimeSpans.size() > 1) {
            // ��һ�׸�Ĳ���ʱ��
            long nextTime = convertTimeToMillSecond(
                    mTimeSpans.get(0).EndMinute, mTimeSpans.get(0).EndSecond);
            timespan = nextTime - startTime;
        }
        this.seek(startTime, timespan);
    }

    // /�õ���һ�׸�Ĳ���ʱ����
    private long[] getTimeSpan(long position) {
        if (hasAPEEnd(position)) {
            position = 0;// ��ͷ��ʼ����
        }
        long[] times = new long[2];// ��һ��Ԫ�ر�ʾ��ǰ����Ĳ���ʱ�䣬�ڶ���Ԫ�ر�ʾ��һ�׸��뵱ǰ�����ʱ����
        times[0] = position;
        times[1] = -1;
        for (TimeSpan span : mTimeSpans) {
            long startPos = convertTimeToMillSecond(span.StartMinute,
                    span.StartSecond);
            long endPos = convertTimeToMillSecond(span.EndMinute,
                    span.EndSecond);
            if (position >= startPos && position < endPos) {
                times[0] = position;
                times[1] = endPos - position;
                break;
            } else if (position < startPos) {
                times[0] = startPos;
                times[1] = endPos - startPos;
                break;
            }
        }
        return times;
    }

    private long convertTimeToMillSecond(int minute, int second) {
        return (minute * 60 + second) * 1000;
    }

    private boolean hasAPEEnd(long pos) {
        if (pos >= mPlayer.duration())
            return true;
        long endTime = convertTimeToMillSecond(
                mTimeSpans.get(mTimeSpans.size() - 1).EndMinute,
                mTimeSpans.get(mTimeSpans.size() - 1).EndSecond);
        return pos >= endTime;
    }

    private static class TimeSpan {
        public int StartMinute;
        public int StartSecond;
        public int EndMinute;
        public int EndSecond;
    }

    private static class Shuffler {
        private final Random mRandom = new Random();
        private int mPrevious;

        public int nextInt(int interval) {
            int ret;
            do {
                ret = mRandom.nextInt(interval);
            } while (ret == mPrevious && interval > 1);
            mPrevious = ret;
            return ret;
        }
    }

    private class AudioPlayer {
        private MediaPlayer mMediaPlayer = new MediaPlayer();
        private Handler mHandler;
        private boolean mIsInitialized = false;
        private float mVolume;

        public AudioPlayer() {
            mMediaPlayer.setWakeMode(BonovoAudioPlayerService.this,
                    PowerManager.PARTIAL_WAKE_LOCK);
        }

        public void setDataSource(String path) {
//            if (D)
//                Log.d(TAG, "ready to play :" + path);
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setOnPreparedListener(null);
                if (path.startsWith("content://")) {
                    mMediaPlayer.setDataSource(BonovoAudioPlayerService.this,
                            Uri.parse(path));
                } else {
                    mMediaPlayer.setDataSource(path);
                }
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepare();
                mHandler.sendEmptyMessage(NOTIFY_LISTENER);
                mIsInitialized = true;
            } catch (final IOException ex) {
                mIsInitialized = false;
            } catch (final IllegalArgumentException ex) {
                mIsInitialized = false;
            } catch (final Exception ex) {
                mIsInitialized = false;
            }

            if (!mIsInitialized) {
                final String message = getResources().getString(R.string.service_alert_cannot_play, path);
//                Toast.makeText(BonovoAudioPlayerService.this, message, Toast.LENGTH_SHORT).show();
                if (Config.Debug.D)
                    Log.d(TAG, "can not play :" + path);

                return;
            }

            mMediaPlayer.setOnCompletionListener(completeListener);
            mMediaPlayer.setOnErrorListener(errorListener);
            mIsInitialized = true;
        }

        public boolean isInitialized() {
            return mIsInitialized;
        }

        public float getVolume() {
            return mVolume;
        }

        public void setVolume(float vol) {
            mMediaPlayer.setVolume(vol, vol);
        }

        public void start() {
            mMediaPlayer.start();
        }

        public void pause() {
            mMediaPlayer.pause();
        }

        public void stop() {
            mMediaPlayer.reset();
            mIsInitialized = false;
        }

        public void release() {
            stop();
            mMediaPlayer.release();
        }

        public void setHandler(final Handler handler) {
            mHandler = handler;
        }

        public long duration() {
            return mMediaPlayer.getDuration();
        }

        public long position() {
            return mMediaPlayer.getCurrentPosition();
        }

        public long seek(long whereto) {
            mMediaPlayer.seekTo((int) whereto);
            return whereto;
        }

        MediaPlayer.OnCompletionListener completeListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mWakeLock.acquire(3000);
                mHandler.sendEmptyMessage(TRACK_ENDED);
                mHandler.sendEmptyMessage(RELEASE_WAKE_LOCK);
            }
        };


        MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        mIsInitialized = false;
                        mMediaPlayer.release();
                        mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setWakeMode(BonovoAudioPlayerService.this,
                                PowerManager.PARTIAL_WAKE_LOCK);
                        mHandler.sendMessageDelayed(
                                mHandler.obtainMessage(SERVER_DIED), 2000);
                        return true;
                    default:
                        Log.i(TAG, "MediaPlayer error : " + what + ", " + extra);
                        break;
                }
                return false;
            }
        };
    }
}
