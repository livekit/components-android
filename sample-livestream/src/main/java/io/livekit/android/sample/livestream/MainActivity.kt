package io.livekit.android.sample.livestream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.plusAssign
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import io.livekit.android.sample.livestream.ui.screen.NavGraphs
import io.livekit.android.sample.livestream.ui.theme.AppTheme

@OptIn(ExperimentalAnimationApi::class)
val defaultAnimations = RootNavGraphDefaultAnimations(
    enterTransition = { fadeIn(animationSpec = tween(300)) },
    exitTransition = { fadeOut(animationSpec = tween(300)) }
)

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface {
                    val navController = rememberAnimatedNavController()
                    val bottomSheetNavigator = rememberBottomSheetNavigator()

                    navController.navigatorProvider += bottomSheetNavigator
                    ModalBottomSheetLayout(
                        bottomSheetNavigator = bottomSheetNavigator,
                        // other configuration for you bottom sheet screens, like:
                        sheetShape = RoundedCornerShape(16.dp),
                        sheetBackgroundColor = MaterialTheme.colorScheme.background
                    ) {
                        DestinationsNavHost(
                            navGraph = NavGraphs.root,
                            navController = navController,
                            engine = rememberAnimatedNavHostEngine(
                                rootDefaultAnimations = defaultAnimations,
                            )
                        )
                    }
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