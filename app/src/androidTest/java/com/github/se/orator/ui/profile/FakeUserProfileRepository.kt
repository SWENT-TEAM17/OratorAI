package com.github.se.orator.model.profile

import android.net.Uri

class FakeUserProfileRepository : UserProfileRepository {
  override fun getCurrentUserUid(): String {
    return "test_uid"
  }

  override fun addUserProfile(
      userProfile: UserProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess()
  }

  override fun getUserProfile(
      uid: String,
      onSuccess: (UserProfile?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val userProfile =
        UserProfile(
            uid = "test_uid",
            name = "Test User",
            age = 30,
            statistics =
                UserStatistics(
                    speechesGiven = 5,
                    successfulSessions = 3,
                    successfulSpeeches = 2,
                    interviewsGiven = 4,
                    successfulInterviews = 2,
                    negotiationsGiven = 1,
                    successfulNegotiations = 1))
    onSuccess(userProfile)
  }

  override fun updateUserProfile(
      userProfile: UserProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess()
  }

  // Implement other methods as needed...
  override fun uploadProfilePicture(
      uid: String,
      imageUri: Uri,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess("https://example.com/profile.jpg")
  }

  override fun getAllUserProfiles(
      onSuccess: (List<UserProfile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess(emptyList())
  }

  override fun updateUserProfilePicture(
      uid: String,
      downloadUrl: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess()
  }

  override fun getFriendsProfiles(
      friendUids: List<String>,
      onSuccess: (List<UserProfile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess(emptyList())
  }

  override fun deleteUserProfile(
      uid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    onSuccess()
  }
}
