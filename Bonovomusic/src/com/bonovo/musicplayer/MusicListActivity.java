package com.bonovo.musicplayer;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.bonovo.musicplayer.playlist.PlayListManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MusicListActivity extends Activity implements OnClickListener,
        ServiceConnection, OnItemClickListener {
    private static final String TAG = "MusicListActivity";
    private static boolean D = true;

    private static final int PLAY_LIST = 1;
    private static final int FOLDER = 2;
    private static final int ARTIST = 3;
    private static final int ALBUM = 4;
    private static final int SCHOOL = 5;
    private static final int LIST_ITEM = 9;
    private static final int EXIT = 10;

    private static final int SHOW_DIALOG = 11;
    private static final int FILE_BROWSER_DIALOG = 1;

    private static final String LIST_TYPE = "type";
    private static final String KEY_PLAYLIST = "playlist";
    private static final String KEY_FOLDER = "folder";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_ALBUM = "album";
    private static final String KEY_SCHOOL = "school";
    private static final String KEY_APEFILELIST = "apefilelist";
    private static final String KEY_SELECTEDFILELIST = "selectedfilelist";
    private static final int REQUEST_CODE_PLAYLIST = 1;
    private static final int REQUEST_CODE_APEFILELIST = 2;

    private Button mPlayListButton;
    private Button mPlayFolderButton;
    private Button mArtistButton;
    private Button mAlbumButton;
    private Button mSchoolButton;
    private ImageButton mCloseButton;
    private ImageButton mOptionButton1;
    private ImageButton mOptionButton2;
    private ImageButton mOptionButton3;
    private ImageButton mOptionButton4;
    private ListView mListView;
    private IMusicPlayerService mService;
    private Dialog mDialog;

    private final List<View> mSelectedList = new ArrayList<View>();
    private final Vector<File> mDeleteList = new Vector<File>();
    // private List<ListItem> mFolderList;
    private List<String> mFileList = new ArrayList<String>();
    // private List<ListItem> mListToPlay;
    private int mFolderCount;
    private BrowserListAdapter mAdapter;
    private int mCurrentList;
    private PlayListOrg mPlayList;

    private String mCurrentFolder;
    private String mCurrentArtist;
    private String mCurrentAlbum;
    private String mCurrentSchool;

    private View mPreListButton = null;
    private ButtonGroupListener mGroupListener;
    private boolean mSelectMode = false;
    private String mSelectedAPEFile;

    static class ListItem {
        int id;
        String text;
    }

    private final Handler mViewEventHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            int what = msg.what;
            switch (what) {
                case R.id.browser_button_play_list:
                    if (D)
                        Log.d(TAG, "PLAY_LIST");
                    initList(PLAY_LIST);
                    if (mPlayList == null) {
                        mPlayList = new PlayListOrg(MusicListActivity.this);
                        mPlayList.init();
                    }
                    showPlayList();
                    break;
                case R.id.browser_button_folder_list:
                    if (D)
                        Log.d(TAG, "FOLDER");
                    initList(FOLDER);
                    showFolderList();
                    break;
                case R.id.browser_button_artist_list:
                    if (D)
                        Log.d(TAG, "ARTIST");
                    initList(ARTIST);
                    showArtistList();
                    break;
                case R.id.browser_button_album_list:
                    if (D)
                        Log.d(TAG, "ALBUM");
                    initList(ALBUM);
                    showAlbumList();
                    break;
                case R.id.browser_button_school_list:
                    if (D)
                        Log.d(TAG, "SCHOOL");
                    initList(SCHOOL);
                    showSchoolList();
                    lightButton();
                    break;
                case R.id.browser_title_button_close:
                    if (D)
                        Log.d(TAG, "EXIT");
                    finish();
                    break;
                case LIST_ITEM:
                    if (mGroupListener != null) {
                        mGroupListener.onListItemPressed((View) msg.obj, msg.arg1);
                    }
                    break;
                // case SHOW_DIALOG:
                // switch (msg.arg1) {
                // case FILE_BROWSER_DIALOG:
                // showFileBrowserDialog();
                // break;
                // }
            }
        }
    };

    //
    // private void showFileBrowserDialog() {
    // new FileBrowserDialog(this).show();
    // }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (D)
            Log.d(TAG, "onCreate !");

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.music_list_layout);
        setupViews();
        bindService();
        restoreList();

        PlayListManager playListManager = new PlayListManager(null);
        playListManager.load();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListView.setAdapter(null);
        if (mPlayList != null) {
            mPlayList.destroy();
            mPlayList = null;
        }
        saveList();
        BonovoMusicPlayerUtil.unbindPlayBackService(this, this);
    }

    private void bindService() {
        if (D)
            Log.d(TAG, "bind service");
        BonovoMusicPlayerUtil.bindPlayBackService(this, this);
    }

    private void restoreList() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        int listType = pref.getInt(LIST_TYPE, -1);
        if (listType == -1) {
            return;
        }
        mCurrentList = listType;
        switch (listType) {
            case PLAY_LIST:
                if (mPlayList == null) {
                    mPlayList = new PlayListOrg(this);
                    mPlayList.init();
                }
                initList(PLAY_LIST);
                showPlayList();
                break;
            case FOLDER:
                String folder = pref.getString(KEY_FOLDER, null);
                if (folder != null) {
                    mCurrentFolder = folder;
                }
                initList(FOLDER);
                showFolderList();
                break;
            case ALBUM:
                String album = pref.getString(KEY_ALBUM, null);
                if (album != null) {
                    mCurrentAlbum = album;
                }
                initList(ALBUM);
                showAlbumList();
                break;
            case ARTIST:
                String artist = pref.getString(KEY_ARTIST, null);
                if (artist != null) {
                    mCurrentArtist = artist;
                }
                initList(ARTIST);
                showArtistList();
                break;
            case SCHOOL:
                String school = pref.getString(KEY_SCHOOL, null);
                if (school != null) {
                    mCurrentSchool = school;
                }
                initList(SCHOOL);
                showSchoolList();
                break;
        }
    }

    private void saveList() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(LIST_TYPE, mCurrentList);
        switch (mCurrentList) {
            case FOLDER:
                editor.putString(KEY_FOLDER, mCurrentFolder);
                break;
            case ALBUM:
                editor.putString(KEY_ALBUM, mCurrentAlbum);
                break;
            case ARTIST:
                editor.putString(KEY_ARTIST, mCurrentArtist);
                break;
            case SCHOOL:
                editor.putString(KEY_SCHOOL, mCurrentSchool);
                break;
            case PLAY_LIST:
                break;
        }
        editor.apply();
    }

    private void setupViews() {
        mPlayListButton = (Button) findViewById(R.id.browser_button_play_list);
        mPlayListButton.setOnClickListener(this);
        mPlayFolderButton = (Button) findViewById(R.id.browser_button_folder_list);
        mPlayFolderButton.setOnClickListener(this);
        mArtistButton = (Button) findViewById(R.id.browser_button_artist_list);
        mArtistButton.setOnClickListener(this);
        mAlbumButton = (Button) findViewById(R.id.browser_button_album_list);
        mAlbumButton.setOnClickListener(this);
        mSchoolButton = (Button) findViewById(R.id.browser_button_school_list);
        mSchoolButton.setOnClickListener(this);
        mCloseButton = (ImageButton) findViewById(R.id.browser_title_button_close);
        mCloseButton.setOnClickListener(this);
        mOptionButton1 = (ImageButton) findViewById(R.id.operation_1);
        mOptionButton1.setOnClickListener(mOperationListener);
        mOptionButton2 = (ImageButton) findViewById(R.id.operation_2);
        mOptionButton2.setOnClickListener(mOperationListener);
        mOptionButton3 = (ImageButton) findViewById(R.id.operation_3);
        mOptionButton3.setOnClickListener(mOperationListener);
        mOptionButton4 = (ImageButton) findViewById(R.id.operation_4);
        mOptionButton4.setOnClickListener(mOperationListener);
        mAdapter = new BrowserListAdapter(this);
        mListView = (ListView) findViewById(R.id.browser_list_view);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (mViewEventHandler != null) {
            mViewEventHandler.sendEmptyMessage(id);
        }
    }

    private void initGroupListener() {
        switch (mCurrentList) {
            case FOLDER:
                if (mGroupListener == null
                        || !(mGroupListener instanceof PlayFolderListener)) {
                    mGroupListener = new PlayFolderListener();
                }
                break;
            case ARTIST:
                if (mGroupListener == null
                        || !(mGroupListener instanceof ArtistListener)) {
                    mGroupListener = new ArtistListener();
                }
                break;
            case ALBUM:
                if (mGroupListener == null
                        || !(mGroupListener instanceof AlbumListener)) {
                    mGroupListener = new AlbumListener();
                }
                break;
            case PLAY_LIST:
                if (mGroupListener == null
                        || !(mGroupListener instanceof PlayListListener)) {
                    mGroupListener = new PlayListListener();
                }
                break;
            case SCHOOL:
                if (mGroupListener == null
                        || !(mGroupListener instanceof SchoolListener)) {
                    mGroupListener = new SchoolListener();
                }
                break;
            default:
                Log.e(TAG, "can't get list listener");
        }
    }

    private void initList(int listType) {
        // mListView = (ListView) findViewById(R.id.browser_list_view);
        // mListView.setOnItemClickListener(this);
        mSelectMode = false;
        mCurrentList = listType;
        initGroupListener();
        mGroupListener.showOptionButtons();
        lightButton();
    }

    private void lightButton() {
        if (mPreListButton != null) {
            mPreListButton.setBackgroundDrawable(null);
        }
        View v = null;
        switch (mCurrentList) {
            case FOLDER:
                v = mPlayFolderButton;
                break;
            case ARTIST:
                v = mArtistButton;
                break;
            case ALBUM:
                v = mAlbumButton;
                break;
            case SCHOOL:
                v = mSchoolButton;
                break;
            case PLAY_LIST:
                v = mPlayListButton;
                break;
        }
        v.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.browser_button_back_ground));
        mPreListButton = v;
    }

    private void showPlayList() {
        List<String> list = mPlayList.getPlayList();
        ArrayList<ListItem> viewList = null;
        if (list != null) {
            viewList = new ArrayList<ListItem>();
            for (String title : list) {
                ListItem item = new ListItem();
                item.id = R.drawable.browser_list_item_music;
                item.text = title;
                viewList.add(item);
            }
        }
        refreshList(viewList);
    }

    private void showFolderList() {
        if (mCurrentFolder != null) {
            showOneFolder(mCurrentFolder);
        } else {
            // String state = Environment.getExternalStorageState();
            // if (!Environment.MEDIA_MOUNTED.equals(state)
            // && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Log.i(TAG, "can't access SDCARD");
            // return;
            // }
            // File dir = Environment.getExternalStorageDirectory();
            File dir = new File("/");
            showOneFolder(dir);
        }
    }

    private void showArtistList() {
        if (mCurrentArtist != null) {
            showOneArtist(mCurrentArtist);
        } else {
            Cursor c = BonovoMusicPlayerUtil.getArtistCursor(this);
            if (c != null && c.moveToFirst()) {
                int artistId = c
                        .getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
                ArrayList<ListItem> list = new ArrayList<ListItem>();
                do {
                    ListItem item = new ListItem();
                    item.id = R.drawable.browser_list_item_dir;
                    item.text = c.getString(artistId);
                    list.add(item);
                } while (c.moveToNext());
                refreshList(list);
                c.close();
            } else {
                refreshList(null);
                Toast.makeText(this, "cant't get artist list",
                        Toast.LENGTH_LONG).show();
                if (D)
                    Log.d(TAG, "can't get cursor for artist!");
            }
        }
    }

    private void showAlbumList() {
        if (mCurrentAlbum != null) {
            showOneAlbum(mCurrentAlbum);
        } else {
            Cursor c = BonovoMusicPlayerUtil.getAlbumCursor(this);
            if (c != null && c.moveToFirst()) {
                int artistId = c
                        .getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
                ArrayList<ListItem> list = new ArrayList<ListItem>();
                do {
                    ListItem item = new ListItem();
                    item.id = R.drawable.browser_list_item_dir;
                    item.text = c.getString(artistId);
                    list.add(item);
                } while (c.moveToNext());
                refreshList(list);
                c.close();
            } else {
                refreshList(null);
            }
        }
    }

    private void showOneSchool(View view) {
        ListViewHolder holder = (ListViewHolder) view.getTag();
        String school = holder.text.getText().toString();
        showOneSchool(school);
    }

    private void showOneSchool(String school) {
        mCurrentSchool = school;
        Cursor c = BonovoMusicPlayerUtil.getMusicBySchool(this, school);
        if (c != null) {
            ArrayList<ListItem> list = null;
            if (c.moveToFirst()) {
                list = new ArrayList<ListItem>();
                do {
                    long audioId = c.getLong(c
                            .getColumnIndex(MediaStore.Audio.Media._ID));
                    Cursor audioC = BonovoMusicPlayerUtil.getMusicById(this,
                            audioId);
                    if (audioC != null) {
                        if (audioC.moveToFirst()) {
                            ListItem item = new ListItem();
                            item.id = R.drawable.browser_list_item_music;
                            item.text = c
                                    .getString(c
                                            .getColumnIndex(MediaStore.Audio.Media.TITLE));
                            list.add(item);
                        }
                        audioC.close();
                    }
                } while (c.moveToNext());
            }
            refreshList(list);
            c.close();
        }
    }

    private void showSchoolList() {
        if (mCurrentSchool != null) {
            showOneSchool(mCurrentSchool);
        } else {
            Cursor c = BonovoMusicPlayerUtil.getGenList(this);
            if (c != null && c.moveToFirst()) {
                int schoolId = c
                        .getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);
                ArrayList<ListItem> list = new ArrayList<ListItem>();
                do {
                    ListItem item = new ListItem();
                    item.id = R.drawable.browser_list_item_dir;
                    item.text = c.getString(schoolId);
                    list.add(item);
                } while (c.moveToNext());
                refreshList(list);
                c.close();
            } else {
                refreshList(null);
            }
        }
    }

    private void refreshList(List<ListItem> list) {
        mAdapter.setList(list);
        mListView.setAdapter(mAdapter);
        mListView.invalidate();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (D)
            Log.d(TAG, "onServiceConnected");
        mService = ((BonovoMusicPlayerUtil.ServiceBinder) service).getService();
        if (mService == null) {
            if (D)
                Log.d(TAG, "can't bind to playback service!");
            Toast.makeText(this, "Can't bind playback service",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

    private void showOneAlbum(View view) {
        ListViewHolder holder = (ListViewHolder) view.getTag();
        String album = holder.text.getText().toString();
        showOneAlbum(album);
    }

    private void showOneAlbum(String album) {
        mCurrentAlbum = album;
        Cursor c = BonovoMusicPlayerUtil.getMusicByAlbum(this, album);
        if (c != null) {
            if (c.moveToFirst()) {
                ArrayList<ListItem> list = new ArrayList<ListItem>();
                do {
                    ListItem item = new ListItem();
                    item.id = R.drawable.browser_list_item_music;
                    item.text = c.getString(c
                            .getColumnIndex(MediaStore.Audio.Media.TITLE));
                    list.add(item);
                } while (c.moveToNext());
                refreshList(list);
            }
            c.close();
        }
    }

    private void showOneArtist(String artist) {
        mCurrentArtist = artist;
        Cursor c = BonovoMusicPlayerUtil.getMusicByArtist(this, artist);
        if (c != null) {
            if (c.moveToFirst()) {
                ArrayList<ListItem> list = new ArrayList<ListItem>();
                do {
                    ListItem item = new ListItem();
                    item.id = R.drawable.browser_list_item_music;
                    item.text = c.getString(c
                            .getColumnIndex(MediaStore.Audio.Media.TITLE));
                    // @ TODO item. file_id = c.getLong(_id);
                    // add to ListToPlay...
                    list.add(item);
                } while (c.moveToNext());
                refreshList(list);
            }
            c.close();
        }
    }

    private void showOneArtist(View view) {
        ListViewHolder holder = (ListViewHolder) view.getTag();
        String artist = holder.text.getText().toString();
        showOneArtist(artist);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (mViewEventHandler != null) {
            Message msg = mViewEventHandler.obtainMessage(LIST_ITEM);
            if (msg != null) {
                msg.arg1 = position;
                msg.obj = view;
                mViewEventHandler.sendMessage(msg);
            }
        }
    }

    private final OnClickListener mOperationListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.operation_1:
                    mGroupListener.onButton1Pressed(view);
                    break;
                case R.id.operation_2:
                    mGroupListener.onButton2Pressed(view);
                    break;
                case R.id.operation_3:
                    mGroupListener.onButton3Pressed(view);
                    break;
                case R.id.operation_4:
                    mGroupListener.onButton4Pressed(view);
                    break;
            }
        }
    };

    private void showOneFolder(String path) {
        File file = new File(path);
        showOneFolder(file);
    }

    private void showOneFolder(File dir) {
        mCurrentFolder = dir.getPath();
        List<List<String>> list = BonovoMusicPlayerUtil.getSubFolderFileLists(
                this, dir.getAbsolutePath());
        if (list != null) {
            mFolderCount = list.get(0) == null ? 0 : list.get(0).size();
            mFileList = list.get(1);
            refreshList(BonovoMusicPlayerUtil.getFolderItem(list));
        } else {
            refreshList(null);
        }
    }

    private void deleteSelectedFiles() {
        if (mSelectedList == null || mSelectedList.size() == 0)
            return;
        if (D)
            Log.d(TAG, "get file list");
        for (View v : mSelectedList) {
            ListViewHolder holder = (ListViewHolder) v.getTag();
            String fileName = holder.text.getText().toString();
            String fullFilePath = mCurrentFolder + File.separator + fileName;
            Log.d(TAG, "mCurrentFolder : " + mCurrentFolder);
            File file = new File(fullFilePath);
            if (!file.exists())
                continue;
            if (file.isDirectory()) {
                String[] files = file.list();
                if (files != null && files.length != 0) {
                    for (String subFileName : files) {
                        mDeleteList.add(new File(file.getAbsolutePath()
                                + File.separator + subFileName));
                    }
                    mDeleteList.add(file);
                }
            } else {
                mDeleteList.add(file);
            }
        }
        if (D)
            Log.d(TAG, "delete files");
        for (File fileToDelete : mDeleteList) {
            if (fileToDelete != null) {
                if (D)
                    Log.d(TAG,
                            "delete file : " + fileToDelete.getAbsolutePath());
                try {
                    fileToDelete.delete();
                } catch (SecurityException ex) {
                    Log.e(TAG,
                            "failed to delete file : " + fileToDelete.getPath());
                }
            }
        }
    }

    interface ButtonGroupListener {
        void onButton1Pressed(final View v);

        void onButton2Pressed(final View v);

        void onButton3Pressed(final View v);

        void onButton4Pressed(final View v);

        void onListItemPressed(final View v, int position);

        void showOptionButtons();
    }

    private class PlayListListener implements ButtonGroupListener {
        @Override
        public void onButton1Pressed(View v) {
            if (mSelectedList != null) {
                for (View view : mSelectedList) {
                    view.setBackgroundDrawable(null);
                }
                mSelectedList.clear();
            }
            mSelectMode = mSelectMode ? false : true;
        }

        @Override
        public void onButton2Pressed(View v) {
            Intent intent = new Intent(MusicListActivity.this,
                    FileBrowserActivity.class);
            startActivityForResult(intent, REQUEST_CODE_PLAYLIST);
        }

        @Override
        public void onButton3Pressed(View v) {
            List<View> list = mSelectedList;
            if (list == null || list.size() == 0)
                return;
            // @ TODO showDialog();
            for (View view : mSelectedList) {
                ListViewHolder holder = (ListViewHolder) view.getTag();
                String title = holder.text.getText().toString();
                mPlayList.delete(title);
            }
            showPlayList();
        }

        @Override
        public void onButton4Pressed(View v) {
            // @ TODO showDialog();
            mPlayList.empty();
            showPlayList();
        }

        @Override
        public void onListItemPressed(View v, int position) {
            if (mSelectMode) {
                if (v.getBackground() != null) {
                    v.setBackgroundDrawable(null);
                    mSelectedList.remove(v);
                } else {
                    v.setBackgroundResource(R.drawable.browser_box_select);
                    mSelectedList.add(v);
                }
                Log.d(TAG, "selected list size : " + mSelectedList.size());
            } else {
                List<Long> idList = mPlayList.getIdList();
                if (idList != null && mService != null) {
                    mService.open(idList, position);
                    mService.play();
                    finish();
                }
            }
        }

        @Override
        public void showOptionButtons() {
            mOptionButton1
                    .setBackgroundResource(R.drawable.browser_button_selected_selector);
            mOptionButton2
                    .setBackgroundResource(R.drawable.browser_button_add_selector);
            mOptionButton3
                    .setBackgroundResource(R.drawable.browser_button_delete_selector);
            mOptionButton4
                    .setBackgroundResource(R.drawable.browser_button_empty_selector);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult!");
        if (requestCode == REQUEST_CODE_PLAYLIST
                && resultCode == Activity.RESULT_OK
                && mCurrentList == PLAY_LIST) {
            showPlayList();
        } else if (requestCode == REQUEST_CODE_APEFILELIST
                && resultCode == Activity.RESULT_OK) {
            String result = data.getStringExtra(KEY_SELECTEDFILELIST);
            mService.openAPE(mSelectedAPEFile, result);
            mService.play();
            finish();
        }
    }

    private class SchoolListener implements ButtonGroupListener {
        @Override
        public void onButton1Pressed(View v) {
            if (mCurrentSchool != null) {
                showOneSchool(mCurrentSchool);
            } else {
                showSchoolList();
            }
        }

        @Override
        public void onButton2Pressed(View v) {
        }

        @Override
        public void onButton3Pressed(View v) {
        }

        @Override
        public void onButton4Pressed(View v) {
            mCurrentSchool = null;
            showSchoolList();
        }

        @Override
        public void onListItemPressed(View v, int position) {
            if (mCurrentSchool != null) {
                if (D)
                    Log.d(TAG, "get music by school : " + mCurrentSchool);
                Cursor c = BonovoMusicPlayerUtil.getMusicBySchool(
                        MusicListActivity.this, mCurrentSchool);
                if (c != null) {
                    ArrayList<Long> fileList = new ArrayList<Long>();
                    if (c.moveToFirst()) {
                        do {
                            fileList.add(c.getLong(c
                                    .getColumnIndex(MediaStore.Audio.Media._ID)));
                        } while (c.moveToNext());
                    }
                    c.close();
                    mService.open(fileList, position);
                    mService.play();
                    finish();
                }
            } else {
                showOneSchool(v);
            }
        }

        @Override
        public void showOptionButtons() {
            mOptionButton1
                    .setBackgroundResource(R.drawable.browser_button_refresh_selector);
            mOptionButton2.setBackgroundDrawable(null);
            mOptionButton3.setBackgroundDrawable(null);
            mOptionButton4
                    .setBackgroundResource(R.drawable.browser_button_back_selector);

        }

    }

    private class AlbumListener implements ButtonGroupListener {
        @Override
        public void onButton1Pressed(View v) {
            if (mCurrentAlbum != null) {
                showOneAlbum(mCurrentAlbum);
            } else {
                showAlbumList();
            }
        }

        @Override
        public void onButton2Pressed(View v) {
        }

        @Override
        public void onButton3Pressed(View v) {
        }

        @Override
        public void onButton4Pressed(View v) {
            mCurrentAlbum = null;
            showAlbumList();
        }

        @Override
        public void onListItemPressed(View v, int position) {
            if (mCurrentAlbum != null) {
                if (D)
                    Log.d(TAG, "get music by album : " + mCurrentArtist);
                Cursor c = BonovoMusicPlayerUtil.getMusicByAlbum(
                        MusicListActivity.this, mCurrentAlbum);
                if (c != null) {
                    ArrayList<Long> fileList = new ArrayList<Long>();
                    if (c.moveToFirst()) {
                        do {
                            fileList.add(c.getLong(c
                                    .getColumnIndex(MediaStore.Audio.Media._ID)));
                        } while (c.moveToNext());
                    }
                    c.close();
                    mService.open(fileList, position);
                    mService.play();
                    finish();
                }
            } else {
                showOneAlbum(v);
            }
        }

        @Override
        public void showOptionButtons() {
            mOptionButton1
                    .setBackgroundResource(R.drawable.browser_button_refresh_selector);
            mOptionButton2.setBackgroundDrawable(null);
            mOptionButton3.setBackgroundDrawable(null);
            mOptionButton4
                    .setBackgroundResource(R.drawable.browser_button_back_selector);
        }

    }

    private class ArtistListener implements ButtonGroupListener {
        @Override
        public void onButton1Pressed(View v) {
            if (mCurrentArtist != null) {
                showOneArtist(mCurrentArtist);
            } else {
                showArtistList();
            }
        }

        @Override
        public void onButton2Pressed(View v) {
        }

        @Override
        public void onButton3Pressed(View v) {
        }

        @Override
        public void onButton4Pressed(View v) {
            mCurrentArtist = null;
            showArtistList();
        }

        @Override
        public void onListItemPressed(View v, int position) {
            if (mCurrentArtist != null) {
                if (D)
                    Log.d(TAG, "get music by artust : " + mCurrentArtist);
                Cursor c = BonovoMusicPlayerUtil.getMusicByArtist(
                        MusicListActivity.this, mCurrentArtist);
                if (c != null) {
                    ArrayList<Long> fileList = new ArrayList<Long>();
                    if (c.moveToFirst()) {
                        do {
                            fileList.add(c.getLong(c
                                    .getColumnIndex(MediaStore.Audio.Media._ID)));
                        } while (c.moveToNext());
                    }
                    c.close();
                    mService.open(fileList, position);
                    mService.play();
                    finish();
                }
            } else {
                showOneArtist(v);
            }
        }

        @Override
        public void showOptionButtons() {
            mOptionButton1
                    .setBackgroundResource(R.drawable.browser_button_refresh_selector);
            mOptionButton2.setBackgroundDrawable(null);
            mOptionButton3.setBackgroundDrawable(null);
            mOptionButton4
                    .setBackgroundResource(R.drawable.browser_button_back_selector);
        }
    }

    private class PlayFolderListener implements ButtonGroupListener {
        @Override
        public void onButton1Pressed(View v) {
            if (mSelectedList != null) {
                for (View view : mSelectedList) {
                    view.setBackgroundDrawable(null);
                }
                mSelectedList.clear();
            }
            if (mSelectMode) {
                mOptionButton1
                        .setBackgroundResource(R.drawable.browser_selected_no);
                mSelectMode = false;
            } else {
                mOptionButton1
                        .setBackgroundResource(R.drawable.browser_selected_yes);
                mSelectMode = true;
            }
        }

        @Override
        public void onButton2Pressed(View v) {
        }

        @Override
        public void onButton3Pressed(View v) {
            // delete selected items
            // @ TODO
            // show error dialog...
            if (mSelectMode) {
                deleteSelectedFiles();
                showOneFolder(mCurrentFolder);
            }
            return;
        }

        @Override
        public void onButton4Pressed(View v) {
            // back to parent folder
            // if
            // (mCurrentFolder.equals(Environment.getExternalStorageDirectory()
            // .getPath())) {
            // return;
            // }
            if (mCurrentFolder.equals("/")) {
                return;
            }
            File file = new File(mCurrentFolder);
            if (file.exists()) {
                showOneFolder(file.getParent());
            }
        }

        @Override
        public void onListItemPressed(View view, int position) {
            if (mSelectMode) {
                view.setBackgroundResource(R.drawable.browser_box_select);
                mSelectedList.add(view);
            } else {
                String fullFileName = getSelectedFile(view);
                if (D)
                    Log.d(TAG, "fullFileName ... : " + fullFileName);
                File file = new File(fullFileName);
                if (!file.exists()) {
                    if (D)
                        Log.d(TAG, "file not exist ... : " + fullFileName);
                    return;
                }
                if (file.isDirectory()) {
                    showOneFolder(file);
                } else {
                    if (hasCUEFile(fullFileName)) {
                        CUEManager cue = new CUEManager();
                        cue.setFilePath(fullFileName.toLowerCase().replace(
                                ".ape", ".cue"));
                        boolean is = cue.parse();
                        if (is) {
                            MusicListActivity.this.mSelectedAPEFile = fullFileName;
                            Intent intent = new Intent();
                            intent.setClass(MusicListActivity.this,
                                    APEMusicListActivity.class);
                            intent.putExtra(KEY_APEFILELIST,
                                    cue.getSongListResult());
                            startActivityForResult(intent,
                                    REQUEST_CODE_APEFILELIST);
                        } else {
                            play(position);
                            finish();
                        }
                    } else {
                        play(position);
                        finish();
                    }

                }
            }
        }

        private void play(int position) {
            List<Long> ids = BonovoMusicPlayerUtil.getAudioIdsByName(
                    MusicListActivity.this, mCurrentFolder, mFileList);
            mService.open(ids, position - mFolderCount);
            mService.play();
        }

        private boolean hasCUEFile(String fileName) {
            boolean hasCUE = false;
            fileName = fileName.toLowerCase();
            if (fileName.endsWith(".ape")) {
                File file = new File(fileName.replace(".ape", ".cue"));
                if (file.exists()) {
                    hasCUE = true;
                }
            }
            return hasCUE;
        }

        private String getSelectedFile(View view) {
            ListViewHolder item = (ListViewHolder) view.getTag();
            String fullFileName = mCurrentFolder + File.separator
                    + item.text.getText().toString();
            return fullFileName;
        }

        @Override
        public void showOptionButtons() {
            mOptionButton1
                    .setBackgroundResource(R.drawable.browser_button_selected_selector);
            mOptionButton2.setBackgroundDrawable(null);
            mOptionButton3
                    .setBackgroundResource(R.drawable.browser_button_delete_selector);
            mOptionButton4
                    .setBackgroundResource(R.drawable.browser_button_back_selector);
        }
    }
}
