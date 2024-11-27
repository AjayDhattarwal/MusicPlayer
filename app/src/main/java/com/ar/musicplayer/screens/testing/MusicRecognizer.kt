package com.ar.musicplayer.screens.testing

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.RuntimeShader
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.ar.musicplayer.components.home.HomeScreenRowCard
import com.ar.musicplayer.data.models.SongResponse
import com.ar.musicplayer.data.models.TrackRecognition
import com.ar.musicplayer.utils.AudioRecorder.AndroidAudioRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import java.io.File
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


@Composable
fun MusicRecognizer(playSong: (SongResponse) -> Unit,togglePlaying: () -> Unit, backHandler: @Composable () -> Unit) {

    val context = LocalContext.current

    val viewModel: MusicRecognizerViewModel = viewModel()

    val pair by viewModel.trackResponse.collectAsState()
    val track by remember { derivedStateOf { pair?.first } }
    val relatedTracks by remember { derivedStateOf { pair?.second } }


    if(track != null){
        BackHandler {
            viewModel.clearResult()
        }
    }else{
        backHandler()
    }
    val scrollState = rememberScrollState()

    var isPlayed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        if (track == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){

                RotatingBall(context){
                    viewModel.recognizeSong(it)
                }
            }
        }else{
            Column(
                modifier = Modifier
                    .drawBehind {
                        drawRect(
                            color = Color.Black,
                        )
                    }
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ) {
                EnhancedTrackDetailsUI(
                    track = track,
                    togglePlaying = togglePlaying
                ){
                    if(!isPlayed){
                        if(it.title != null){
                            playSong(SongResponse(title = it.title, subtitle = it.subtitle, isYoutube = true))
                            isPlayed = true
                        }
                    }
                }
                if(track?.relatedTracksUrl != null) {
                    Text(
                        text = "Related",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Spacer(Modifier.width(1.dp))
                    }

                    itemsIndexed(relatedTracks?.tracks ?: emptyList(), key = { index, item ->  item.key ?: index }){ index , track ->

                        HomeScreenRowCard(
                            item = track,
                            isRadio = false,
                            subtitle = track.subtitle ?: "",
                            cornerRadius = 0,
                            imageUrl = track.images?.coverart ?: track.images?.coverarthq ?: track.images?.background,
                            title = track.title ?: "",
                            size = 170,
                            onClick = { _, _ ->

                            }
                        )
                    }
                }
                Spacer(Modifier.height(120.dp))
            }

        }

    }
}

