package com.github.se.orator.model.profile

import android.net.Uri
import android.util.Log
import com.github.se.orator.utils.formatDate
import com.github.se.orator.utils.getCurrentDate
import com.github.se.orator.utils.getDaysDifference
import com.github.se.orator.utils.parseDate
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

/**
 * Repository class for managing user profiles in Firestore.
 *
 * @property db The Firestore database instance.
 */
class UserProfileRepositoryFirestore(private val db: FirebaseFirestore) : UserProfileRepository {

  private val collectionPath = "user_profiles"

  companion object {
    const val FIELD_PROFILE_PIC = "profilePic"
  }

  /**
   * Get the UID for the current authenticated user.
   *
   * @return The UID of the current user, or null if not authenticated.
   */
  override fun getCurrentUserUid(): String? {
    return FirebaseAuth.getInstance().currentUser?.uid
  }

  /**
   * Add a new user profile to Firestore.
   *
   * @param userProfile The user profile to be added.
   * @param onSuccess Callback to be invoked on successful addition.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  override fun addUserProfile(
      userProfile: UserProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(userProfile.uid).set(userProfile),
        onSuccess,
        onFailure)
  }

  /**
   * Get user profile by UID.
   *
   * @param uid The UID of the user whose profile is to be fetched.
   * @param onSuccess Callback to be invoked with the fetched user profile.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  override fun getUserProfile(
      uid: String,
      onSuccess: (UserProfile?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).document(uid).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userProfile = task.result?.let { documentToUserProfile(it) }
        onSuccess(userProfile)
      } else {
        task.exception?.let { e ->
          Log.e("UserProfileRepository", "Error getting user profile", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Fetches all user profiles from the Firestore database. On success, it returns a list of
   * [UserProfile] objects through the [onSuccess] callback. On failure, it returns an exception
   * through the [onFailure] callback.
   *
   * @param onSuccess A lambda function that receives a list of [UserProfile] objects if the
   *   operation succeeds.
   * @param onFailure A lambda function that receives an [Exception] if the operation fails.
   */
  override fun getAllUserProfiles(
      onSuccess: (List<UserProfile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val profiles = querySnapshot.documents.mapNotNull { documentToUserProfile(it) }
          onSuccess(profiles)
        }
        .addOnFailureListener { exception ->
          Log.e("UserProfileRepository", "Error fetching all user profiles", exception)
          onFailure(exception)
        }
  }

  /**
   * Update an existing user profile in Firestore.
   *
   * @param userProfile The user profile to be updated.
   * @param onSuccess Callback to be invoked on successful update.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  override fun updateUserProfile(
      userProfile: UserProfile,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(userProfile.uid).set(userProfile),
        onSuccess,
        onFailure)
  }

  // Delete a user profile
  override fun deleteUserProfile(
      uid: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    performFirestoreOperation(
        db.collection(collectionPath).document(uid).delete(), onSuccess, onFailure)
  }

  /**
   * Upload profile picture to Firebase Storage.
   *
   * @param uid The UID of the user.
   * @param imageUri The URI of the image to be uploaded.
   * @param onSuccess Callback to be invoked with the download URL of the uploaded image.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  override fun uploadProfilePicture(
      uid: String,
      imageUri: Uri,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Create a reference to the location where the profile picture will be stored
    val storageReference =
        FirebaseStorage.getInstance().reference.child("profile_pictures/$uid.jpg")

    Log.d("FirebaseStorage", "Uploading to: profile_pictures/$uid.jpg")
    // Upload the image to Firebase Storage
    storageReference
        .putFile(imageUri)
        .addOnSuccessListener {
          // Get the download URL after successful upload
          storageReference.downloadUrl
              .addOnSuccessListener { uri ->
                // Call onSuccess with the download URL
                onSuccess(uri.toString())
              }
              .addOnFailureListener { exception ->
                // Handle the failure to get the download URL
                onFailure(exception)
              }
        }
        .addOnFailureListener { exception ->
          // Handle failure of the image upload
          onFailure(exception)
        }
  }

  /**
   * Update user profile picture URL in Firestore.
   *
   * @param uid The UID of the user.
   * @param downloadUrl The download URL of the uploaded profile picture.
   * @param onSuccess Callback to be invoked on successful update.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  override fun updateUserProfilePicture(
      uid: String,
      downloadUrl: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    Log.d(
        "UserProfileRepositoryFirestore",
        "Attempting to update Firestore for user: $uid with URL: $downloadUrl")

    // Update the user's Firestore document with the profile picture download URL
    db.collection(collectionPath)
        .document(uid)
        .update(FIELD_PROFILE_PIC, downloadUrl)
        .addOnSuccessListener {
          Log.d(
              "UserProfileRepositoryFirestore",
              "Profile picture URL updated in Firestore successfully.")
          onSuccess()
        }
        .addOnFailureListener { exception ->
          Log.e(
              "UserProfileRepositoryFirestore",
              "Failed to update profile picture URL in Firestore.",
              exception)
          onFailure(exception)
        }
  }

  /**
   * Convert Firestore document to UserProfile object.
   *
   * @param document The Firestore document to be converted.
   * @return The converted UserProfile object, or null if conversion fails.
   */
  private fun documentToUserProfile(document: DocumentSnapshot): UserProfile? {
    return try {
      val uid = document.id
      val name = document.getString("name") ?: return null
      val age = document.getLong("age")?.toInt() ?: return null
      val lastLoginDate = document.getString("lastLoginDate")
      val currentStreak = document.getLong("currentStreak") ?: 0L

      // Retrieve the 'statistics' map from the document
      val statisticsMap = document.get("statistics") as? Map<*, *>
      val statistics =
          statisticsMap?.let {
            // Extract 'sessionsGiven' map and convert values to Int
            val sessionsGivenMapAny = it["sessionsGiven"] as? Map<String, Any>
            val sessionsGiven =
                sessionsGivenMapAny
                    ?.mapValues { entry -> (entry.value as? Number)?.toInt() ?: 0 }
                    ?.toMutableMap() ?: mutableMapOf()

            // Extract 'successfulSessions' map and convert values to Int
            val successfulSessionsMapAny = it["successfulSessions"] as? Map<String, Any>
            val successfulSessions =
                successfulSessionsMapAny
                    ?.mapValues { entry -> (entry.value as? Number)?.toInt() ?: 0 }
                    ?.toMutableMap() ?: mutableMapOf()

            // Extract 'improvement' value
            val improvement = (it["improvement"] as? Number)?.toFloat() ?: 0.0f

            // Extract 'previousRuns' list and map each entry to 'SpeechStats'
            val previousRunsList = it["previousRuns"] as? List<Map<String, Any>>
            val previousRuns =
                previousRunsList?.map { run ->
                  SpeechStats(
                      title = run["title"] as? String ?: "",
                      duration = (run["duration"] as? Number)?.toInt() ?: 0,
                      date = run["date"] as? Timestamp ?: Timestamp.now(),
                      accuracy = (run["accuracy"] as? Number)?.toFloat() ?: 0.0f,
                      wordsPerMinute = (run["wordsPerMinute"] as? Number)?.toInt() ?: 0)
                } ?: emptyList()

            // Construct the 'UserStatistics' object
            UserStatistics(
                sessionsGiven = sessionsGiven,
                successfulSessions = successfulSessions,
                improvement = improvement,
                previousRuns = previousRuns)
          } ?: UserStatistics() // Default to an empty 'UserStatistics' if none found

      // Retrieve other fields from the document
      val friends = document.get("friends") as? List<String> ?: emptyList()
      val profilePic = document.getString(FIELD_PROFILE_PIC)
      val bio = document.getString("bio")

      // Construct and return the 'UserProfile' object
      UserProfile(
          uid = uid,
          name = name,
          age = age,
          statistics = statistics,
          friends = friends,
          profilePic = profilePic,
          currentStreak = currentStreak,
          lastLoginDate = lastLoginDate,
          bio = bio)
    } catch (e: Exception) {
      Log.e("UserProfileRepository", "Error converting document to UserProfile", e)
      null
    }
  }

