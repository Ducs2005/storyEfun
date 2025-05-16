package com.example.storyefun.data.api

import com.example.storyefun.data.models.Post
import retrofit2.http.GET

interface ApiService {
    @GET("audiobooks")
    suspend fun getPosts(): List<Post>
}