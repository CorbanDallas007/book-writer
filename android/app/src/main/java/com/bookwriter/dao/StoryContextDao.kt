@Dao
interface StoryContextDao {
    @Query("SELECT * FROM story_context")
    fun getAllStoryContexts(): List<StoryContextEntity>

    @Query("SELECT * FROM story_context WHERE keyword = :keyword")
    fun getStoryContextByKeyword(keyword: String): StoryContextEntity?

    @Insert
    suspend fun insertStoryContext(context: StoryContextEntity)

    @Delete
    suspend fun deleteStoryContext(context: StoryContextEntity)
}