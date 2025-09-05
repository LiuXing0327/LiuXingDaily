/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.listener


interface UndoRedoListener {
    fun onUndoRedoChanged(canUndo: Boolean, canRedo: Boolean)
}