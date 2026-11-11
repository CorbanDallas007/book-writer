@Database(entities = [ChapterEntity::class, NoteEntity::class, StoryContextEntity::class], version = 1)
abstract class BookDatabase : RoomDatabase() {

    abstract fun chapterDao(): ChapterDao
    abstract fun noteDao(): NoteDao
    abstract fun storyContextDao(): StoryContextDao

    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        fun getDatabase(context: Context): BookDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "book_database"
                ).build()
                INSTANCE = db
                db
            }
        }
    }
}