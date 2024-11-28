[1mdiff --git a/app/src/androidTest/java/com/github/se/orator/ui/friends/FriendsUITests.kt b/app/src/androidTest/java/com/github/se/orator/ui/friends/FriendsUITests.kt[m
[1mindex 2c36792..3b14a43 100644[m
[1m--- a/app/src/androidTest/java/com/github/se/orator/ui/friends/FriendsUITests.kt[m
[1m+++ b/app/src/androidTest/java/com/github/se/orator/ui/friends/FriendsUITests.kt[m
[36m@@ -226,7 +226,6 @@[m [mclass FriendsUITests {[m
         .onNodeWithTag("deleteFriendButton#${profile1.uid}", useUnmergedTree = true)[m
         .assertExists()[m
         .performClick()[m
[31m-[m
     composeTestRule[m
         .onNodeWithTag("deleteFriendButton#${profile1.uid}", useUnmergedTree = true)[m
         .assertIsNotDisplayed()[m
[1mdiff --git a/app/src/main/java/com/github/se/orator/model/profile/UserProfileRepository.kt b/app/src/main/java/com/github/se/orator/model/profile/UserProfileRepository.kt[m
[1mindex 9108310..e480ba5 100644[m
[1m--- a/app/src/main/java/com/github/se/orator/model/profile/UserProfileRepository.kt[m
[1m+++ b/app/src/main/java/com/github/se/orator/model/profile/UserProfileRepository.kt[m
[36m@@ -62,22 +62,53 @@[m [minterface UserProfileRepository {[m
       onFailure: (Exception) -> Unit[m
   )[m
 [m
[32m+[m[32m  /**[m
[32m+[m[32m   * Fetches all user profiles from the Firestore database. On success, it returns a list of[m
[32m+[m[32m   * [UserProfile] objects through the [onSuccess] callback. On failure, it returns an exception[m
[32m+[m[32m   * through the [onFailure] callback.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param onSuccess A lambda function that receives a list of [UserProfile] objects if the[m
[32m+[m[32m   *   operation succeeds.[m
[32m+[m[32m   * @param onFailure A lambda function that receives an [Exception] if the operation fails.[m
[32m+[m[32m   */[m
   fun getAllUserProfiles(onSuccess: (List<UserProfile>) -> Unit, onFailure: (Exception) -> Unit)[m
 [m
[32m+[m[32m  /**[m
[32m+[m[32m   * Sends a friend request from the current user to another user.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param currentUid The UID of the user sending the request.[m
[32m+[m[32m   * @param friendUid The UID of the user receiving the request.[m
[32m+[m[32m   * @param onSuccess Callback invoked on successful operation.[m
[32m+[m[32m   * @param onFailure Callback invoked with an [Exception] on failure.[m
[32m+[m[32m   */[m
   fun sendFriendRequest([m
       currentUid: String,[m
       friendUid: String,[m
       onSuccess: () -> Unit,[m
       onFailure: (Exception) -> Unit[m
   )[m
[31m-[m
[32m+[m[32m  /**[m
[32m+[m[32m   * Accepts a friend request, establishing a friendship between two users.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param currentUid The UID of the current user.[m
[32m+[m[32m   * @param friendUid The UID of the user who sent the request.[m
[32m+[m[32m   * @param onSuccess Callback invoked on successful operation.[m
[32m+[m[32m   * @param onFailure Callback invoked with an [Exception] on failure.[m
[32m+[m[32m   */[m
   fun acceptFriendRequest([m
       currentUid: String,[m
       friendUid: String,[m
       onSuccess: () -> Unit,[m
       onFailure: (Exception) -> Unit[m
   )[m
[31m-[m
[32m+[m[32m  /**[m
[32m+[m[32m   * Declines a friend request from another user.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param currentUid The UID of the current user.[m
[32m+[m[32m   * @param friendUid The UID of the user who sent the request.[m
[32m+[m[32m   * @param onSuccess Callback invoked on successful operation.[m
[32m+[m[32m   * @param onFailure Callback invoked with an [Exception] on failure.[m
[32m+[m[32m   */[m
   fun declineFriendRequest([m
       currentUid: String,[m
       friendUid: String,[m
[36m@@ -85,6 +116,14 @@[m [minterface UserProfileRepository {[m
       onFailure: (Exception) -> Unit[m
   )[m
 [m
[32m+[m[32m  /**[m
[32m+[m[32m   * Deletes an existing friendship between two users.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param currentUid The UID of the current user.[m
[32m+[m[32m   * @param friendUid The UID of the friend to remove.[m
[32m+[m[32m   * @param onSuccess Callback invoked on successful operation.[m
[32m+[m[32m   * @param onFailure Callback invoked with an [Exception] on failure.[m
[32m+[m[32m   */[m
   fun deleteFriend([m
       currentUid: String,[m
       friendUid: String,[m
[36m@@ -92,6 +131,17 @@[m [minterface UserProfileRepository {[m
       onFailure: (Exception) -> Unit[m
   )[m
 [m
[32m+[m[32m  /**[m
[32m+[m[32m   * Cancel a previously sent friend request.[m
[32m+[m[32m   *[m
[32m+[m[32m   * This function removes the `friendUid` from the current user's `sentReq` list and removes the[m
[32m+[m[32m   * `currentUid` from the friend's `recReq` list, effectively canceling the friend request.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param currentUid The UID of the current user who sent the friend request.[m
[32m+[m[32m   * @param friendUid The UID of the friend to whom the request was sent.[m
[32m+[m[32m   * @param onSuccess Callback to be invoked on successful cancellation.[m
[32m+[m[32m   * @param onFailure Callback to be invoked on failure with the exception.[m
[32m+[m[32m   */[m
   fun cancelFriendRequest([m
       currentUid: String,[m
       friendUid: String,[m
[36m@@ -136,5 +186,12 @@[m [minterface UserProfileRepository {[m
    */[m
   fun deleteUserProfile(uid: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)[m
 [m
[32m+[m[32m  /**[m
[32m+[m[32m   * Updates the login streak for a user based on their last login date.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param uid The UID of the user.[m
[32m+[m[32m   * @param onSuccess Callback invoked on successful operation.[m
[32m+[m[32m   * @param onFailure Callback invoked on failure.[m
[32m+[m[32m   */[m
   fun updateLoginStreak(uid: String, onSuccess: () -> Unit, onFailure: () -> Unit)[m
 }[m
[1mdiff --git a/app/src/main/java/com/github/se/orator/model/profile/UserProfileRepositoryFirestore.kt b/app/src/main/java/com/github/se/orator/model/profile/UserProfileRepositoryFirestore.kt[m
[1mindex fcde8cf..293a4b7 100644[m
[1m--- a/app/src/main/java/com/github/se/orator/model/profile/UserProfileRepositoryFirestore.kt[m
[1m+++ b/app/src/main/java/com/github/se/orator/model/profile/UserProfileRepositoryFirestore.kt[m
[36m@@ -122,7 +122,13 @@[m [mclass UserProfileRepositoryFirestore(private val db: FirebaseFirestore) : UserPr[m
         onFailure)[m
   }[m
 [m
[31m-  // Delete a user profile[m
[32m+[m[32m  /**[m
[32m+[m[32m   * Deletes a user profile from Firestore.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param uid The UID of the user to delete.[m
[32m+[m[32m   * @param onSuccess Callback invoked on successful deletion.[m
[32m+[m[32m   * @param onFailure Callback invoked with an [Exception] on failure.[m
[32m+[m[32m   */[m
   override fun deleteUserProfile([m
       uid: String,[m
       onSuccess: () -> Unit,[m
[36m@@ -344,6 +350,14 @@[m [mclass UserProfileRepositoryFirestore(private val db: FirebaseFirestore) : UserPr[m
         }[m
   }[m
 [m
[32m+[m[32m  /**[m
[32m+[m[32m   * Sends a friend request from the current user to another user.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param currentUid The UID of the user sending the request.[m
[32m+[m[32m   * @param friendUid The UID of the user receiving the request.[m
[32m+[m[32m   * @param onSuccess Callback invoked on successful operation.[m
[32m+[m[32m   * @param onFailure Callback invoked with an [Exception] on failure.[m
[32m+[m[32m   */[m
   override fun sendFriendRequest([m
       currentUid: String,[m
       friendUid: String,[m
[36m@@ -394,7 +408,14 @@[m [mclass UserProfileRepositoryFirestore(private val db: FirebaseFirestore) : UserPr[m
           onFailure(exception)[m
         }[m
   }[m
[31m-[m
[32m+[m[32m  /**[m
[32m+[m[32m   * Accepts a friend request, establishing a friendship between two users.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param currentUid The UID of the current user.[m
[32m+[m[32m   * @param friendUid The UID of the user who sent the request.[m
[32m+[m[32m   * @param onSuccess Callback invoked on successful operation.[m
[32m+[m[32m   * @param onFailure Callback invoked with an [Exception] on failure.[m
[32m+[m[32m   */[m
   override fun acceptFriendRequest([m
       currentUid: String,[m
       friendUid: String,[m
[36m@@ -456,6 +477,14 @@[m [mclass UserProfileRepositoryFirestore(private val db: FirebaseFirestore) : UserPr[m
         }[m
   }[m
 [m
[32m+[m[32m  /**[m
[32m+[m[32m   * Declines a friend request from another user.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param currentUid The UID of the current user.[m
[32m+[m[32m   * @param friendUid The UID of the user who sent the request.[m
[32m+[m[32m   * @param onSuccess Callback invoked on successful operation.[m
[32m+[m[32m   * @param onFailure Callback invoked with an [Exception] on failure.[m
[32m+[m[32m   */[m
   override fun declineFriendRequest([m
       currentUid: String,[m
       friendUid: String,[m
[36m@@ -567,6 +596,14 @@[m [mclass UserProfileRepositoryFirestore(private val db: FirebaseFirestore) : UserPr[m
         }[m
   }[m
 [m
[32m+[m[32m  /**[m
[32m+[m[32m   * Deletes an existing friendship between two users.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param currentUid The UID of the current user.[m
[32m+[m[32m   * @param friendUid The UID of the friend to remove.[m
[32m+[m[32m   * @param onSuccess Callback invoked on successful operation.[m
[32m+[m[32m   * @param onFailure Callback invoked with an [Exception] on failure.[m
[32m+[m[32m   */[m
   override fun deleteFriend([m
       currentUid: String,[m
       friendUid: String,[m
[36m@@ -612,6 +649,13 @@[m [mclass UserProfileRepositoryFirestore(private val db: FirebaseFirestore) : UserPr[m
         }[m
   }[m
 [m
[32m+[m[32m  /**[m
[32m+[m[32m   * Updates the login streak for a user based on their last login date.[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param uid The UID of the user.[m
[32m+[m[32m   * @param onSuccess Callback invoked on successful operation.[m
[32m+[m[32m   * @param onFailure Callback invoked on failure.[m
[32m+[m[32m   */[m
   override fun updateLoginStreak(uid: String, onSuccess: () -> Unit, onFailure: () -> Unit) {[m
     val userRef = db.collection(collectionPath).document(uid)[m
     db.runTransaction { transaction ->[m
[1mdiff --git a/app/src/main/java/com/github/se/orator/model/profile/UserProfileViewModel.kt b/app/src/main/java/com/github/se/orator/model/profile/UserProfileViewModel.kt[m
[1mindex 66b6dc4..38c7c55 100644[m
[1m--- a/app/src/main/java/com/github/se/orator/model/profile/UserProfileViewModel.kt[m
[1m+++ b/app/src/main/java/com/github/se/orator/model/profile/UserProfileViewModel.kt[m
[36m@@ -10,9 +10,12 @@[m [mimport kotlinx.coroutines.flow.StateFlow[m
 import kotlinx.coroutines.flow.asStateFlow[m
 [m
 /**[m
[31m- * ViewModel for managing user profiles and friends' profiles.[m
[32m+[m[32m * ViewModel for managing user profiles and their interactions, including:[m
[32m+[m[32m * - Fetching and updating user profiles.[m
[32m+[m[32m * - Managing friends, friend requests, and session statistics.[m
[32m+[m[32m * - Handling profile picture uploads and login streaks.[m
  *[m
[31m- * @property repository The repository for accessing user profile data.[m
[32m+[m[32m * @param repository The repository for accessing user profile data.[m
  */[m
 class UserProfileViewModel(internal val repository: UserProfileRepository) : ViewModel() {[m
 [m
[36m@@ -268,6 +271,11 @@[m [mclass UserProfileViewModel(internal val repository: UserProfileRepository) : Vie[m
     }[m
   }[m
 [m
[32m+[m[32m  /**[m
[32m+[m[32m   * Sends a friend request to the specified user if the user did not already sent one[m
[32m+[m[32m   *[m
[32m+[m[32m   * @param friend The [UserProfile] of the user to send a request to.[m
[32m+[m[32m   */[m
   fun sendRequest(friend: UserProfile) {[m
     val currentUid = repository.getCurrentUserUid()[m
     if (currentUid != null) {[m
[36m@@ -336,11 +344,9 @@[m [mclass UserProfileViewModel(internal val repository: UserProfileRepository) : Vie[m
                 "UserProfileViewModel",[m
                 "Failed to cancel friend request to ${friend.name}.",[m
                 exception)[m
[31m-            // Optionally, notify the UI about the failure (e.g., via another StateFlow or LiveData)[m
           })[m
     } else {[m
       Log.e("UserProfileViewModel", "Cannot cancel friend request: User is not authenticated.")[m
[31m-      // Optionally, handle unauthenticated state here (e.g., prompt user to log in)[m
     }[m
   }[m
 [m
[36m@@ -436,7 +442,7 @@[m [mclass UserProfileViewModel(internal val repository: UserProfileRepository) : Vie[m
   }[m
 [m
   /**[m
[31m-   * Deletes a friend from the current user's list of friends.[m
[32m+[m[32m   * Deletes a friend from both the current user's and friend's list of friends.[m
    *[m
    * @param friend The `UserProfile` of the friend to be deleted.[m
    */[m
[36m@@ -450,10 +456,10 @@[m [mclass UserProfileViewModel(internal val repository: UserProfileRepository) : Vie[m
             Log.d("UserProfileViewModel", "Friend deleted: ${friend.name}")[m
 [m
             // Option 1: Refresh the user profile to reflect changes[m
[31m-            getUserProfile(currentUid)[m
[32m+[m[32m            //getUserProfile(currentUid)[m
 [m
             // Option 2: Manually update the local state[m
[31m-            /*[m
[32m+[m
             // Remove the friend from the local friendsProfiles[m
             friendsProfiles_.value = friendsProfiles_.value.filter { it.uid != friend.uid }[m
 [m
[36m@@ -463,7 +469,7 @@[m [mclass UserProfileViewModel(internal val repository: UserProfileRepository) : Vie[m
                 val updatedProfile = userProfile_.value!!.copy(friends = updatedFriendsList)[m
                 userProfile_.value = updatedProfile[m
             }[m
[31m-            */[m
[32m+[m
           },[m
           onFailure = { exception ->[m
             Log.e("UserProfileViewModel", "Failed to delete friend.", exception)[m
[1mdiff --git a/app/src/main/java/com/github/se/orator/ui/friends/AddFriends.kt b/app/src/main/java/com/github/se/orator/ui/friends/AddFriends.kt[m
[1mindex 672cf0a..c75ef25 100644[m
[1m--- a/app/src/main/java/com/github/se/orator/ui/friends/AddFriends.kt[m
[1m+++ b/app/src/main/java/com/github/se/orator/ui/friends/AddFriends.kt[m
[36m@@ -66,12 +66,15 @@[m [mimport com.github.se.orator.ui.theme.AppDimensions[m
 import com.github.se.orator.ui.theme.ProjectTheme[m
 [m
 /**[m
[31m- * Composable function that displays the "Add Friends" screen, allowing users to search and add[m
[31m- * friends. The screen contains a top app bar with a back button, a search field to look for[m
[31m- * friends, and a list of matching user profiles based on the search query.[m
[32m+[m[32m * Composable function that displays the "Add Friends" screen, where users can:[m
[32m+[m[32m * - Search for other users to send friend requests.[m
[32m+[m[32m * - View and manage their sent friend requests.[m
[32m+[m[32m *[m
[32m+[m[32m * The screen includes a search bar, a list of filtered user profiles, and an expandable section[m
[32m+[m[32m * showing filtered sent friend requests.[m
  *[m
  * @param navigationActions Actions to handle navigation within the app.[m
[31m- * @param userProfileViewModel ViewModel for managing user profile data and friend addition logic.[m
[32m+[m[32m * @param userProfileViewModel ViewModel for managing user profile data and friend request logic.[m
  */[m
 @OptIn(ExperimentalMaterial3Api::class)[m
 @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")[m
[36m@@ -247,11 +250,12 @@[m [mfun AddFriendsScreen([m
 }[m
 [m
 /**[m
[31m- * Composable function that represents a single sent friend request item in the list. Displays the[m
[31m- * friend's profile picture, name, bio, and an option to cancel the request.[m
[32m+[m[32m * Composable function that represents a single sent friend request item in the list.[m
[32m+[m[32m *[m
[32m+[m[32m * It displays the friend's profile picture, name, bio, and a button to cancel the request.[m
  *[m
[31m- * @param sentRequest The [UserProfile] object representing the friend to whom the request was sent.[m
[31m- * @param userProfileViewModel The [UserProfileViewModel] that handles request cancellation.[m
[32m+[m[32m * @param sentRequest The [UserProfile] object representing the user to whom the request was sent.[m
[32m+[m[32m * @param userProfileViewModel The [UserProfileViewModel] that handles friend request cancellation.[m
  */[m
 @Composable[m
 fun SentFriendRequestItem(sentRequest: UserProfile, userProfileViewModel: UserProfileViewModel) {[m
[36m@@ -314,13 +318,17 @@[m [mfun SentFriendRequestItem(sentRequest: UserProfile, userProfileViewModel: UserPr[m
 }[m
 [m
 /**[m
[31m- * Composable function that represents a single user item in a list. Displays the user's profile[m
[31m- * picture, name, and bio, and allows adding the user as a friend.[m
[32m+[m[32m * Composable function that represents a single user item in a list of search results.[m
[32m+[m[32m *[m
[32m+[m[32m * It displays the user's profile picture, name, and bio. Users can click on the profile picture to[m
[32m+[m[32m * view an enlarged version or click on the user item to send a friend request.[m
[32m+[m[32m *[m
[32m+[m[32m * If the user has already sent a friend request to the current user, a dialog will appear, giving[m
[32m+[m[32m * the option to accept, reject, or decide later.[m
  *[m
  * @param user The [UserProfile] object representing the user being displayed.[m
[31m- * @param userProfileViewModel The [UserProfileViewModel] that handles the logic of adding a user as[m
[31m- *   a friend.[m
[31m- * @param onProfilePictureClick Callback when the profile picture is clicked.[m
[32m+[m[32m * @param userProfileViewModel The [UserProfileViewModel] that handles friend request actions.[m
[32m+[m[32m * @param onProfilePictureClick Callback triggered when the profile picture is clicked.[m
  */[m
 @Composable[m
 fun UserItem([m
[1mdiff --git a/app/src/main/java/com/github/se/orator/ui/friends/FriendsScreen.kt b/app/src/main/java/com/github/se/orator/ui/friends/FriendsScreen.kt[m
[1mindex f18c30c..971bf59 100644[m
[1m--- a/app/src/main/java/com/github/se/orator/ui/friends/FriendsScreen.kt[m
[1m+++ b/app/src/main/java/com/github/se/orator/ui/friends/FriendsScreen.kt[m
[36m@@ -53,12 +53,16 @@[m [mimport com.github.se.orator.utils.parseDate[m
 import kotlinx.coroutines.launch[m
 [m
 /**[m
[31m- * Composable function that displays the "View Friends" screen, showing a list of friends with a[m
[31m- * search bar and options to navigate to other screens like "Add Friends" and "Leaderboard."[m
[31m- * Additionally, displays received friend requests with options to accept or decline them.[m
[32m+[m[32m * Composable function that displays the "View Friends" screen.[m
  *[m
[31m- * @param navigationActions Actions to handle navigation within the app.[m
[31m- * @param userProfileViewModel ViewModel for managing user profile data and fetching friends.[m
[32m+[m[32m * The screen includes:[m
[32m+[m[32m * - A search bar to filter both the friends list and received friend requests.[m
[32m+[m[32m * - An expandable section showing received friend requests with options to accept or decline.[m
[32m+[m[32m * - A list of friends with options to view their details or remove them.[m
[32m+[m[32m * - Navigation options to "Add Friends" and "Leaderboard."[m
[32m+[m[32m *[m
[32m+[m[32m * @param navigationActions Object to handle navigation within the app.[m
[32m+[m[32m * @param userProfileViewModel ViewModel providing user data and friend management functionality.[m
  */[m
 @OptIn(ExperimentalMaterial3Api::class)[m
 @Composable[m
[36m@@ -218,7 +222,7 @@[m [mfun ViewFriendsScreen([m
                             enter = expandVertically(),[m
                             exit = shrinkVertically()) {[m
                               LazyColumn([m
[31m-                                  modifier = Modifier.testTag("receivedFriendRequestsList"),[m
[32m+[m[32m                                  modifier = Modifier.testTag("viewFriendsList"),[m
                                   contentPadding =[m
                                       PaddingValues(vertical = AppDimensions.paddingSmall),[m
                                   verticalArrangement =[m
[36m@@ -280,12 +284,16 @@[m [mfun ViewFriendsScreen([m
 }[m
 [m
 /**[m
[31m- * Composable function that represents a single friend item in the list. Displays the friend's[m
[31m- * profile picture, name, and bio, along with an option to delete the friend.[m
[32m+[m[32m * Composable function that represents a single friend item in the list.[m
[32m+[m[32m *[m
[32m+[m[32m * It displays:[m
[32m+[m[32m * - The friend's profile picture, name, and bio.[m
[32m+[m[32m * - The friend's login streak or the last login date.[m
[32m+[m[32m * - An option to remove the friend from the user's friend list.[m
  *[m
  * @param friend The [UserProfile] object representing the friend being displayed.[m
  * @param userProfileViewModel The [UserProfileViewModel] that handles friend deletion.[m
[31m- * @param onProfilePictureClick Callback when the profile picture is clicked.[m
[32m+[m[32m * @param onProfilePictureClick Callback triggered when the friend's profile picture is clicked.[m
  */[m
 @Composable[m
 fun FriendItem([m
[36m@@ -388,11 +396,12 @@[m [mfun FriendItem([m
 }[m
 [m
 /**[m
[31m- * Composable function to display a profile picture with a circular shape. Uses Coil to load the[m
[31m- * image asynchronously.[m
[32m+[m[32m * Composable function to display a profile picture in a circular shape.[m
[32m+[m[32m * - Loads the image asynchronously using the Coil library.[m
[32m+[m[32m * - Defaults to a placeholder if the profile picture URL is null.[m
[32m+[m[32m * - Supports a click action on the profile picture.[m
  *[m
[31m- * @param profilePictureUrl The URL of the profile picture to display. Defaults to a placeholder if[m
[31m- *   null.[m
[32m+[m[32m * @param profilePictureUrl The URL of the profile picture to display.[m
  * @param onClick Action to be performed when the profile picture is clicked.[m
  */[m
 @Composable[m
[36m@@ -410,10 +419,12 @@[m [mfun ProfilePicture(profilePictureUrl: String?, onClick: () -> Unit) {[m
 }[m
 [m
 /**[m
[31m- * Button triggering the removing of a friend in the user's friend list.[m
[32m+[m[32m * Composable function for the button to remove a friend from the user's friend list.[m
[32m+[m[32m * - Displays a delete icon.[m
[32m+[m[32m * - Shows a Toast message on successful removal of the friend.[m
  *[m
[31m- * @param friend The friend to be removed.[m
[31m- * @param userProfileViewModel The view model for the user's profile.[m
[32m+[m[32m * @param friend The [UserProfile] of the friend to be removed.[m
[32m+[m[32m * @param userProfileViewModel The [UserProfileViewModel] that handles friend deletion logic.[m
  */[m
 @Composable[m
 fun DeleteFriendButton(friend: UserProfile, userProfileViewModel: UserProfileViewModel) {[m
[36m@@ -436,10 +447,12 @@[m [mfun DeleteFriendButton(friend: UserProfile, userProfileViewModel: UserProfileVie[m
 [m
 /**[m
  * Computes the current streak of a friend based on their last login date and current streak.[m
[32m+[m[32m * - A streak continues if the last login was on the same day or the following day.[m
[32m+[m[32m * - A broken streak resets to 0.[m
  *[m
  * @param lastLoginDateString The last login date as a string in "yyyy-MM-dd" format. Can be null.[m
  * @param currentStreak The current streak value.[m
[31m- * @return The streak to be displayed: either currentStreak or 0.[m
[32m+[m[32m * @return The streak to be displayed: the `currentStreak` if active, otherwise 0.[m
  */[m
 fun currentFriendStreak(lastLoginDateString: String?, currentStreak: Long): Long {[m
   if (!lastLoginDateString.isNullOrEmpty()) {[m
[36m@@ -455,12 +468,16 @@[m [mfun currentFriendStreak(lastLoginDateString: String?, currentStreak: Long): Long[m
   return -1L // No last login date recorded[m
 }[m
 /**[m
[31m- * Composable function that represents a single friend request item in the list. Displays the[m
[31m- * friend's profile picture, name, bio, and options to accept or decline the request.[m
[32m+[m[32m * Composable function that represents a single friend request item in the list.[m
[32m+[m[32m *[m
[32m+[m[32m * It displays:[m
[32m+[m[32m * - The requester's profile picture, name, and bio.[m
[32m+[m[32m * - Buttons to accept or decline the friend request.[m
[32m+[m[32m * - Handles user interactions and updates the state through the ViewModel.[m
  *[m
[31m- * @param friendRequest The [UserProfile] object representing the friend who sent the request.[m
[31m- * @param userProfileViewModel The [UserProfileViewModel] that handles accepting or declining[m
[31m- *   requests.[m
[32m+[m[32m * @param friendRequest The [UserProfile] object representing the user who sent the request.[m
[32m+[m[32m * @param userProfileViewModel The [UserProfileViewModel] that handles accepting or declining the[m
[32m+[m[32m *   request.[m
  */[m
 @Composable[m
 fun FriendRequestItem(friendRequest: UserProfile, userProfileViewModel: UserProfileViewModel) {[m
