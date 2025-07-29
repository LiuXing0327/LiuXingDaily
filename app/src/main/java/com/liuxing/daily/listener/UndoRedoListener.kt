package com.liuxing.daily.listener

/**
 * Author：流星
 * DateTime：2025/7/17 11:48
 * Description：监听撤销和重做
 */
interface UndoRedoListener {
    fun onUndoRedoChanged(canUndo: Boolean, canRedo: Boolean)
}