package com.example.noteappfinal

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao : NoteDao) {

    val notes : Flow<List<NoteEntity>> = noteDao.getAllNotes()



    suspend fun moveToTrash(note: NoteEntity) {
        noteDao.update(note.copy(isTrashed = true))
    }

    suspend fun restoreFromTrash(note: NoteEntity) {
        noteDao.update(note.copy(isTrashed = false))
    }

    fun getTrashNotes(): Flow<List<NoteEntity>> = noteDao.getTrashNotes()

    suspend fun insertOrupdate(note : NoteEntity){
        if (note.id == 0L) noteDao.insert(note) else noteDao.update(note)
    }

    suspend fun delete(note : NoteEntity) = noteDao.delete(note)
    suspend fun deleteunpinned() = noteDao.deleteAllUnpinned()
    suspend fun pinAll() = noteDao.pinAll()
    suspend fun replaceAll(newNotes : List<NoteEntity>) = noteDao.replaceAll(newNotes)


}
