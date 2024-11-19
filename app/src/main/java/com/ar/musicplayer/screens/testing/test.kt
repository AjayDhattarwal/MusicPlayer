package com.ar.musicplayer.screens.testing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun RhythmLogoCanvas() {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center){

        val stroke = Stroke(17f)

        Row(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                val r = Path().apply {

                    moveTo(size.width * 0f, size.height * 1f)
                    lineTo(size.width * 0f, size.height * -0.5f)

                    cubicTo(
                        size.width * 0.1f, size.height * -0.3f,
                        size.width * 0.2f, size.height * 0.3f,
                        size.width * 0f, size.height * 0.5f
                    )
                    cubicTo(
                        size.width * 0.0f, size.height * 0.4f,
                        size.width * 0.05f, size.height * 1f,
                        size.width * 0.15f, size.height * 1f
                    )
                }

                drawPath(r, color = Color.White, style = stroke)

                val h = Path().apply {
                    moveTo(size.width * 0.17f, size.height * 0.0f)
                    lineTo(size.width * 0.17f, size.height * 1f)

                    moveTo(size.width * 0.17f, size.height * 0.5f)
                    lineTo(size.width * 0.26f, size.height * 0.5f)

                    moveTo(size.width * 0.26f, size.height * 0.0f)
                    lineTo(size.width * 0.26f, size.height * 1f)

                }

                drawPath(h, color = Color.White, style = stroke)

                val y = Path().apply {
                    moveTo(size.width * 0.3f, size.height * 0f)
                    lineTo(size.width * 0.35f, size.height * 0.5f)

                    moveTo(size.width * 0.4f, size.height * 0f)
                    lineTo(size.width * 0.35f, size.height * 0.5f)

                    moveTo(size.width * 0.35f, size.height * 0.5f)
                    lineTo(size.width * 0.33f, size.height * 1f)

                }

                drawPath(y, color = Color.White, style = stroke)

                val t = Path().apply {
                    moveTo(size.width * 0.44f, size.height * 0f)
                    lineTo(size.width * 0.47f, size.height * -1.5f)

                    moveTo(size.width * 0.44f, size.height * 0f)
                    lineTo(size.width * 0.42f, size.height * 1.5f)

                    moveTo(size.width * 0.42f, size.height * 0f)
                    lineTo(size.width * 0.48f, size.height * -0.05f)

                    moveTo(size.width * 0.47f, size.height * -1.5f)

                    quadraticBezierTo(
                        size.width * 0.44f, size.height * -1.2f,
                        size.width * 0.32f, size.height * -1.1f
                    )

                    moveTo(size.width * 0.34f, size.height * -1.12f)

                    lineTo(size.width * 0.33f, size.height * -0.5f)

                    cubicTo(
                        size.width * 0.65f, size.height * -1.2f,
                        size.width * 0.45f, size.height * 0.3f,
                        size.width * 0.445f, size.height * -0.3f
                    )

                }

                drawPath(t, color = Color.White, style = stroke)

                val longH = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.0f)
                    lineTo(size.width * 0.47f, size.height * 1.4f)

                    moveTo(size.width * 0.49f, size.height * 0.5f)
                    lineTo(size.width * 0.58f, size.height * 0.5f)

                    moveTo(size.width * 0.59f, size.height * 0.0f)
                    lineTo(size.width * 0.56f, size.height * 1.2f)

                }

                drawPath(longH, color = Color.White, style = stroke)

                val m = Path().apply {
                    moveTo(size.width * 0.63f, size.height * 0f)
                    lineTo(size.width * 0.61f, size.height * 1f)

                    moveTo(size.width * 0.63f, size.height * 0f)
                    lineTo(size.width * 0.68f, size.height * 0.5f)

                    lineTo(size.width * 0.74f, size.height * -0f)

                    moveTo(size.width * 0.74f, size.height * 0f)
                    lineTo(size.width * 0.72f, size.height * 1f)

                }

                drawPath(m, color = Color.White, style = stroke)

                val o = Path().apply {
                    addOval(Rect(
                        left = size.width * 0.79f,
                        top = size.height * 0.2f,
                        right = size.width * 0.92f,
                        bottom = size.height * 0.9f
                    ))
                }
                drawPath(o, color = Color.White, style = stroke)

            }




        }
    }
}

@Preview
@Composable
fun RhythmApp() {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        RhythmLogoCanvasAnim()
    }
}


