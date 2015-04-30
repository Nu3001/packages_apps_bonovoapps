package com.bonovo.musicplayer.playlist;

import java.util.List;

/**
 * Created by zybo on 9/18/14.
 */
public interface PlayListStore {
    public List<PlayList> getPlayLists();
    public void createPlayList(String name);
    public void deletePlayList(long id);
    public List<PlayList.PlayListItem> getPlayListItems();
}
