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

class UserProfileRepositoryFirestore(private val db: FirebaseFirestore) {

  private val collectionPath = "user_profiles"

  // Get the UID for the current authenticated user
  fun getCurrentUserUid(): String? {
    return Firebase.auth.currentUser?.uid
  }

  // Add a new user profile
  fun addUserProfile(
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
  fun getUserProfile(
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

  // Update an existing user profile
  fun updateUserProfile(
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
  fun deleteUserProfile(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    performFirestoreOperation(
        db.collection(collectionPath).document(uid).delete(), onSuccess, onFailure)
  }

  // Upload profile picture to Firebase Storage
  fun uploadProfilePicture(
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
  fun updateUserProfilePicture(
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

  fun getFriendsProfiles(
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
