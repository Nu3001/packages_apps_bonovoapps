package com.bonovo.musicplayer.playlist;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.bonovo.musicplayer.MediaStoreUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zybo on 9/18/14.
 */
public class AndroidPlayListStore implements PlayListStore {

    private WeakReference<Context> mContextRef;

    public AndroidPlayListStore(final Context context) {
        mContextRef = new WeakReference<Context>(context);
    }

    @Override
    public List<PlayList> getPlayLists() {
        final Context context = mContextRef.get();
        if (context == null)
            return null;
        final ArrayList<PlayList> playLists = new ArrayList<PlayList>();
        final Cursor c = MediaStoreUtil.queryPlayList(context);
        if (c.moveToFirst()) {
            do {
                playLists.add(cursor2Playlist(c));
            } while (c.moveToNext());
        }
        c.close();
        return playLists;
    }

    private PlayList cursor2Playlist(final Cursor cursor) {

        return null;
    }

    @Override
    public void createPlayList(String name) {
        final Context context = mContextRef.get();
        if (context == null)
            return;
        MediaStoreUtil.insertPlayList(context, name);
    }

    @Override
    public void deletePlayList(long id) {
        final Context context = mContextRef.get();
        if (context == null)
            return;
    }

    @Override
    public List<PlayList.PlayListItem> getPlayListItems() {
        return null;
    }
}
