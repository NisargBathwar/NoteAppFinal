package com.example.noteappfinal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = ThemePreference(application)

    private val _isDark = MutableStateFlow(false)
    val isDark : StateFlow<Boolean> = _isDark

    init {
        viewModelScope.launch {
            prefs.isDark.collect { value ->
                _isDark.value = value
            }
        }
    }


    fun toggle(){
        val newValue = !_isDark.value
        _isDark.value = newValue
        viewModelScope.launch {
            prefs.setDarkTheme(newValue)
        }
    }

}