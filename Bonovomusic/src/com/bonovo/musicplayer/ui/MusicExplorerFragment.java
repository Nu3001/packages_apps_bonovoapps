package com.bonovo.musicplayer.ui;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bonovo.musicplayer.BonovoMusicExplorerActivity;
import com.bonovo.musicplayer.R;

import java.util.List;

/**
 * Created by zybo on 9/19/14.
 */
public class MusicExplorerFragment extends ListFragment
        implements BonovoMusicExplorerActivity.ActivityKeyListener {
    protected BonovoMusicExplorerActivity.OnPlayListener mOnPlayListener;

    public BonovoMusicExplorerActivity.OnPlayListener getOnPlayListener() {
        return mOnPlayListener;
    }

    public void setOnPlayListener(BonovoMusicExplorerActivity.OnPlayListener listener) {
        mOnPlayListener = listener;
    }

    public void playList(List<Long> list, int position) {
        if (mOnPlayListener != null) {
            mOnPlayListener.onPlay(list, position);
        }
    }

    @Override
    public boolean onKeyBack() {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final FrameLayout view = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);
        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.setMargins(10, 10, 10, 10);
        view.setBackgroundResource(R.drawable.explorer_detail_background2);
        return view;
    }
}
