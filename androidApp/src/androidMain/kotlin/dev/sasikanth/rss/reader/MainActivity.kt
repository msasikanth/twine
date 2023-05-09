package dev.sasikanth.rss.reader

import MainView
import android.graphics.Color
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.arkivanov.decompose.defaultComponentContext
import dev.sasikanth.rss.reader.database.DriverFactory
import dev.sasikanth.rss.reader.database.createDatabase
import dev.sasikanth.rss.reader.home.HomeComponent
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.utils.DefaultDispatchersProvider

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT

    val dispatchersProvider = DefaultDispatchersProvider()
    val database = createDatabase(DriverFactory(this))
    val component = HomeComponent(
      componentContext = defaultComponentContext(),
      rssRepository = RssRepository(
        feedQueries = database.feedQueries,
        postQueries = database.postQueries,
        ioDispatcher = dispatchersProvider.io
      ),
      dispatchersProvider = dispatchersProvider
    )

    setContent {
      MainView(component = component)
    }
  }
}
