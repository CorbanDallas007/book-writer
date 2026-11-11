@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE chapter_id = :chapterId ORDER BY created_at DESC")
    fun getNotesByChapter(chapterId: String): List<NoteEntity>

    @Query("SELECT * FROM notes ORDER BY created_at DESC")
    fun getAllNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getNoteById(noteId: String): NoteEntity?

    @Insert
    suspend fun insertNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}