package com.github.se.orator.model.profile

import android.net.Uri

/** Interface for managing user profiles in a data store. */
interface UserProfileRepository {

  /**
   * Get the UID for the current authenticated user.
   *
   * @return The UID of the current user, or null if not authenticated.
   */
  fun getCurrentUserUid(): String?

  /**
   * Add a new user profile to the data store.
   *
   * @param userProfile The user profile to be added.
   * @param onSuccess Callback to be invoked on successful addition.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  fun addUserProfile(
      userProfile: UserProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Get user profile by UID.
   *
   * @param uid The UID of the user whose profile is to be fetched.
   * @param onSuccess Callback to be invoked with the fetched user profile.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  fun getUserProfile(uid: String, onSuccess: (UserProfile?) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Update an existing user profile in the data store.
   *
   * @param userProfile The user profile to be updated.
   * @param onSuccess Callback to be invoked on successful update.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  fun updateUserProfile(
      userProfile: UserProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Upload a profile picture to the storage system.
   *
   * @param uid The UID of the user.
   * @param imageUri The URI of the image to be uploaded.
   * @param onSuccess Callback to be invoked with the download URL of the uploaded image.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  fun uploadProfilePicture(
      uid: String,
      imageUri: Uri,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getAllUserProfiles(onSuccess: (List<UserProfile>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Update the profile picture URL in the data store.
   *
   * @param uid The UID of the user.
   * @param downloadUrl The download URL of the uploaded profile picture.
   * @param onSuccess Callback to be invoked on successful update.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  fun updateUserProfilePicture(
      uid: String,
      downloadUrl: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Get friends' profiles based on their UIDs.
   *
   * @param friendUids List of UIDs of the friends.
   * @param onSuccess Callback to be invoked with the list of friends' profiles.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  fun getFriendsProfiles(
      friendUids: List<String>,
      onSuccess: (List<UserProfile>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Delete a user profile from the data store.
   *
   * @param uid The UID of the user whose profile is to be deleted.
   * @param onSuccess Callback to be invoked on successful deletion.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  fun deleteUserProfile(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Calculates the mean (average) of the elements in a given queue.
   *
   * This function takes a queue of numerical values (represented as an `ArrayDeque<Double>`) and
   * returns the mean of its elements. If the queue is empty, the function returns 0.
   *
   * @param values An `ArrayDeque<Double>` containing the metrics values.
   * @return The mean of the values or 0 if it is empty
   */
  fun getMetricMean(values: ArrayDeque<Double>): Double

  fun updateLoginStreak(uid: String, onSuccess: () -> Unit, onFailure: () -> Unit)
}
