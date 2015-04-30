package com.bonovo.musicplayer;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.bonovo.musicplayer.MusicListActivity.ListItem;

import java.io.File;
import java.util.List;

public class FileBrowserActivity extends Activity implements OnClickListener,
        OnItemClickListener {

    private static final String TAG = "FileBrowserActivity";
    private static final boolean D = true;

    private ImageButton mButtonBack;
    private ImageButton mButtonChoose;
    private ImageButton mButtonDelete;
    private ImageButton mButtonOk;
    private ImageButton mButtonClose;
    private ListView mListView;

    private View mView;
    // private Handler mCallBack;
    private BrowserListAdapter mAdapter;
    private String mCurrentFolder;
    private List<ListItem> mItemList;
    private List<List<String>> mSubFolderAndFileList;

    // private List<Integer> mSelectedList = new ArrayList<Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.file_browser);
        mAdapter = new BrowserListAdapter(this);
        setupViews();
        initList();
    }

    private void invalidate() {
        mListView.setAdapter(mAdapter);
        mListView.invalidate();
    }

    private void initList() {
        File dir = Environment.getExternalStorageDirectory();
        String path = dir.getAbsolutePath();
        mCurrentFolder = path;
        mSubFolderAndFileList = BonovoMusicPlayerUtil.getSubFolderFileLists(
                this, path);
        mItemList = BonovoMusicPlayerUtil.getFolderItem(mSubFolderAndFileList);
        mAdapter.setList(mItemList);
        invalidate();
    }

    private void setupViews() {
        mView = findViewById(R.id.file_browser_button_layout);
        mButtonBack = (ImageButton) mView
                .findViewById(R.id.filebrowser_button_back);
        mButtonBack.setOnClickListener(this);
        mButtonChoose = (ImageButton) mView
                .findViewById(R.id.filebrowser_button_choose);
        mButtonChoose.setOnClickListener(this);
        mButtonDelete = (ImageButton) mView
                .findViewById(R.id.filebrowser_button_delete);
        mButtonDelete.setOnClickListener(this);
        mButtonOk = (ImageButton) mView
                .findViewById(R.id.filebrowser_button_ok);
        mButtonOk.setOnClickListener(this);
        mButtonClose = (ImageButton) mView
                .findViewById(R.id.filebrowser_button_close);
        mButtonClose.setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.filebrowser_list);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position,
                            long id) {
        ListViewHolder holder = (ListViewHolder) view.getTag();
        String fileName = holder.text.getText().toString();
        String fullFilePath = mCurrentFolder + File.separator + fileName;
        File file = new File(fullFilePath);
        if (file.isDirectory()) {
            if (D)
                Log.d(TAG, "fullFilePath : " + fullFilePath);
            mCurrentFolder = fullFilePath;
            showOneFolder(fullFilePath);
        } else {
            if (!mAdapter.getSelectedList()[position]) {
                mAdapter.setSelected(position);
                view.setBackgroundResource(R.drawable.browser_box_select);
            } else {
                mAdapter.releaseSelected(position);
                view.setBackgroundDrawable(null);
            }
        }
        invalidate();
    }

    private void showOneFolder(String path) {
        mCurrentFolder = path;
        mSubFolderAndFileList = BonovoMusicPlayerUtil.getSubFolderFileLists(
                this, path);
        mItemList = BonovoMusicPlayerUtil.getFolderItem(mSubFolderAndFileList);
        mAdapter.setList(mItemList);
        invalidate();
    }

    private void selectAll() {
        int count = mAdapter.getCount();
        int folderCount = mSubFolderAndFileList.get(0) == null ? 0
                : mSubFolderAndFileList.get(0).size();
        // mSelectedList.addAll(arg0);
        for (int i = folderCount; i < count; i++) {
            mAdapter.setSelected(i);
        }
        invalidate();
    }

    private void deSelectAll() {
        int count = mAdapter.getCount();
        int folderCount = mSubFolderAndFileList.get(0) == null ? 0
                : mSubFolderAndFileList.get(0).size();
        // mSelectedList.addAll(arg0);
        for (int i = folderCount; i < count; i++) {
            mAdapter.releaseSelected(i);
        }
        invalidate();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.filebrowser_button_back:
                File file = new File(mCurrentFolder);
                if (file.exists()) {
                    showOneFolder(file.getParent());
                }
                break;
            case R.id.filebrowser_button_choose:
                if (isSelected()) {
                    deSelectAll();
                } else {
                    selectAll();
                }
                break;
            case R.id.filebrowser_button_delete:
                boolean[] selectedList = mAdapter.getSelectedList();
                for (int i = 0; i < selectedList.length; i++) {
                    boolean selectFlag = selectedList[i];
                    if (selectFlag) {
                        String filePath = mCurrentFolder + File.separator
                                + mItemList.get(i).text;
                        if (D)
                            Log.d(TAG, "begin to delete file :" + filePath);
                        File audioFile = new File(filePath);
                        if (audioFile.exists()) {
                            try {
                                audioFile.delete();
                            } catch (SecurityException ex) {
                                Log.e(TAG,
                                        "failed to delete file : "
                                                + audioFile.getPath());
                            }
                        }
                    }
                }
                mSubFolderAndFileList = BonovoMusicPlayerUtil
                        .getSubFolderFileLists(this, mCurrentFolder);
                mItemList = BonovoMusicPlayerUtil
                        .getFolderItem(mSubFolderAndFileList);
                mAdapter.setList(mItemList);
                invalidate();
                break;
            case R.id.filebrowser_button_ok:
                boolean[] selected_List = mAdapter.getSelectedList();
                PlayListOrg playList = new PlayListOrg(this);
                playList.init();
                for (int i = 0; i < selected_List.length; i++) {
                    boolean selectFlag = selected_List[i];
                    if (selectFlag) {
                        String filePath = mCurrentFolder + File.separator
                                + mItemList.get(i).text;
                        Cursor c = BonovoMusicPlayerUtil.getMusicByPath(this,
                                filePath);
                        if (c != null) {
                            if (c.moveToFirst()) {
                                long audioId = c
                                        .getLong(c
                                                .getColumnIndex(MediaStore.Audio.Media._ID));
                                playList.add(audioId);
                            }
                            c.close();
                        }
                    }
                }
                playList.destroy();
                setResult(Activity.RESULT_OK);
                finish();
                break;
            case R.id.filebrowser_button_close:
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean isSelected() {
        boolean[] selectedList = mAdapter.getSelectedList();
        boolean isSelected = false;
        for (boolean selectFlag : selectedList) {
            if (selectFlag) {
                isSelected = true;
                break;
            }
        }
        return isSelected;
    }
    // Cursor c = BonovoMusicPlayerUtil.getMusicCursor(this);
    // if (c != null) {
    // if (c.moveToFirst()) {
    // ArrayList<ListItem> list = new ArrayList<ListItem>();
    // do {
    // ListItem item = new ListItem();
    // item.id = R.drawable.browser_list_item_music;
    // item.text = c.getString(c
    // .getColumnIndex(MediaStore.Audio.Media.TITLE));
    // list.add(item);
    // } while (c.moveToNext());
    // mAdapter.setList(list);
    // invalidate();
    // }
    // c.close();
}
