package com.benzveen.imagetopdf.Adapter;

import android.content.Context;
import android.net.Uri;
import androidx.core.view.MotionEventCompat;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.benzveen.imagetopdf.R;
import com.benzveen.imagetopdf.Utils.ItemTouchHelperClass;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdapterGridBasic extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperClass.ItemTouchHelperAdapter {

    public ArrayList<ImageDocument> items = new ArrayList<>();
    private SparseBooleanArray selected_items;
    private int current_selected_idx = -1;
    private OnStartDragListener mDragStartListener = null;

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemRemoved(int position) {

    }

    public interface OnItemClickListener {
        void onItemClick(View view, ImageDocument obj, int position);

        void onItemLongClick(View view, ImageDocument obj, int pos);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }
    public void setDragListener(OnStartDragListener dragStartListener) {
        this.mDragStartListener = dragStartListener;
    }
    public AdapterGridBasic(Context context, ArrayList<ImageDocument> items) {
        this.items = items;
        ctx = context;
        selected_items = new SparseBooleanArray();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public View lyt_parent;
        public AppCompatImageButton bt_move;
        public AppCompatImageButton bt_crop;
        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            bt_move = (AppCompatImageButton) v.findViewById(R.id.itemSelector);
            bt_crop = v.findViewById(R.id.imageCrop);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_image, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            displayImageOriginal(ctx, view.image, items.get(position).getImageDocument());
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), holder.getAdapterPosition());
                    }
                }
            });
            view.lyt_parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnItemClickListener == null) return false;
                    mOnItemClickListener.onItemLongClick(v, items.get(position), holder.getAdapterPosition());
                    return true;
                }
            });
            view.bt_move.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN && mDragStartListener != null) {
                        mDragStartListener.onStartDrag(holder);
                    }
                    return false;
                }
            });

            view.bt_crop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), holder.getAdapterPosition());
                    }
                }
            });
            toggleCheckedIcon(view,position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void toggleCheckedIcon(OriginalViewHolder holder, int position) {
        if (selected_items.get(position, false)) {
            holder.image.setColorFilter(R.color.colorAccent);
            if (current_selected_idx == position) resetCurrentIndex();
        } else {
            holder.image.setColorFilter(null);
            if (current_selected_idx == position) resetCurrentIndex();
        }
    }
    public void selectAll() {
        for (int i = 0; i < items.size(); i++) {
            selected_items.put(i, true);
            notifyItemChanged(i);
        }
    }

    public void removeData(int position) {
        items.remove(position);
        resetCurrentIndex();
    }

    public void clearSelections() {
        selected_items.clear();
        notifyDataSetChanged();
    }

    public void toggleSelection(int pos) {
        current_selected_idx = pos;
        if (selected_items.get(pos, false)) {
            selected_items.delete(pos);
        } else {
            selected_items.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    private void resetCurrentIndex() {
        current_selected_idx = -1;
    }

    public int getSelectedItemCount() {
        return selected_items.size();
    }

    public static void displayImageOriginal(Context ctx, ImageView img, Uri url) {
        try {
            Glide.with(ctx).load(url)
                    .diskCacheStrategy(DiskCacheStrategy.NONE).thumbnail()
                    .into(img);
        } catch (Exception e) {
        }
    }
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selected_items.size());
        for (int i = 0; i < selected_items.size(); i++) {
            items.add(selected_items.keyAt(i));
        }
        return items;
    }
    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public ImageDocument getItem(int position) {
        return items.get(position);
    }
}