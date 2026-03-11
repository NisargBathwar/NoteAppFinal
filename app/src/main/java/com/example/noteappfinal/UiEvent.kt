package com.example.noteappfinal

sealed class UiEvent {
    data class ShowSnackbar(val message: String, val action : String? = null) : UiEvent()
    data class Navigation(val value : String) : UiEvent()
    data class Toast(val message : String) : UiEvent()
    object NavigationBack : UiEvent()
}