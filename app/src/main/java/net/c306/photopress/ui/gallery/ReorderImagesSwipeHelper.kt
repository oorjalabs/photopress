package net.c306.photopress.ui.gallery

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.RecyclerView

class ReorderImagesSwipeHelper(private val mListener: OnDragActionListener) :
    ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
        0
    ) {
    
    interface OnDragActionListener {
        fun onMoved(fromPosition: Int, toPosition: Int): Boolean
        fun dragComplete(startPosition: Int, endPosition: Int)
    }
    
    private var isDragging = false
    private var dragStartPosition = -1
    private var dragEndPosition = -1
    
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val startPosition = viewHolder.adapterPosition
        val endPosition = target.adapterPosition
        mListener.onMoved(startPosition, endPosition)
        
        isDragging = true
        
        if (dragStartPosition == -1)
            dragStartPosition = startPosition
        
        dragEndPosition = endPosition
        
        return true
    }
    
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        
        if (actionState == ACTION_STATE_IDLE && isDragging) {
            mListener.dragComplete(dragStartPosition, dragEndPosition)
            dragStartPosition = -1
            dragEndPosition = -1
            isDragging = false
        }
        
        if (actionState == ACTION_STATE_DRAG) {
            viewHolder?.itemView?.alpha = 0.5f
        }
        
        super.onSelectedChanged(viewHolder, actionState)
    }
    
    override fun clearView(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.alpha = 1.0f
    }
    
    
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    
    override fun isLongPressDragEnabled(): Boolean = true
    
    override fun isItemViewSwipeEnabled(): Boolean = false
    
    
}