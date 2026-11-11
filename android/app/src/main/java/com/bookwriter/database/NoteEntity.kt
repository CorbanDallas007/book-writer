@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "raw_text") val rawText: String,
    @ColumnInfo(name = "corrected_text") val correctedText: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "chapter_id") val chapterId: String?,
    @ColumnInfo(name = "processing_style") val processingStyle: String
)