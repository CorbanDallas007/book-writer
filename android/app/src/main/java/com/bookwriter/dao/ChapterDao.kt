@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters ORDER BY order")
    fun getAllChapters(): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    fun getChapterById(chapterId: String): ChapterEntity?

    @Insert
    suspend fun insertChapter(chapter: ChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)
}