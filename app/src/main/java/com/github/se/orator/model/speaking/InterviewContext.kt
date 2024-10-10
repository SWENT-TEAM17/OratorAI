package com.github.se.orator.model.speaking

data class InterviewContext(
    val interviewType: String,
    val role: String,
    val company: String,
    val focusAreas: List<String>
)
