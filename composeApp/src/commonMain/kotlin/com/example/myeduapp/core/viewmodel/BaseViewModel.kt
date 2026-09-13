package com.example.myeduapp.core.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.CoroutineScope

/**
 * Voyager [ScreenModel]-backed ViewModel for Android + iOS.
 * Use [screenModelScope] for coroutines; obtain via [cafe.adriel.voyager.core.model.rememberScreenModel].
 */
abstract class BaseViewModel : ScreenModel {
    protected val viewModelScope: CoroutineScope
        get() = screenModelScope
}
