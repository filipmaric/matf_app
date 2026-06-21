package rs.ac.bg.matf.attendance.registration

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import rs.ac.bg.matf.R
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService

class AttendanceRegistrationQrController(
    private val activity: AttendanceRegistrationActivity,
    private val previewView: PreviewView,
    private val cameraExecutor: ExecutorService,
) {
    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 4021
    }

    private val barcodeScanner = BarcodeScanning.getClient()
    private var cameraProvider: ProcessCameraProvider? = null
    private var running = false

    fun start(onOutcome: (AttendanceRegistrationQrOutcome) -> Unit) {
        if (!hasCameraPermission()) {
            onOutcome(AttendanceRegistrationQrOutcome.PermissionRequired)
            requestCameraPermission()
            return
        }
        if (running) return
        running = true
        onOutcome(AttendanceRegistrationQrOutcome.ScanningStarted)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider
            bindCamera(provider, onOutcome)
        }, ContextCompat.getMainExecutor(activity))
    }

    fun stop() {
        running = false
        cameraProvider?.unbindAll()
    }

    fun close() {
        stop()
        barcodeScanner.close()
    }

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE,
        )
    }

    fun handlePermissionResult(requestCode: Int, grantResults: IntArray): AttendanceRegistrationQrPermissionOutcome {
        if (requestCode != CAMERA_PERMISSION_REQUEST_CODE) return AttendanceRegistrationQrPermissionOutcome.NotHandled
        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            return AttendanceRegistrationQrPermissionOutcome.Granted
        } else {
            return AttendanceRegistrationQrPermissionOutcome.Denied
        }
    }

    private fun bindCamera(
        provider: ProcessCameraProvider,
        onOutcome: (AttendanceRegistrationQrOutcome) -> Unit,
    ) {
        if (!running) return
        provider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                imageProxy.close()
                return@setAnalyzer
            }

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(image)
                .addOnSuccessListener(ContextCompat.getMainExecutor(activity)) { barcodes ->
                    val rawValue = barcodes.firstOrNull()?.rawValue?.trim().orEmpty()
                    if (rawValue.isNotBlank() && running) {
                        val target = parseAttendanceTarget(rawValue)
                        if (target != null) {
                            stop()
                            onOutcome(AttendanceRegistrationQrOutcome.TargetDetected(target))
                        } else {
                            onOutcome(AttendanceRegistrationQrOutcome.InvalidCode(activity.getString(R.string.attendance_scan_invalid)))
                        }
                    }
                }
                .addOnFailureListener(ContextCompat.getMainExecutor(activity)) {
                    onOutcome(AttendanceRegistrationQrOutcome.InvalidCode(it.message ?: activity.getString(R.string.attendance_scan_invalid)))
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }

        provider.bindToLifecycle(activity, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
    }

    private fun parseAttendanceTarget(rawValue: String): AttendanceTarget? {
        return try {
            val uri = Uri.parse(rawValue)
            val segments = uri.pathSegments
            val attendanceIndex = segments.indexOf("attendance")
            if (attendanceIndex < 0 || segments.size < attendanceIndex + 6) return null

            val kind = segments[attendanceIndex + 1]
            val eventId = segments[attendanceIndex + 2].toLongOrNull() ?: return null
            val eventDate = segments[attendanceIndex + 3]
            val joinMarker = segments[attendanceIndex + 4]
            val joinToken = segments[attendanceIndex + 5]
            if (kind.isBlank() || eventDate.isBlank() || joinMarker != "join" || joinToken.isBlank()) return null

            AttendanceTarget(kind = kind, eventId = eventId, eventDate = eventDate, joinToken = joinToken)
        } catch (_: Exception) {
            null
        }
    }
}
