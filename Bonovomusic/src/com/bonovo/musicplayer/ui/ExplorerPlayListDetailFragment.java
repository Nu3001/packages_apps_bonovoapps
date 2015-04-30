package com.bonovo.musicplayer.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.bonovo.musicplayer.MediaStoreUtil;
import com.bonovo.musicplayer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zybo on 9/24/14.
 */
public class ExplorerPlayListDetailFragment
        extends MusicExplorerFragment
        implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final String KEY_PLAY_LIST_NAME = "play_list_name";
    public static final String KEY_PLAY_LIST_ID = "play_list_id";
    private static final String TAG = "ExplorerPlayListDetailFragment";
    private static final boolean D = true;
    private PlayListManager.PlayListManagerCallback mCallback;
    private String mPlayListName;
    private ListView mListView;
    private Activity mActivity;
    private long mPlayListId;
    private SimpleCursorAdapter mAdapter;
    private List<String> mMusicTitleList;
    private List<Long> mMusicIDList;
    private Object mMusicListLock;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPlayListName = getArguments().getString(KEY_PLAY_LIST_NAME);
        mPlayListId = getArguments().getLong(KEY_PLAY_LIST_ID);
        mMusicTitleList = new ArrayList<String>();
        mMusicIDList = new ArrayList<Long>();
        mMusicListLock = new Object();
        getLoaderManager().initLoader(0, getArguments(), this);
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.explorer_play_list_detail_layout, null);
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        view.findViewById(R.id.explorer_play_list_add).setVisibility(View.INVISIBLE);
//        view.findViewById(R.id.explorer_play_list_delete).setVisibility(View.INVISIBLE);
//        final TextView textView = (TextView) view.findViewById(R.id.explorer_play_list_title);
//        textView.setText(mPlayListName);
//        mListView = (ListView) view.findViewById(R.id.explorer_detail_list);
        mListView = getListView();
        mAdapter = new SimpleCursorAdapter(mActivity, R.layout.explorer_play_list_music_layout,
                null,
                new String[]{
                        MediaStore.Audio.Playlists.Members.TITLE,
                        MediaStore.Audio.Playlists.Members.ARTIST,
                        MediaStore.Audio.Playlists.Members.ALBUM,
                        MediaStore.Audio.Playlists.Members._ID,
                },
                new int[]{
                        R.id.explorer_play_list_music_title,
                        R.id.explorer_play_list_music_artist,
                        R.id.explorer_play_list_music_album,
                        R.id.explorer_play_list_music_album,
                },
                0
        );
        mListView.setAdapter(mAdapter);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                final int id = view.getId();
                switch (id) {
                    case R.id.explorer_play_list_music_title:
                        final String title = cursor.getString(columnIndex);
                        final TextView titleText = (TextView) view;
                        titleText.setText(title);
                        titleText.setOnClickListener(ExplorerPlayListDetailFragment.this);
                        break;
                    case R.id.explorer_play_list_music_artist:
                        final String artist = cursor.getString(columnIndex);
                        final TextView artistText = (TextView) view;
                        artistText.setText(artist);
                        artistText.setOnClickListener(ExplorerPlayListDetailFragment.this);
                        break;
                    case R.id.explorer_play_list_music_album:
                        if (columnIndex == cursor.getColumnIndex(MediaStore.Audio.Playlists.Members._ID)) {
                            final View parentView = (View) view.getParent();
                            final ImageButton deleteButton = (ImageButton) parentView.
                                    findViewById(R.id.explorer_play_list_music_delete);
                            deleteButton.setOnClickListener(ExplorerPlayListDetailFragment.this);
                            deleteButton.setTag(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID)));
                        } else {
                            final String album = cursor.getString(columnIndex);
                            final TextView albumText = (TextView) view;
                            albumText.setText(album);
                            albumText.setOnClickListener(ExplorerPlayListDetailFragment.this);
                        }
                        break;
//                    case R.id.explorer_play_list_music_delete:
//                        final ImageButton deleteButton = (ImageButton) view;
//                        deleteButton.setOnClickListener(ExplorerPlayListDetailFragment.this);
//                        break;
                }
                return true;
            }
        });
        setListShown(false);
    }

    public void setCallback(final PlayListManager.PlayListManagerCallback callback) {
        mCallback = callback;
    }

    @Override
    public boolean onKeyBack() {
        mCallback.onHidePlayListDetail();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.explorer_play_list_music_title:
//            case R.id.explorer_play_list_music_artist:
//            case R.id.explorer_play_list_music_album:
                if (D)
                    Log.d(TAG, "play !");
                synchronized (mMusicListLock) {
                    for (int i = 0; i < mMusicTitleList.size(); i++) {
                        final TextView titleText = (TextView) v;
                        final String title = mMusicTitleList.get(i);
                        if (title.equals(titleText.getText())) {
                            mOnPlayListener.onPlay(mMusicIDList, i);
                        }
                    }
                }
                break;
            case R.id.explorer_play_list_music_delete:
                if (D)
                    Log.d(TAG, "delete !");
                final Long musicID = (Long) v.getTag();
                MediaStoreUtil.deleteFromPlayList(mActivity, mPlayListId, musicID);
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mActivity,
                MediaStore.Audio.Playlists.Members.getContentUri("external", args.getLong(KEY_PLAY_LIST_ID)),
//                null, null, null, null);
                new String[]{
                        MediaStore.Audio.Playlists.Members._ID,
                        MediaStore.Audio.Playlists.Members.AUDIO_ID,
                        MediaStore.Audio.Playlists.Members.TITLE,
                        MediaStore.Audio.Playlists.Members.ARTIST,
                        MediaStore.Audio.Playlists.Members.ALBUM,
                }, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        int position = data.getPosition();
        if (position == -1)
            data.moveToFirst();
        for (int i = 0; i < data.getCount(); i++) {
            data.moveToPosition(i);
            synchronized (mMusicListLock) {
                int index = data.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
                final long id = data.getLong(index);
                mMusicIDList.add(data.getLong(index));
                index = data.getColumnIndex(MediaStore.Audio.Playlists.Members.TITLE);
                final String title = data.getString(index);
                mMusicTitleList.add(data.getString(index));
            }
        }

        data.moveToFirst();
        mAdapter.swapCursor(data);
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        synchronized (mMusicListLock) {
            mMusicTitleList.clear();
            mMusicIDList.clear();
        }
    }
}