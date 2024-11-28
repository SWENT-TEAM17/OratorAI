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

  /**
   * Fetches all user profiles from the Firestore database. On success, it returns a list of
   * [UserProfile] objects through the [onSuccess] callback. On failure, it returns an exception
   * through the [onFailure] callback.
   *
   * @param onSuccess A lambda function that receives a list of [UserProfile] objects if the
   *   operation succeeds.
   * @param onFailure A lambda function that receives an [Exception] if the operation fails.
   */
  fun getAllUserProfiles(onSuccess: (List<UserProfile>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Sends a friend request from the current user to another user.
   *
   * @param currentUid The UID of the user sending the request.
   * @param friendUid The UID of the user receiving the request.
   * @param onSuccess Callback invoked on successful operation.
   * @param onFailure Callback invoked with an [Exception] on failure.
   */
  fun sendFriendRequest(
      currentUid: String,
      friendUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
  /**
   * Accepts a friend request, establishing a friendship between two users.
   *
   * @param currentUid The UID of the current user.
   * @param friendUid The UID of the user who sent the request.
   * @param onSuccess Callback invoked on successful operation.
   * @param onFailure Callback invoked with an [Exception] on failure.
   */
  fun acceptFriendRequest(
      currentUid: String,
      friendUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
  /**
   * Declines a friend request from another user.
   *
   * @param currentUid The UID of the current user.
   * @param friendUid The UID of the user who sent the request.
   * @param onSuccess Callback invoked on successful operation.
   * @param onFailure Callback invoked with an [Exception] on failure.
   */
  fun declineFriendRequest(
      currentUid: String,
      friendUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes an existing friendship between two users.
   *
   * @param currentUid The UID of the current user.
   * @param friendUid The UID of the friend to remove.
   * @param onSuccess Callback invoked on successful operation.
   * @param onFailure Callback invoked with an [Exception] on failure.
   */
  fun deleteFriend(
      currentUid: String,
      friendUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Cancel a previously sent friend request.
   *
   * This function removes the `friendUid` from the current user's `sentReq` list and removes the
   * `currentUid` from the friend's `recReq` list, effectively canceling the friend request.
   *
   * @param currentUid The UID of the current user who sent the friend request.
   * @param friendUid The UID of the friend to whom the request was sent.
   * @param onSuccess Callback to be invoked on successful cancellation.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  fun cancelFriendRequest(
      currentUid: String,
      friendUid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

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
   * Updates the login streak for a user based on their last login date.
   *
   * @param uid The UID of the user.
   * @param onSuccess Callback invoked on successful operation.
   * @param onFailure Callback invoked on failure.
   */
  fun updateLoginStreak(uid: String, onSuccess: () -> Unit, onFailure: () -> Unit)
}
