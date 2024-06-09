package shrtl.`in`.ui.components

import UrlStats
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import shrtl.`in`.util.timestampToDate

@Composable
fun SimpleLineChart(urlStats: UrlStats) {
    val maxClicks = urlStats.clickCounts.maxOrNull() ?: 0
    val yScale = if (maxClicks > 0) 200f / maxClicks else 1f

    val textMeasurer = rememberTextMeasurer()
    val justText = true
    if (justText) {
        LazyColumn {
            urlStats.clicks.forEach { (date, clicks) ->
                // header
                item {
                    Row {
                        Text(
                            text = "Date",
                            modifier = Modifier.padding(start = 10.dp),
                        )
                        Text(
                            text = "Clicks",
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                }

                item {
                    Row {
                        Text(
                            text = timestampToDate(date.toLong()),
                            modifier = Modifier.padding(start = 10.dp),
                        )
                        Text(
                            text = clicks.toString(),
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                }
            }
        }
        return
    }
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = canvasWidth / urlStats.dates.size

        urlStats.clickCounts.forEachIndexed { index, clickCount ->
            val barHeight = clickCount * yScale
            val left = index * barWidth
            drawRect(
                color = Color.Blue,
                topLeft = Offset(left, canvasHeight - barHeight),
                size = Size(barWidth, barHeight),
            )
        }

        urlStats.dates.forEachIndexed { index, date ->
            val x = index * barWidth + barWidth / 2
            drawText(
                textMeasurer = textMeasurer,
                text = date,
                topLeft = Offset(x, canvasHeight),
            )
        }
    }
}
