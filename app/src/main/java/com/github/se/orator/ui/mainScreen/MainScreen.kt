package com.github.se.orator.ui.theme.mainScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.github.se.orator.R
import kotlinx.coroutines.delay

/**
 * The main screen's composable responsible to display the welcome text, the practice mode cards
 * and the "view my progress" button
 */
@Composable
fun MainScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // The name to be displayed (hard coded for now)
                var name = "name"

                // Welcome text
                Text(
                    modifier = Modifier
                        .padding(vertical = 32.dp, horizontal = 32.dp)
                        .padding(start = 16.dp)
                        .padding(top = 32.dp)
                        .testTag("mainScreenText"),
                    text = "Hi $name, what do you want to practice today ?",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                // Practice mode cards
                StackedCards()

                // Progress button
                ProgressButton() {
                    // go to progress page
                }

            }
        }
    )
}

@Composable
fun ProgressButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .padding(vertical = 32.dp)
            .border(
                BorderStroke(2.dp, Color.Black),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("mainScreenButton")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chart Icon
            Image(
                painter = painterResource(R.drawable.chart),
                contentDescription = "Chart Icon",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp)
            )
            Text(
                text = "View my progress",
                color = Color(0, 48, 168),
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

/**
 * Function to create the "card stack" effect
 */
@Composable
fun StackedCards() {
    Box(
        modifier = Modifier
            .padding(start = 64.dp, end = 64.dp, top = 64.dp, bottom = 64.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .testTag("stackedCards")
    ) {
        // The "hidden cards" behind
        // HiddenCard(Modifier.offset(14.dp, (-30).dp), 150)
        //HiddenCard(Modifier.offset(7.dp, (-15).dp), 200)

        // Main front card

        var visible by remember { mutableStateOf(true) }
        val cardItems = listOf(
            "Prepare for an interview",
            "Prepare for an interview",
            "Prepare for an interview"
        )

        // Use a LazyRow to hold the sliding cards
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(16.dp)
        ) {
            items(cardItems.size) { index ->
                val item = cardItems[index]
                MainCard(
                    visible = visible,
                    onCardClick = {
                        visible = !visible
                        LaunchedEffect(visible) {
                            delay(500)
                            visible = !visible
                        }
                    }
                )
            }
        }
    }
}

///**
// * @param modifier
// * @param opacity different for each stacked card
// * Function to create the hidden cards with different opacity
// */
//@Composable
//fun HiddenCard(modifier: Modifier = Modifier, opacity: Int) {
//    Card(
//        shape = RoundedCornerShape(16.dp),
//        backgroundColor = Color(216, 234, 237, opacity), // Light gray background
//        modifier = modifier
//            .size(300.dp)
//            .zIndex(0f)
//            .border(BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(16.dp))
//    ) {}
//}

/**
 * Function to create the main card which will show the current selected mode
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainCard(visible: Boolean, onCardClick: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally() + fadeIn(),
        exit = slideOutHorizontally() + fadeOut()
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color(216, 234, 237, 250),
            modifier = Modifier
                .size(300.dp)
                .zIndex(1f)
                .clickable { onCardClick }
                .border(BorderStroke(1.dp, Color.Black), shape = RoundedCornerShape(16.dp)),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // The context of the mode
                Text(
                    modifier = Modifier.padding(vertical = 32.dp),
                    text = "Prepare for an interview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                // The front card's descriptive image
                Image(
                    painter = painterResource(R.drawable.job_interview),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(180.dp)
                        .padding(vertical = 16.dp)
                )
            }
        }
    }
}

