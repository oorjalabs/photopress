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
    
    abstract fun dao(): Dao
    
    companion object {
        private const val databaseName = "pp_adklfhh238yh_db"
        const val DATABASE_VERSION = 1
        
        private var db: AppDatabase? = null
        
        private var dbInstance: Dao? = null
        
        fun getInstance(context: Context): Dao {
            if (dbInstance == null) {
                db = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
                    .fallbackToDestructiveMigration()
                    
                    // Added `deleted` column to tasks
//                    .addMigrations(object : Migration(1, 2) {
//                        override fun migrate(database: SupportSQLiteDatabase) {
//                            database.execSQL("ALTER TABLE tasks ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
//                        }
//                    })
                    
                    .build()
                dbInstance = db?.dao()
            }
            return dbInstance!!
        }
        
        private fun close() {
            db?.close()
        }
        
    }
}