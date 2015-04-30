package com.bonovo.musicplayer.ui;

import android.content.CursorLoader;
import android.provider.MediaStore;

/**
 * Created by zybo on 10/9/14.
 */
public class ExplorerAlbumFragment extends /*MusicExplorerFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener */
        ExplorerCursorFragment {

    @Override
    protected CursorLoader getMainLoader() {
        return new CursorLoader(getActivity(),
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
    }

    @Override
    protected CursorLoader getDetailLoader(String type) {
        return new CursorLoader(getActivity(),
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.ALBUM + "=?",
                new String[]{type},
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        );
    }

    @Override
    protected String getMainTextColumn() {
        return MediaStore.Audio.Albums.ALBUM;
    }

    @Override
    protected String getDetailTextColumn() {
        return MediaStore.Audio.Media.TITLE;
    }
//    private static final String TAG = "ExplorerAlbumFragment";
//    private static final boolean D = true;
//    private Activity mActivity;
//    private ListView mListView;
//    private SimpleCursorAdapter mAdapter;
//
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        mActivity = activity;
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        mAdapter = new SimpleCursorAdapter(mActivity, android.R.layout.simple_list_item_1,
//                null,
//                new String[]{MediaStore.Audio.Albums.ALBUM},
//                new int[]{android.R.id.text1}, 0);
//        mListView = getListView();
//        mListView.setAdapter(mAdapter);
//        setListShown(false);
//        getLoaderManager().initLoader(0, null, this);
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//    }
//
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        return new CursorLoader(mActivity,
//                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
//                null,
//                null,
//                null,
//                null);
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        if (D) {
//            data.moveToFirst();
//            for (int i = 0; i < data.getCount(); i++) {
//                data.moveToPosition(i);
//                Log.d(TAG, "get album : " + data.getString(data.getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
//            }
//        }
//        data.moveToFirst();
//        mAdapter.swapCursor(data);
//        setListShown(true);
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//
//    }
//
//    @Override
//    public void onClick(View v) {
//
//    }
}
