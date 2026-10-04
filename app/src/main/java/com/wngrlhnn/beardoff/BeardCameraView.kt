package com.wngrlhnn.beardoff

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BeardCameraView(context: Context) : FrameLayout(context) {

    private val previewView = PreviewView(context).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
        implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private val executor: ExecutorService = Executors.newFixedThreadPool(2)

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setMinFaceSize(0.15f)
            .build()
    )

    private var imageCapture: ImageCapture? = null
    private var started = false

    var onFaceDetected: ((Boolean) -> Unit)? = null

    init {
        addView(previewView)
    }

    fun start(owner: LifecycleOwner) {
        if (started) return
        started = true

        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            analysis.setAnalyzer(executor) { proxy ->
                val mediaImage = proxy.image
                if (mediaImage == null) {
                    proxy.close()
                    return@setAnalyzer
                }

                val image = InputImage.fromMediaImage(
                    mediaImage,
                    proxy.imageInfo.rotationDegrees
                )

                detector.process(image)
                    .addOnSuccessListener { faces ->
                        onFaceDetected?.invoke(faces.isNotEmpty())
                    }
                    .addOnFailureListener {
                        onFaceDetected?.invoke(false)
                    }
                    .addOnCompleteListener {
                        proxy.close()
                    }
            }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    owner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageCapture,
                    analysis
                )
            } catch (_: Exception) {
                started = false
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePhoto(onFile: (File) -> Unit) {
        val capture = imageCapture ?: return
        val file = File(context.cacheDir, "beardoff_${System.currentTimeMillis()}.jpg")

        capture.takePicture(
            ImageCapture.OutputFileOptions.Builder(file).build(),
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    post { onFile(file) }
                }

                override fun onError(exception: ImageCaptureException) {
                    // UI remains usable if the camera capture fails.
                }
            }
        )
    }

    fun release() {
        started = false
        detector.close()
        executor.shutdown()
    }
}
