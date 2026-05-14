package com.android.messaging.ui.conversation.composer.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val CaptureStopIcon: ImageVector
    get() {
        val cachedIcon = cachedCaptureStopIcon
        if (cachedIcon != null) {
            return cachedIcon
        }

        val icon = ImageVector.Builder(
            name = "capture_stop",
            defaultWidth = 61.5.dp,
            defaultHeight = 61.5.dp,
            viewportWidth = 246f,
            viewportHeight = 246f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 12f,
            ) {
                moveTo(123f, 17.5f)
                curveTo(181.266f, 17.5f, 228.5f, 64.734f, 228.5f, 123f)
                curveTo(228.5f, 181.266f, 181.266f, 228.5f, 123f, 228.5f)
                curveTo(64.734f, 228.5f, 17.5f, 181.266f, 17.5f, 123f)
                curveTo(17.5f, 64.734f, 64.734f, 17.5f, 123f, 17.5f)
                close()
            }

            path(
                fill = SolidColor(Color.Black),
            ) {
                moveTo(94f, 90f)
                horizontalLineTo(152f)
                curveTo(154.209f, 90f, 156f, 91.791f, 156f, 94f)
                verticalLineTo(152f)
                curveTo(156f, 154.209f, 154.209f, 156f, 152f, 156f)
                horizontalLineTo(94f)
                curveTo(91.791f, 156f, 90f, 154.209f, 90f, 152f)
                verticalLineTo(94f)
                curveTo(90f, 91.791f, 91.791f, 90f, 94f, 90f)
                close()
            }
        }.build()

        cachedCaptureStopIcon = icon
        return icon
    }

private var cachedCaptureStopIcon: ImageVector? = null
