@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "order") val order: Int
)