import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sasikanth.rss.reader.home.HomeComponent
import dev.sasikanth.rss.reader.home.ui.HomeScreen

@Composable
fun App(component: HomeComponent) {
  MaterialTheme(
    colorScheme = darkColorScheme()
  ) {
    Surface(modifier = Modifier.fillMaxSize()) {
      HomeScreen(component)
    }
  }
}
