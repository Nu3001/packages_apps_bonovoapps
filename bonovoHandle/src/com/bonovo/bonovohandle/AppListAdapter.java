package com.bonovo.bonovohandle;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private ArrayList<AppItem> appItem = new ArrayList<AppItem>();
    private AppListTransfer appListTransfer;
    private AppListSwap appListSwap;
    private int appListSize;
    private Context mContext;

    public AppListAdapter(Context mContext, AppListTransfer transfer, AppListSwap appListSwap, ArrayList<AppItem> appItem, int appList) {
        this.mContext = mContext;
        this.appListTransfer = transfer;
        this.appListSwap = appListSwap;
        this.appItem = appItem;
        this.appListSize = appList;
    }

    @Override
    public AppListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.applist_item, null);

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        viewHolder.txtTitle.setText(appItem.get(position).getTitle());
        viewHolder.imgIcon.setImageDrawable(appItem.get(position).getIcon());
        viewHolder.chkBox.setChecked(appItem.get(position).isSelected());
        viewHolder.chkBox.setTag(appItem.get(position));

        viewHolder.chkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                CheckBox cb = (CheckBox) v;

                if (appListSize == 5 && cb.isChecked()) {
                    cb.setChecked(false);
                    Toast.makeText(mContext, "Please select a maximum of 5 apps.", Toast.LENGTH_SHORT).show();
                } else {

                    if (cb.isChecked()) {
                        appListSize = appListSize + 1;
                    }

                    AppItem item = (AppItem) cb.getTag();
                    item.setSelected(cb.isChecked());
                    item.setPosition(viewHolder.getAdapterPosition());
                    appListTransfer.setValues(item);
                }
            }
        });

    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(appItem, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        if (appItem.get(toPosition).isSelected()) {
            appListSwap.appSwap(appItem.get(toPosition), fromPosition, toPosition);
        }
        return true;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtTitle;
        public ImageView imgIcon;
        public CheckBox chkBox;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txtTitle = (TextView) itemLayoutView.findViewById(R.id.item_title);
            imgIcon = (ImageView) itemLayoutView.findViewById(R.id.item_icon);
            chkBox = (CheckBox) itemLayoutView.findViewById(R.id.chkBox);
        }
    }

    @Override
    public int getItemCount() {
        return appItem.size();
    }

}
