package com.bonovo.musicplayer;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bonovo.musicplayer.ui.ExplorerAlbumFragment;
import com.bonovo.musicplayer.ui.ExplorerArtistFragment;
import com.bonovo.musicplayer.ui.ExplorerFolderFragment;
import com.bonovo.musicplayer.ui.ExplorerGenresFragment;
import com.bonovo.musicplayer.ui.ExplorerPlayListFragment;
import com.bonovo.musicplayer.ui.ExplorerPlayListFragment_bak;
import com.bonovo.musicplayer.ui.MusicExplorerFragment;

import java.util.List;

/**
 * Created by zybo on 9/19/14.
 */
public class BonovoMusicExplorerActivity extends Activity
        implements AbsListView.OnItemClickListener, ServiceConnection {
    private static final String TAG = "BonovoMusicExplorerActivity";
    private static final boolean D = true;
    private static final String STATE_CATEGORY = "state";
    private ListView mCategoryList;
    private int mCurrentCategory;
    private MusicExplorerFragment mCurrentFragment;
    private IMusicPlayerService mService;
    private OnPlayListener mOnPlayListener;
    private boolean mServiceConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mOnPlayListener = new BonovoOnPlayListener();
        setContentView(R.layout.music_explorer_layout);
        setupViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCategoryList.performItemClick(null, mCurrentCategory, 0);
        BonovoMusicPlayerUtil.bindPlayBackService(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CATEGORY, mCurrentCategory);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentCategory = savedInstanceState.getInt(STATE_CATEGORY);
    }

    private void setupViews() {
        mCategoryList = (ListView) findViewById(R.id.explorer_category_list);
        final CharSequence[] categoryArray = getResources().getTextArray(R.array.music_category);
        mCategoryList.setAdapter(new ArrayAdapter<CharSequence>(this, R.layout.music_explorer_category_layout,
                android.R.id.text1, categoryArray));
        mCategoryList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mCategoryList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (D) {
            Log.d(TAG, "item clicked : " + position);
        }
        MusicExplorerFragment fragment = null;
        switch (position) {
            case 0:
                fragment = new ExplorerFolderFragment();
                break;
            case 1:
                fragment = new ExplorerPlayListFragment();
                break;
            case 2:
                fragment = new ExplorerArtistFragment();
                break;
            case 3:
                fragment = new ExplorerGenresFragment();
                break;
            case 4:
                fragment = new ExplorerAlbumFragment();
                break;
        }
        if (fragment != null) {
            fragment.setOnPlayListener(mOnPlayListener);
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().
                    replace(R.id.explorer_detail_container, fragment).commit();
            mCurrentFragment = fragment;
        }
    }

    @Override
    public void onBackPressed() {
        if (!mCurrentFragment.onKeyBack())
            finish();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (D)
            Log.d(TAG, "onServiceConnected");
        mService = ((BonovoMusicPlayerUtil.ServiceBinder) service).getService();
        mServiceConnected = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        //mService.unregisterStatusListener();
        mServiceConnected = false;
    }

    public interface ActivityKeyListener {
        public boolean onKeyBack();
    }

    public interface OnPlayListener {
        public void onPlay(List<Long> fileList, int position);
    }

    private class BonovoOnPlayListener implements OnPlayListener {
        @Override
        public void onPlay(List<Long> fileList, int position) {
            if (mService != null) {
                mService.open(fileList, position);
                mService.play();
            }
            finish();
        }
    }
}
