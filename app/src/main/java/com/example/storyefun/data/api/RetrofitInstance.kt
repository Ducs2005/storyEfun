package com.example.storyefun.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
//            .baseUrl("https://682053d872e59f922ef8551e.mockapi.io/api/audiobooks/")
            .baseUrl("http://192.168.1.4:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}