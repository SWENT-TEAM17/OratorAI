package com.github.se.orator.ui.navigation

import androidx.compose.foundation.Image
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigation
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigationItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.github.se.orator.R

@Composable
fun BottomNavigationMenu() {
    BottomNavigation(backgroundColor = Color.White, contentColor = Color.Black) {
        BottomNavigationItem(
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "Home",
                    modifier = Modifier.testTag("HomeButton") // Test tag for Home button
                )
            },
            selected = true,
            onClick = { /* Handle home click */})
        BottomNavigationItem(
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile",
                    modifier = Modifier.testTag("ProfileButton") // Test tag for Profile button
                )
            },
            selected = false,
            onClick = { /* Handle profile click */})
        BottomNavigationItem(
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.friends),
                    contentDescription = "Friends",
                    modifier = Modifier.testTag("FriendsButton") // Test tag for Friends button
                )
            },
            selected = false,
            onClick = { /* Handle settings click */})
    }
}