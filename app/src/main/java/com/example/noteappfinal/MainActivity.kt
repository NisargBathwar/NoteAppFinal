package com.example.noteappfinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.noteappfinal.ui.theme.NoteAppFinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm : NoteViewModel = viewModel()
           NoteAppRoot(vm = vm )
        }
    }
}

//
//@Composable
//fun NoteApp(){
//    val vm : NoteViewModel = viewModel()
//    val themevm : ThemeViewModel = viewModel()
//    val isDark by themevm.isDark.collectAsState()
//    val navController = rememberNavController()
//    NoteAppFinalTheme(darkTheme = isDark) {
//        Surface(color = MaterialTheme.colorScheme.background) {
//            NavHost(navController , startDestination = "list"){
//            composable("list"){
//                NoteScreen(
//                    vm = vm,
//                    onToggleTheme = { themevm.toggle() },
//                    isDarkTheme = isDark,
//                    onEditNote = { note ->
//                        vm.onEvent(NoteEvent.Edit(note))
//                        navController.navigate("edit")
//                    },
//                    onOpenTrash = {navController.navigate("trash")}
//                )
//
//            }
//
//                composable("edit"){
//                    NoteEditScreen(vm = vm ,
//                        onBack = {navController.popBackStack()})
//                }
//
//                composable("trash") {
//                    TrashScreen(
//                        vm = vm,
//                        onBack = {navController.popBackStack()}
//                    )
//                }
//            }
//        }
//    }
//}
