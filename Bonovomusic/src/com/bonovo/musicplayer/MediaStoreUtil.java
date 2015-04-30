package com.bonovo.musicplayer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zybo on 9/18/14.
 */
public class MediaStoreUtil {
    public static final int FILE_TYPE_MP3 = 1;
    private static final int FIRST_AUDIO_FILE_TYPE = FILE_TYPE_MP3;
    public static final int FILE_TYPE_M4A = 2;
    public static final int FILE_TYPE_WAV = 3;
    public static final int FILE_TYPE_AMR = 4;
    public static final int FILE_TYPE_AWB = 5;
    public static final int FILE_TYPE_WMA = 6;
    public static final int FILE_TYPE_OGG = 7;
    public static final int FILE_TYPE_AAC = 8;
    public static final int FILE_TYPE_MKA = 9;
    public static final int FILE_TYPE_FLAC = 10;
    public static final int FILE_TYPE_APE = 11;
    public static final int FILE_TYPE_3GP_AUDIO = 12;
    private static final int LAST_AUDIO_FILE_TYPE = FILE_TYPE_3GP_AUDIO;
    private static final String VOLUME_NAME = "external";
    private static final Uri PLAY_LIST_URI =
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
    private static final String[] PLAY_LIST_COLUMNS = new String[]{
            MediaStore.Audio.PlaylistsColumns.NAME,
            MediaStore.Audio.Playlists._ID
    };
    private static final String TAG = "MediaStoreUtil";
    private static final Set<String> AUDIO_FILE_TYPE = new HashSet<String>();

    static {
        AUDIO_FILE_TYPE.add("MP3");
        AUDIO_FILE_TYPE.add("MPGA");
        AUDIO_FILE_TYPE.add("MP1");
        AUDIO_FILE_TYPE.add("MP2");
        AUDIO_FILE_TYPE.add("M4A");
        AUDIO_FILE_TYPE.add("WAV");
        AUDIO_FILE_TYPE.add("AMR");
        AUDIO_FILE_TYPE.add("AWB");
        AUDIO_FILE_TYPE.add("AMR");
        AUDIO_FILE_TYPE.add("WMA");
        AUDIO_FILE_TYPE.add("FLAC");
        AUDIO_FILE_TYPE.add("APE");
        AUDIO_FILE_TYPE.add("OGG");
        AUDIO_FILE_TYPE.add("OGA");
        AUDIO_FILE_TYPE.add("AAC");
        AUDIO_FILE_TYPE.add("MKA");
        AUDIO_FILE_TYPE.add("3GP");
        AUDIO_FILE_TYPE.add("3GPP");
        AUDIO_FILE_TYPE.add("3G2");
        AUDIO_FILE_TYPE.add("3GPP2");
        AUDIO_FILE_TYPE.add("MID");
        AUDIO_FILE_TYPE.add("MIDI");
        AUDIO_FILE_TYPE.add("XMF");
        AUDIO_FILE_TYPE.add("RTTTL");
        AUDIO_FILE_TYPE.add("SMF");
        AUDIO_FILE_TYPE.add("IMY");
        AUDIO_FILE_TYPE.add("RTX");
        AUDIO_FILE_TYPE.add("OTA");
        AUDIO_FILE_TYPE.add("MXMF");
    }

    private MediaStoreUtil() {
    }

