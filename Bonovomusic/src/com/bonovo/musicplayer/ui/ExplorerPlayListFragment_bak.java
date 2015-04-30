package com.bonovo.musicplayer.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.bonovo.musicplayer.MediaStoreUtil;
import com.bonovo.musicplayer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zybo on 9/23/14.
 */
public class ExplorerPlayListFragment_bak extends MusicExplorerFragment {
    private static final String TAG = "ExplorerPlayListFragment";
    private static final boolean D = true;
    private ListView mListView;
    private Activity mActivity;
    private LoaderManager mLoaderManager;
    private PlayListOnClickListener mPlayListOnClickListener;
    private PlayListManager.PlayListManagerCallback mCallback;
    private Fragment mCurrentFragment;
    private Map<String, Long> mPlayListMap;
    private List<Long> mMusicIdList;
    private int mPlayPosition;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mPlayListMap = new HashMap<String, Long>();
        mPlayListOnClickListener = new PlayListOnClickListener();
        mMusicIdList = new ArrayList<Long>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.explorer_play_list_layout, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mListView = (ListView) view.findViewById(R.id.explorer_folder_list);
        final PlayListAdapter adapter = new PlayListAdapter(
                mActivity,
                R.layout.explorer_play_list_item,
                MediaStoreUtil.openPlayListCursor(mActivity),
                new String[]{
                        MediaStore.Audio.Playlists.NAME,
                        MediaStore.Audio.Playlists.NAME,
                        MediaStore.Audio.Playlists.NAME,
                },
                new int[]{
                        R.id.explorer_play_list_title,
                        R.id.explorer_play_list_add,
                        R.id.explorer_play_list_delete,
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        mListView.setAdapter(adapter);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                final int id = view.getId();
                final String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
                view.setTag(name);
                switch (id) {
                    case R.id.explorer_play_list_title:
                        final TextView textView = (TextView) view;
                        textView.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME)));
//                        textView.setOnClickListener(mPlayListOnClickListener);
                        final long playListId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                        mPlayListMap.put(name, playListId);
                        break;
                    case R.id.explorer_play_list_add:
                    case R.id.explorer_play_list_delete:
//                        view.setOnClickListener(mPlayListOnClickListener);
                        break;
                }
                return true;
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (D)
                    Log.d(TAG, "onItemClick .... position : " + position);
            }
        });
    }

    public void setCallback(PlayListManager.PlayListManagerCallback callback) {
        mCallback = callback;
    }

    @Override
    public boolean onKeyBack() {
        if (mCurrentFragment != null) {
            getFragmentManager().popBackStack();
            mCurrentFragment = null;
            return true;
        }
        return super.onKeyBack();
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    private class PlayListOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final String tag = (String) v.getTag();
            if (v.getId() == R.id.explorer_play_list_add) {
                if (D)
                    Log.d(TAG, "add , tag : " + tag);
                FragmentManager fm = getFragmentManager();
                mCurrentFragment = new ExplorerPlayListDetailFragment();
                fm.beginTransaction().addToBackStack(null).
                        replace(R.id.explorer_detail_container, mCurrentFragment).commit();
            }
            if (v.getId() == R.id.explorer_play_list_delete) {
                if (D)
                    Log.d(TAG, "delete , tag : " + tag);
            }
            if (v.getId() == R.id.explorer_play_list_title) {
                if (D)
                    Log.d(TAG, "text pressed!");
//                final TextView textView = (TextView) v;
                FragmentManager fm = getFragmentManager();
                final Fragment fragment = new ExplorerPlayListDetailFragment();
                final Bundle args = new Bundle();
                final String name = (String) v.getTag();
                args.putString(ExplorerPlayListDetailFragment.KEY_PLAY_LIST_NAME, name);
                args.putLong(ExplorerPlayListDetailFragment.KEY_PLAY_LIST_ID, mPlayListMap.get(name));
                fragment.setArguments(args);
                mCurrentFragment = fragment;
                fm.beginTransaction().replace(R.id.explorer_detail_container, mCurrentFragment)
                        .addToBackStack(null).commit();
            }
        }
    }

    private class PlayListAdapter extends SimpleCursorAdapter {
        public PlayListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public int getCount() {
            return super.getCount() + 1;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position < getCount() - 1)
                return super.getView(position, convertView, parent);
            else {
                final LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                return inflater.inflate(R.layout.explorer_play_list_add_layout, null);
            }
        }
    }
}
