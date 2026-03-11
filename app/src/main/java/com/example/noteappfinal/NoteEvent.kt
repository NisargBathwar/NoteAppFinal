package com.example.noteappfinal

sealed interface NoteEvent{
    data class TitleChange(val newString : String) : NoteEvent
    data class ContentChange(val newContent : String) : NoteEvent
    data class Edit(val note : NoteEntity) : NoteEvent
    data class Delete(val note : NoteEntity) : NoteEvent
    object Save : NoteEvent
    object CancelEdit : NoteEvent

    data class requestDelete(val note : NoteEntity): NoteEvent
    object cancelDelete : NoteEvent
    object UndoDelete : NoteEvent

    data class SearchChange(val newSearch : String) : NoteEvent
    data class sort(val ordere : SortOrder) : NoteEvent
    object toggleLayout : NoteEvent
    data class TogglePin(val note : NoteEntity) : NoteEvent
    object BulkDeleteUnpinned : NoteEvent
    object PinAll : NoteEvent
    object openTrash : NoteEvent

    data class colorIndex(val index : Int) : NoteEvent

    data class  movetoTrash(val note : NoteEntity) : NoteEvent
    data class restorefromtrash(val note : NoteEntity) : NoteEvent
}

