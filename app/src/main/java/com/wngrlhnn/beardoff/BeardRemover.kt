package com.wngrlhnn.beardoff

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object BeardRemover {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setMinFaceSize(0.15f)
            .build()
    )

    fun process(
        context: Context,
        inputFile: File,
        intensity: Float,
        onResult: (Bitmap?) -> Unit
    ) {
        val source = loadCorrectedBitmap(inputFile)
        if (source == null) {
            onResult(null)
            return
        }

        if (intensity <= 0f) {
            onResult(source)
            return
        }

        detector.process(InputImage.fromBitmap(source, 0))
            .addOnSuccessListener { faces ->
                val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                val result = if (face == null) {
                    source
                } else {
                    removeLowerFaceHair(source, face, intensity.coerceIn(0f, 1f))
                }
                onResult(result)
                if (result !== source) source.recycle()
            }
            .addOnFailureListener {
                onResult(source)
            }
    }

    private fun removeLowerFaceHair(bitmap: Bitmap, face: Face, intensity: Float): Bitmap {
        val out = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val width = out.width
        val height = out.height
        val rect = face.boundingBox

        val left = rect.left.coerceIn(0, width - 1)
        val right = rect.right.coerceIn(left + 1, width)
        val top = rect.top.coerceIn(0, height - 1)
        val bottom = rect.bottom.coerceIn(top + 1, height)

        val faceW = (right - left).toFloat()
        val faceH = (bottom - top).toFloat()
        val centerX = (left + right) * 0.5f

        val mouthLandmark = face.getLandmark(FaceLandmark.MOUTH_BOTTOM)?.position
        val mouthY = mouthLandmark?.y ?: (top + faceH * 0.67f)

        val regionTop = max(top.toFloat(), mouthY - faceH * 0.18f)
        val regionBottom = min(bottom.toFloat(), top + faceH * 0.98f)

        val skin = sampleSkin(bitmap, left, right, top, bottom)
        val skinLum = luminance(skin)

        val scaledW = max(1, width / 16)
        val scaledH = max(1, height / 16)
        val tiny = Bitmap.createScaledBitmap(bitmap, scaledW, scaledH, true)

        val pixels = IntArray(width * height)
        val tinyPixels = IntArray(tiny.width * tiny.height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        tiny.getPixels(tinyPixels, 0, tiny.width, 0, 0, tiny.width, tiny.height)

        for (y in regionTop.toInt() until regionBottom.toInt().coerceAtMost(height)) {
            val vertical = ((y - regionTop) / max(1f, regionBottom - regionTop)).coerceIn(0f, 1f)
            for (x in left until right.coerceAtMost(width)) {
                val dx = (x - centerX) / (faceW * 0.47f)
                val dy = (y - regionTop) / max(1f, regionBottom - regionTop)
                val ellipse = dx * dx + (dy * 1.05f) * (dy * 1.05f)
                if (ellipse > 1f) continue

                // Keep the mouth/lips intact.
                val mouthDx = (x - centerX) / (faceW * 0.22f)
                val mouthDy = (y - mouthY) / (faceH * 0.055f)
                if (mouthDx * mouthDx + mouthDy * mouthDy < 1f) continue

                val mask = ((1f - ellipse) / 0.35f).coerceIn(0f, 1f)
                val index = y * width + x
                val original = pixels[index]

                val tx = (x.toFloat() / width * tiny.width).toInt().coerceIn(0, tiny.width - 1)
                val ty = (y.toFloat() / height * tiny.height).toInt().coerceIn(0, tiny.height - 1)
                val blurred = tinyPixels[ty * tiny.width + tx]

                val currentLum = luminance(original)
                val darkRatio = ((skinLum - currentLum) / max(1f, skinLum)).coerceIn(0f, 1f)

                val lift = 1f + darkRatio * 0.35f
                val tinted = adjustColorForLuminance(skin, skinLum, currentLum * lift)
                val smooth = mixColor(tinted, blurred, 0.22f)
                val strength = intensity * mask * (0.62f + darkRatio * 0.30f) * (0.92f + vertical * 0.08f)

                pixels[index] = mixColor(original, smooth, strength.coerceIn(0f, 0.94f))
            }
        }

        out.setPixels(pixels, 0, width, 0, 0, width, height)
        tiny.recycle()
        return out
    }

    private fun sampleSkin(bitmap: Bitmap, left: Int, right: Int, top: Int, bottom: Int): Int {
        val faceW = right - left
        val faceH = bottom - top
        val y0 = (top + faceH * 0.46f).toInt()
        val y1 = (top + faceH * 0.67f).toInt()
        val x0 = (left + faceW * 0.17f).toInt()
        val x1 = (left + faceW * 0.37f).toInt()
        val x2 = (left + faceW * 0.63f).toInt()
        val x3 = (left + faceW * 0.83f).toInt()

        var r = 0L
        var g = 0L
        var b = 0L
        var count = 0

        fun addSample(x: Int, y: Int) {
            val c = bitmap.getPixel(
                x.coerceIn(0, bitmap.width - 1),
                y.coerceIn(0, bitmap.height - 1)
            )
            val rr = Color.red(c)
            val gg = Color.green(c)
            val bb = Color.blue(c)
            val lum = (0.2126f * rr + 0.7152f * gg + 0.0722f * bb)
            if (lum > 35f) {
                r += rr
                g += gg
                b += bb
                count++
            }
        }

        for (y in y0 until y1.coerceAtMost(bitmap.height)) {
            for (x in x0 until x1.coerceAtMost(bitmap.width)) addSample(x, y)
            for (x in x2 until x3.coerceAtMost(bitmap.width)) addSample(x, y)
        }

        if (count == 0) return Color.rgb(190, 160, 135)
        return Color.rgb((r / count).toInt(), (g / count).toInt(), (b / count).toInt())
    }

    private fun adjustColorForLuminance(color: Int, baseLum: Float, targetLum: Float): Int {
        val scale = (targetLum / max(1f, baseLum)).coerceIn(0.65f, 1.45f)
        return Color.rgb(
            (Color.red(color) * scale).toInt().coerceIn(0, 255),
            (Color.green(color) * scale).toInt().coerceIn(0, 255),
            (Color.blue(color) * scale).toInt().coerceIn(0, 255)
        )
    }

    private fun mixColor(a: Int, b: Int, amount: Float): Int {
        val t = amount.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(a) + (Color.red(b) - Color.red(a)) * t).toInt().coerceIn(0, 255),
            (Color.green(a) + (Color.green(b) - Color.green(a)) * t).toInt().coerceIn(0, 255),
            (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t).toInt().coerceIn(0, 255)
        )
    }

    private fun luminance(color: Int): Float =
        0.2126f * Color.red(color) +
            0.7152f * Color.green(color) +
            0.0722f * Color.blue(color)

    private fun loadCorrectedBitmap(file: File): Bitmap? {
        val source = BitmapFactory.decodeFile(file.absolutePath) ?: return null
        val rotation = try {
            ExifInterface(file.absolutePath).rotationDegrees
        } catch (_: Exception) {
            0
        }

        if (rotation == 0) return source

        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        val rotated = Bitmap.createBitmap(
            source, 0, 0, source.width, source.height, matrix, true
        )
        if (rotated !== source) source.recycle()
        return rotated
    }

    fun saveToGallery(context: Context, bitmap: Bitmap): Boolean {
        return try {
            val name = "BeardOff_${System.currentTimeMillis()}.jpg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/BeardOff"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri: Uri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                ) ?: return false

                resolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                }

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                true
            } else {
                if (
                    ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) return false

                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "BeardOff"
                )
                if (!dir.exists()) dir.mkdirs()

                val file = File(dir, name)
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }

                android.media.MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("image/jpeg"),
                    null
                )
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
