package com.github.se.orator.model.speaking

import java.io.Serializable

/**
 * The PracticeContext sealed class represents the context in which a speaking practice session
 * takes place.
 *
 * @param type The type of the practice context.
 */
sealed class PracticeContext : Serializable

/**
 * The InterviewContext data class represents the context in which an interview practice session
 * takes place.
 *
 * @param interviewType The type of interview.
 * @param role The role being applied for.
 * @param company The company being applied to.
 * @param jobDescription The description of the job.
 * @param focusAreas The focus areas for the interview.
 */
data class InterviewContext(
    val targetPosition: String,
    val companyName: String,
    val interviewType: String,
    val experienceLevel: String,
    val jobDescription: String,
    val focusArea: String,
) : PracticeContext()

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
    val purpose: String,
    val audienceSize: String,
    val audienceDemographic: String,
    val presentationStyle: String,
    val mainPoints: List<String>,
    val experienceLevel: String,
    val anticipatedChallenges: List<String>,
    val focusArea: String,
    val feedbackType: String
) : PracticeContext()

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
    val salesGoal: String,
    val keyFeatures: List<String>,
    val anticipatedChallenges: List<String>,
    val negotiationFocus: String,
    val feedbackType: String
) : PracticeContext()