  /**
   * Helper function to perform Firestore operations.
   *
   * @param task The Firestore task to be performed.
   * @param onSuccess Callback to be invoked on successful completion.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  private fun performFirestoreOperation(
      task: Task<Void>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    task.addOnCompleteListener { result ->
      if (result.isSuccessful) {
        onSuccess()
      } else {
        result.exception?.let { e ->
          Log.e("UserProfileRepository", "Error performing Firestore operation", e)
          onFailure(e)
        }
      }
    }
  }

  /**
   * Get friends' profiles based on the UIDs stored in the user's profile.
   *
   * @param friendUids List of UIDs of the friends to be retrieved.
   * @param onSuccess Callback to be invoked with the list of friends' profiles.
   * @param onFailure Callback to be invoked on failure with the exception.
   */
  override fun getFriendsProfiles(
      friendUids: List<String>,
      onSuccess: (List<UserProfile>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (friendUids.isEmpty()) {
      onSuccess(emptyList()) // Return an empty list if no friends
      return
    }

    db.collection(collectionPath)
        .whereIn("uid", friendUids)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val friends = querySnapshot.documents.mapNotNull { documentToUserProfile(it) }
          onSuccess(friends)
        }
        .addOnFailureListener { exception ->
          Log.e("UserProfileRepository", "Error fetching friends profiles", exception)
          onFailure(exception)
        }
  }

  override fun updateLoginStreak(uid: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
    val userRef = db.collection(collectionPath).document(uid)
    db.runTransaction { transaction ->
          val snapshot = transaction.get(userRef)
          val currentDate = getCurrentDate()
          val lastLoginDateString = snapshot.getString("lastLoginDate")
          val currentStreak = snapshot.getLong("currentStreak") ?: 0L
          val updatedStreak: Long
          val lastLoginDate: Date?
          if (lastLoginDateString != null) {
            lastLoginDate = parseDate(lastLoginDateString)
            val daysDifference = getDaysDifference(lastLoginDate, currentDate)
            updatedStreak =
                when (daysDifference) {
                  0L -> currentStreak // Same day login
                  1L -> currentStreak + 1 // Consecutive day
                  else -> 1L // Streak broken
                }
          } else {
            // First-time login
            updatedStreak = 1L
          }
          // Update the fields
          transaction.update(
              userRef,
              mapOf("lastLoginDate" to formatDate(currentDate), "currentStreak" to updatedStreak))
        }
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          Log.e("UserProfileRepository", "Error updating login streak", exception)
          onFailure()
        }
  }
}
