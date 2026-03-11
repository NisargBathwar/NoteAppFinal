package com.example.noteappfinal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0L ,
    val title : String ,
    val content : String ,
    val timeStamp : Long = System.currentTimeMillis(),
    val colorIndex : Int = 0,
    val isPinned : Boolean = false,
    val isDeleted : Boolean = false,
    val isTrashed : Boolean  = false
)