package com.github.se.orator.model.speaking

import java.io.Serializable

/**
 * The PracticeContext sealed class represents the context in which a speaking practice session
 * takes place.
 *
 * @param type The type of the practice context.
 */
sealed class PracticeContext(val type: String) : Serializable

/**
 * The InterviewContext data class represents the context in which an interview practice session
 * takes place.
 *
 * @param interviewType The type of interview.
 * @param role The role for which the interview is being conducted.
 * @param company The company at which the interview is being conducted.
 * @param focusAreas The focus areas for the interview.
 */
data class InterviewContext(
    val interviewType: String,
    val role: String,
    val company: String,
    val focusAreas: List<String>
) : PracticeContext("InterviewContext")

/**
 * The PublicSpeakingContext data class represents the context in which a public speaking practice
 * session takes place.
 *
 * @param occasion The occasion for the public speaking session.
 * @param audienceDemographic The demographic of the audience.
 * @param mainPoints The main points to be covered in the speech.
 */
data class PublicSpeakingContext(
    val occasion: String,
    val audienceDemographic: String,
    val mainPoints: List<String>
) : PracticeContext("PublicSpeakingContext")

/**
 * The SalesPitchContext data class represents the context in which a sales pitch practice session
 * takes place.
 *
 * @param product The product being pitched.
 * @param targetAudience The target audience for the pitch.
 * @param keyFeatures The key features of the product.
 */
data class SalesPitchContext(
    val product: String,
    val targetAudience: String,
    val keyFeatures: List<String>
) : PracticeContext("SalesPitchContext")
