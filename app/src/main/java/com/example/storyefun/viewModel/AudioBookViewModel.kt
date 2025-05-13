//package com.example.storyefun.viewModel
//
//import android.content.Context
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.runtime.mutableStateOf
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.storyefun.data.models.AudioBook
//import com.example.storyefun.data.rest.RetrofitClient
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.launch
//
//class AudioBookViewModel : ViewModel() {
//    private val _data = mutableStateOf<List<AudioBook>>(emptyList())
//    val data = _data
//
//    private val _error = mutableStateOf<String>("")
//    val error = _error
//
//    init {
//        fetchAudioBooks()
//    }
//
//    private fun fetchAudioBooks() {
//        viewModelScope.launch {
//            try {
//                val response = RetrofitClient.apiService.fetchData()
//                _data.value = response
//            } catch (e: Exception) {
//                _error.value = "Error fetching data: ${e.message}"
//            }
//        }
//    }
//}
