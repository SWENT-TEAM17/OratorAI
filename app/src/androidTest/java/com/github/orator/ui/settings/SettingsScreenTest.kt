import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.orator.model.profile.UserProfileRepository
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.settings.SettingsScreen
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigationActions: NavigationActions
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var userProfileViewModel: UserProfileViewModel
    @Before
    fun setUp() {
        navigationActions = mock(NavigationActions::class.java)
        userProfileRepository = mock(UserProfileRepository::class.java)
        userProfileViewModel = UserProfileViewModel(userProfileRepository)
        `when`(navigationActions.currentRoute()).thenReturn(Screen.HOME)
    }
    @Test
    fun testBackButtonExistsAndClickable() {
        composeTestRule.setContent {
            SettingsScreen(navigationActions = navigationActions, userProfileViewModel = userProfileViewModel)
        }

        composeTestRule.onNodeWithContentDescription("Back").assertExists()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        verify(navigationActions).goBack()  // Verify navigation back action
    }

    @Test
    fun testSettingsButtonsExist() {
        composeTestRule.setContent {
            SettingsScreen(navigationActions = navigationActions, userProfileViewModel = userProfileViewModel)
        }

        // Test that each setting button exists and is clickable
        val settingsTags = listOf(
            "account_management",
            "storage_settings",
            "permissions",
            "theme",
            "invite_friends",
            "notifications",
            "rate_on_the_app_store",
            "about"
        )

        settingsTags.forEach { tag ->
            composeTestRule.onNodeWithTag(tag).assertExists()
            composeTestRule.onNodeWithTag(tag).performClick()
        }
    }
}