@Composable
fun RhythmLogoCanvasAnim() {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {

        val stroke = Stroke(17f)

        val tProgress = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            tProgress.animateTo(1f, animationSpec = tween(4000, easing = LinearOutSlowInEasing))
        }

        Row(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {


                val r = Path().apply {
                    moveTo(size.width * 0f, size.height * 1f)
                    lineTo(size.width * 0f , size.height * -0.5f )

                    cubicTo(
                        size.width * 0.1f, size.height * -0.3f ,
                        size.width * 0.2f , size.height * 0.3f ,
                        size.width * 0f , size.height * 0.5f
                    )
                    cubicTo(
                        size.width * 0.0f , size.height * 0.4f,
                        size.width * 0.05f, size.height * 1f ,
                        size.width * 0.15f , size.height * 1f
                    )
                }

                val h = Path().apply {
                    moveTo(size.width * 0.17f, size.height * 0.0f)
                    lineTo(size.width * 0.17f , size.height * 1f )

                    moveTo(size.width * 0.17f , size.height * 0.5f )
                    lineTo(size.width * 0.26f , size.height * 0.5f )

                    moveTo(size.width * 0.26f , size.height * 0.0f)
                    lineTo(size.width * 0.26f , size.height * 1f )
                }

                val y = Path().apply {
                    moveTo(size.width * 0.3f , size.height * 0f)
                    lineTo(size.width * 0.35f , size.height * 0.5f )

                    moveTo(size.width * 0.4f , size.height * 0f)
                    lineTo(size.width * 0.35f , size.height * 0.5f )

                    moveTo(size.width * 0.35f , size.height * 0.5f)
                    lineTo(size.width * 0.33f , size.height * 1f )
                }

                val t = Path().apply {
                    moveTo(size.width * 0.44f  , size.height * 0f)
                    lineTo(size.width * 0.49f , size.height * -2.4f * tProgress.value )

                    moveTo(size.width * 0.44f, size.height * 0f)
                    lineTo(size.width * 0.42f, size.height * 2f * tProgress.value)

                    moveTo(size.width * 0.42f, size.height * 0f * tProgress.value)
                    lineTo(size.width * 0.48f, size.height * -0.05f * tProgress.value )

                    moveTo(size.width * 0.49f, size.height * -2.4f * tProgress.value)

                    quadraticBezierTo(
                        size.width * 0.47f, size.height * -1.9f * tProgress.value,
                        size.width * 0.28f, size.height * -1.8f * tProgress.value
                    )
                    moveTo(size.width * 0.49f, size.height * -2.4f * tProgress.value)
                    quadraticBezierTo(
                        size.width * 0.47f, size.height * -2f * tProgress.value,
                        size.width * 0.28f, size.height * -1.8f * tProgress.value
                    )

                    moveTo(size.width * 0.3f, size.height * -1.8f * tProgress.value)

                    lineTo(size.width * 0.28f, size.height * -1f * tProgress.value)

                    cubicTo(
                        size.width * 0.69f, size.height * -1.2f * tProgress.value,
                        size.width * 0.49f, size.height * 0.3f * tProgress.value,
                        size.width * 0.445f, size.height * -0.3f * tProgress.value
                    )
                }

                val longH = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.0f)
                    lineTo(size.width * 0.47f, size.height * 1.4f )

                    moveTo(size.width * 0.49f, size.height * 0.5f)
                    lineTo(size.width * 0.58f, size.height * 0.5f )

                    moveTo(size.width * 0.59f, size.height * 0.0f)
                    lineTo(size.width * 0.56f, size.height * 1.2f )
                }

                val m = Path().apply {
                    moveTo(size.width * 0.63f , size.height * 0f )
                    lineTo(size.width * 0.61f , size.height * 1f)

                    moveTo(size.width * 0.63f, size.height * 0f)
                    lineTo(size.width * 0.68f, size.height * 0.5f )

                    lineTo(size.width * 0.74f, size.height * 0f)

                    moveTo(size.width * 0.74f, size.height * 0f)
                    lineTo(size.width * 0.72f, size.height * 1f)
                }

                val o = Path().apply {
                    addOval(
                        Rect(
                            left = size.width * 0.79f ,
                            top = size.height * 0.2f,
                            right = size.width * 0.92f ,
                            bottom = size.height * 0.9f
                        )
                    )
                }

                drawPath(r, color = Color.White, style = stroke)

                drawPath(h, color = Color.White, style = stroke)

                drawPath(y, color = Color.White, style = stroke)

                drawPath(t, color = Color.White, style = stroke)

                drawPath(longH, color = Color.White, style = stroke)

                drawPath(m, color = Color.White, style = stroke)

                drawPath(o, color = Color.White, style = stroke)
            }
        }
    }
}
