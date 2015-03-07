package com.example.radio;

import java.lang.ref.WeakReference;
import java.util.List;

import com.example.radio.RadioImportActivity.ListItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class ListViewHolder {
    ImageView image;
    TextView text;
}

public class RadioListAdapter extends BaseAdapter {
   // private static final String TAG = "RadioListAdapter";
    private List<ListItem> mList;
    private WeakReference<Context> mContext;

    public RadioListAdapter(Context context) {
        mContext = new WeakReference<Context>(context);
    }

    public void setList(List<ListItem> list) {
        mList = list;
    }

    public final List<ListItem> getList() {
        return mList;
    }

    @Override public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override public Object getItem(int position) {
        return mList == null ? null : mList.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View view, ViewGroup parent) {
        ListViewHolder holder = null;
        ListItem item = (ListItem) getItem(position);
        if (item == null) {
            return null;
        }
        //if (mSelectedList != null) {
            view = LayoutInflater.from(mContext.get()).inflate(
                    R.layout.list_item, parent, false);
            holder = new ListViewHolder();
            holder.image = (ImageView) view.findViewById(R.id.list_item_image);
            holder.text = (TextView) view.findViewById(R.id.list_item_text);
            view.setTag(holder);
            //if (mSelectedList[position] && item.id == R.drawable.browser_list_item_music) {
                //view.setBackgroundResource(R.drawable.browser_box_select);
            //}
        /*} else {
            if (view == null) {
                view = LayoutInflater.from(mContext.get()).inflate(
                        R.layout.list_item, parent, false);
                holder = new ListViewHolder();
                holder.image = (ImageView) view
                        .findViewById(R.id.list_item_image);
                holder.text = (TextView) view.findViewById(R.id.list_item_text);
                view.setTag(holder);
            } else {
                holder = (ListViewHolder) view.getTag();
            }
            
        }*/
        holder.image.setBackgroundResource(item.id);
        holder.text.setText(item.text);
        holder.text.setTextSize(17);

        return view;
    }
}