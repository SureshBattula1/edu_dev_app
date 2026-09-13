package com.example.myeduapp.features.auth

import com.example.myeduapp.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * App-level bootstrap ViewModel (lives outside Voyager [Screen]).
 * Uses its own scope because [cafe.adriel.voyager.core.model.rememberScreenModel]
 * requires a Voyager Screen host.
 */
class AppViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun checkAuth() {
        scope.launch { authRepository.checkAuth() }
    }

    fun clear() {
        scope.cancel()
    }
}
