package com.github.se.orator.model.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for managing user profiles and friends' profiles.
 *
 * @property repository The repository for accessing user profile data.
 */
class UserProfileViewModel(internal val repository: UserProfileRepository) : ViewModel() {

  // Mutable state flow to hold the user profile
  private val userProfile_ = MutableStateFlow<UserProfile?>(null)
  val userProfile: StateFlow<UserProfile?> = userProfile_.asStateFlow()

  // Mutable state flow to hold the list of all profiles
  private val allProfiles_ = MutableStateFlow<List<UserProfile>>(emptyList())
  val allProfiles: StateFlow<List<UserProfile>> = allProfiles_.asStateFlow()

  // Mutable state flow to hold the list of friends' profiles
  private val friendsProfiles_ = MutableStateFlow<List<UserProfile>>(emptyList())
  val friendsProfiles: StateFlow<List<UserProfile>> = friendsProfiles_.asStateFlow()

  // Mutable state flow to hold the list of friends' profiles
  private val recReqProfiles_ = MutableStateFlow<List<UserProfile>>(emptyList())
  val recReqProfiles: StateFlow<List<UserProfile>> = recReqProfiles_.asStateFlow()
  // Mutable state flow to hold the list of friends' profiles
  private val sentReqProfiles_ = MutableStateFlow<List<UserProfile>>(emptyList())
  val sentReqProfiles: StateFlow<List<UserProfile>> = sentReqProfiles_.asStateFlow()
  // Selected friend's profile
  private val selectedFriend_ = MutableStateFlow<UserProfile?>(null)
  val selectedFriend: StateFlow<UserProfile?> = selectedFriend_.asStateFlow()

  // Loading state to indicate if the profile is being fetched
  private val isLoading_ = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = isLoading_.asStateFlow()

  // Init block to fetch user profile automatically after authentication
  init {
    val uid = repository.getCurrentUserUid()
    if (uid != null) {
      getUserProfile(uid)
    } else {
      isLoading_.value = false
    }
  }

