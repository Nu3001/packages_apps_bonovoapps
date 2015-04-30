package com.bonovo.musicplayer.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bonovo.musicplayer.FileExplorer;
import com.bonovo.musicplayer.MediaStoreUtil;
import com.bonovo.musicplayer.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by zybo on 10/11/14.
 */
public class ExplorerPickMusicFragment extends ListFragment {
    private Activity mActivity;
    private ListView mListView;
    private String mCurrentPath;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCurrentPath = FileExplorer.getRootPath();
        mListView = getListView();
        setListShown(false);
        refreshList(mCurrentPath);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        final FrameLayout layout = (FrameLayout) view;
//        final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) layout.getLayoutParams();
//        layoutParams.setMargins(10, 10, 10, 10);
//        view.setBackgroundResource(R.drawable.explorer_detail_background2);
    }

    private void refreshList(String path) {
        final String[] files = FileExplorer.createList(path);
        if (files != null && files.length != 0) {
            mCurrentPath = path;
            mListView.setAdapter(new ArrayAdapter<String>(
                    mActivity, android.R.layout.simple_list_item_1, files));
            setListShown(true);
        } else {
            Toast.makeText(mActivity, "folder is null!", Toast.LENGTH_SHORT).show();
        }
    }

    private class FileItemListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final TextView textView = (TextView) view.findViewById(android.R.id.text1);
            final CharSequence fileName = textView.getText();
            final File selectedFile = new File(mCurrentPath + File.separator + fileName.toString());
            if (selectedFile.isDirectory()) {
                refreshList(selectedFile.getPath());
            } else {
                int selectedPosition = 0;
                final String[] fileNames = new File(mCurrentPath).list();
                final ArrayList<Long> fileList = new ArrayList<Long>();
                for (int i = 0; i < fileNames.length; i++) {
                    final String childName = mCurrentPath + File.separator + fileNames[i];
                    final File childFile = new File(childName);
                    if (childFile.isDirectory())
                        continue;
                    final long mediaID = MediaStoreUtil.checkMediaFile(mActivity, childName);
                    if (mediaID != -1) {
                        fileList.add(mediaID);
                        if (childName.equals(selectedFile.getAbsolutePath()))
                            selectedPosition = fileList.size() - 1;
                    } else {
//                        Toast.makeText(mActivity, R.string.toast_can_not_play, Toast.LENGTH_SHORT).show();
//                        MediaScannerConnection.scanFile(mActivity,
//                                new String[]{childName}, null, new MediaScannerConnection.OnScanCompletedListener() {
//                                    @Override
//                                    public void onScanCompleted(String path, Uri uri) {
//                                        if (D)
//                                            Log.d(TAG, "media scanner , path : " + path + " , uri : " + uri);
//                                    }
//                                }
//                        );
                    }
                }
            }
        }
    }
}
