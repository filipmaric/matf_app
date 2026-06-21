package com.example.matfapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.view.inputmethod.EditorInfo
import com.example.matfapp.BuildConfig
import com.example.matfapp.R
import com.example.matfapp.auth.AuthRepository
import com.example.matfapp.auth.AuthState
import com.example.matfapp.auth.AuthViewModel
import com.example.matfapp.auth.AuthViewModelFactory
import com.example.matfapp.auth.HttpAuthApi
import com.example.matfapp.ui.home.MainActivity

class LoginActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_USERNAME = "extra_username"
    }

    private lateinit var viewModel: AuthViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private var sessionRestoreRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val repository = AuthRepository(
            context = this,
            api = HttpAuthApi(BuildConfig.BACKEND_BASE_URL),
        )
        viewModel = ViewModelProvider(this, AuthViewModelFactory(this, repository))[AuthViewModel::class.java]

        bindViews()
        usernameInput.setText(intent.getStringExtra(EXTRA_USERNAME).orEmpty())
        wireUi()
        observeState()
    }

    override fun onStart() {
        super.onStart()
        if (!sessionRestoreRequested) {
            sessionRestoreRequested = true
            viewModel.restoreSession()
        }
    }

    private fun bindViews() {
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
    }

    private fun wireUi() {
        usernameInput.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                passwordInput.requestFocus()
                true
            } else {
                false
            }
        })
        passwordInput.setOnEditorActionListener(OnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                loginButton.performClick()
                true
            } else {
                false
            }
        })
        loginButton.setOnClickListener {
            viewModel.login(
                username = usernameInput.text?.toString().orEmpty().trim(),
                password = passwordInput.text?.toString().orEmpty(),
                deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim()
            )
        }
    }

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            render(state)
        }
    }

    private fun render(state: AuthState) {
        val loading = state is AuthState.Loading
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        loginButton.isEnabled = !loading

        when (state) {
            is AuthState.Loading -> {
                errorText.visibility = View.GONE
            }
            is AuthState.LoggedOut -> {
                errorText.visibility = View.GONE
            }
            is AuthState.LoggedIn -> {
                launchMainActivity(state.session.token)
            }
            is AuthState.Error -> {
                errorText.visibility = View.VISIBLE
                errorText.text = state.message
            }
        }
    }

    private fun launchMainActivity(token: String) {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_BEARER_TOKEN, token)
                putExtra(MainActivity.EXTRA_USERNAME, usernameInput.text?.toString().orEmpty().trim())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finish()
    }
}
