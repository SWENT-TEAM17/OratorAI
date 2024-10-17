package com.github.se.orator.ui.friends

import ViewFriendsScreen
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileRepositoryFirestore
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.profile.UserStatistics
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class FriendsUITests {

    @get:Rule
    val composeTestRule = createComposeRule()

    //Friends List
    private val profile1 = UserProfile("1", "John Doe", 99, statistics = UserStatistics())
    private val profile2 = UserProfile("2", "Jane Doe", 100, statistics = UserStatistics())

    @Mock private lateinit var mockNavigationActions: NavigationActions
    @Mock private lateinit var mockUserProfileRepository: UserProfileRepository
    private lateinit var mockUserProfileViewModel: UserProfileViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testActionButtonWorks(){
        viewFriendsTestsSetup()

        composeTestRule.onNodeWithTag("viewFriendsDrawerMenu").assertIsNotDisplayed()

        composeTestRule.onNodeWithTag("viewFriendsMenuButton")
            .assertExists("The menu button on the view friends screen does not exist")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag("viewFriendsDrawerMenu").assertIsDisplayed()
        composeTestRule.onNodeWithTag("viewFriendsDrawerTitle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("viewFriendsAddFriendButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("viewFriendsLeaderboardButton").assertIsDisplayed()
    }

    @Test
    fun testFriendsListIsDisplayed(){
        //When getUserProfiles is called on the repository, a custom list of profiles is passed.
        `when`(mockUserProfileRepository.getFriendsProfiles(any(), any(), any())).then {
            it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(profile1, profile2))
        }
        viewFriendsTestsSetup()

        composeTestRule.onNodeWithTag("viewFriendsList").assertIsDisplayed()
        composeTestRule.onNodeWithTag("viewFriendsItem#1").assertIsDisplayed()
    }

    @Test
    fun testCanGoToAddFriendAndLeaderboardScreens(){
        viewFriendsTestsSetup()

        composeTestRule.onNodeWithTag("friendsDrawerMenu").performClick()

        composeTestRule.onNodeWithTag("viewFriendsAddFriendButton").performClick()
        verify(mockNavigationActions).navigateTo(eq(Screen.ADD_FRIENDS))

        composeTestRule.onNodeWithTag("viewFriendsLeaderboardButton").performClick()
        verify(mockNavigationActions).navigateTo(eq(Screen.ADD_FRIENDS))
    }

    @Test
    fun testCanSearchForFriends(){
        //When getUserProfiles is called on the repository, a custom list of profiles is passed.
        `when`(mockUserProfileRepository.getFriendsProfiles(any(), any(), any())).then {
            it.getArgument<(List<UserProfile>) -> Unit>(1)(listOf(profile1, profile2))
        }
        viewFriendsTestsSetup()

        composeTestRule.onNodeWithTag("viewFriendsSearch").performClick()
        composeTestRule.onNodeWithTag("viewFriendsSearch").performTextInput("John")

        composeTestRule.onNodeWithTag("viewFriendsItem#1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("viewFriendsItem#2").assertIsNotDisplayed()
    }


    /**
     * Function used to setup a testing environment for the ViewFriendsScreen tests
     */
    private fun viewFriendsTestsSetup(){
        `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.FRIENDS)
        composeTestRule.setContent { ViewFriendsScreen(mockNavigationActions, mockUserProfileViewModel) }
    }
}