package com.benzveen.imagetopdf.Utils;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

public class ItemTouchHelperClass extends ItemTouchHelper.Callback {

    private ItemTouchHelperAdapter adapter;
    public static boolean isItemSwipe = true;

    public interface ItemTouchHelperAdapter {
        void onItemMoved(int fromPosition, int toPosition);

        void onItemRemoved(int position);
    }

    public ItemTouchHelperClass(ItemTouchHelperAdapter ad) {
        adapter = ad;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int upFlags = ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END;
        //int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;

        return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,upFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        adapter.onItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.onItemRemoved(viewHolder.getAdapterPosition());
    }
}