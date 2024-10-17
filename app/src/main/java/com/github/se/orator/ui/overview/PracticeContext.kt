package com.github.se.orator.model.speaking

import java.io.Serializable

sealed class PracticeContext(val type: String) : Serializable

data class InterviewContext(
    val interviewType: String,
    val role: String,
    val company: String,
    val focusAreas: List<String>
) : PracticeContext("InterviewContext")

data class PublicSpeakingContext(
    val occasion: String,
    val audienceDemographic: String,
    val mainPoints: List<String>
) : PracticeContext("PublicSpeakingContext")

data class SalesPitchContext(
    val product: String,
    val targetAudience: String,
    val keyFeatures: List<String>
) : PracticeContext("SalesPitchContext")
