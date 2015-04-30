package com.bonovo.musicplayer.playlist;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zybo on 9/18/14.
 */
public class PlayList {
    private PlayListStore mPlayListStore;
    private WeakReference<PlayListListener> mListener;
    private String mName;
    private long mId;
    private List<PlayListItem> mPlayListItems;

    public PlayList(String name, PlayListStore playListStore) {
        mPlayListStore = playListStore;
        mPlayListItems = new ArrayList<PlayListItem>();
        mName = name;
    }

    public void load() {
        if (!mPlayListItems.isEmpty())
            mPlayListItems.clear();
        mPlayListItems = mPlayListStore.getPlayListItems();
    }

    public long getId() {
        return mId;
    }

    public void setListener(final PlayListListener listener) {
        if (listener == null)
            mListener = null;
        else
            mListener = new WeakReference<PlayListListener>(listener);
    }

    public void add(final PlayListItem item) {

    }

    public void add(final List<PlayListItem> items) {

    }

    public void delete(final PlayListItem item) {

    }

    public void delete(final List<PlayListItem> items) {

    }

    public String getName() {
        return mName;
    }

    public interface PlayListListener {
        public void onItemAdded();

        public void onItemDeleted();
    }

    public class PlayListItem {

    }
}