    public static void clear(final Context context) {
        if (context == null)
            return;
        final ContentResolver contentResolver = context.getContentResolver();
//        contentResolver.delete(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null);
//        contentResolver.delete(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, null, null);
//        contentResolver.delete(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null);
        final Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DATA},
                null,
                null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null)
            return;
        cursor.moveToFirst();
        while (cursor.isAfterLast()) {
            final String path = cursor.getString(
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            final File file = new File(path);
            if (!file.exists()) {
//                final Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
//                contentResolver.delete(uri, null, null);
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)));
            }
            cursor.moveToNext();
        }
        cursor.close();
    }

    public static Cursor queryPlayList(final Context context) {
        if (context == null)
            return null;
        final ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.query(
                PLAY_LIST_URI,
                null,
                null,
                null,
                MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
    }

    public static void insertPlayList(Context context, String name) {
        final ContentResolver contentResolver = context.getContentResolver();
        final ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.PlaylistsColumns.NAME, "name");
        contentResolver.insert(PLAY_LIST_URI, values);
    }

    public static Cursor queryPlayListMember(final Context context, final long playListId) {
        if (context == null)
            return null;
        final ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.query(
                MediaStore.Audio.Playlists.Members.getContentUri(VOLUME_NAME, playListId),
                null,
                null,
                null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER
        );
    }

    public static long checkMediaFile(Context context, String filePath) {
        long result = -1;
        if (context == null)
            return result;

        final ContentResolver contentResolver = context.getContentResolver();
        final Cursor c = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID},
                MediaStore.Audio.Media.DATA + "=?",
                new String[]{filePath},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        );
        if (c.moveToFirst()) {
            result = c.getLong(c.getColumnIndex(MediaStore.Audio.Media._ID));
        }
        c.close();
        return result;
    }

    public static List<Long> createDefaultList(final Context context) {
        if (context == null)
            return null;
        final ContentResolver contentResolver = context.getContentResolver();
        final Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID},
                null,
                null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null)
            return null;
        final List<Long> idList = new ArrayList<Long>();
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                idList.add(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            }
        }
        cursor.close();
        return idList;
    }

    public static void scan(final Context context) {
        if (context == null)
            return;
        MediaScannerConnection.scanFile(context,
                FileExplorer.getExternalRootFolders(), null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        if (Config.Debug.D)
                            Log.d(TAG, "media scanner , path : " + path + " , uri : " + uri);
                    }
                }
        );
    }

    public static void deleteFromPlayList(final Context context, final long playListId,
                                          final long musicId) {
        if (context == null)
            return;
        final ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(MediaStore.Audio.Playlists.Members.getContentUri("external",
                        playListId), MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?",
                new String[]{String.valueOf(musicId)});
    }

    public static Cursor openPlayListCursor(final Context context) {
        if (context == null)
            return null;
        final ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
    }

    public static Cursor openPlayListMusicCursor(final Context context, final long playListId) {
        if (context == null || playListId < 0)
            return null;

        final ContentResolver contentResolver = context.getContentResolver();
        return contentResolver.query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playListId),
                null, null, null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
    }

    public static boolean hasPlayList(final Context context, final String name) {
        final ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Playlists._ID},
                MediaStore.Audio.Playlists.NAME + "=?",
                new String[]{name}, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
        boolean result = false;
        if (cursor != null) {
            result = cursor.getCount() > 0;
            cursor.close();
        }
        return result;
    }

    public static long createPlayList(final Context context, final String name) {
        if (context == null || name == null)
            return -1;
        long result = -1;
        final ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Playlists._ID},
                MediaStore.Audio.Playlists.NAME + "=?",
                new String[]{name}, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
        if (cursor.getCount() < 1) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Audio.Playlists.NAME, name);
            contentResolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, contentValues);

            cursor = contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Playlists._ID},
                    MediaStore.Audio.Playlists.NAME + "=?",
                    new String[]{name}, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
        }
        cursor.moveToFirst();
        result = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
        cursor.close();
        return result;
    }

    public static void deletePlayList(final Context context, final String name) {
        if (context == null || name == null)
            return;
        final ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Playlists.NAME + "=?",
                new String[]{name}
        );
    }

    public static long getPlayListIdByName(final Context context, final String name) {
        long result = -1;
        if (context == null)
            return result;
        if (name == null || name.trim().isEmpty())
            return result;
        final ContentResolver contentResolver = context.getContentResolver();
        final Cursor c = contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Playlists._ID},
                MediaStore.Audio.Playlists.NAME + "=?",
                new String[]{name},
                MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
        if (c.moveToFirst()) {
            result = c.getLong(c.getColumnIndex(MediaStore.Audio.Playlists._ID));
        }
        c.close();
        return result;
    }

    public static void addToPlayList(final Context context, long playListId,
                                     long audioId) {
        ContentResolver resolver = context.getContentResolver();
        String[] cols = new String[]{"count(*)"};
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
                playListId);
        Cursor q = resolver.query(uri, null,
                MediaStore.Audio.Playlists.Members.AUDIO_ID + "=" + audioId,
                null, null);
        if (q != null) {
            if (!q.moveToFirst()) {
                Cursor cur = resolver.query(uri, cols, null, null, null);
                cur.moveToFirst();
                int base = cur.getInt(0);
                cur.close();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
                values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER,
                        base + 1);
                resolver.insert(uri, values);
            }
            q.close();
        }
    }

    public static long getMusicIdByPath(final Context context, String path) {
        long result = -1;
        if (context == null)
            return result;
        if (path == null || path.trim().isEmpty())
            return result;
        final Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID},
                MediaStore.Audio.Media.DATA + "= ?",
                new String[]{path}, null);
        if (c.moveToFirst()) {
            result = c.getLong(c.getColumnIndex(MediaStore.Audio.Media._ID));
        }
        c.close();
        return result;
    }

    public static void likeMedia(final Context context, final long id) {
        final String playListName = context.getResources().getString(R.string.explorer_play_list_i_like);
        final long playListId = createPlayList(context, playListName);
        if (playListId != -1)
            addToPlayList(context, playListId, id);
    }

    public static Set<String> getPlayListMembers(final Context context, final long playListId) {
        if (context == null || playListId < 0)
            throw new IllegalArgumentException();
        final ContentResolver contentResolver = context.getContentResolver();
        final Cursor cursor = contentResolver.query(MediaStore.Audio.Playlists.Members.getContentUri("external", playListId),
                new String[]{MediaStore.Audio.Playlists.Members.DATA},
                null,
                null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
        cursor.moveToFirst();
        final HashSet<String> result = new HashSet<String>();
        while (!cursor.isAfterLast()) {
            result.add(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.DATA)));
            cursor.moveToNext();
        }
        return result;
    }

    public static boolean isAudioFile(final String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
//            final String[] parts = fileName.split(".");
//            if (parts == null)
//                return false;
//            final String extension = parts[parts.length - 1];
            final String prefix = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (AUDIO_FILE_TYPE.contains(prefix.toUpperCase()))
                return true;
        }
        return false;
    }

    public static void unlikeMedia(final Context context, final long id) {
        final String playListName = context.getResources().getString(R.string.explorer_play_list_i_like);
        final long playListId = createPlayList(context, playListName);
        if (playListId != -1)
            deleteFromPlayList(context, playListId, id);
    }

    public static boolean isMediaLiked(final Context context, final long id) {
        final String playListName = context.getResources().getString(R.string.explorer_play_list_i_like);
        if (!hasPlayList(context, playListName))
            return false;
        final long playListId = createPlayList(context, playListName);
        final ContentResolver contentResolver = context.getContentResolver();
        final Cursor cursor = contentResolver.query(MediaStore.Audio.Playlists.Members.getContentUri("external", playListId),
                new String[]{MediaStore.Audio.Playlists.Members._ID},
                MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?",
                new String[]{String.valueOf(id)},
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER
        );
        boolean liked = cursor.getCount() > 0;
        cursor.close();
        return liked;
    }
}
