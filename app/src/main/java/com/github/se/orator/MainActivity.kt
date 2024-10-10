package com.github.se.orator

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.ui.authentification.SignInScreen
import com.github.se.orator.ui.network.createChatGPTService
import com.github.se.orator.ui.overview.ChatScreen
import com.github.se.orator.ui.overview.FeedbackScreen
import com.github.se.orator.ui.overview.SpeakingSecond
import com.github.se.orator.ui.overview.SpeakingStart
import com.github.se.orator.ui.theme.ProjectTheme
import com.github.se.orator.viewModel.ChatViewModel
import com.github.se.orator.viewModel.ChatViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth
  private lateinit var chatViewModel: ChatViewModel

  @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize Firebase Auth
    auth = FirebaseAuth.getInstance()
    auth.currentUser?.let {
      // Sign out the user if they are already signed in
      // This is useful for testing purposes
      auth.signOut()
    }



    val apiKey = "sk-1biCkCzmuVEldxLy8fUBG9UtxUCvXM2Sp90lhbaUJzT3BlbkFJg8gOeJefosH5MJrRjMI30LSAvM8x9H6bXGWwjIlBsA"
    val organizationId = "org-btfcw6ORfA1BTBtUq4lHB8cO"

    val chatGPTService = createChatGPTService(apiKey, organizationId)

    val factory = ChatViewModelFactory(chatGPTService)
    chatViewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)

    val interviewContext = InterviewContext(
      interviewType = "job interview",
      role = "Consultant",
      company = "McKinsey",
      focusAreas = listOf("Problem-solving", "Leadership", "Teamwork")
    )

    chatViewModel.initializeConversation(interviewContext)

    enableEdgeToEdge()
    setContent { ProjectTheme { Scaffold(modifier = Modifier.fillMaxSize()) { ChatScreen(viewModel = chatViewModel) } } }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  ProjectTheme { Greeting("Android") }
}
