package com.example.moodiq.domain.model

data class RecommendationResult(
    val smartQueue: List<Song>,
    val suggestions: List<Song>,
    val moodTag: String
)
