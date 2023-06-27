package io.livekit.android.sample.livestream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import io.livekit.android.sample.livestream.room.data.LivestreamApi
import io.livekit.android.sample.livestream.ui.theme.AppTheme
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@OptIn(ExperimentalAnimationApi::class)
val defaultAnimations = RootNavGraphDefaultAnimations(
    enterTransition = { fadeIn(animationSpec = tween(300)) },
    exitTransition = { fadeOut(animationSpec = tween(300)) }
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val livestreamApi = Retrofit.Builder()
            .baseUrl(DebugServerInfo.API_SERVER_URL)
            .client(OkHttpClient())
            .build()
            .create(LivestreamApi::class.java)
        setContent {
            AppTheme {
                Surface {
                    val navController = rememberAnimatedNavController()
                    val navHostEngine = rememberAnimatedNavHostEngine(
                        rootDefaultAnimations = defaultAnimations,
                    )
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        navController = navController,
                        engine = navHostEngine,
                        dependenciesContainerBuilder = {
                            dependency(livestreamApi)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTheme {
        Greeting("Android")
    }
}