@Composable
fun EnhancedTrackDetailsUI(track: TrackRecognition?, togglePlaying: () -> Unit,  onPlayPause: (TrackRecognition) -> Unit = {}) {
    val images = track?.images
    var isPlaying by remember { mutableStateOf(false) }

    Column(modifier = Modifier) {
        Box {

            Image(
                painter = rememberAsyncImagePainter(images?.background ?: images?.coverarthq ?: images?.coverart),
                contentDescription = "Background Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = 0.4f
                    }
                    .background(Color.Black)
                    .height(300.dp),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .offset {
                        IntOffset(0, 180)
                    },
                verticalAlignment = Alignment.CenterVertically
            ){

                TrackImageWithPlayPause(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    isPlaying = { isPlaying },
                    onPlayPauseClick = {
                        track?.let { onPlayPause(it) }
                        togglePlaying()
                        isPlaying = !isPlaying
                    } ,
                    imageUrl = images?.coverart ?: images?.coverarthq ?: images?.background
                )

                Column(
                    Modifier.offset {
                        IntOffset(0, 120)
                    }
                ) {
                    Text(
                        text = track?.title ?: "Unknown Title",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track?.subtitle ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    Text(
                        text = "Genre: ${track?.genres?.primary ?: "Unknown"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.LightGray,
                        maxLines = 1,
                        modifier = Modifier
                    )
                }
            }

            // Main Cover Art overlapping the background
        }

        Spacer(modifier = Modifier.height(70.dp))


        track?.sections?.forEach { section ->
            if(section.tabname != "Related"){
                Text(
                    text = if(section.tabname == "Song") "Details" else section.tabname ?: "Section",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                section.metadata?.forEach { metadata ->
                    Text(
                        text = "${metadata.title}: ${metadata.text}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        modifier = Modifier.padding(start = 36.dp, top = 4.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
fun TrackImageWithPlayPause(
    modifier: Modifier = Modifier,
    isPlaying: ()  -> Boolean,
    onPlayPauseClick: () -> Unit,
    imageUrl: String?
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onPlayPauseClick() }
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = "Cover Art",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Icon(
            imageVector = if (isPlaying()) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = "PlayPause",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .padding(8.dp)
        )
    }
}

@Language("AGSL")
val RECOGNIZER_SHADER = """
    uniform float iTime;
    uniform vec2 iResolution; 

    uniform float iAmplitude;
    
    float orenNayarDiffuse_3_0(
      vec3 lightDirection,
      vec3 viewDirection,
      vec3 surfaceNormal,
      float roughness,
      float albedo) {
      
      float LdotV = dot(lightDirection, viewDirection);
      float NdotL = dot(lightDirection, surfaceNormal);
      float NdotV = dot(surfaceNormal, viewDirection);

      float s = LdotV - NdotL * NdotV;
      float t = mix(1.0, max(NdotL, NdotV), step(0.0, s));

      float sigma2 = roughness * roughness;
      float A = 1.0 + sigma2 * (albedo / (sigma2 + 0.13) + 0.5 / (sigma2 + 0.33));
      float B = 0.45 * sigma2 / (sigma2 + 0.09);

      return albedo * max(0.0, NdotL) * (A + B * s / t) / 3.14159265;
    }


    vec3 mod289_1_1(vec3 x) {
      return x - floor(x * (1.0 / 289.0)) * 289.0;
    }

    vec4 mod289_1_1(vec4 x) {
      return x - floor(x * (1.0 / 289.0)) * 289.0;
    }

    vec4 permute_1_2(vec4 x) {
         return mod289_1_1(((x*34.0)+1.0)*x);
    }

    vec4 taylorInvSqrt_1_3(vec4 r)
    {
      return 1.79284291400159 - 0.85373472095314 * r;
    }

    float snoise_1_4(vec3 v)
      {
      const vec2  C = vec2(1.0/6.0, 1.0/3.0) ;
      const vec4  D_1_5 = vec4(0.0, 0.5, 1.0, 2.0);

    // First corner
      vec3 i  = floor(v + dot(v, C.yyy) );
      vec3 x0 =   v - i + dot(i, C.xxx) ;

    // Other corners
      vec3 g_1_6 = step(x0.yzx, x0.xyz);
      vec3 l = 1.0 - g_1_6;
      vec3 i1 = min( g_1_6.xyz, l.zxy );
      vec3 i2 = max( g_1_6.xyz, l.zxy );
      vec3 x1 = x0 - i1 + C.xxx;
      vec3 x2 = x0 - i2 + C.yyy; 
      vec3 x3 = x0 - D_1_5.yyy;    

    // Permutations
      i = mod289_1_1(i);
      vec4 p = permute_1_2( permute_1_2( permute_1_2(
                 i.z + vec4(0.0, i1.z, i2.z, 1.0 ))
               + i.y + vec4(0.0, i1.y, i2.y, 1.0 ))
               + i.x + vec4(0.0, i1.x, i2.x, 1.0 ));


      float n_ = 0.142857142857; 
      vec3  ns = n_ * D_1_5.wyz - D_1_5.xzx;

      vec4 j = p - 49.0 * floor(p * ns.z * ns.z); 

      vec4 x_ = floor(j * ns.z);
      vec4 y_ = floor(j - 7.0 * x_ );    

      vec4 x = x_ *ns.x + ns.yyyy;
      vec4 y = y_ *ns.x + ns.yyyy;
      vec4 h = 1.0 - abs(x) - abs(y);

      vec4 b0 = vec4( x.xy, y.xy );
      vec4 b1 = vec4( x.zw, y.zw );


      vec4 s0 = floor(b0)*2.0 + 1.0;
      vec4 s1 = floor(b1)*2.0 + 1.0;
      vec4 sh = -step(h, vec4(0.0));

      vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy ;
      vec4 a1_1_7 = b1.xzyw + s1.xzyw*sh.zzww ;

      vec3 p0_1_8 = vec3(a0.xy,h.x);
      vec3 p1 = vec3(a0.zw,h.y);
      vec3 p2 = vec3(a1_1_7.xy,h.z);
      vec3 p3 = vec3(a1_1_7.zw,h.w);

    //Normalise gradients
      vec4 norm = taylorInvSqrt_1_3(vec4(dot(p0_1_8,p0_1_8), dot(p1,p1), dot(p2, p2), dot(p3,p3)));
      p0_1_8 *= norm.x;
      p1 *= norm.y;
      p2 *= norm.z;
      p3 *= norm.w;

    // Mix final noise value
      vec4 m = max(0.6 - vec4(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.0);
      m = m * m;
      return 42.0 * dot( m*m, vec4( dot(p0_1_8,x0), dot(p1,x1),
                                    dot(p2,x2), dot(p3,x3) ) );
      }


    vec3 mod289_2_9(vec3 x) {
      return x - floor(x * (1.0 / 289.0)) * 289.0;
    }

    vec2 mod289_2_9(vec2 x) {
      return x - floor(x * (1.0 / 289.0)) * 289.0;
    }

    vec3 permute_2_10(vec3 x) {
      return mod289_2_9(((x*34.0)+1.0)*x);
    }

    float snoise_2_11(vec2 v)
      {
      const vec4 C = vec4(0.211324865405187, 
                          0.366025403784439,  
                         -0.577350269189626,  
                          0.024390243902439); 

      vec2 i  = floor(v + dot(v, C.yy) );
      vec2 x0 = v -   i + dot(i, C.xx);


      vec2 i1;
      
      i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
      
      vec4 x12 = x0.xyxy + C.xxzz;
      x12.xy -= i1;

    // Permutations
      i = mod289_2_9(i); 
      vec3 p = permute_2_10( permute_2_10( i.y + vec3(0.0, i1.y, 1.0 ))
        + i.x + vec3(0.0, i1.x, 1.0 ));

      vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
      m = m*m ;
      m = m*m ;



      vec3 x = 2.0 * fract(p * C.www) - 1.0;
      vec3 h = abs(x) - 0.5;
      vec3 ox = floor(x + 0.5);
      vec3 a0 = x - ox;

      
      m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );

    // Compute final noise value at P
      vec3 g;
      g.x  = a0.x  * x0.x  + h.x  * x0.y;
      g.yz = a0.yz * x12.xz + h.yz * x12.yw;
      return 130.0 * dot(m, g);
    }



    float smin_4_12(float a, float b, float k) {
      float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
      return mix(b, a, h) - k * h * (1.0 - h);
    }



    float tri( float x ){
      return abs( fract(x) - .5 );
    }

    vec3 tri3( vec3 p ){

      return vec3(
          tri( p.z + tri( p.y * 1. ) ),
          tri( p.z + tri( p.x * 1. ) ),
          tri( p.y + tri( p.x * 1. ) )
      );

    }


    float triNoise3D( vec3 p, float spd , float time){

      float z  = 1.4;
    	float rz =  0.;
      vec3  bp =   p;

    	for( float i = 0.; i <= 3.; i++ ){

        vec3 dg = tri3( bp * 2. );
        p += ( dg + time * .1 * spd );

        bp *= 1.8;
    		z  *= 1.5;
    		p  *= 1.2;

        float t = tri( p.z + tri( p.x + tri( p.y )));
        rz += t / z;
        bp += 0.14;

    	}

    	return rz;

    }



    mat3 calcLookAtMatrix( in vec3 ro, in vec3 ta, in float roll ) {
      float s = sin(0.0);
      float c = cos(0.0);
      vec3 ww = normalize( ta - ro );
      vec3 uu = normalize( cross(ww,vec3(s,c,0.0) ) );
      vec3 vv = normalize( cross(uu,ww));
      return mat3( uu, vv, ww );
    }

    vec3 doBackground(vec2 p) {
      vec3 col = vec3(0.0);

      col += snoise_1_4(vec3(p * 0.1, iTime)) * vec3(0.3, 0.1, 0.05);
      col += snoise_1_4(vec3(iTime, p * 0.4)) * vec3(0.1, 0.2, 0.4);
      return clamp(col, vec3(0.0), vec3(1.0));
    }

    float sdCone(vec3 p, vec2 c) {
      float q = length(p.xy);
      return dot(normalize(c),vec2(q,p.z));
    }

    float sdBox(vec3 p, vec3 b) {
      vec3 d = abs(p) - b;
      return min(max(d.x,max(d.y,d.z)),0.8) + length(max(d,0.0));
    }

    float sdCylinder(vec3 p, vec3 c) {
      return length(p.xz-c.xy)-c.z;
    }

    vec2 doModel(vec3 p) {
      float r = 0.5 + pow(triNoise3D(p * 0.5 + iTime * vec3(0, 0.1, 0), 1.5, iTime) * 0.1, 1.) ;
      float d = length(p) - r * iAmplitude  ; //amptitude

      return vec2(d, 0.0);
    }

    vec3 calcIntersection(vec3 ro, vec3 rd) {
      const float maxd = 20.0;
      const float prec = 0.001;
      float h = prec * 2.0;
      float t = +0.0;
      float r = -1.0;
      float k = -1.0;
      float g = 0.0;

      for (int i = 0; i < 90; i++) {
        if (h < prec || t > maxd) break;
        vec2 m = doModel(ro + rd * t);
        h = m.x;
        k = m.y;
        t += h;
        g += 0.025;
      }

      g = pow(g, 2.0);

      if (t < maxd) r = t;

      return vec3(r, k, g);
    }

    vec3 calcNormal(vec3 pos) {
      const float eps = 0.002;

      const vec3 v1 = vec3( 1.0,-1.0,-1.0);
      const vec3 v2 = vec3(-1.0,-1.0, 1.0);
      const vec3 v3 = vec3(-1.0, 1.0,-1.0);
      const vec3 v4 = vec3( 1.0, 1.0, 1.0);

    	return normalize(
        v1*doModel( pos + v1*eps ).x +
        v2*doModel( pos + v2*eps ).x +
        v3*doModel( pos + v3*eps ).x +
        v4*doModel( pos + v4*eps ).x
     	);
    }

    vec3 doLighting(vec3 pos, vec3 nor, vec3 rd) {
      vec3 lig = vec3(0.0);
      
      vec3  dir1 = normalize(vec3(0.3, 0.9, 0.1));
      vec3  col1 = vec3(0.3, 0.5, 1.0);
      float dif1 = orenNayarDiffuse_3_0(dir1, normalize(-rd), nor, 0.5, 1.9);
      
      vec3  dir2 = normalize(vec3(0, -1, 0.5));
      vec3  col2 = vec3(0.4, 0.0, 0.2);
      float dif2 = orenNayarDiffuse_3_0(dir2, normalize(-rd), nor, 0.5, 1.9);
      
      lig += dif1 * col1;
      lig += dif2 * col2;
      lig += vec3(0.005, 0.03, 0.01);
      
      return lig;
    }

    // AGSL Shader Main Function
    half4 main(float2 fragCoord) {
        half4 o = half4(0.0);  
        
        
        float2 p = (-iResolution.xy + 2.0 * fragCoord.xy) / iResolution.y;

        // Background color
        half3 color = doBackground(p);

        // Camera settings and movement (based on time)
        float an = 0.0; // You can replace this with a time-based value like 9 * iTime
        half3 camPos = half3(3.5 * sin(an), 1.0, 3.5 * cos(an));  // Camera position
        half3 camTar = half3(0.0, 0.0, 0.0);  // Camera target
        mat3 camMat = calcLookAtMatrix(camPos, camTar, 0.0);  // Camera matrix

      
        half3 ro = camPos ;  
        half3 rd = normalize(camMat * half3(p.xy, 2.0));  

        // Intersection calculation
        half3 t = calcIntersection(ro, rd);
        if (t.x > -0.5) {
            half3 pos = ro + rd * t.x;  
            half3 nor = calcNormal(pos);  

            color = mix(doLighting(pos, nor, rd), color, 0.0);
        }

      
        color += t.z * mix(half3(0.1, 0.8, 1.5), half3(1.5, 0, 0), 1.0 - (p.y + 0.7)) 
                 * pow(triNoise3D(half3(p, iTime), 2.5, iTime) * 3.0 + 0.9, 0.3);

        
        color.g = smoothstep(-0.2, 1.1, color.g);  
        color.r = smoothstep(0.1, 0.9, color.r);  

       
        o.rgb = color;
        o.a   = 1.0;

        return o;
    }

""".trimIndent()




@Composable
fun RotatingBall(
    context: Context,
    onRecorded: (String) -> Unit = {}
) {
    var waveAmplitude by remember { mutableStateOf(0f) }
    val audioScope = rememberCoroutineScope()
    val recorder by lazy {
        AndroidAudioRecorder(context.applicationContext)
    }


    LaunchedEffect(Unit) {
        val file = File(context.cacheDir, "audio.mp3").also {
            recorder.start(it)
        }
        delay(10000)
        recorder.stop()
        onRecorded(file.absolutePath)
    }



    DisposableEffect(Unit) {
        val job = audioScope.launch(Dispatchers.IO) {
            val audioRecord = setupAudioRecord(context)
            if (audioRecord != null) {
                val bufferSize = AudioRecord.getMinBufferSize(
                    44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val buffer = ShortArray(bufferSize)

                audioRecord.startRecording()
                val startTime = System.currentTimeMillis()

                try {
                    while (isActive && (System.currentTimeMillis() - startTime) < 10000) { // Check for 10 seconds
                        val read = audioRecord.read(buffer, 0, buffer.size)
                        if (read > 0) {
                            waveAmplitude = (calculateAmplitude(buffer) / 300).coerceIn(0f, 70f)
                        }
                    }
                    waveAmplitude = 0f

                } finally {
                    waveAmplitude = 0f
                    audioRecord.stop()
                    audioRecord.release()
                }
            }
        }

        onDispose {
            job.cancel()
        }
    }




    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        RotatingBallsBox(waveAmplitude = { waveAmplitude })
    }
    else{
        Box(Modifier.size(300.dp)){
            RotatingBallsCanvas(waveAmplitude = { waveAmplitude })
        }
    }

}


@Composable
fun RotatingBallsCanvas(waveAmplitude: () -> Float, modifier: Modifier = Modifier) {
    val numBalls = 300
    val baseRadius = 250f
    val viewingDistance = 400f
    val waveFrequency = 2f
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Canvas(modifier = modifier) {

        val centerX = size.width / 2f
        val centerY = size.height / 2f

        val goldenAngle = Math.PI * (3.0 - Math.sqrt(5.0)) // ~137.5 degrees
        val random = Random(System.currentTimeMillis())

        for (i in 0 until numBalls) {
            if (random.nextBoolean()) {
                val theta = i * goldenAngle
                val phi = Math.acos(1 - 2 * (i + 0.5) / numBalls)

                val distanceFromCenter = baseRadius
                val waveOffset = waveAmplitude() * sin(
                    (distanceFromCenter / baseRadius) * waveFrequency
                )
                val dynamicRadius = baseRadius + waveOffset

                // 3D position on the sphere
                val x = dynamicRadius * sin(phi) * cos(theta)
                val y = dynamicRadius * sin(phi) * sin(theta)
                val z = dynamicRadius * cos(phi)

                // Apply rotation
                val rotatedX =
                    x * cos(rotation.value.toRadians()) - z * sin(rotation.value.toRadians())
                val rotatedZ =
                    x * sin(rotation.value.toRadians()) + z * cos(rotation.value.toRadians())
                val ballRadius = (0.5f * waveAmplitude()).coerceIn(4f, 7f)

                // Project 3D position to 2D
                val screenX =
                    (rotatedX * viewingDistance / (rotatedZ + viewingDistance)) + centerX
                val screenY = (y * viewingDistance / (rotatedZ + viewingDistance)) + centerY

                val color = Color(
                    red = (0.5f + 0.5f * (rotatedZ / baseRadius)).toFloat(),
                    green = 0.2f + (waveOffset / waveAmplitude() * 0.3f).coerceIn(0f, 0.3f),
                    blue = 0.7f + 0.3f * (rotatedZ / baseRadius).toFloat(),
                    alpha = 1f
                )

                // Draw the ball
                drawCircle(
                    center = Offset(screenX.toFloat(), screenY.toFloat()),
                    radius = ballRadius,
                    color = color,
                    style = Fill
                )
            }
        }


    }
}


@SuppressLint("NewApi")
@Composable
fun RotatingBallsBox(waveAmplitude: () -> Float){
    var iTime by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            iTime += 0.1f
            delay(100L)
        }
    }

    Box(modifier = Modifier.fillMaxSize().drawWithCache {
        // Create the shader with AGSL code
        val shader = RuntimeShader(RECOGNIZER_SHADER)

        // Set shader uniforms
        shader.setFloatUniform("iTime", iTime)
        shader.setFloatUniform("iResolution", size.width, size.height)
        shader.setFloatUniform("iAmplitude", ((waveAmplitude() / 100f) + 1f).coerceIn(1f, 2f))

        val shaderBrush = ShaderBrush(shader)

        onDrawBehind {
            drawRect(
                brush = shaderBrush,
                size = size
            )
        }
    })
}

private fun Float.toRadians() = (this * Math.PI / 180).toFloat()




fun setupAudioRecord(context: Context): AudioRecord? {
    val sampleRate = 44100
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
    if (ActivityCompat.checkSelfPermission(
           context,
           Manifest.permission.RECORD_AUDIO
       ) != PackageManager.PERMISSION_GRANTED
   ) {
       return null
   }
   return AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize
    )
}


fun calculateAmplitude(buffer: ShortArray): Float {
    var sum = 0f
    for (sample in buffer) {
        sum += Math.abs(sample.toFloat())
    }
    return sum / buffer.size
}



