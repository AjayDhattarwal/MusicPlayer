package com.ar.musicplayer.components.modifier

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import org.intellij.lang.annotations.Language


@Language("AGSL")
val BACKGROUND_SHADER = """
    
    uniform float2 resolution;
    layout(color) uniform half4 color1;
    layout(color) uniform half4 color2;
    layout(color) uniform half4 color3;

    half4 main(in float2 fragCoord) {
        float2 uv = fragCoord / resolution.xy;

        // Define the color stops for a diagonal gradient
        float gradientFactor = uv.x + uv.y;
        gradientFactor = clamp(gradientFactor, 0.0, 1.0);
        
        // Create gradient color stops
        half4 colorStart = mix(color1, color2, gradientFactor);
        half4 colorEnd = mix(color2, color3, gradientFactor);

        // Final color
        half4 finalColor = mix(colorStart, colorEnd, gradientFactor);

        return finalColor;
    }
""".trimIndent()



@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun Modifier.shader(
    colors: List<Color>,
): Modifier = this.then(
    Modifier.drawWithCache {
        val shader = RuntimeShader(BACKGROUND_SHADER)
        val shaderBrush = ShaderBrush(shader)

        // Set the resolution of the shader
        shader.setFloatUniform("resolution", size.width, size.height)

        onDrawBehind {

            colors.forEachIndexed { index, color ->
                val colorName = "color${index + 1}"  // Generate uniform name, e.g., color1, color2, etc.
                shader.setColorUniform(
                    colorName,
                    android.graphics.Color.valueOf(
                        color.red, color.green, color.blue, color.alpha
                    )
                )
            }

            drawRect(shaderBrush)
        }
    }
)
