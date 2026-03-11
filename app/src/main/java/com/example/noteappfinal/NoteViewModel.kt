package com.example.noteappfinal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


enum class SortOrder{
    NEWEST_FIRST ,
    OLDED_FIRST,
    TITLE_A_Z
}
data class NoteUiState(
    val title : String = "",
    val content : String = "",
    val editingId : Long? = null,
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST,
    val colorIndex : Int = 0,
    val isGrid : Boolean = false,
)


class NoteViewModel(application: Application ) : AndroidViewModel(application) {

    private val repository : NoteRepository

    val notes : StateFlow<List<NoteEntity>>

    private var recentlyDeletedNote : NoteEntity? = null

    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState : StateFlow<NoteUiState> = _uiState


    private val _deleteTarget = MutableStateFlow<NoteEntity?>(null)
    val deletetarget = _deleteTarget.asStateFlow()



    private val _event = MutableSharedFlow<UiEvent>()
    val event = _event.asSharedFlow()

    private val deleteStack = ArrayDeque<NoteEntity>()

    private val searchQuery = MutableStateFlow("")
    private val sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)

    init {
        val dao = NoteDatabase.getInstance(application)
        repository = NoteRepository(dao.noteDao())

        notes = combine(
            repository.notes,
            searchQuery.debounce(300),
            sortOrder
        ) { notes, query, order ->

            val filtered = if (query.isBlank()) notes else {
                notes.filter {
                    it.title.contains(query, true) ||
                            it.content.contains(query, true)
                }
            }

            when (order) {
                SortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.id }
                SortOrder.OLDED_FIRST -> filtered.sortedBy { it.id }
                SortOrder.TITLE_A_Z -> filtered.sortedBy { it.title.lowercase() }
            }

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )
    }

    val trashNotes: Flow<List<NoteEntity>> = repository.getTrashNotes()


    fun onEvent(event : NoteEvent){
        when(event){
            is NoteEvent.TitleChange -> _uiState.update { it.copy(title = event.newString) }
            is NoteEvent.ContentChange -> _uiState.update { it.copy(content = event.newContent) }
            is NoteEvent.Edit -> startEdit(event.note)
            is NoteEvent.Delete -> delete(event.note)
            is NoteEvent.Save -> saveNote()
            is NoteEvent.PinAll -> viewModelScope.launch { repository.pinAll() }
            is NoteEvent.CancelEdit -> cancelEdit()
            is NoteEvent.UndoDelete -> RestoreLastDelete()
            is NoteEvent.SearchChange -> onSearchChange(event.newSearch)
            is NoteEvent.BulkDeleteUnpinned -> viewModelScope.launch { repository.deleteunpinned() }
            is NoteEvent.sort -> onSortChange(event.ordere)
            is NoteEvent.toggleLayout -> onToggleChange()
            is NoteEvent.TogglePin -> onTogglepin(event.note)
            is NoteEvent.requestDelete -> requestDelete(event.note)
            is NoteEvent.cancelDelete -> cancelDelete()
            is NoteEvent.restorefromtrash -> restoreFromTrash(event.note)
            is NoteEvent.colorIndex -> {_uiState.update { it.copy(colorIndex = event.index) }}
            is NoteEvent.movetoTrash ->{viewModelScope.launch { repository.moveToTrash(event.note) }}
            is NoteEvent.openTrash -> openTrash()
        }
    }



    private fun restoreFromTrash(note: NoteEntity) {
        viewModelScope.launch {
            repository.insertOrupdate(note.copy(isTrashed = false))
        }
    }



    private fun onTogglepin(note : NoteEntity){
        val toggle =  note.copy(isPinned = !note.isPinned)
        viewModelScope.launch {
            try {
                repository.insertOrupdate(toggle)
            }catch (e : Exception){
                _event.emit(UiEvent.ShowSnackbar("Pin Failed : ${e.message}"))
            }
        }
    }


    fun requestDelete(note: NoteEntity) {
        _deleteTarget.value = note
    }

    fun cancelDelete() {
        _deleteTarget.value = null
    }

    fun confirmDelete() {
        val note = _deleteTarget.value ?: return
        viewModelScope.launch {
            repository.moveToTrash(note)
            _deleteTarget.value = null
        }
    }

    private fun onSortChange(neworder : SortOrder){
        _uiState.update { it.copy(sortOrder = neworder) }
    }

    private fun onToggleChange(){
        _uiState.update { state ->
            state.copy(isGrid = !state.isGrid)
        }
    }


    private fun openTrash(){
        viewModelScope.launch {
            _event.emit(UiEvent.Navigation("trash"))
        }
    }

    private fun startEdit(note : NoteEntity){
        _uiState.update {
            it.copy(
                editingId = note.id ,
                title = note.title,
                content = note.content,
                colorIndex = note.colorIndex
            )
        }

        viewModelScope.launch {
            _event.emit(UiEvent.Navigation("edit"))
        }
    }

    private fun cancelEdit(){
        _uiState.update {
            it.copy(
                editingId =  null ,
                title = "",
                content  = "",
                colorIndex = 0
            )
        }
    }

    private fun saveNote(){
        val s = _uiState.value
        if (s.title.isBlank() && s.content.isBlank()) return

        val save = NoteEntity(
            id = s.editingId ?: 0L,
            title = s.title,
            content = s.content,
            colorIndex = s.colorIndex,
            isPinned = false
        )

        viewModelScope.launch {
          repository.insertOrupdate(save)
            _uiState.update {
                it.copy(
                    title = "",
                    content = "",
                    editingId = null ,
                    colorIndex = 0
                )
            }
            _event.emit(UiEvent.Toast("Saved"))
        }
    }

    private fun delete(note : NoteEntity){
        viewModelScope.launch {
            try {
                recentlyDeletedNote = note
                repository.delete(note)
                deleteStack.addFirst(note)
                _event.emit(UiEvent.ShowSnackbar("Note Delete" , "Undo"))
            }catch (e : Exception){
                _event.emit(UiEvent.ShowSnackbar("Delete Failed : ${e.message}"))
            }
        }
    }

    fun onSearchChange(query: String) {
        searchQuery.value = query
    }

    private fun  RestoreLastDelete(){
        viewModelScope.launch {
            val note = if (deleteStack.isNotEmpty()) deleteStack.removeFirst() else null
            if (note != null){
                repository.insertOrupdate(note)
            }else{
                _event.emit(UiEvent.ShowSnackbar("Nothing To Restore"))
            }
        }
    }

}