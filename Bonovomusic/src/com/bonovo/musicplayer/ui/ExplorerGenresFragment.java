package com.bonovo.musicplayer.ui;

import android.content.ContentResolver;
import android.content.CursorLoader;
import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Created by zybo on 10/9/14.
 */
public class ExplorerGenresFragment extends ExplorerCursorFragment {


    @Override
    protected CursorLoader getMainLoader() {
        return new CursorLoader(getActivity(),
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);
    }

    @Override
    protected CursorLoader getDetailLoader(String type) {
        final ContentResolver contentResolver = getActivity().getContentResolver();
        final Cursor c = contentResolver.query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Genres._ID},
                MediaStore.Audio.Genres.NAME + "=?",
                new String[]{type},
                MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);
        if (c.moveToFirst()) {
            final long id = c.getLong(c.getColumnIndex(MediaStore.Audio.Genres._ID));
            return new CursorLoader(getActivity(),
                    MediaStore.Audio.Genres.Members.getContentUri("external", id),
                    null,
                    null,
                    null,
                    MediaStore.Audio.Genres.Members.DEFAULT_SORT_ORDER
                    );
        }
        return null;
    }

    @Override
    protected String getMainTextColumn() {
        return MediaStore.Audio.Genres.NAME;
    }

    @Override
    protected String getDetailTextColumn() {
        return MediaStore.Audio.Genres.Members.TITLE;
    }
}
