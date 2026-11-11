@Entity(tableName = "story_context")
data class StoryContextEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "keyword") val keyword: String,
    @ColumnInfo(name = "details") val details: String
)