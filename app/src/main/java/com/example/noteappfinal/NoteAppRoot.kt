package com.example.noteappfinal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.noteappfinal.ui.theme.NoteAppFinalTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NoteAppRoot(vm: NoteViewModel = viewModel()) {

    // route state used to drive AnimatedContent screens (no NavHost)
    val routeState = remember { mutableStateOf("list") }
    var currentRoute by remember { routeState } // alias for readability

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Collect ViewModel UI events (navigation / snackbar / toast)
    LaunchedEffect(Unit) {
        vm.event.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            event.message,
                            event.action
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            vm.onEvent(NoteEvent.UndoDelete)
                        }
                    }
                }

                is UiEvent.Toast -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }

                // NOTE: your code used UiEvent.Navigation and UiEvent.NavigationBack names.
                // We update the local route state here so AnimatedContent shows the right screen.
                is UiEvent.Navigation -> {
                    // event.value is the route string in your existing UiEvent implementation
                    currentRoute = event.value
                }

                UiEvent.NavigationBack -> {
                    // simple behavior: navigate back to the list screen
                    currentRoute = "list"
                }
            }
        }
    }

    NoteAppFinalTheme {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerpad ->
            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    val forward = when {
                        initialState == "list" && targetState == "edit" -> true
                        initialState == "edit" && targetState == "list" -> false
                        else -> true
                    }
                    if (forward) {
                        (slideInHorizontally { width -> width } + fadeIn(animationSpec = tween(250))) with
                                (slideOutHorizontally { width -> -width } + fadeOut(tween(250)))
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn(tween(220))) with
                                (slideOutHorizontally { width -> width } + fadeOut(tween(220)))
                    }.using(SizeTransform(clip = false))
                }
            ) { route ->
                when (route) {
                    "edit" -> {
                        // Edit screen: when Back pressed, emit cancel + go back to list route
                        NoteEditScreen(
                            vm = vm,
                            onBack = {
                                vm.onEvent(NoteEvent.CancelEdit)
                                currentRoute = "list"
                            }
                        )
                    }

                    "trash" -> {
                        // Trash screen (if you want a dedicated trash route)
                        TrashScreen(
                            vm = vm,
                            onBack = {
                                currentRoute = "list"
                            }
                        )
                    }

                    else -> {
                        // default list screen
                        NoteScreen(
                            vm = vm,
                            onEditNote = { note ->
                                // Ask VM to start editing — VM should emit UiEvent.Navigation("edit")
                                vm.onEvent(NoteEvent.Edit(note))
                            },
                            onToggleTheme = {},
                            isDarkTheme = false,
                            onOpenTrash = {
                                // either emit an event that VM converts to UiEvent.Navigation("trash")
                                // or directly switch route from here:
                                // currentRoute = "trash"
                            }
                        )
                    }
                }
            }
        }
    }
}