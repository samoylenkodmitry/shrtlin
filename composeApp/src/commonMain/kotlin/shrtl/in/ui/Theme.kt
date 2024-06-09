package shrtl.`in`.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object Theme {
    object Icons {
        val Logo: ImageVector
            get() {
                if (logo != null) {
                    return logo!!
                }
                logo =
                    Builder(
                        name = "Logo",
                        defaultWidth = 110.0.dp,
                        defaultHeight = 100.0.dp,
                        viewportWidth = 110.0f,
                        viewportHeight = 100.0f,
                    ).apply {
                        path(
                            fill = SolidColor(Color(0x00000000)),
                            stroke = SolidColor(Color(0xFF007BFF)),
                            strokeLineWidth = 6.0f,
                            strokeLineCap = Butt,
                            strokeLineJoin = Miter,
                            strokeLineMiter = 4.0f,
                            pathFillType = NonZero,
                        ) {
                            moveTo(40.0f, 30.0f)
                            horizontalLineToRelative(20.0f)
                            arcToRelative(10.0f, 10.0f, 0.0f, false, true, 0.0f, 20.0f)
                            horizontalLineToRelative(-10.0f)
                            arcToRelative(10.0f, 10.0f, 0.0f, false, false, 0.0f, 20.0f)
                            horizontalLineToRelative(20.0f)
                        }
                    }
                        .build()
                return logo!!
            }

        private var logo: ImageVector? = null

        // MIT https://github.com/DevSrSouza/compose-icons
        val Clipboard: ImageVector
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

        // MIT https://github.com/DevSrSouza/compose-icons
        val UserMinus: ImageVector
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

        // MIT https://github.com/DevSrSouza/compose-icons
        val Delete: ImageVector
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

        // MIT https://github.com/DevSrSouza/compose-icons
        val User: ImageVector
            get() {
                if (user != null) {
                    return user!!
                }
                user =
                    Builder(
                        name = "User",
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
                            moveTo(20.0f, 21.0f)
                            verticalLineToRelative(-2.0f)
                            arcToRelative(4.0f, 4.0f, 0.0f, false, false, -4.0f, -4.0f)
                            horizontalLineTo(8.0f)
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
                            moveTo(12.0f, 7.0f)
                            moveToRelative(-4.0f, 0.0f)
                            arcToRelative(4.0f, 4.0f, 0.0f, true, true, 8.0f, 0.0f)
                            arcToRelative(4.0f, 4.0f, 0.0f, true, true, -8.0f, 0.0f)
                        }
                    }
                        .build()
                return user!!
            }

        private var user: ImageVector? = null
        val Trash2: ImageVector
            get() {
                if (trash2 != null) {
                    return trash2!!
                }
                trash2 =
                    Builder(
                        name = "Trash2",
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
                            moveTo(3.0f, 6.0f)
                            lineToRelative(2.0f, 0.0f)
                            lineToRelative(16.0f, 0.0f)
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
                            moveTo(19.0f, 6.0f)
                            verticalLineToRelative(14.0f)
                            arcToRelative(2.0f, 2.0f, 0.0f, false, true, -2.0f, 2.0f)
                            horizontalLineTo(7.0f)
                            arcToRelative(2.0f, 2.0f, 0.0f, false, true, -2.0f, -2.0f)
                            verticalLineTo(6.0f)
                            moveToRelative(3.0f, 0.0f)
                            verticalLineTo(4.0f)
                            arcToRelative(2.0f, 2.0f, 0.0f, false, true, 2.0f, -2.0f)
                            horizontalLineToRelative(4.0f)
                            arcToRelative(2.0f, 2.0f, 0.0f, false, true, 2.0f, 2.0f)
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
                            moveTo(10.0f, 11.0f)
                            lineTo(10.0f, 17.0f)
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
                            moveTo(14.0f, 11.0f)
                            lineTo(14.0f, 17.0f)
                        }
                    }
                        .build()
                return trash2!!
            }

        private var trash2: ImageVector? = null
    }
}
