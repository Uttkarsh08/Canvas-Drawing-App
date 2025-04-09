package com.example.drawingapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.model.DrawingPath

class MainViewModel : ViewModel() {

    private val repository = DrawingRepository()
    private val _paths = MutableLiveData<List<DrawingPath>>(emptyList())
    val paths: LiveData<List<DrawingPath>> = _paths

    fun addPath(path: DrawingPath) {
        repository.addPath(path)
        _paths.value = repository.getPaths()
    }

    fun undo() {
        _paths.value = repository.undo()
    }

    fun redo() {
        _paths.value = repository.redo()
    }

}
