import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sasikanth.rss.reader.home.HomeComponent
import dev.sasikanth.rss.reader.home.ui.HomeScreen
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun App(component: HomeComponent) {
  AppTheme {
    Surface(modifier = Modifier.fillMaxSize()) {
      HomeScreen(component)
    }
  }
}
