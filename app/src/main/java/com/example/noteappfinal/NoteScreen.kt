package com.example.noteappfinal

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissValue
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.noteappfinal.ui.theme.NoteColors
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NoteScreen(vm : NoteViewModel , onEditNote : (NoteEntity) -> Unit , onToggleTheme : ()-> Unit , isDarkTheme : Boolean , onOpenTrash : () -> Unit) {
    val state by vm.uiState.collectAsState()
    val notes by vm.notes.collectAsState()
    val title = state.title
    val content = state.content
    var searchquery by rememberSaveable { mutableStateOf("") }
    val deletedTarget by vm.deletetarget.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val query = searchquery
    var uiSearch by remember { mutableStateOf(query) }

    LaunchedEffect(Unit) {
        snapshotFlow { uiSearch }
            .debounce(300)
            .distinctUntilChanged()
            .collect { debounce->
                vm.onEvent(NoteEvent.SearchChange(debounce))
            }
    }

    Scaffold(snackbarHost = {SnackbarHost(snackbarHostState)}) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp)
                .background(color = MaterialTheme.colorScheme.background)
        ) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notes" , style = MaterialTheme.typography.titleLarge)
                Row {
                    IconButton(onClick = {vm.onEvent(NoteEvent.openTrash)}) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Trash")
                    }
                }
            }

            OutlinedTextField(
                value = uiSearch,
                onValueChange = {uiSearch = it},
                modifier = Modifier.fillMaxWidth().height(55.dp),
                placeholder = { Text("Search Note...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                shape = RoundedCornerShape(40.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedTextColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                maxLines = 5
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { vm.onEvent(NoteEvent.TitleChange(it)) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { vm.onEvent(NoteEvent.ContentChange(it))},
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { vm.onEvent(NoteEvent.Save) }) {
                        Text("Save")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dark Mode")
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleTheme() }
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sort:")
                    TextButton(onClick = { vm.onEvent(NoteEvent.sort(SortOrder.NEWEST_FIRST)) }) { Text("Newest") }
                    TextButton(onClick = { vm.onEvent(NoteEvent.sort(SortOrder.OLDED_FIRST)) }) { Text("Oldest") }
                    TextButton(onClick = { vm.onEvent(NoteEvent.sort(SortOrder.TITLE_A_Z))}) { Text("A-Z") }

                    IconButton(onClick = {vm.onEvent(NoteEvent.toggleLayout)}) {
                        Icon(
                            imageVector = if (state.isGrid)
                            Icons.Default.ViewList
                            else
                            Icons.Default.GridView,
                            contentDescription = "Toggle Layout"
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))




            val pinnedNotes = notes.filter { it.isPinned }
            val otherNotes = notes.filter { !it.isPinned }



            if (notes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Matching Note.")
                }
            } else {

                if (state.isGrid) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notes, key = { it.id }) { item ->
                            NoteItem(
                                note = item,
                                onDelete = { vm.onEvent(NoteEvent.requestDelete(item)) },
                                onClick = { onEditNote(item) },
                                onTogglePin = { vm.onEvent(NoteEvent.TogglePin(item))}
                            )
                        }
                    }
                }
                else {

                LazyColumn(
                    Modifier.fillMaxSize(), verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    if (pinnedNotes.isNotEmpty()) {
                        item {
                            Text("Pinned", style = MaterialTheme.typography.titleSmall)
                        }
                        items(pinnedNotes, key = { it.id }) { item ->
                            SwipeToDelete(
                                note = item,
                                onDelete = { vm.onEvent(NoteEvent.requestDelete(item)) },
                                onClick = { onEditNote(item) },
                                onTogglePin = { vm.onEvent(NoteEvent.TogglePin(item)) }
                            )
                        }
                    }
                    if (otherNotes.isNotEmpty()) {
                        if (pinnedNotes.isNotEmpty()) {
                            item { Spacer(Modifier.height(8.dp)) }
                        }

                        item { Text("Others", style = MaterialTheme.typography.titleSmall) }

                        items(items = otherNotes, key = { it.id }) { item ->
                            SwipeToDelete(
                                note = item,
                                onDelete = { vm.onEvent(NoteEvent.requestDelete(item)) },
                                onClick = { onEditNote(item) },
                                onTogglePin = { vm.onEvent(NoteEvent.TogglePin(item)) }
                            )
                        }
                    }
                }

            }
                if (deletedTarget != null){
                    AlertDialog(
                        onDismissRequest = {vm.onEvent(NoteEvent.cancelDelete)},
                        title = {Text("Delete Note")},
                        text = {Text("Are you sure want to delete note ?")},
                        confirmButton = {
                            TextButton(onClick = {
                               scope.launch {
                                   vm.confirmDelete()
                                   val result = snackbarHostState.showSnackbar(
                                       message = "Note Deleted",
                                       actionLabel = "Undo",
                                       withDismissAction = true,
                                       duration = SnackbarDuration.Short
                                   )
                                   if (result == SnackbarResult.ActionPerformed) {
                                       vm.onEvent(NoteEvent.UndoDelete)
                                   }
                               }

                            }
                            ) { Text("Delete")}
                        },
                        dismissButton = {
                            TextButton(onClick = {vm.onEvent(NoteEvent.cancelDelete)}) { Text("Cancel") }
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDelete(note : NoteEntity , onDelete: () -> Unit , onClick: () -> Unit , onTogglePin: () -> Unit){

    val SwipeState = rememberDismissState(
        confirmStateChange = { value ->
            when(value){
                DismissValue.DismissedToStart -> {
                    onDelete()
                    true
                }
                DismissValue.DismissedToEnd -> {
                    onTogglePin()
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismiss(
        state = SwipeState,
        background = {
           val directions = SwipeState.dismissDirection
            when(directions){
                DismissDirection.StartToEnd ->{
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(Color(0xFF4CAF50)).padding(start = 30.dp), contentAlignment = Alignment.CenterStart){
                        Icon(Icons.Default.PushPin , null)
                    }
                }
                DismissDirection.EndToStart -> {
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(Color.Red).padding(end = 20.dp) , contentAlignment = Alignment.CenterEnd){
                        Icon(Icons.Default.Delete , null)
                    }
                }

                else -> {}
            }
        },
        dismissContent = {
            NoteItem(
                note = note,
                onDelete = onDelete,
                onClick = onClick,
                onTogglePin = onTogglePin
            )
        }
    )
}


@Composable
fun NoteItem(note : NoteEntity , onDelete : () -> Unit , onClick : () -> Unit , onTogglePin : () -> Unit){

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = "cardScale"
    )

    val backgroundColor = NoteColors.getOrElse(note.colorIndex){NoteColors.first()}

    Card(
        Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(interactionSource = interactionSource, indication = ripple()) { onClick() } ,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.elevatedCardElevation(4.dp))
    {
        Row(Modifier.padding(12.dp) , verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.background
                )
                if (note.content.isNotBlank()){
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }

            IconButton(onClick = onTogglePin ) {
                Icon(imageVector = if (note.isPinned) {Icons.Default.PushPin} else Icons.Outlined.PushPin
                    , contentDescription = if (note.isPinned) "Unpin" else "Pin")
            }
            IconButton(onClick = {onDelete()}) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(vm : NoteViewModel,onBack : () -> Unit){
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.editingId== null) "New Note" else "Edit Note")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        vm.onEvent(NoteEvent.CancelEdit)
                        onBack()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack , contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        vm.onEvent(NoteEvent.Save)
                        vm.onEvent(NoteEvent.CancelEdit)
                        onBack()
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) {  innerpadding->
        Column(Modifier
            .fillMaxSize()
            .padding(innerpadding)
            .padding(12.dp)) {
            OutlinedTextField(
                value = state.title,
                onValueChange = {vm.onEvent(NoteEvent.TitleChange(it))},
                label = {Text("Title")},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.content,
                onValueChange = {vm.onEvent(NoteEvent.ContentChange(it))},
                label = {Text("Content")},
                modifier= Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            Text("Color" , style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp) ,
                verticalAlignment = Alignment.CenterVertically) {

                NoteColors.forEachIndexed { index , color ->
                    val isSelected = index == state.colorIndex
                    Box(Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { vm.onEvent(NoteEvent.colorIndex(index)) }
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(vm: NoteViewModel, onBack: () -> Unit) {

    val trashNotes by vm.trashNotes.collectAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trash") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (trashNotes.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Trash Is Empty") }
        } else {

            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trashNotes, key = { it.id }) { item ->

                    NoteItem(
                        note = item,
                        onClick = {}, // no editing in trash
                        onTogglePin = {}, // ignore pin in trash
                        onDelete = { vm.onEvent(NoteEvent.Delete(item)) }
                    )
                }
            }
        }
    }
}
