package rs.ac.bg.matf.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import android.graphics.Color
import rs.ac.bg.matf.BuildConfig
import rs.ac.bg.matf.R
import rs.ac.bg.matf.attendance.history.AttendanceHistoryActivity
import rs.ac.bg.matf.attendance.registration.AttendanceRegistrationActivity
import rs.ac.bg.matf.ui.login.LoginActivity
import rs.ac.bg.matf.auth.AuthRepository
import rs.ac.bg.matf.auth.HttpAuthApi
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_BEARER_TOKEN = "extra_bearer_token"
        const val EXTRA_USERNAME = "extra_username"
        const val MENU_LOGOUT = 1
        const val MENU_ATTENDANCE_HISTORY = 2
    }

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var toolbar: Toolbar
    private lateinit var scanAttendanceButton: ImageButton
    private lateinit var bearerToken: String
    private lateinit var username: String
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bearerToken = intent.getStringExtra(EXTRA_BEARER_TOKEN).orEmpty()
        username = intent.getStringExtra(EXTRA_USERNAME).orEmpty()
        if (bearerToken.isBlank()) {
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_main)
        authRepository = AuthRepository(
            context = this,
            api = HttpAuthApi(BuildConfig.BACKEND_BASE_URL),
        )

        bindViews()
        setupToolbar()
        wireUi()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.toolbar)
        scanAttendanceButton = findViewById(R.id.scanAttendanceButton)
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.app_title)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.text_primary))
        toolbar.navigationIcon = null
        toolbar.overflowIcon = AppCompatResources.getDrawable(
            this,
            androidx.appcompat.R.drawable.abc_ic_menu_overflow_material,
        )?.mutate()?.also {
            DrawableCompat.setTint(it, Color.WHITE)
        }
        toolbar.menu.add(0, MENU_LOGOUT, 0, getString(R.string.logout_button)).apply {
            setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_NEVER)
        }
        toolbar.menu.add(0, MENU_ATTENDANCE_HISTORY, 1, getString(R.string.attendance_history_menu)).apply {
            setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_NEVER)
        }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_LOGOUT -> {
                    logout()
                    true
                }
                MENU_ATTENDANCE_HISTORY -> {
                    startActivity(
                        Intent(this, AttendanceHistoryActivity::class.java).apply {
                            putExtra(AttendanceHistoryActivity.EXTRA_BEARER_TOKEN, bearerToken)
                        }
                    )
                    true
                }
                else -> false
            }
        }
    }

    private fun wireUi() {
        scanAttendanceButton.setOnClickListener {
            startActivity(
                Intent(this, AttendanceRegistrationActivity::class.java).apply {
                    putExtra(AttendanceRegistrationActivity.EXTRA_BEARER_TOKEN, bearerToken)
                }
            )
        }
    }

    private fun logout() {
        executor.execute {
            authRepository.logout()
            runOnUiThread {
                redirectToLogin()
            }
        }
    }

    private fun redirectToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                putExtra(LoginActivity.EXTRA_USERNAME, username)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finish()
    }
}
