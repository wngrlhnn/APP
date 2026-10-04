package com.wngrlhnn.jeepexplorer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun JeepIllustration(body: Long, accent: Long, pickup: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val c = Color(body)
        val a = Color(accent)
        val roofY = h * 0.35f
        val hoodY = h * 0.46f
        val wheelY = h * 0.73f

        drawRoundRect(
            color = c,
            topLeft = Offset(w * 0.08f, hoodY),
            size = Size(w * 0.84f, h * 0.24f),
            cornerRadius = CornerRadius(22f, 22f)
        )

        val cabin = Path().apply {
            moveTo(w * 0.30f, hoodY)
            lineTo(w * 0.38f, roofY)
            lineTo(w * 0.66f, roofY)
            lineTo(w * 0.76f, hoodY)
            close()
        }
        drawPath(cabin, c)

        drawPath(
            Path().apply {
                moveTo(w * 0.39f, roofY + 6)
                lineTo(w * 0.50f, roofY + 6)
                lineTo(w * 0.50f, hoodY - 6)
                lineTo(w * 0.36f, hoodY - 6)
                close()
            },
            Color(0xFF9FB8C8)
        )

        drawPath(
            Path().apply {
                moveTo(w * 0.54f, roofY + 6)
                lineTo(w * 0.65f, roofY + 6)
                lineTo(w * 0.73f, hoodY - 6)
                lineTo(w * 0.54f, hoodY - 6)
                close()
            },
            Color(0xFF7894A5)
        )

        if (pickup) {
            drawRect(
                color = c,
                topLeft = Offset(w * 0.10f, h * 0.52f),
                size = Size(w * 0.24f, h * 0.12f)
            )
        }

        drawRoundRect(
            color = Color(0xFF151719),
            topLeft = Offset(w * 0.70f, h * 0.51f),
            size = Size(w * 0.15f, h * 0.08f),
            cornerRadius = CornerRadius(10f, 10f)
        )

        listOf(w * 0.24f, w * 0.75f).forEach { x ->
            drawCircle(Color(0xFF101112), h * 0.085f, Offset(x, wheelY))
            drawCircle(a, h * 0.035f, Offset(x, wheelY))
            drawCircle(Color(0xFF202326), h * 0.017f, Offset(x, wheelY))
        }

        repeat(7) { i ->
            val x = w * 0.54f + i * w * 0.025f
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(x, h * 0.49f),
                end = Offset(x, h * 0.58f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
