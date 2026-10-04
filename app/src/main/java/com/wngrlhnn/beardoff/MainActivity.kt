package com.wngrlhnn.beardoff

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permission.launch(Manifest.permission.CAMERA)
        setContent { BeardOffScreen() }
    }
}

@Composable
fun BeardOffScreen() {
    val context = LocalContext.current
    var cleanMode by remember { mutableStateOf(true) }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { view ->
                    val providerFuture = ProcessCameraProvider.getInstance(ctx)
                    providerFuture.addListener({
                        val provider = providerFuture.get()
                        val preview = Preview.Builder().build()
                        preview.surfaceProvider = view.surfaceProvider
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            context as ComponentActivity,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview
                        )
                    }, Executors.newSingleThreadExecutor())
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Column(
            Modifier.align(Alignment.BottomCenter).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (cleanMode) "BeardOff" else "מצב רגיל",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { cleanMode = !cleanMode }) {
                Text(if (cleanMode) "הסר זקן" else "החזר זקן")
            }
        }
    }
}
