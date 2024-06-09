package shrtl.`in`.ui.screen

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import shrtl.`in`.core.Navigator
import shrtl.`in`.util.shader.BLACK_CHERRY_COSMOS_2_PLUS_EFFECT
import shrtl.`in`.util.shader.shaderBackground

@Composable
fun BoxScope.ErrorScreen(message: String) {
    Column(
        modifier =
            Modifier.align(Alignment.Center)
                .shaderBackground(BLACK_CHERRY_COSMOS_2_PLUS_EFFECT, 0.05f),
    ) {
        Text("Error: $message")
        Button(onClick = {
            Navigator.main()
        }) {
            Text("Back")
        }
    }
}
