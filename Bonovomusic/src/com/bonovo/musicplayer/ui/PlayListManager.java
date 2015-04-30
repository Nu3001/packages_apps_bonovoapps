package com.bonovo.musicplayer.ui;

import android.app.FragmentManager;

import com.bonovo.musicplayer.BonovoMusicExplorerActivity;

/**
 * Created by zybo on 9/24/14.
 */
public class PlayListManager {
    private FragmentManager mFm;
    private int mContainerViewId;
    private BonovoMusicExplorerActivity.OnPlayListener mOnPlayListener;
    private PlayListManagerCallback mCallback;

    public PlayListManager(final FragmentManager fm, final int id) {
        mFm = fm;
        mContainerViewId = id;
        mCallback = new Callback();
    }

    public void start() {
        ExplorerPlayListFragment_bak fragment = new ExplorerPlayListFragment_bak();
        fragment.setCallback(mCallback);
        mFm.beginTransaction()
                .replace(mContainerViewId, fragment)
                .commit();
    }

    public void setOnPlayListener(BonovoMusicExplorerActivity.OnPlayListener listener) {
        mOnPlayListener = listener;
    }

    public interface PlayListManagerCallback {
        void onShowPlayListDetail(final String name);
        void onHidePlayListDetail();
    }

    private class Callback implements PlayListManagerCallback {
        @Override
        public void onShowPlayListDetail(String name) {
            ExplorerPlayListDetailFragment fragment = new ExplorerPlayListDetailFragment();
            fragment.setCallback(mCallback);
            mFm.beginTransaction()
                    .replace(mContainerViewId, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void onHidePlayListDetail() {
            mFm.popBackStack();
        }
    }

}
