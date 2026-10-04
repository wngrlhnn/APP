package com.wngrlhnn.beardoff

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {

    private var cameraView: BeardCameraView? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val cameraGranted = result[Manifest.permission.CAMERA] == true
            if (cameraGranted) {
                cameraView?.start(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
        setContent { BeardOffApp() }
    }

    override fun onDestroy() {
        cameraView?.release()
        super.onDestroy()
    }

    @Composable
    private fun BeardOffApp() {
        val context = LocalContext.current
        var enabled by rememberSaveable { mutableStateOf(true) }
        var intensity by rememberSaveable { mutableFloatStateOf(0.85f) }
        var faceDetected by remember { mutableStateOf(false) }
        var captured by remember { mutableStateOf<Bitmap?>(null) }
        var busy by remember { mutableStateOf(false) }
        var saved by remember { mutableStateOf(false) }

        MaterialTheme {
            Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize()) {
                    captured?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "תמונה לאחר הסרת זקן",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } ?: AndroidView(
                        factory = { ctx ->
                            BeardCameraView(ctx).also { view ->
                                cameraView = view
                                view.onFaceDetected = { faceDetected = it }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (captured == null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 22.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (faceDetected) Color(0xFF63E6BE)
                                                else Color(0xFFFFC857)
                                            )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (faceDetected) "פנים זוהו • מוכן"
                                        else "ממקם את הפנים במרכז",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp)
                                .navigationBarsPadding()
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.68f),
                                shape = RoundedCornerShape(28.dp),
                                tonalElevation = 8.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                "הסרת זקן",
                                                color = Color.White,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                if (enabled) "פעיל בזמן צילום" else "כבוי",
                                                color = Color.White.copy(alpha = 0.65f),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Switch(
                                            checked = enabled,
                                            onCheckedChange = { enabled = it }
                                        )
                                    }

                                    AnimatedVisibility(visible = enabled) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("עוצמה", color = Color.White.copy(alpha = 0.8f))
                                                Text(
                                                    "${(intensity * 100).toInt()}%",
                                                    color = Color.White.copy(alpha = 0.8f)
                                                )
                                            }
                                            Slider(
                                                value = intensity,
                                                onValueChange = { intensity = it },
                                                valueRange = 0.25f..1f
                                            )
                                        }
                                    }

                                    Button(
                                        enabled = !busy,
                                        onClick = {
                                            busy = true
                                            cameraView?.takePhoto { file ->
                                                BeardRemover.process(
                                                    context = context,
                                                    inputFile = file,
                                                    intensity = if (enabled) intensity else 0f
                                                ) { result ->
                                                    runOnUiThread {
                                                        busy = false
                                                        result?.let {
                                                            captured = it
                                                            saved = BeardRemover.saveToGallery(context, it)
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        Text(if (busy) "מעבד…" else "צלם תמונה")
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(20.dp)
                                .navigationBarsPadding(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.72f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (saved) "נשמר בגלריה ✓" else "התמונה מוכנה",
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    captured = null
                                    saved = false
                                    cameraView?.start(this@MainActivity)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text("צילום נוסף")
                            }
                        }
                    }
                }
            }
        }
    }
}
