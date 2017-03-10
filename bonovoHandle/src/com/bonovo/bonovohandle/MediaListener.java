package com.bonovo.bonovohandle;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.RemoteController;
import android.media.RemoteController.MetadataEditor;
import android.media.RemoteControlClient;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import java.lang.Runnable;
import java.util.List;

public class MediaListener extends NotificationListenerService implements RemoteController.OnClientUpdateListener {

    private final static String TAG = "MediaListener";

    private RemoteController mRemoteController;
    private Context mContext;

    private PackageManager mPackagemanager;
    private Intent mIntent;
    private ActivityManager mActivityManager;

    private final String APPLICATIONS = "applications";
    private final String MEDIA_APP = "media_app";
    private final String MEDIA_PLAYING = "media_playing";

    private Handler mHandler;

    private ServiceBinder  serviceBinder = new ServiceBinder();

    public class ServiceBinder extends Binder{

        public MediaListener getService(){
            return MediaListener.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind");
        return serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification notification) {
    }

    @Override
    public void onCreate() {
        //saving the context for further reuse
        mContext = getApplicationContext();
        mHandler = new Handler();
        mPackagemanager = mContext.getPackageManager();
        mActivityManager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
        mIntent = new Intent();
        mIntent.setAction(Intent.ACTION_MAIN);
        mIntent.addCategory(Intent.CATEGORY_APP_MUSIC);
        SharedPreferences sp = mContext.getSharedPreferences(APPLICATIONS, MODE_PRIVATE);
        String packagename = sp.getString(MEDIA_APP,"");
        Boolean playing = sp.getBoolean(MEDIA_PLAYING,false);
        launchLastMediaApp(packagename,playing);
        mRemoteController = new RemoteController(mContext, this);
        if(!((AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE)).registerRemoteController(mRemoteController)) {
            throw new RuntimeException("Error while registering RemoteController!");
        }
    }

    @Override
    public void onDestroy() {
        ((AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE)).unregisterRemoteController(mRemoteController);
    }

    @Override
    public void onClientChange(boolean clearing) {
        if (clearing){
            writeCurrentMediaPref(getCurrentMediaApp(),false);
        }
    }

    @Override
    public void onClientMetadataUpdate(MetadataEditor state) {


    }

    @Override
    public void onClientPlaybackStateUpdate(int state) {
        switch(state) {
            case RemoteControlClient.PLAYSTATE_PLAYING:
                writeCurrentMediaPref(getCurrentMediaApp(),true);
                break;
            case RemoteControlClient.PLAYSTATE_PAUSED:
                // because the BonovoHandle takes audio focus and causes the running music app
                // to pause during sleep, we won't really know if the music was truly paused or
                // just paused due to focus.  Thus, let's assume we still need to restart music
                // when rebooting if paused.  If the music was actually 'stopped', then we'll
                // still catch that condition below and not restart on boot.
                writeCurrentMediaPref(getCurrentMediaApp(),true);
                break;
            default:
                writeCurrentMediaPref(getCurrentMediaApp(),false);
                break;
        }
    }

    @Override
    public void onClientPlaybackStateUpdate(int state, long arg1, long arg2, float arg3) {
        onClientPlaybackStateUpdate(state);
    }


    @Override
    public void onClientTransportControlUpdate(int arg0) {

    }

    private String getCurrentMediaApp() {
        Log.d(TAG,"Starting FindMediaApp");
        String task = "";
        String packagename = "";
        boolean finished = false;
        List<ResolveInfo> packages = mPackagemanager.queryIntentActivities(mIntent,0);
        List<ActivityManager.RunningTaskInfo> tasks = mActivityManager.getRunningTasks(20); // try up to 20 tasks
        Log.d(TAG,"Resolve Count:" + packages.size());
        Log.d(TAG,"Task Count:" + tasks.size());
        for (ActivityManager.RunningTaskInfo ti : tasks) { // loop through running tasks
            task = ti.baseActivity.getPackageName();
            Log.d(TAG,"TI:"+task);
            for (ResolveInfo ri : packages) { // Loop through avail packages
                packagename = ri.activityInfo.packageName;
                Log.d(TAG,"RI"+packagename);
                if (task.equals(packagename)) {
                    finished = true;
                    break;
                }
            }
            if (finished) { // if we found the task in innerloop, stop the outer loop too
                break;
            }
        }
        if (finished) { // did we find a match?
            return task;
        }else {
            return ""; //we did not find a match
        }
    }

    private void writeCurrentMediaPref(String taskname, boolean playing){
        SharedPreferences sp = mContext.getSharedPreferences(APPLICATIONS, MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putString(MEDIA_APP,taskname);
        editor.putBoolean(MEDIA_PLAYING, playing);
        editor.commit();
    }

    private void launchLastMediaApp(String packageName,Boolean playing) {
        if (packageName.equals("")) {
            return;
            }
        try {
            Toast info = Toast.makeText(mContext,"Package:" +packageName + " AutoPlay:" +(playing ? "True" : "False"),
                    Toast.LENGTH_SHORT);
            info.show();
            Intent launch = mPackagemanager.getLaunchIntentForPackage(packageName);
            if (launch != null) {
                startActivityAsUser(launch,UserHandle.CURRENT);
                if (playing) {
                    mHandler.postDelayed(setMediaPlaying,1000); // try to start playing after 1 second to let APP start
                }
            }
        } catch (Exception e) {
            Toast error = Toast.makeText(mContext,"Failed Launching:" + packageName + " error:" +e.getClass().getSimpleName(),Toast.LENGTH_SHORT);
            error.show();
            }
    }

    private Runnable setMediaPlaying = new Runnable() {
        public void run() {
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            boolean playing = am.isMusicActive();
            if (! playing) { // if it's already playing, don't send play command
                Toast info = Toast.makeText(mContext,"Sending Play",
                        Toast.LENGTH_LONG);
                info.show();
                long eventtime = SystemClock.uptimeMillis() - 1;
                KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
                am.dispatchMediaKeyEvent(downEvent);

                eventtime++;
                KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
                am.dispatchMediaKeyEvent(upEvent);
                Intent launch = new Intent(Intent.ACTION_MAIN);
                launch.addCategory(Intent.CATEGORY_HOME);
                launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityAsUser(launch,UserHandle.CURRENT);
            }
        }
    };
}