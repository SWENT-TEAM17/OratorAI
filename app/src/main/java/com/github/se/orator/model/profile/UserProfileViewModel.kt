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

  // Selected friend's profile
  private val selectedFriend_ = MutableStateFlow<UserProfile?>(null)
  val selectedFriend: StateFlow<UserProfile?> = selectedFriend_.asStateFlow()

  // Loading state to indicate if the profile is being fetched
  private val isLoading_ = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = isLoading_.asStateFlow()

  // Queue of the last ten "words per minute" metric
  private val recentTalkTimeSec_ = MutableStateFlow<ArrayDeque<Double>>(ArrayDeque())
  val recentWPM: StateFlow<ArrayDeque<Double>> = recentTalkTimeSec_.asStateFlow()

  // Queue of the last ten "talk time" metric
  private val recentTalkTimePerc_ = MutableStateFlow<ArrayDeque<Double>>(ArrayDeque())
  val recentTalkTime: StateFlow<ArrayDeque<Double>> = recentTalkTimePerc_.asStateFlow()


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
          profile?.friends?.let {
            fetchFriendsProfiles(it)
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
  fun addFriend(friend: UserProfile) {
    val currentUserProfile = userProfile_.value
    if (currentUserProfile != null) {
      // Check if the friend is already in the list to avoid duplicates
      if (!currentUserProfile.friends.contains(friend.uid)) {
        val updatedFriendsList =
            currentUserProfile.friends.toMutableList().apply { add(friend.uid) }

        // Create a new profile object with the updated friends list
        val updatedProfile = currentUserProfile.copy(friends = updatedFriendsList)

        // Update the user profile with the new friends list
        updateUserProfile(updatedProfile)

        // Optionally: Update the state with the new friend in friendsProfiles
        friendsProfiles_.value += friend
      } else {
        Log.d("UserProfileViewModel", "Friend ${friend.name} is already in the list.")
      }
    } else {
      Log.e("UserProfileViewModel", "Failed to add friend: Current user profile is null.")
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
     * Adds a metric value to the queue while ensuring the queue maintains a maximum size of 10 elements.
     *
     * This function adds the given metric value to the end of the queue. If the queue already contains
     * 10 elements, the oldest element (at the front of the queue) is removed before adding the new metric.
     *
     * @param queue The queue to which the metric will be added.
     *              The queue is updated in-place to reflect the changes.
     * @param value The new value to be added to the queue.
     *
     * @return The queue with the new value
     */
    private fun addLatestMetric(queue: MutableStateFlow<ArrayDeque<Double>>, value: Double): ArrayDeque<Double> {
        val updatedQueue = queue.value.apply {
            if (size >= 10) {
                removeFirst()
            } // Remove the oldest element if the queue is full
            addLast(value) // Add the new metric to the end of the queue
        }
        return updatedQueue
    }

    /**
     * Adds the latest "talk time seconds" value to its respective queue and update the profile to save
     * the updated queue
     *
     * @param value The new value to be added to the queue.
     */
    fun addTalkTimeSec(value: Double){
        val currentUserProfile = userProfile_.value
        if (currentUserProfile != null) {
            val updatedQueue = addLatestMetric(recentTalkTimeSec_, value)

            // Create a new profile object with the updated queue
            val updatedProfile = currentUserProfile.copy(statistics = UserStatistics(recentTalkTimeSec = updatedQueue))

            // Updates the user profile with the new one
            updateUserProfile(updatedProfile)

            userProfile_.value = updatedProfile
        } else {
            Log.e("UserProfileViewModel", "Failed to add new metric value: Current user profile is null.")
        }
    }

    /**
     * Adds the latest "talk time percentage" value to its respective queue and update the profile to save
     * the updated queue
     *
     * @param value The new value to be added to the queue.
     */
    fun addTalkTimePerc(value: Double){
        val currentUserProfile = userProfile_.value
        if (currentUserProfile != null) {
            val updatedQueue = addLatestMetric(recentTalkTimePerc_, value)

            // Create a new profile object with the updated queue
            val updatedProfile = currentUserProfile.copy(statistics = UserStatistics(recentTalkTimePerc = updatedQueue))

            // Updates the user profile with the new one
            updateUserProfile(updatedProfile)

            userProfile_.value = updatedProfile
        } else {
            Log.e("UserProfileViewModel", "Failed to add new metric value: Current user profile is null.")
        }
    }

    /**
     * Calculates the means of the values of talk time seconds and percentage queues and update the
     * profile with the new means
     */
    fun updateMetricMean() {
        val currentUserProfile = userProfile_.value

        if (currentUserProfile != null) {
            val currentStats = currentUserProfile.statistics
           // Calculate the mean of the values of the metric queues
           val updatedTalkTimeSecMean = repository.getMetricMean(recentTalkTimeSec_.value)
           val updatedTalkTimePercMean = repository.getMetricMean(recentTalkTimePerc_.value)

            // Create a new statistics object with the updated means
            val updatedStats = currentStats.copy(
                talkTimeSecMean = updatedTalkTimeSecMean,
                talkTimePercMean = updatedTalkTimePercMean
            )

            // Create a new profile object with the updated stats
            val updatedProfile = currentUserProfile.copy(statistics = updatedStats)

            userProfile_.value = updatedProfile
        } else {
            Log.e("UserProfileViewModel", "Failed to update metric means: Current user profile is null.")
        }
    }
}
