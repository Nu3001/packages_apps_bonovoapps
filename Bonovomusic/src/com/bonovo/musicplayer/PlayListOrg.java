package com.bonovo.musicplayer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PlayListOrg {
    private static final String TAG = "PlayList";
    private static final boolean D = true;
    private WeakReference<Context> mContextRef;

    /* package */static final String PLAY_LIST_NAME = "bonovo_play_list";

    private List<String> mTitleList = new ArrayList<String>();
    private List<Long> mIdList = new ArrayList<Long>();

    private boolean isInited = false;
    // private Cursor mPlayListCursor;
    private long mPlayListId;

    PlayListOrg(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }
        mContextRef = new WeakReference<Context>(context);
    }

    void init() {
        if (isInited)
            return;
        Context context = mContextRef.get();
        if (context == null)
            return;
        Cursor c = BonovoMusicPlayerUtil.getPlayList(context, PLAY_LIST_NAME);
        if (c != null) {
            if (!c.moveToFirst()) {
                c.close();
                return;
            }
            mPlayListId = c.getLong(c
                    .getColumnIndex(MediaStore.Audio.Playlists._ID));
            // mPlayListCursor = c;
            // isInited = true;
        }
        isInited = true;
    }

    void add(long audioId) {
        if (!isInited)
            throw new IllegalStateException("play list is not inited!");

        final Context context = mContextRef.get();
        if (context != null) {
            BonovoMusicPlayerUtil.addToPlayList(context, mPlayListId, audioId);
        }
    }

    void delete(String title) {
        if (!isInited)
            throw new IllegalStateException("play list is not inited!");
        final Context context = mContextRef.get();
        if (context == null)
            return;
        Cursor c = BonovoMusicPlayerUtil.getMusicByTitle(context, title);
        if (c != null) {
            long audioId;
            if (c.moveToFirst()) {
                audioId = c.getLong(c
                        .getColumnIndex(MediaStore.Audio.Media._ID));
                c.close();
                Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                        "external", mPlayListId);
                context.getContentResolver().delete(uri,
                        MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?",
                        new String[]{String.valueOf(audioId)});
            }
        }
    }

    void empty() {
        if (!isInited)
            throw new IllegalStateException("play list is not inited!");
        final Context context = mContextRef.get();
        if (context != null) {
            BonovoMusicPlayerUtil.clearPlayList(context, mPlayListId);
        }
    }

    final List<String> getPlayList() {
        if (!isInited)
            throw new IllegalStateException("play list is not inited!");
        final Context context = mContextRef.get();
        if (context == null)
            return null;
        mTitleList.clear();
        Cursor c = BonovoMusicPlayerUtil.getMusicByPlayList(context,
                mPlayListId);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    long audioId = c
                            .getLong(c
                                    .getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID));
                    String title = getTitle(audioId);
                    if (title != null) {
                        mIdList.add(audioId);
                        mTitleList.add(title);
                        if (D)
                            Log.d(TAG, "add element , ID :" + audioId
                                    + ", title :" + title);
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
        return mTitleList;
    }

    public final List<Long> getIdList() {
        return mIdList;
    }

    private String getTitle(long id) {
        if (!isInited)
            throw new IllegalStateException("play list is not inited!");
        final Context context = mContextRef.get();
        if (context == null)
            return null;
        String title = null;
        Cursor c = BonovoMusicPlayerUtil.getMusicById(context, id);
        if (c != null) {
            if (c.moveToFirst()) {
                title = c.getString(c
                        .getColumnIndex(MediaStore.Audio.Media.TITLE));
            }
        }
        return title;
    }

    public void destroy() {
        mContextRef = null;
        if (mIdList != null) {
            mIdList.clear();
            mIdList = null;
        }
        if (mTitleList != null) {
            mTitleList.clear();
            mTitleList = null;
        }
        isInited = false;
    }
}
