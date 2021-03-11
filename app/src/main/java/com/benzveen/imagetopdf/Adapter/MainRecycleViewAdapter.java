package com.benzveen.imagetopdf.Adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.benzveen.imagetopdf.R;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<File> items = new ArrayList<>();

    private OnLoadMoreListener onLoadMoreListener;
    private SparseBooleanArray selected_items;
    private int current_selected_idx = -1;
    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, File value, int position);

        void onItemLongClick(View view, File obj, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public MainRecycleViewAdapter(Context context, List<File> items) {
        this.items = items;
        ctx = context;
        selected_items = new SparseBooleanArray();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView brief;
        public TextView size;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.fileImageView);
            name = (TextView) v.findViewById(R.id.fileItemTextview);
            brief = (TextView) v.findViewById(R.id.dateItemTimeTextView);
            size = (TextView) v.findViewById(R.id.sizeItemTimeTextView);
            lyt_parent = (View) v.findViewById(R.id.listItemLinearLayout);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final File obj = items.get(position);
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            view.name.setText(obj.getName());
            Date lastModDate = new Date(obj.lastModified());
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
            String strDate = formatter.format(lastModDate);
            view.brief.setText(strDate);
            view.size.setText(GetSize(obj.length()));
           /* view.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view,items.get(position), position);
                    }
                }
            });*/

            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener == null) return;
                    mOnItemClickListener.onItemClick(v, obj, position);
                }
            });
            view.lyt_parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnItemClickListener == null) return false;
                    mOnItemClickListener.onItemLongClick(v, obj, position);
                    return true;
                }
            });
            toggleCheckedIcon(holder, position);
            view.image.setImageResource(R.drawable.ic_iconfinder_24_2133056);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void toggleCheckedIcon(RecyclerView.ViewHolder holder, int position) {
        OriginalViewHolder view = (OriginalViewHolder) holder;
        if (selected_items.get(position, false)) {
            view.lyt_parent.setBackgroundColor(Color.parseColor("#4A32740A"));
            if (current_selected_idx == position) resetCurrentIndex();
        } else {
            view.lyt_parent.setBackgroundColor(Color.parseColor("#ffffff"));
            if (current_selected_idx == position) resetCurrentIndex();
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    public String GetSize(long size) {
        String[] dictionary = {"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int index = 0;
        double m = size;
        DecimalFormat dec = new DecimalFormat("0.00");
        for (index = 0; index < dictionary.length; index++) {
            if (m < 1024) {
                break;
            }
            m = m / 1024;
        }
        return dec.format(m).concat(" " + dictionary[index]);

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

    public int getSelectedItemCount() {
        return selected_items.size();
    }

    public void selectAll() {
        for (int i = 0; i < items.size(); i++) {
            selected_items.put(i, true);
            notifyItemChanged(i);
        }
    }

    public void clearSelections() {
        selected_items.clear();
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selected_items.size());
        for (int i = 0; i < selected_items.size(); i++) {
            items.add(selected_items.keyAt(i));
        }
        return items;
    }

    public void removeData(int position) {
        items.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        current_selected_idx = -1;
    }

}