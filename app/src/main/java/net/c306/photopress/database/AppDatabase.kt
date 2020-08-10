package net.c306.photopress.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.c306.photopress.database.AppDatabase.Companion.DATABASE_VERSION
import net.c306.photopress.database.TypeConverters as Converters

@Database(entities = [PhotoPressPost::class, UploadedMedia::class, PostImage::class], version = DATABASE_VERSION)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    
    abstract fun postsDao(): PostsDao
    abstract fun uploadedImagesDao(): UploadedImagesDao
    abstract fun localImagesDao(): LocalImagesDao
    
    
    companion object {
        private const val databaseName = "pp_adklfhh238yh_db"
        const val DATABASE_VERSION = 3
        
        private var db: AppDatabase? = null
        
        private var postsInstance: PostsDao? = null
        private var uploadedImagesInstance: UploadedImagesDao? = null
        private var localImagesInstance: LocalImagesDao? = null
        
        fun getPostsInstance(context: Context): PostsDao {
            if (postsInstance == null) {
                initDb(context)
            }
            
            return postsInstance!!
        }
        
        fun getUploadedImagesInstance(context: Context): UploadedImagesDao {
            if (uploadedImagesInstance == null) {
                initDb(context)
            }
            
            return uploadedImagesInstance!!
        }
        
        fun getLocalImagesInstance(context: Context): LocalImagesDao {
            if (localImagesInstance == null) {
                initDb(context)
            }
            
            return localImagesInstance!!
        }
        
        private fun initDb(context: Context) {
            if (postsInstance == null) {
                db = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
                    .fallbackToDestructiveMigration()
            
                    // Added `deleted` column to tasks
//                    .addMigrations(object : Migration(1, 2) {
//                        override fun migrate(database: SupportSQLiteDatabase) {
//                            database.execSQL("ALTER TABLE tasks ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
//                        }
//                    })
            
                    .build()
                postsInstance = db?.postsDao()
                uploadedImagesInstance = db?.uploadedImagesDao()
                localImagesInstance = db?.localImagesDao()
            }
        }
        
        private fun close() {
            db?.close()
        }
        
    }
}