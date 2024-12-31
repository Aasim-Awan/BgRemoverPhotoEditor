package com.photoTools.bgEraser

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PickerViewModel(private val repository: PickerRepo) : ViewModel() {

    init {
        repository.getImages()
    }

    fun getImages(){
        repository.getImages()
    }

    fun onFetchedImage(): MutableLiveData<List<Uri>> {
        return repository.onFetchedImages
    }

}

class PickerViewModelFactory(private val repository: PickerRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PickerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PickerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}