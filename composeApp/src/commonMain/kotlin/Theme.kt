import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object Theme {
    object Icons {
        public val Clipboard: ImageVector
            get() {
                if (clipboard != null) {
                    return clipboard!!
                }
                clipboard =
                    Builder(
                        name = "Clipboard",
                        defaultWidth = 24.0.dp,
                        defaultHeight = 24.0.dp,
                        viewportWidth = 24.0f,
                        viewportHeight = 24.0f,
                    ).apply {
                        path(
                            fill = SolidColor(Color(0x00000000)),
                            stroke = SolidColor(Color(0xFF000000)),
                            strokeLineWidth = 2.0f,
                            strokeLineCap = Round,
                            strokeLineJoin =
                                StrokeJoin.Companion.Round,
                            strokeLineMiter = 4.0f,
                            pathFillType = NonZero,
                        ) {
                            moveTo(16.0f, 4.0f)
                            horizontalLineToRelative(2.0f)
                            arcToRelative(2.0f, 2.0f, 0.0f, false, true, 2.0f, 2.0f)
                            verticalLineToRelative(14.0f)
                            arcToRelative(2.0f, 2.0f, 0.0f, false, true, -2.0f, 2.0f)
                            horizontalLineTo(6.0f)
                            arcToRelative(2.0f, 2.0f, 0.0f, false, true, -2.0f, -2.0f)
                            verticalLineTo(6.0f)
                            arcToRelative(2.0f, 2.0f, 0.0f, false, true, 2.0f, -2.0f)
                            horizontalLineToRelative(2.0f)
                        }
                        path(
                            fill = SolidColor(Color(0x00000000)),
                            stroke = SolidColor(Color(0xFF000000)),
                            strokeLineWidth = 2.0f,
                            strokeLineCap = Round,
                            strokeLineJoin =
                                StrokeJoin.Companion.Round,
                            strokeLineMiter = 4.0f,
                            pathFillType = NonZero,
                        ) {
                            moveTo(9.0f, 2.0f)
                            lineTo(15.0f, 2.0f)
                            arcTo(1.0f, 1.0f, 0.0f, false, true, 16.0f, 3.0f)
                            lineTo(16.0f, 5.0f)
                            arcTo(1.0f, 1.0f, 0.0f, false, true, 15.0f, 6.0f)
                            lineTo(9.0f, 6.0f)
                            arcTo(1.0f, 1.0f, 0.0f, false, true, 8.0f, 5.0f)
                            lineTo(8.0f, 3.0f)
                            arcTo(1.0f, 1.0f, 0.0f, false, true, 9.0f, 2.0f)
                            close()
                        }
                    }
                        .build()
                return clipboard!!
            }

        private var clipboard: ImageVector? = null
        public val UserMinus: ImageVector
            get() {
                if (userMinus != null) {
                    return userMinus!!
                }
                userMinus =
                    Builder(
                        name = "UserMinus",
                        defaultWidth = 24.0.dp,
                        defaultHeight = 24.0.dp,
                        viewportWidth = 24.0f,
                        viewportHeight = 24.0f,
                    ).apply {
                        path(
                            fill = SolidColor(Color(0x00000000)),
                            stroke = SolidColor(Color(0xFF000000)),
                            strokeLineWidth = 2.0f,
                            strokeLineCap = Round,
                            strokeLineJoin =
                                StrokeJoin.Companion.Round,
                            strokeLineMiter = 4.0f,
                            pathFillType = NonZero,
                        ) {
                            moveTo(16.0f, 21.0f)
                            verticalLineToRelative(-2.0f)
                            arcToRelative(4.0f, 4.0f, 0.0f, false, false, -4.0f, -4.0f)
                            horizontalLineTo(5.0f)
                            arcToRelative(4.0f, 4.0f, 0.0f, false, false, -4.0f, 4.0f)
                            verticalLineToRelative(2.0f)
                        }
                        path(
                            fill = SolidColor(Color(0x00000000)),
                            stroke = SolidColor(Color(0xFF000000)),
                            strokeLineWidth = 2.0f,
                            strokeLineCap = Round,
                            strokeLineJoin =
                                StrokeJoin.Companion.Round,
                            strokeLineMiter = 4.0f,
                            pathFillType = NonZero,
                        ) {
                            moveTo(8.5f, 7.0f)
                            moveToRelative(-4.0f, 0.0f)
                            arcToRelative(4.0f, 4.0f, 0.0f, true, true, 8.0f, 0.0f)
                            arcToRelative(4.0f, 4.0f, 0.0f, true, true, -8.0f, 0.0f)
                        }
                        path(
                            fill = SolidColor(Color(0x00000000)),
                            stroke = SolidColor(Color(0xFF000000)),
                            strokeLineWidth = 2.0f,
                            strokeLineCap = Round,
                            strokeLineJoin =
                                StrokeJoin.Companion.Round,
                            strokeLineMiter = 4.0f,
                            pathFillType = NonZero,
                        ) {
                            moveTo(23.0f, 11.0f)
                            lineTo(17.0f, 11.0f)
                        }
                    }
                        .build()
                return userMinus!!
            }

        private var userMinus: ImageVector? = null
        public val Delete: ImageVector
            get() {
                if (delete != null) {
                    return delete!!
                }
                delete =
                    Builder(
                        name = "Delete",
                        defaultWidth = 24.0.dp,
                        defaultHeight = 24.0.dp,
                        viewportWidth = 24.0f,
                        viewportHeight = 24.0f,
                    ).apply {
                        path(
                            fill = SolidColor(Color(0x00000000)),
                            stroke = SolidColor(Color(0xFF000000)),
                            strokeLineWidth = 2.0f,
                            strokeLineCap = Round,
                            strokeLineJoin =
                                StrokeJoin.Companion.Round,
                            strokeLineMiter = 4.0f,
                            pathFillType = NonZero,
                        ) {
                            moveTo(21.0f, 4.0f)
                            horizontalLineTo(8.0f)
                            lineToRelative(-7.0f, 8.0f)
                            lineToRelative(7.0f, 8.0f)
                            horizontalLineToRelative(13.0f)
                            arcToRelative(2.0f, 2.0f, 0.0f, false, false, 2.0f, -2.0f)
                            verticalLineTo(6.0f)
                            arcToRelative(2.0f, 2.0f, 0.0f, false, false, -2.0f, -2.0f)
                            close()
                        }
                        path(
                            fill = SolidColor(Color(0x00000000)),
                            stroke = SolidColor(Color(0xFF000000)),
                            strokeLineWidth = 2.0f,
                            strokeLineCap = Round,
                            strokeLineJoin =
                                StrokeJoin.Companion.Round,
                            strokeLineMiter = 4.0f,
                            pathFillType = NonZero,
                        ) {
                            moveTo(18.0f, 9.0f)
                            lineTo(12.0f, 15.0f)
                        }
                        path(
                            fill = SolidColor(Color(0x00000000)),
                            stroke = SolidColor(Color(0xFF000000)),
                            strokeLineWidth = 2.0f,
                            strokeLineCap = Round,
                            strokeLineJoin =
                                StrokeJoin.Companion.Round,
                            strokeLineMiter = 4.0f,
                            pathFillType = NonZero,
                        ) {
                            moveTo(12.0f, 9.0f)
                            lineTo(18.0f, 15.0f)
                        }
                    }
                        .build()
                return delete!!
            }

        private var delete: ImageVector? = null
    }
}
