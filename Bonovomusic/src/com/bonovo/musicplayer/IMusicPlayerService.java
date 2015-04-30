package com.bonovo.musicplayer;

import java.util.List;

public interface IMusicPlayerService {
    public static final String SERVICE_COMMAND_PLAY = "command_play";
    public static final String SERVICE_COMMAND_NEXT = "command_next";
    public static final String SERVICE_COMMAND_PRE = "command_pre";
    public static final String SERVICE_COMMAND_STOP = "command_stop";
    public static final int MODE_ALL = 1;
    public static final int MODE_SINGLE = 2;
    public static final int MODE_RANDOM = 3;

    public List<String> getLRCResult();

    public boolean parseLRC();

    void open(List<Long> list, int position);

//    void open(String path);

    void openAPE(String path, String selectedFileList);

    // void open(String folder, List<String> list, int position);

    //
    // int getQueuePosition();
    //
    void stopService();

    void pause();

    void scan();

    boolean play();

    void prev();

    void next();

    void showNotification();

    void cancelNotification();

    //
    // long duration();
    //
    long position();

    //
    long seek(long pos);

    int getPlayMode();

    //
    // String getTrackName();
    //
    // String getAlbumName();
    //
    // //
    // // long getAlbumId();
    // //
    // String getArtistName();
    //
    // long getArtistId();
    //
    // void enqueue(long[] list, int action);
    //
    // long[] getQueue();
    //
    // void moveQueueItem(int from, int to);
    //
    // void setQueuePosition(int index);
    //
    // String getPath();
    //
    // String getPrePath();
    //
    // String getNextPath();
    //
    // long getAudioId();
    //
    void setPlayMode(int playMode);

    boolean isPlaying();

    //
    // int removeTracks(int first, int last);
    //
    // int removeTrack(long id);
    //
    // void setRepeatMode(int repeatmode);
    //
    // int getRepeatMode();
    //
    // int getMediaMountedCount();
    //
    // int getAudioSessionId();

    MusicStatus getCurrentStatus();

    void likeCurrent();

    void registerStatusListener(final MusicStatusChangeListener listener);

    void unregisterStatusListener(MusicStatusChangeListener listener);

    public void setScanListener(MusicScanListener listener);

    interface MusicStatusChangeListener {
        public void onStatusChange(final MusicStatus status);
    }

    interface MusicScanListener {
        public void onStart();

        public void onStop();
    }

    static class MusicStatus {
        final String currMusic;
        final String preMusic;
        final String nextMusic;
        final String currentAlbum;
        final String currentGenre;
        final String currentArtist;
        final long duration;
        final String albumArt;
        final boolean like;
        final boolean scanning;
	 final String currMusicFile;

        // final String sumb;

        MusicStatus(final String cM, final String pM, final String nM,
                    final String cAl, final String cS, final String cAr, long d,
                    String aa, final boolean l, final boolean s,String f) {
            currMusic = cM;
            preMusic = pM;
            nextMusic = nM;
            currentAlbum = cAl;
            currentGenre = cS;
            currentArtist = cAr;
            duration = d;
            albumArt = aa;
            like = l;
            scanning = s;
	     currMusicFile=f;
        }
    }


}
