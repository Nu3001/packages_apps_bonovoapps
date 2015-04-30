package com.bonovo.musicplayer.ui;

import android.content.CursorLoader;
import android.provider.MediaStore;

/**
 * Created by zybo on 10/9/14.
 */
public class ExplorerArtistFragment extends ExplorerCursorFragment {




    @Override
    protected CursorLoader getMainLoader() {
        return new CursorLoader(getActivity(),
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
    }

    @Override
    protected CursorLoader getDetailLoader(String type) {
        return new CursorLoader(getActivity(),
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.ARTIST + "=?",
                new String[]{type},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        );
    }

    @Override
    protected String getMainTextColumn() {
        return MediaStore.Audio.Artists.ARTIST;
    }

    @Override
    protected String getDetailTextColumn() {
        return MediaStore.Audio.Media.TITLE;
    }
}
