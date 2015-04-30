package com.bonovo.musicplayer.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.bonovo.musicplayer.FileExplorer;
import com.bonovo.musicplayer.MediaStoreUtil;
import com.bonovo.musicplayer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zybo on 10/11/14.
 */
public class ExplorerPickFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ExplorerPickFragment";
    private static final boolean D = true;
    private Activity mActivity;
    private ListView mListView;
    private ArrayAdapter mAdapter;
    private String mCurrentPath;
    private Button mBackButton;
    private Button mSaveButton;
    private TextView mPathText;
    private long mPlayListId;
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangedListener;
    private Set<String> mFileSet;
    private Set<String> mPlayListFiles;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.explorer_play_list_pick_layout, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(R.id.explorer_play_list_file_list);
        mCurrentPath = FileExplorer.getRootPath();
        mOnCheckedChangedListener = new FileCnCheckedChangedListener();
        mFileSet = new HashSet<String>();
        mPlayListId = getArguments().getLong(ExplorerPlayListDetailFragment.KEY_PLAY_LIST_ID);
        mPlayListFiles = MediaStoreUtil.getPlayListMembers(getActivity(), mPlayListId);
        mListView.setOnItemClickListener(new FileOnClickListener());
        mBackButton = (Button) view.findViewById(R.id.explorer_play_list_file_back);
        mBackButton.setOnClickListener(this);
        mSaveButton = (Button) view.findViewById(R.id.explorer_play_list_file_save);
        mSaveButton.setOnClickListener(this);
        mPathText = (TextView) view.findViewById(R.id.explorer_play_list_file_path);
        refreshList();
    }

    private void refreshList() {
        if (mCurrentPath.equals(FileExplorer.getRootPath())) {
            mBackButton.setVisibility(View.INVISIBLE);
        } else {
            mBackButton.setVisibility(View.VISIBLE);
        }
        final String[] files = FileExplorer.createList(mCurrentPath);
        final List<String> fileList = Arrays.asList(files);
        final List<String> realList = new ArrayList<String>();
        for (String fileName : fileList) {
            final String path = mCurrentPath + File.separator + fileName;
            if (!mPlayListFiles.contains(path)) {
                realList.add(fileName);
            } else {
                if (D)
                    Log.d(TAG, "skip : " + path);
            }
        }

        mAdapter = new FileAdapter(mActivity, R.layout.explorer_play_list_file_item,
                android.R.id.text1, (String[])realList.toArray(new String[realList.size()]));
        mListView.setAdapter(mAdapter);
        mPathText.setText(mCurrentPath);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.explorer_play_list_file_back) {
            mCurrentPath = FileExplorer.getParentPath(mCurrentPath);
            refreshList();
        }
        if (id == R.id.explorer_play_list_file_save) {
//            final long playListId = MediaStoreUtil.getPlayListIdByName(mActivity, mPlayListId);
            for (String path : mFileSet) {
                final long musicId = MediaStoreUtil.getMusicIdByPath(mActivity, path);
                MediaStoreUtil.addToPlayList(mActivity, mPlayListId, musicId);
            }
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    private class FileAdapter extends ArrayAdapter<String> {
        public FileAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View v = super.getView(position, convertView, parent);
            final TextView textView = (TextView) v.findViewById(android.R.id.text1);
            final String fileName = mCurrentPath + File.separator + textView.getText().toString();
            final CheckBox checkBox = (CheckBox) v.findViewById(R.id.explorer_play_list_file_check);
            if (FileExplorer.isDirectory(fileName)) {
                checkBox.setVisibility(View.GONE);
            } else {
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setTag(fileName);
                if (mFileSet.contains(fileName)) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }
            }
            v.setTag(fileName);
            checkBox.setOnCheckedChangeListener(mOnCheckedChangedListener);
            return v;
        }
    }

    private class FileCnCheckedChangedListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (D)
                Log.d(TAG, "checked change : " + isChecked);
            final String path = (String) buttonView.getTag();
            if (isChecked) {
                mFileSet.add(path);
            } else {
                mFileSet.remove(path);
            }
        }
    }

    private class FileOnClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final String path = view.getTag().toString();
            mCurrentPath = path;
            refreshList();
        }
    }
}
