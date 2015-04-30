package com.bonovo.musicplayer.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.bonovo.musicplayer.MediaStoreUtil;
import com.bonovo.musicplayer.R;
import java.util.Map;

import java.util.HashMap;

/**
 * Created by zybo on 9/26/14.
 */
public class ExplorerPlayListFragment
        extends MusicExplorerFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {
    private static final String TAG = "ExplorerPlayListFragment";
    private static final boolean D = true;
    private Activity mActivity;
    private long mPlayListId;
    private String mPlayListName;
    private SimpleCursorAdapter mAdapter;
    private Fragment mCurrentFragment;
    private Map<String, Long> mPlayListMap;
    private LayoutInflater mLayoutInflater;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mPlayListMap = new HashMap<String, Long>();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        mLayoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAdapter = new SimpleCursorAdapter(
                mActivity,
                R.layout.explorer_play_list_item,
//                        MediaStoreUtil.openPlayListCursor(mActivity),
                null,
                new String[]{
                        MediaStore.Audio.Playlists.NAME,
                },
                new int[]{
                        R.id.explorer_play_list_title,
                },
                0
        );
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                final int id = view.getId();
                if (id == R.id.explorer_play_list_title) {
                    final TextView textView = (TextView) view;
                    final String playListName = cursor.getString(columnIndex);
                    textView.setText(playListName);
                    textView.setOnClickListener(ExplorerPlayListFragment.this);
                    final long playListId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                    mPlayListMap.put(cursor.getString(columnIndex), playListId);
                    view.setTag(playListName);
                    final ViewGroup parent = (ViewGroup) view.getParent();
                    final ImageButton addButton = (ImageButton) parent.findViewById(R.id.explorer_play_list_add);
                    final ImageButton deleteButton = (ImageButton) parent.findViewById(R.id.explorer_play_list_delete);
                    deleteButton.setTag(textView.getText().toString());
                    addButton.setOnClickListener(ExplorerPlayListFragment.this);
                    addButton.setTag(playListName);
                    deleteButton.setOnClickListener(ExplorerPlayListFragment.this);
                }
                return true;
            }
        });
        setListAdapter(mAdapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mActivity,
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (D) {
            Log.d(TAG, "on load finished!");
        }
        mAdapter.swapCursor(data);
        setListShown(true);
        setupFootView();
    }


    private void setupFootView() {
        int count = getListView().getFooterViewsCount();
        if (count > 0)
            return;
        final View rootView = mLayoutInflater.inflate(R.layout.explorer_play_list_add_layout, null);
        final ImageButton addButton = (ImageButton) rootView.findViewById(R.id.explorer_play_list_add_list);
        addButton.setOnClickListener(this);
        getListView().addFooterView(rootView);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (D) {
            Log.d(TAG, "on list item clicked");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onClick(View v) {

        final String tag = (String) v.getTag();
        switch (v.getId()) {
            case R.id.explorer_play_list_add: {
                if (D)
                    Log.d(TAG, "add , tag : " + tag);
                FragmentManager fm = getFragmentManager();
//                final ExplorerPickMusicFragment fragment = new ExplorerPickMusicFragment();
                Fragment fragment = new ExplorerPickFragment();
                final Bundle args = new Bundle();
                final String name = (String) v.getTag();
//                args.putString(ExplorerPlayListDetailFragment.KEY_PLAY_LIST_NAME, name);
                args.putLong(ExplorerPlayListDetailFragment.KEY_PLAY_LIST_ID, mPlayListMap.get(name));
                fragment.setArguments(args);
//                fragment.setOnPlayListener(getOnPlayListener());
                mCurrentFragment = fragment;
                fm.beginTransaction().replace(R.id.explorer_detail_container, mCurrentFragment)
                        .addToBackStack(null).commit();
            }
            break;
            case R.id.explorer_play_list_delete:
                if (D)
                    Log.d(TAG, "delete , tag : " + tag);
//                final String name = (String) v.getTag();
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                final String playListName = (String) v.getTag();
                builder.setMessage(getResources().getString(R.string.explorer_delete_play_list_message,playListName ))
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MediaStoreUtil.deletePlayList(getActivity(), playListName);
                            }
                        });
                builder.create().show();
//                final String playListName = (String) v.getTag();
//                if (playListName != null)
//                    MediaStoreUtil.deletePlayList(getActivity(), playListName);
                break;
            case R.id.explorer_play_list_title:
                if (D)
                    Log.d(TAG, "text pressed!");
                FragmentManager fm = getFragmentManager();
                final ExplorerPlayListDetailFragment fragment = new ExplorerPlayListDetailFragment();
                final Bundle args = new Bundle();
                final String name = (String) v.getTag();
                args.putString(ExplorerPlayListDetailFragment.KEY_PLAY_LIST_NAME, name);
                args.putLong(ExplorerPlayListDetailFragment.KEY_PLAY_LIST_ID, mPlayListMap.get(name));
                fragment.setArguments(args);
                fragment.setOnPlayListener(getOnPlayListener());
                mCurrentFragment = fragment;
                fm.beginTransaction().replace(R.id.explorer_detail_container, mCurrentFragment)
                        .addToBackStack(null).commit();
                break;
            case R.id.explorer_play_list_add_list:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setTitle(R.string.explorer_play_list_add_dialog_title);
                final View view = mLayoutInflater.inflate(R.layout.explorer_play_list_add_dialog_layout, null);
                final EditText editText = (EditText) view.findViewById(R.id.explorer_play_list_dialog_name);
                dialogBuilder.setView(view);
                dialogBuilder.setNegativeButton(android.R.string.cancel, null);
                dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String playListName = editText.getText().toString();
                        if (playListName != null && !playListName.equals(""))
                            MediaStoreUtil.createPlayList(getActivity(), playListName);
                    }
                });
                dialogBuilder.create().show();
                break;
        }
    }

    @Override
    public boolean onKeyBack() {
        if (mCurrentFragment != null) {
            getFragmentManager().popBackStack();
            mCurrentFragment = null;
            return true;
        } else {
            return false;
        }
    }
}
