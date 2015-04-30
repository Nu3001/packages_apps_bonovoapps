package com.bonovo.musicplayer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by zybo on 9/22/14.
 */
public class ExplorerItemListView extends ListView {

    public ExplorerItemListView(Context context) {
        super(context);
    }

    public ExplorerItemListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExplorerItemListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected int computeVerticalScrollExtent() {
        return 200;
    }

    @Override
    protected int computeVerticalScrollOffset() {
        int range = super.computeVerticalScrollRange();
        int extent = super.computeVerticalScrollExtent();
        int offset = super.computeVerticalScrollOffset();
        if (range == extent)
            return 0;
        int result =  (int)((float)range * offset / (range - extent));
        return result;
    }
}
