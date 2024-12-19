import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EmptyScreen() {
  Box(
      modifier = Modifier.fillMaxSize().background(Color.White), // Change color if needed
      contentAlignment = Alignment.Center) {
        Text("Hello, this is a trial ")
      }
}

@Preview(showBackground = true)
@Composable
fun EmptyScreenPreview() {
  EmptyScreen()
}
