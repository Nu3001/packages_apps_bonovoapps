package com.bonovo.musicplayer.playlist;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zybo on 9/18/14.
 */
public class PlayListManager {
    private static final String TAG = "PlayListManager";
    private static final boolean D = false;
    private static final int MESSAGE_LOAD_PLAY_LIST = 101;
    private static final int MESSAGE_CREATE_PLAY_LIST = 102;
    private static final int MESSAGE_DELETE_PLAY_LIST = 103;
    private boolean mLoaded;
    private PlayListStore mPlayListStore;
    private Map<String, PlayList> mPlayListMap;
    private Handler mHandler;
    private Handler mCallbackHandler;
    private WeakReference<PlayListManagerListener> mListenerRef;

    public PlayListManager(PlayListStore playListStore) {
        mPlayListMap = new Hashtable<String, PlayList>();
        final HandlerThread playListManagerThread =
                new HandlerThread("PlayListManagerThread");
        playListManagerThread.start();
        mHandler = new PlayListManagerHandler(playListManagerThread.getLooper());
        mCallbackHandler = new Handler();
        mLoaded = false;
        mPlayListStore = playListStore;
    }

    public void load() {
        mHandler.sendEmptyMessage(MESSAGE_LOAD_PLAY_LIST);
    }

    public void setListener(final PlayListManagerListener listener) {
        if (listener == null)
            mListenerRef = null;
        else
            mListenerRef = new WeakReference<PlayListManagerListener>(listener);
    }

    public Set<String> getPlayListNames() {
        if (!mLoaded) {
            load();
        }
        return mPlayListMap.keySet();
    }

    public PlayList getPlayListByName(final String playListName) {
        if (!mLoaded) {
            load();
        }

        final Set<String> keySet = mPlayListMap.keySet();
        if (!keySet.contains(playListName)) {
            return null;
        }

        return mPlayListMap.get(playListName);
    }

    public void createPlayList(final String playListName) {
        mHandler.obtainMessage(MESSAGE_CREATE_PLAY_LIST, playListName);
    }

    public void deletePlayList(final String playListName) {
        mHandler.obtainMessage(MESSAGE_DELETE_PLAY_LIST, playListName);
    }

    interface PlayListManagerListener {
        public void onPlayListCreated(String name);

        public void onPlayListDeleted(String name);

        public void onPlayListLoaded();
    }

    private class PlayListManagerHandler extends Handler {
        public PlayListManagerHandler(final Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            final PlayListManagerListener listener = mListenerRef.get();
            switch (msg.what) {
                case MESSAGE_LOAD_PLAY_LIST:
                    if (!mPlayListMap.isEmpty())
                        mPlayListMap.clear();
                    final List<PlayList> playListList = mPlayListStore.getPlayLists();
                    for (int i = 0; i < playListList.size(); i++) {
                        final PlayList playList = playListList.get(i);
                        final String name = playList.getName();
                        mPlayListMap.put(name, playList);
                    }
                    mLoaded = true;
                    if (listener != null) {
                        mCallbackHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onPlayListLoaded();
                            }
                        });
                    }
                    break;
                case MESSAGE_CREATE_PLAY_LIST:
                    final String playList2Create = (String) msg.obj;
                    mPlayListStore.createPlayList(playList2Create);
                    if (listener != null) {
                        mCallbackHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onPlayListCreated(playList2Create);
                            }
                        });
                    }
                    break;
                case MESSAGE_DELETE_PLAY_LIST:
                    final String playList2Delete = (String) msg.obj;
//                    mPlayListStore.deletePlayList(playList2Delete);
                    if (listener != null) {
                        mCallbackHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onPlayListDeleted(playList2Delete);
                            }
                        });
                    }
                    break;
            }
        }
    }
}
