package com.example.noteappfinal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao{
    @Query("select * from notes where isTrashed = 0 order by timeStamp desc")
    fun getAllNotes() : Flow<List<NoteEntity>>



    @Query("SELECT * FROM notes WHERE isTrashed = 1")
    fun getTrashNotes(): Flow<List<NoteEntity>>
    @Query("delete from notes")
    suspend fun deleteAll()

    @Query("delete from notes where isPinned=0")
    suspend fun deleteAllUnpinned()

    @Query("update notes set isPinned = 1")
    suspend fun pinAll()

    @Transaction
    suspend fun replaceAll(newNotes : List<NoteEntity>){
        deleteAll()
        newNotes.forEach { insert(it) }
    }

    @Update
    suspend fun update(note : NoteEntity)

    @Upsert
    suspend fun insert(note : NoteEntity) : Long

    @Delete
    suspend fun delete(note : NoteEntity)
}