package com.example.storyefun.data.models

data class Volume(
    var id: String = "",
    var name: String = "",
    var title: String = "",
    val order: Long = 0,
    var chapters: List<Chapter> = emptyList(),

)
