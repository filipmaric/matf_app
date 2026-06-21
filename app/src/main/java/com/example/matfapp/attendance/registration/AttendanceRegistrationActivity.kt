package rs.ac.bg.matf.attendance.registration

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import rs.ac.bg.matf.BuildConfig
import rs.ac.bg.matf.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AttendanceRegistrationActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_BEARER_TOKEN = "extra_bearer_token"
    }

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val networkExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val uiHandler = Handler(Looper.getMainLooper())
    private val locationCancellationSource = CancellationTokenSource()

    private lateinit var bearerToken: String
    private lateinit var previewView: PreviewView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var messageText: TextView
    private lateinit var eventPanel: LinearLayout
    private lateinit var eventTitle: TextView
    private lateinit var eventDetails: TextView
    private lateinit var challengeChoicesContainer: LinearLayout
    private lateinit var closeButton: Button
    private lateinit var ui: AttendanceRegistrationUi

    private lateinit var stateManager: AttendanceRegistrationStateManager
    private lateinit var qrController: AttendanceRegistrationQrController
    private lateinit var geofencingController: AttendanceRegistrationGeofencingController
    private lateinit var pollingController: AttendanceRegistrationPollingController
    private lateinit var challengeController: AttendanceRegistrationChallengeController
    private lateinit var coordinator: AttendanceRegistrationCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_scan)

        bearerToken = intent.getStringExtra(EXTRA_BEARER_TOKEN).orEmpty()
        if (bearerToken.isBlank()) {
            finish()
            return
        }

        bindViews()
        stateManager = AttendanceRegistrationStateManager()
        val api = AttendanceApi(BuildConfig.BACKEND_BASE_URL)
        val geofenceFormatter = AttendanceRegistrationGeofenceFormatter(this)
        val errorFormatter = AttendanceRegistrationErrorFormatter(this)
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        pollingController = AttendanceRegistrationPollingController(uiHandler)
        geofencingController = AttendanceRegistrationGeofencingController(
            activity = this,
            stateManager = stateManager,
            fusedLocationClient = fusedLocationClient,
            locationCancellationSource = locationCancellationSource,
            formatter = geofenceFormatter,
        )
        challengeController = AttendanceRegistrationChallengeController(
            activity = this,
            bearerToken = bearerToken,
            stateManager = stateManager,
            api = api,
            networkExecutor = networkExecutor,
            pollingController = pollingController,
            errorFormatter = errorFormatter,
        )
        qrController = AttendanceRegistrationQrController(
            activity = this,
            previewView = previewView,
            cameraExecutor = cameraExecutor,
        )
        coordinator = AttendanceRegistrationCoordinator(
            activity = this,
            stateManager = stateManager,
            qrController = qrController,
            geofencingController = geofencingController,
            challengeController = challengeController,
            renderView = { renderCurrentState() },
        )

        setupBackHandling()
        wireUi()
        if (!coordinator.restore(savedInstanceState)) {
            coordinator.start()
        }
        renderCurrentState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        stateManager.saveInstanceState(outState)
    }

    override fun onDestroy() {
        coordinator.onDestroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (coordinator.onQrPermissionResult(requestCode, grantResults)) return
        if (coordinator.onGeofencePermissionResult(requestCode, grantResults)) return
    }

    private fun bindViews() {
        previewView = findViewById(R.id.previewView)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        messageText = findViewById(R.id.messageText)
        eventPanel = findViewById(R.id.eventPanel)
        eventTitle = findViewById(R.id.eventTitle)
        eventDetails = findViewById(R.id.eventDetails)
        challengeChoicesContainer = findViewById(R.id.choicesContainer)
        closeButton = findViewById(R.id.closeButton)
        ui = AttendanceRegistrationUi(
            context = this,
            previewView = previewView,
            progressBar = progressBar,
            statusText = statusText,
            messageText = messageText,
            eventPanel = eventPanel,
            eventTitle = eventTitle,
            eventDetails = eventDetails,
            challengeChoicesContainer = challengeChoicesContainer,
            closeButton = closeButton,
        )
    }

    private fun setupBackHandling() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
        )
    }

    private fun wireUi() {
        closeButton.setOnClickListener {
            finish()
        }
    }

    private fun renderCurrentState() {
        ui.render(stateManager.state) { selectedCode ->
            coordinator.onSelectedCode(selectedCode)
        }
    }
}
