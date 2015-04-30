package com.bonovo.musicplayer;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Binder;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bonovo.musicplayer.MusicListActivity.ListItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class BonovoMusicPlayerUtil {
    private static final String TAG = "BonovoMusicPlayerUtil";
    private static final boolean D = false;
    private static final Object[] sTimeArgs = new Object[5];
    private static final String[] ARTIST_COLUMNS = new String[]{MediaStore.Audio.Artists.ARTIST};
    private static final String[] ALBUM_COLUMNS = new String[]{MediaStore.Audio.Albums.ALBUM};
    private static final String[] MUSIC_COLUMNS = new String[]{
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA};
    private static final String[] MUSIC_INFO_COLUMNS = new String[]{
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA};
    private static final String[] ID_COLUMNS = new String[]{BaseColumns._ID};
    private static final String[] AUDIO_ID_COLUMNS = new String[]{MediaStore.Audio.Playlists.Members.AUDIO_ID};
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder,
            Locale.getDefault());

    // private static final String[] SCHOOL_COLUMNS = new String[] {
    // "distinct " + MediaStore.Audio.Media.
    // };

    private BonovoMusicPlayerUtil() {
    }

    public static boolean bindPlayBackService(final Activity context,
                                              final ServiceConnection connection) {
        if (context == null || connection == null) {
            return false;
        }
        ContextWrapper contextWrapper = new ContextWrapper(context);
        Intent serviceIntent = new Intent(context,
                BonovoAudioPlayerService.class);
        contextWrapper.startService(serviceIntent);
        contextWrapper.bindService(new Intent(context,
                        BonovoAudioPlayerService.class), connection,
                Service.BIND_AUTO_CREATE);
        return true;
    }

    public static String makeTimeString(Context context, long secs) {
        // String durationFormat = context
        // .getString(secs < 3600 ? R.string.durationformatshort
        // : R.string.durationformatlong);
        String durationFormat = context.getString(R.string.duration_format_long);
        sFormatBuilder.setLength(0);
        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;
        return sFormatter.format(durationFormat, timeArgs).toString();
    }

    public static void unbindPlayBackService(final Activity context,
                                             final ServiceConnection connection) {
        if (context == null) {
            return;
        }
        context.unbindService(connection);
    }

    public static void startListActivity(Activity activity) {
        if (D)
            Log.d(TAG, "start List Activity");
//        Intent activityIntent = new Intent(activity, MusicListActivity.class);
        Intent activityIntent = new Intent(activity, BonovoMusicExplorerActivity.class);
        activity.startActivity(activityIntent);
        return;
    }

    public static void startMusicSettingActivity(Activity activity) {
        // if (D) Log.d(TAG, "start List Activity");
        return;
    }

    public static Cursor getArtistCursor(final Context context) {
        final ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, ARTIST_COLUMNS,
                null, null, null);
        return c;
    }

    public static Cursor getAlbumCursor(final Context context) {
        final ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                ALBUM_COLUMNS, null, null,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        return c;
    }

    public static Cursor getMusicByArtist(final Context context, String artist) {
        ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MUSIC_COLUMNS, MediaStore.Audio.Media.ARTIST + " = ?",
                new String[]{artist},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        return c;
    }

    public static Cursor getMusicByTitle(final Context context, String title) {
        ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MUSIC_COLUMNS, MediaStore.Audio.Media.TITLE + " = ?",
                new String[]{title},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        return c;
    }

    static Cursor getMusicByAlbum(final Context context, String album) {
        ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MUSIC_COLUMNS, MediaStore.Audio.Media.ALBUM + " = ?",
                new String[]{album},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        return c;
    }

    static Cursor getMusicById(final Context context, long id) {
        ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MUSIC_INFO_COLUMNS, MediaStore.Audio.Media._ID + " = ?",
                new String[]{Long.toString(id)}, null);
        return c;
    }

    static Cursor getMusicByPlayList(final Context context, long playListId) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
                playListId);
        Cursor c = resolver.query(uri, AUDIO_ID_COLUMNS, null, null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
        return c;
    }

    static Cursor getPlayList(final Context context, String playListName) {
        ContentResolver resolver = context.getContentResolver();
        boolean isFound = false;
        Cursor playListCursor = resolver.query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, ID_COLUMNS,
                MediaStore.Audio.Playlists.NAME + " = ?",
                new String[]{playListName},
                MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
        if (playListCursor != null) {
            if (!playListCursor.moveToFirst()) {
                return null;
            }
            isFound = true;
        }
        if (!isFound) {
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Audio.Playlists.NAME, playListName);
            context.getContentResolver().insert(
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
            getPlayList(context, playListName);
        }
        return playListCursor;
    }

    static void clearPlayList(final Context context, long playListId) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
                playListId);
        resolver.delete(uri, null, null);
    }

    static void addToPlayList(final Context context, long playListId,
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

    static Cursor getMusicByPath(final Context context, String path) {
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MUSIC_INFO_COLUMNS, MediaStore.Audio.Media.DATA + "= ?",
                new String[]{path}, null);
        return c;
    }

    static Cursor getGenList(final Context context) {
        Cursor c = context
                .getContentResolver()
                .query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                        null,
                        MediaStore.Audio.Genres._ID
                                + " in (select distinct genre_id from audio_genres_map)",
                        null, MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);
        return c;
    }

    static String getGenreByAudioId(final Context context, long audioId) {
        Cursor c = context
                .getContentResolver()
                .query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                        null,
                        MediaStore.Audio.Genres._ID
                                + " in (select genre_id from audio_genres_map where audio_id = "
                                + String.valueOf(audioId) + ")", null, null);
        String genreName = null;
        if (c != null) {
            if (c.moveToFirst()) {
                genreName = c.getString(c
                        .getColumnIndex(MediaStore.Audio.Genres.NAME));
            }
            c.close();
        }
        return genreName;
    }

    static void setBackground(View v, Bitmap bm) {
        if (bm == null) {
            v.setBackgroundResource(0);
            return;
        }
        int vwidth = v.getWidth();
        int vheight = v.getHeight();
        int bwidth = bm.getWidth();
        int bheight = bm.getHeight();
        float scalex = (float) vwidth / bwidth;
        float scaley = (float) vheight / bheight;
        // float scale = Math.max(scalex, scaley) * 1.3f;
        float scale = Math.min(scalex, scaley);
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bg = Bitmap.createBitmap(vwidth, vheight, config);
        Canvas c = new Canvas(bg);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        Matrix matrix = new Matrix();
        matrix.setTranslate(-bwidth / (float) 2, -bheight / (float) 2);
        matrix.postScale(scale, scale);
        matrix.postTranslate(vwidth / (float) 2, vheight / (float) 2);
        c.drawBitmap(bm, matrix, paint);
        if (v instanceof ImageView) {
            final ImageView imageView = (ImageView) v;
            imageView.setImageBitmap(bg);
        } else
            v.setBackgroundDrawable(new BitmapDrawable(bg));
    }

    static Cursor getMusicBySchool(final Context context, String school) {
        ContentResolver resolver = context.getContentResolver();
        Cursor c = null;
        Cursor genresC = resolver.query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, ID_COLUMNS,
                MediaStore.Audio.Genres.NAME + " = ?", new String[]{school},
                MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);
        if (genresC != null) {
            if (!genresC.moveToFirst()) {
                genresC.close();
                return null;
            }
            long genresId = genresC.getLong(genresC
                    .getColumnIndex(MediaStore.Audio.Genres._ID));
            genresC.close();
            Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external",
                    genresId);
            c = resolver.query(uri, MUSIC_INFO_COLUMNS, null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        }
        return c;
    }

    static void checkExternalDirStatus() {
        // boolean mExternalStorageAvailable = false;
        // boolean mExternalStorageWriteable = false;
        // String state = Environment.getExternalStorageState();
        // if (Environment.MEDIA_MOUNTED.equals(state)) {
        // mExternalStorageAvailable = mExternalStorageWriteable = true;
        // } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
        // mExternalStorageAvailable = true;
        // mExternalStorageWriteable = false;
        // } else {
        // mExternalStorageAvailable = mExternalStorageWriteable = false;
        // }
    }

    static List<List<String>> getSubFolderFileLists(Context context, String path) {
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory())
            return null;
        String[] files = dir.list();
        if (files == null)
            return null;
        ArrayList<String> folderList = null;
        ArrayList<String> fileList = null;
        for (String filePath : files) {
            String fullFilePath = path + File.separator + filePath;
            File file = new File(fullFilePath);
            if (file.isDirectory()) {
                if (folderList == null) {
                    folderList = new ArrayList<String>();
                }
                folderList.add(filePath);
            } else {
                if (fileList == null)
                    fileList = new ArrayList<String>();
                Cursor c = context.getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Audio.Media._ID},
                        MediaStore.Audio.Media.DATA + "= ?",
                        new String[]{fullFilePath}, null);
                if (c != null && c.moveToFirst()) {
                    do {
                        fileList.add(filePath);
                    } while (c.moveToNext());
                    c.close();
                }
            }
        }
        ArrayList<List<String>> list = new ArrayList<List<String>>();
        list.add(folderList);
        list.add(fileList);
        return list;
    }

    // public static Cursor getSchoolCursor(Context context) {
    // final ContentResolver resolver = context.getContentResolver();
    // Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    // SCHOOL_COLUMNS,
    // null, null, null);
    // return c;
    // }

    static List<Long> getAudioIdsByName(final Context context, String folder,
                                        List<String> fileNameList) {
        if (context == null || fileNameList == null || fileNameList.size() == 0) {
            return null;
        }
        ArrayList<Long> ids = new ArrayList<Long>();
        for (String fileName : fileNameList) {
            String fullPath = folder != null ? folder + File.separator
                    + fileName : fileName;
            Cursor c = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media._ID},
                    MediaStore.Audio.Media.DATA + "= ?",
                    new String[]{fullPath}, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    ids.add(c.getLong(c
                            .getColumnIndex(MediaStore.Audio.Media._ID)));
                }
                c.close();
            }
        }
        return ids;
    }

    static Cursor getMusicCursor(final Context context) {
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MUSIC_COLUMNS,
                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        return c;
    }

    static List<ListItem> getFolderItem(List<List<String>> list) {
        if (list == null)
            return null;
        List<String> folderList = list.get(0);
        List<String> audioFileList = list.get(1);

        ArrayList<ListItem> viewList = null;
        if (folderList != null) {
            for (String folder : folderList) {
                if (viewList == null)
                    viewList = new ArrayList<ListItem>();
                ListItem item = new ListItem();
                item.id = R.drawable.browser_list_item_dir;
                item.text = folder;
                viewList.add(item);
            }
        }
        if (audioFileList != null) {
            for (String file : audioFileList) {
                if (viewList == null)
                    viewList = new ArrayList<ListItem>();
                ListItem item = new ListItem();
                item.id = R.drawable.browser_list_item_music;
                item.text = file;
                viewList.add(item);
            }
        }
        return viewList;
    }

    static int getCardId(Context context) {
        ContentResolver res = context.getContentResolver();
        Cursor c = res.query(Uri.parse("content://media/external/fs_id"), null,
                null, null, null);
        int id = -1;
        if (c != null) {
            c.moveToFirst();
            id = c.getInt(0);
            c.close();
        }
        return id;
    }

    public static abstract class ServiceBinder extends Binder {
        public abstract IMusicPlayerService getService();
    }
}
