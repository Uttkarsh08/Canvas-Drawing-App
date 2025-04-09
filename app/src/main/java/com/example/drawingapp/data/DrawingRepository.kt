package com.example.drawingapp.data

import com.example.drawingapp.model.DrawingPath

class DrawingRepository {
    private val drawingPaths = mutableListOf<DrawingPath>()
    private val undonePaths = mutableListOf<DrawingPath>()

    fun addPath(path: DrawingPath) {
        drawingPaths.add(path)
        undonePaths.clear()
    }

    fun getPaths(): List<DrawingPath> = drawingPaths

    fun undo(): List<DrawingPath> {
        if (drawingPaths.isNotEmpty()) {
            undonePaths.add(drawingPaths.removeAt(drawingPaths.lastIndex))
        }
        return drawingPaths
    }

    fun redo(): List<DrawingPath> {
        if (undonePaths.isNotEmpty()) {
            drawingPaths.add(undonePaths.removeAt(undonePaths.lastIndex))
        }
        return drawingPaths
    }

    fun clear() {
        drawingPaths.clear()
        undonePaths.clear()
    }
}