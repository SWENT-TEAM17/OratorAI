package com.github.se.orator.ui.network


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.orator.R
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route

/**
 * Composable function to display the offline screen with a Practice Offline button.
 *
 * @param navigationActions The NavigationActions object to handle navigation.
 */
@Composable
fun OfflineScreen(navigationActions: NavigationActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_no_wifi),
            contentDescription = "No connection",
            modifier = Modifier.size(120.dp),
            tint = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "It seems like you don't have any WiFi connection",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navigationActions.navigateTo(Route.PRACTICE_QUESTIONS) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(48.dp)
                .width(200.dp)
        ) {
            Text(text = "Practice Offline", color = Color.White, fontSize = 16.sp)
        }
    }
}



