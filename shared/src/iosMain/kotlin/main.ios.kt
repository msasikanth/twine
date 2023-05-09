import androidx.compose.ui.window.ComposeUIViewController
import dev.sasikanth.rss.reader.home.HomeComponent

fun MainViewController(component: HomeComponent) = ComposeUIViewController { App(component) }
