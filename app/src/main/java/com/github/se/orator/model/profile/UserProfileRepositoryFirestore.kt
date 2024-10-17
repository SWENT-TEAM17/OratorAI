package com.github.se.orator.model.profile

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UserProfileRepositoryFirestore(private val db: FirebaseFirestore): UserProfileRepository {

    private val collectionPath = "user_profiles"

    // Get the UID for the current authenticated user
    override fun getCurrentUserUid(): String? {
        return Firebase.auth.currentUser?.uid
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
  override fun getAllUserProfiles(onSuccess: (List<UserProfile>) -> Unit, onFailure: (Exception) -> Unit) {
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

    // Add a new user profile
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


    // Get user profile by UID
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
    override fun deleteUserProfile(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        performFirestoreOperation(
            db.collection(collectionPath).document(uid).delete(), onSuccess, onFailure)
    }

    // Upload profile picture to Firebase Storage
    override fun uploadProfilePicture(
        uid: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storageReference =
            FirebaseStorage.getInstance().reference.child("profile_pictures/$uid.jpg")
        storageReference
            .putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl
                    .addOnSuccessListener { uri -> onSuccess(uri.toString()) }
                    .addOnFailureListener { exception -> onFailure(exception) }
            }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    // Update user profile picture in Firestore
    override fun updateUserProfilePicture(
        uid: String,
        downloadUrl: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection(collectionPath)
            .document(uid)
            .update("profilePictureUrl", downloadUrl)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    // Helper function to perform Firestore operations
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

    // Convert Firestore document to UserProfile object
    private fun documentToUserProfile(document: DocumentSnapshot): UserProfile? {
        return try {
            val uid = document.id
            val name = document.getString("name") ?: return null
            val age = document.getLong("age")?.toInt() ?: return null
            val statisticsMap = document.get("statistics") as? Map<*, *>
            val statistics =
                statisticsMap?.let {
                    UserStatistics(
                        speechesGiven = it["speechesGiven"] as? Int ?: 0,
                        improvement = it["improvement"] as? Float ?: 0.0f,
                        previousRuns =
                        (it["previousRuns"] as? List<Map<String, Any>>)?.map { run ->
                            SpeechStats(
                                title = run["title"] as? String ?: "",
                                duration = run["duration"] as? Int ?: 0,
                                date = run["date"] as? Timestamp ?: Timestamp.now(),
                                accuracy = run["accuracy"] as? Float ?: 0.0f,
                                wordsPerMinute = run["wordsPerMinute"] as? Int ?: 0)
                        } ?: emptyList())
                } ?: UserStatistics()

            val friends = document.get("friends") as? List<String> ?: emptyList()

            UserProfile(
                uid = uid,
                name = name,
                age = age,
                statistics = statistics,
                friends = friends,
                profilePic = document.getString("profilePic"))
        } catch (e: Exception) {
            Log.e("UserProfileRepository", "Error converting document to UserProfile", e)
            null
        }
    }

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
}