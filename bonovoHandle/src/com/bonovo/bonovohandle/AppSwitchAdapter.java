package com.bonovo.bonovohandle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

    public class AppSwitchAdapter extends RecyclerView.Adapter<AppSwitchAdapter.ViewHolder> {
        private AppItem[] appItem;
        private final ItemClickListener itemClickListener;
        private final ItemLongClickListener itemLongClickListener;
        private int selectedPos = 0;
        private Context mContext;

        public AppSwitchAdapter(Context mContext, AppItem[] appItem, @NonNull ItemClickListener clickListener, @NonNull ItemLongClickListener longClickListener) {
            this.mContext = mContext;
            this.appItem = appItem;
            this.itemClickListener = clickListener;
            this.itemLongClickListener = longClickListener;
        }

        @Override
        public AppSwitchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemLayoutView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.appswitch_item, null);

            return new ViewHolder(itemLayoutView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

            viewHolder.txtTitle.setText(appItem[position].getTitle());
            viewHolder.layoutItem.setSelected(selectedPos == position);

            if (viewHolder.layoutItem.isSelected()) {
                viewHolder.imgIcon.setImageBitmap(iconGlow(appItem[position].getIcon()));
            }
            else {
                viewHolder.imgIcon.setImageDrawable(appItem[position].getIcon());
            }

            viewHolder.layoutItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyItemChanged(selectedPos);
                    selectedPos = viewHolder.getAdapterPosition();
                    notifyItemChanged(selectedPos);
                    itemClickListener.onItemClicked(selectedPos);
                }
            });

            viewHolder.layoutItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    itemLongClickListener.onItemLongClicked(viewHolder.getAdapterPosition(), v);
                    return true;
                }
            });
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private RelativeLayout layoutItem;
            private TextView txtTitle;
            private ImageView imgIcon;

            public ViewHolder(View itemView) {
                super(itemView);
                layoutItem = (RelativeLayout) itemView.findViewById(R.id.appswitch_item);
                txtTitle = (TextView) itemView.findViewById(R.id.item_title);
                imgIcon = (ImageView) itemView.findViewById(R.id.item_icon);
            }
        }

        @Override
        public int getItemCount() {
            return appItem.length;
        }

        private Bitmap iconGlow(Drawable icon) {
            int margin = 24;
            int halfMargin = margin / 2;

            Bitmap bmap = ((BitmapDrawable) icon).getBitmap();
            Bitmap alpha = bmap.extractAlpha();
            Bitmap mBmap = Bitmap.createBitmap(bmap.getWidth() + margin,
                    bmap.getHeight() + margin, Bitmap.Config.ARGB_8888);

            Paint paint = new Paint();
            paint.setColor(Color.rgb(26, 194, 255));
            paint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.OUTER));

            Canvas canvas = new Canvas(mBmap);
            canvas.drawBitmap(alpha, halfMargin, halfMargin, paint);
            canvas.drawBitmap(bmap, halfMargin, halfMargin, null);

            return mBmap;
        }

    }
