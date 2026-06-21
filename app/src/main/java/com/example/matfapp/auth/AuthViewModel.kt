package rs.ac.bg.matf.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AuthViewModel(
    private val repository: AuthRepository,
    private val messageProvider: AuthMessageProvider,
) : ViewModel() {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val _state = MutableLiveData<AuthState>(AuthState.Loading)
    val state: LiveData<AuthState> = _state

    fun restoreSession() {
        _state.value = AuthState.Loading
        executor.execute {
            try {
                val session = repository.restoreSession()
                _state.postValue(
                    if (session != null) AuthState.LoggedIn(session) else AuthState.LoggedOut
                )
            } catch (error: Exception) {
                _state.postValue(AuthState.Error(messageProvider.messageFor(error)))
            }
        }
    }

    fun login(username: String, password: String, deviceName: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error(messageProvider.messageFor(IllegalArgumentException()))
            return
        }
        _state.value = AuthState.Loading
        executor.execute {
            try {
                val session = repository.login(username, password, deviceName)
                _state.postValue(AuthState.LoggedIn(session))
            } catch (error: Exception) {
                _state.postValue(AuthState.Error(messageProvider.messageFor(error)))
            }
        }
    }

    fun logout() {
        _state.value = AuthState.Loading
        executor.execute {
            repository.logout()
            _state.postValue(AuthState.LoggedOut)
        }
    }

    override fun onCleared() {
        executor.shutdownNow()
        super.onCleared()
    }
}