  // Factory for creating UserProfileViewModel with Firestore dependency
  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserProfileViewModel(
                UserProfileRepositoryFirestore(FirebaseFirestore.getInstance()))
                as T
          }
        }
  }

  /**
   * Adds a new user profile or updates an existing one.
   *
   * @param userProfile The user profile to be created or updated.
   */
  fun createOrUpdateUserProfile(userProfile: UserProfile) {
    if (userProfile_.value == null) {
      // Create a new profile if none exists
      Log.d("UserProfileViewModel", "Creating new user profile.")
      addUserProfile(userProfile)
    } else {
      // Update the existing profile
      Log.d("UserProfileViewModel", "Updating existing user profile.")
      updateUserProfile(userProfile)
    }
  }

  /**
   * Adds a new user profile to Firestore.
   *
   * @param userProfile The user profile to be added.
   */
  fun addUserProfile(userProfile: UserProfile) {
    repository.addUserProfile(
        userProfile = userProfile,
        onSuccess = {
          userProfile_.value = userProfile // Set the newly added profile
          Log.d("UserProfileViewModel", "Profile added successfully.")
          // Add the profile to the list containing all profiles
          allProfiles_.value += userProfile
        },
        onFailure = { Log.e("UserProfileViewModel", "Failed to add user profile.", it) })
  }

  /**
   * Fetches the user profile for the current user and updates the state flow.
   *
   * @param uid The UID of the user whose profile is to be fetched.
   */
  fun getUserProfile(uid: String) {
    isLoading_.value = true
    repository.getUserProfile(
        uid = uid,
        onSuccess = { profile ->
            userProfile_.value = profile
            profile?.let {
                // Fetch Friends Profiles
                fetchFriendsProfiles(it.friends)

                // Fetch Received Friend Requests Profiles
                fetchRecReqProfiles(it.recReq)

                // Fetch Sent Friend Requests Profiles
                fetchSentReqProfiles(it.sentReq)

                // Optionally, fetch all user profiles if needed
                fetchAllUserProfiles()
            }
            isLoading_.value = false
            Log.d("UserProfileViewModel", "User profile fetched successfully.")
            Log.d("UserProfileViewModel", "Friends: ${profile?.name}")
        },
        onFailure = {
          // Handle error
          Log.e("UserProfileViewModel", "Failed to fetch user profile.", it)
          isLoading_.value = false
        })
  }

  /**
   * Fetches the friends' profiles based on the UIDs stored in the user's profile.
   *
   * @param friendUids List of UIDs of the friends to be retrieved.
   */
  private fun fetchFriendsProfiles(friendUids: List<String>) {
    repository.getFriendsProfiles(
        friendUids = friendUids,
        onSuccess = { profiles -> friendsProfiles_.value = profiles },
        onFailure = {
          // Handle error
          Log.e("UserProfileViewModel", "Failed to fetch friends' profiles.", it)
        })
  }

  /**
   * Fetches the friends' profiles based on the UIDs stored in the user's profile.
   *
   * @param friendUids List of UIDs of the friends to be retrieved.
   */
  private fun fetchRecReqProfiles(friendUids: List<String>) {
    repository.getFriendsProfiles(
        friendUids = friendUids,
        onSuccess = { profiles -> recReqProfiles_.value = profiles },
        onFailure = {
          // Handle error
          Log.e("UserProfileViewModel", "Failed to fetch friends' profiles.", it)
        })
  }
  /**
   * Fetches the friends' profiles based on the UIDs stored in the user's profile.
   *
   * @param friendUids List of UIDs of the friends to be retrieved.
   */
  private fun fetchSentReqProfiles(friendUids: List<String>) {
    repository.getFriendsProfiles(
        friendUids = friendUids,
        onSuccess = { profiles -> sentReqProfiles_.value = profiles },
        onFailure = {
          // Handle error
          Log.e("UserProfileViewModel", "Failed to fetch friends' profiles.", it)
        })
  }

  /** Fetches all the user profiles */
  private fun fetchAllUserProfiles() {
    repository.getAllUserProfiles(
        onSuccess = { profiles -> allProfiles_.value = profiles },
        onFailure = {
          // Handle error
          Log.e("UserProfileViewModel", "Failed to fetch friends' profiles.", it)
        })
  }

  /**
   * Adds a user profile to the current user's list of friends.
   *
   * @param friend The user profile of the friend to be added.
   */
    /**
     * Accepts a friend request from the specified friend.
     *
     * @param friend The `UserProfile` of the user whose request is being accepted.
     */
    fun acceptFriend(friend: UserProfile) {
        val currentUid = repository.getCurrentUserUid()
        if (currentUid != null) {
            repository.acceptFriendRequest(
                currentUid = currentUid,
                friendUid = friend.uid,
                onSuccess = {
                    Log.d("UserProfileViewModel", "Friend request accepted from ${friend.name}")

                    // Update local state for friends
                    val updatedFriendsList = userProfile_.value?.friends?.toMutableList()?.apply { add(friend.uid) }
                    if (updatedFriendsList != null) {
                        val updatedProfile = userProfile_.value!!.copy(friends = updatedFriendsList)
                        userProfile_.value = updatedProfile
                        friendsProfiles_.value += friend

                        // Remove from received requests
                        val updatedRecReq = userProfile_.value?.recReq?.toMutableList()?.apply { remove(friend.uid) }
                        if (updatedRecReq != null) {
                            val updatedProfileRecReq = userProfile_.value!!.copy(recReq = updatedRecReq)
                            userProfile_.value = updatedProfileRecReq
                            recReqProfiles_.value = recReqProfiles_.value.filter { it.uid != friend.uid }
                        }
                    }
                },
                onFailure = { exception ->
                    Log.e("UserProfileViewModel", "Failed to accept friend request.", exception)
                    // Optionally, notify the UI about the failure
                }
            )
        } else {
            Log.e("UserProfileViewModel", "Cannot accept friend request: User is not authenticated.")
            // Optionally, handle unauthenticated state here (e.g., prompt user to log in)
        }
    }

    /**
     * Declines a friend request from the specified friend.
     *
     * @param friend The `UserProfile` of the user whose request is being declined.
     */
    fun declineFriendRequest(friend: UserProfile) {
        val currentUid = repository.getCurrentUserUid()
        if (currentUid != null) {
            repository.declineFriendRequest(
                currentUid = currentUid,
                friendUid = friend.uid,
                onSuccess = {
                    Log.d("UserProfileViewModel", "Friend request declined from ${friend.name}")

                    // Update local state by removing the declined request
                    val updatedRecReq = userProfile_.value?.recReq?.toMutableList()?.apply { remove(friend.uid) }
                    if (updatedRecReq != null) {
                        val updatedProfile = userProfile_.value!!.copy(recReq = updatedRecReq)
                        userProfile_.value = updatedProfile
                        recReqProfiles_.value = recReqProfiles_.value.filter { it.uid != friend.uid }
                    }
                },
                onFailure = { exception ->
                    Log.e("UserProfileViewModel", "Failed to decline friend request.", exception)
                    // Optionally, notify the UI about the failure
                }
            )
        } else {
            Log.e("UserProfileViewModel", "Cannot decline friend request: User is not authenticated.")
            // Optionally, handle unauthenticated state here (e.g., prompt user to log in)
        }
    }

    fun sendRequest(friend: UserProfile) {
        val currentUid = repository.getCurrentUserUid()
        if (currentUid != null) {
            repository.sendFriendRequest(
                currentUid = currentUid,
                friendUid = friend.uid,
                onSuccess = {
                    Log.d("UserProfileViewModel", "Friend request sent to ${friend.name}")

                    // Update local state for sent requests
                    val updatedSentReq = userProfile_.value?.sentReq?.toMutableList()?.apply { add(friend.uid) }
                    if (updatedSentReq != null) {
                        val updatedProfile = userProfile_.value!!.copy(sentReq = updatedSentReq)
                        userProfile_.value = updatedProfile
                        sentReqProfiles_.value += friend
                    }
                },
                onFailure = { exception ->
                    Log.e("UserProfileViewModel", "Failed to send friend request.", exception)
                    // Optionally, notify the UI about the failure (e.g., via another StateFlow or LiveData)
                }
            )
        } else {
            Log.e("UserProfileViewModel", "Cannot send friend request: User is not authenticated.")
            // Optionally, handle unauthenticated state here (e.g., prompt user to log in)
        }
    }





  /**
   * Updates the user profile.
   *
   * @param profile The user profile to be updated.
   */
  private fun updateUserProfile(profile: UserProfile) {
    repository.updateUserProfile(
        userProfile = profile,
        onSuccess = {
          getUserProfile(profile.uid) // Re-fetch profile after updating
          Log.d("UserProfileViewModel", "Profile updated successfully.")
        },
        onFailure = { Log.e("UserProfileViewModel", "Failed to update user profile.", it) })
  }

  /**
   * Selects a friend's profile to view in detail.
   *
   * @param friend The friend's profile to select.
   */
  fun selectFriend(friend: UserProfile) {
    selectedFriend_.value = friend
  }

  /**
   * Uploads a profile picture.
   *
   * @param uid The UID of the user.
   * @param imageUri The URI of the image to be uploaded.
   */
  fun uploadProfilePicture(uid: String, imageUri: Uri) {
    Log.d("UserProfileViewModel", "Uploading profile picture for user: $uid with URI: $imageUri")
    repository.uploadProfilePicture(
        uid,
        imageUri,
        onSuccess = { downloadUrl ->
          Log.d(
              "UserProfileViewModel",
              "Profile picture uploaded successfully. Download URL: $downloadUrl")
          updateUserProfilePicture(uid, downloadUrl)
        },
        onFailure = { exception ->
          Log.e("UserProfileViewModel", "Failed to upload profile picture.", exception)
        })
  }

  /**
   * Updates Firestore with the profile picture URL.
   *
   * @param uid The UID of the user.
   * @param downloadUrl The download URL of the uploaded profile picture.
   */
  private fun updateUserProfilePicture(uid: String, downloadUrl: String) {
    Log.d(
        "UserProfileViewModel",
        "Updating Firestore for user: $uid with profile picture URL: $downloadUrl")
    repository.updateUserProfilePicture(
        uid,
        downloadUrl,
        onSuccess = {
          Log.d("UserProfileViewModel", "Profile picture URL updated successfully in Firestore.")
          // Optionally, fetch the profile again to check
          getUserProfile(uid)
        },
        onFailure = { exception ->
          Log.e(
              "UserProfileViewModel",
              "Failed to update profile picture URL in Firestore.",
              exception)
        })
  }

  /**
   * Checks if the user profile is incomplete. This method checks if essential fields (like name)
   * are missing or blank.
   *
   * @return True if the profile is incomplete, false otherwise.
   */
  fun isProfileIncomplete(): Boolean {
    // Check if the profile is still loading
    if (isLoading_.value) {
      Log.d("UserProfileViewModel", "Profile is still loading.")
      return false // While loading, return false to prevent redirection
    }

    val profile = userProfile_.value
    Log.d("UserProfileViewModel", "Checking profile completeness. Profile: $profile")

    return profile == null || profile.name.isBlank()
  }

  /**
   * Deletes a friend from the current user's list of friends.
   *
   * @param friend The `UserProfile` of the friend to be deleted.
   */
  fun deleteFriend(friend: UserProfile) {
    val currentUserProfile = userProfile_.value
    if (currentUserProfile != null) {
      // Asserts that the friend is in the friends list
      if (currentUserProfile.friends.contains(friend.uid)) {
        Log.d("UserProfileViewModel", "Removing friend: ${friend.name}")

        // Removes the friend from the friends list
        val updatedFriendsList = friendsProfiles_.value.toMutableList().apply { remove(friend) }

        val updatedProfile = currentUserProfile.copy(friends = updatedFriendsList.map { it.uid })

        // Updates the user profile with the new one
        updateUserProfile(updatedProfile)
        userProfile_.value = updatedProfile

        // Updates the local state with the new friends list
        friendsProfiles_.value = updatedFriendsList
      } else {
        Log.d("UserProfileViewModel", "${friend.name} cannot be deleted: not in the friends list !")
      }
    } else {
      Log.e("UserProfileViewModel", "Failed to remove a friend: current user profile is null.")
    }
  }

  /**
   * Updates the session result in the user profile statistics.
   *
   * @param isSuccess The result of the session.
   * @param sessionType The type of session.
   */
  fun updateSessionResult(isSuccess: Boolean, sessionType: SessionType) {
    val currentUserProfile = userProfile_.value
    if (currentUserProfile != null) {
      val currentStats = currentUserProfile.statistics

      val sessionTypeKey = sessionType.name

      // Update sessions given
      val updatedSessionsGiven = currentStats.sessionsGiven.toMutableMap()
      updatedSessionsGiven[sessionTypeKey] = (updatedSessionsGiven[sessionTypeKey] ?: 0) + 1

      // Update successful sessions if applicable
      val updatedSuccessfulSessions = currentStats.successfulSessions.toMutableMap()
      if (isSuccess) {
        updatedSuccessfulSessions[sessionTypeKey] =
            (updatedSuccessfulSessions[sessionTypeKey] ?: 0) + 1
      }

      val updatedStats =
          currentStats.copy(
              sessionsGiven = updatedSessionsGiven, successfulSessions = updatedSuccessfulSessions)

      val updatedProfile = currentUserProfile.copy(statistics = updatedStats)

      // Save the updated profile to the database
      updateUserProfile(updatedProfile)

      // Update the StateFlow
      userProfile_.value = updatedProfile
    } else {
      Log.e("UserProfileViewModel", "Current user profile is null.")
    }
  }

  fun updateLoginStreak() {
    val uid = repository.getCurrentUserUid()
    if (uid != null) {
      repository.updateLoginStreak(
          uid = uid,
          onSuccess = {
            // Optionally, fetch the updated profile
            getUserProfile(uid)
            Log.d("UserProfileViewModel", "Login streak updated successfully.")
          },
          onFailure = { Log.e("UserProfileViewModel", "Failed to update login streak.") })
    } else {
      Log.e("UserProfileViewModel", "Cannot update streak: User is not authenticated.")
    }
  }
}