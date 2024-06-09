package shrtl.`in`.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import shrtl.`in`.ui.components.Logo
import shrtl.`in`.util.shader.ICE_EFFECT
import shrtl.`in`.util.shader.shaderBackground

@Composable
fun SplashScreen() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .shaderBackground(ICE_EFFECT, 0.1f)
                .padding(16.dp),
    ) {
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Logo()
        }
    }
}
