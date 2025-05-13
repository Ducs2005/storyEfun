package com.example.storyefun.data.models

data class Chapter(
    var id: String = "",        // Default value
    val title: String = "",      // Default value
    val order: Long = 0,
    val content: List<String> = emptyList(),
    //val locked: Boolean = false
    val price : Int = 0,
    val createdAt: Long = 0L // 👈 thêm dòng này
)
