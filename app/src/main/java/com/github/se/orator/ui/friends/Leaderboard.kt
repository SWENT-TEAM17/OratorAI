package com.github.se.orator.ui.friends


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.theme.LightPurpleGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
    val userProfile by userProfileViewModel.userProfile.collectAsState()
    val friendsProfiles by userProfileViewModel.friendsProfiles.collectAsState()

    val leaderboardEntries = remember(userProfile, friendsProfiles) {
        (listOfNotNull(userProfile) + friendsProfiles).sortedByDescending { it.statistics.improvement }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard") },
                navigationIcon = {
                    IconButton(onClick = { navigationActions.goBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { route -> navigationActions.navigateTo(route) },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = Route.FRIENDS
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dropdown selector
            PracticeModeSelector()

            Spacer(modifier = Modifier.height(16.dp))

            // Leaderboard list
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(leaderboardEntries) { index, profile ->
                    LeaderboardItem(rank = index + 1, profile = profile)
                }
            }
        }
    }
}

@Composable
fun PracticeModeSelector() {
    var expanded by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf("Practice mode 1") }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            .clickable { expanded = true }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = selectedMode,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Practice mode 1") },
                onClick = {
                    selectedMode = "Practice mode 1"
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Practice mode 2") },
                onClick = {
                    selectedMode = "Practice mode 2"
                    expanded = false
                }
            )
            // Add more items as needed
        }
    }
}




@Composable
fun LeaderboardItem(rank: Int, profile: UserProfile) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)  // Side padding for each item
            .clip(RoundedCornerShape(20.dp))
        ,
        color = LightPurpleGrey,
        shadowElevation = 4.dp  // Subtle shadow with low elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ProfilePicture(profilePictureUrl = profile.profilePic, onClick = {})
            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "Improvement: ${profile.statistics.improvement}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Display rank as badge on the left side
            Text(
                text = "#$rank",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}