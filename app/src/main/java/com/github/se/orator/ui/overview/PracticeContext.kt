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
 * The InterviewContext data class represents the context in which a job interview practice session
 * takes place.
 *
 * @param targetPosition The target job position.
 * @param companyName The company name.
 * @param interviewType The type of interview.
 * @param experienceLevel The experience level of the interviewee.
 * @param jobDescription The job description.
 * @param focusArea The focus area of the interview.
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
 * @param occasion The occasion for the speech.
 * @param purpose The purpose of the speech.
 * @param audienceSize The size of the audience.
 * @param audienceDemographic The demographic of the audience.
 * @param presentationStyle The style of the presentation.
 * @param mainPoints The main points of the speech.
 * @param experienceLevel The experience level of the speaker.
 * @param anticipatedChallenges The anticipated challenges in the speech.
 * @param focusArea The focus area of the speech.
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
 * @param product The product or service being pitched.
 * @param targetAudience The target audience for the sales pitch.
 * @param salesGoal The primary goal of the sales pitch.
 * @param keyFeatures The key features of the product or service.
 * @param anticipatedChallenges The anticipated challenges in the sales pitch.
 * @param negotiationFocus The focus of the negotiation.
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
