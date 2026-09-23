package com.seki999.echowordy.ui.util

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Drives manual long-press-free drag-to-reorder for a [LazyListState]'s
 * items. The caller already knows which index a drag started at (it starts
 * the gesture from a per-item drag handle), so this only has to track how
 * far the item has been dragged and which other item it currently overlaps.
 */
class DragDropListState(
    private val lazyListState: LazyListState,
    private val onMove: (from: Int, to: Int) -> Unit,
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    private var draggingItemInitialOffset = 0
    private var draggedDistance by mutableStateOf(0f)

    private val draggingItemLayoutInfo: LazyListItemInfo?
        get() = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == draggingItemIndex }

    /** How far, in pixels, the dragged item should currently be translated from its slot. */
    val draggingItemOffset: Float
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggedDistance - item.offset
        } ?: 0f

    fun startDrag(index: Int) {
        val item = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return
        draggingItemIndex = index
        draggingItemInitialOffset = item.offset
        draggedDistance = 0f
    }

    fun onDrag(deltaY: Float) {
        draggedDistance += deltaY

        val current = draggingItemLayoutInfo ?: return
        val startOffset = current.offset + draggingItemOffset
        val endOffset = startOffset + current.size
        val middle = startOffset + (endOffset - startOffset) / 2f

        val target = lazyListState.layoutInfo.visibleItemsInfo.find { item ->
            middle.toInt() in item.offset..(item.offset + item.size) && item.index != current.index
        }
        if (target != null) {
            val from = draggingItemIndex ?: return
            onMove(from, target.index)
            draggingItemIndex = target.index
        }
    }

    fun endDrag() {
        draggingItemIndex = null
        draggedDistance = 0f
        draggingItemInitialOffset = 0
    }
}

@Composable
fun rememberDragDropListState(
    lazyListState: LazyListState,
    onMove: (from: Int, to: Int) -> Unit,
): DragDropListState {
    return remember(lazyListState) { DragDropListState(lazyListState, onMove) }
}
