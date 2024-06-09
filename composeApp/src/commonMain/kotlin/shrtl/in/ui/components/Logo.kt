package shrtl.`in`.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import shrtl.`in`.ui.Theme

@Composable
fun Logo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = rememberVectorPainter(Theme.Icons.Logo),
            contentDescription = "Logo",
            modifier = Modifier,
        )
        Text(
            text = "hrtlin",
            style = TextStyle(fontSize = 46.sp),
            color = Color(0xFF333355),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            modifier =
                Modifier.graphicsLayer {
                    translationX = -45f
                },
        )
    }
}
