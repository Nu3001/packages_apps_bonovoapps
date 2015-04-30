package com.bonovo.musicplayer.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by zybo on 10/9/14.
 */
public abstract class ExplorerCursorFragment extends MusicExplorerFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ExplorerCursorFragment";
    private static final boolean D = true;
    private static final String KEY_DETAIL_LOADER_ARGS = "detail_loader";
    private static final int MAIN_LOADER = 1;
    private static final int DETAIL_LOADER = 2;
    private static final int MESSAGE_CREATE_MUSIC_LIST = 1;
    private Activity mActivity;
    private SimpleCursorAdapter mAdapter;
    private ListView mListView;
    //    protected abstract String[] fromColumns();
    private int mCurrentLoader;
    private SimpleCursorAdapter mMainAdapter;
    private ArrayList<Long> mMusicList;
    private Handler mHandler;

    protected abstract CursorLoader getMainLoader();

    protected abstract CursorLoader getDetailLoader(String type);

    protected abstract String getMainTextColumn();

    protected abstract String getDetailTextColumn();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final String column = getMainTextColumn();
        if (column == null)
            throw new IllegalStateException();
        mAdapter = new SimpleCursorAdapter(mActivity, android.R.layout.simple_list_item_1,
                null,
                new String[]{column},
                new int[]{android.R.id.text1}, 0);
        mHandler = new Handler();
        mListView = getListView();
        mListView.setAdapter(mAdapter);
        setListShown(false);
        mMusicList = new ArrayList<Long>();
        getLoaderManager().initLoader(MAIN_LOADER, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader loader = null;
        if (id == MAIN_LOADER)
            loader = getMainLoader();
        if (id == DETAIL_LOADER) {
            final String name = args.getString(KEY_DETAIL_LOADER_ARGS);
            loader = getDetailLoader(name);
        }
        return loader;
    }

    private void createMusicList(final Cursor cursor) {
        mMusicList.clear();
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                mMusicList.add(cursor.getLong(
                        cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int id = loader.getId();
        mCurrentLoader = id;
        if (data == null) {
            mAdapter.swapCursor(null);
            setListShown(true);
            return;
        }
        if (id == MAIN_LOADER) {
            data.moveToFirst();
            final String column = getMainTextColumn();
            mAdapter = new SimpleCursorAdapter(mActivity, android.R.layout.simple_list_item_1,
                    null,
                    new String[]{column},
                    new int[]{android.R.id.text1}, 0);
            mAdapter.swapCursor(data);
            mListView.setAdapter(mAdapter);
            setListShown(true);
        }
        if (id == DETAIL_LOADER) {
//            mAdapter = getDetailAdapter();
            final String column = getDetailTextColumn();
            if (column == null)
                throw new IllegalStateException();
            mMainAdapter = mAdapter;
            mAdapter = new SimpleCursorAdapter(mActivity, android.R.layout.simple_list_item_1,
                    null,
                    new String[]{column},
                    new int[]{android.R.id.text1}, 0);
            mListView.setAdapter(mAdapter);
            mAdapter.swapCursor(data);
            setListShown(true);
            createMusicList(data);
        }
    }

    @Override
    public boolean onKeyBack() {
        if (D)
            Log.d(TAG, "back pressed , loader : " + (mCurrentLoader == MAIN_LOADER ? "main" : "detail") );
        if (mCurrentLoader == MAIN_LOADER)
            return super.onKeyBack();
        else {
            mCurrentLoader = MAIN_LOADER;
            getLoaderManager().destroyLoader(MAIN_LOADER);
            getLoaderManager().initLoader(MAIN_LOADER, null, ExplorerCursorFragment.this);
//            mListView.setAdapter(mMainAdapter);
//            setListShown(true);

            return true;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (D) {
            Log.d(TAG, "item click !, current loader : " + (mCurrentLoader == MAIN_LOADER ? "main" : "detail"));
            Log.d(TAG, "l : " + l.toString() + ", view : " + v.toString() + ", position : " + position + ", id : " + id);
        }
        if (mCurrentLoader == MAIN_LOADER) {
            final TextView textView = (TextView) v;
            final String name = textView.getText().toString();
            final Bundle args = new Bundle();
            args.putString(KEY_DETAIL_LOADER_ARGS, name);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    getLoaderManager().destroyLoader(DETAIL_LOADER);
                    getLoaderManager().initLoader(DETAIL_LOADER, args, ExplorerCursorFragment.this);
                }
            });
            if (D)
                Log.d(TAG, "list....");
        }
        if (mCurrentLoader == DETAIL_LOADER) {
            if (D)
                Log.d(TAG, "play....");
            playList(mMusicList, position);
        }
    }
}
