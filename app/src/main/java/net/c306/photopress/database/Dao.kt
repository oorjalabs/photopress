package net.c306.photopress.database

import androidx.room.*
import androidx.room.Dao

@Dao
abstract class Dao {
    
    @Query("SELECT * FROM blog_posts ORDER BY timestamp DESC")
    abstract suspend fun getAll(): List<PhotoPressPost>
    
    @Query("SELECT * FROM blog_posts WHERE id IN (:conversionIds)")
    abstract suspend fun loadAllByIds(conversionIds: IntArray): List<PhotoPressPost>
    
//    /** Get blog_posts as a data source for paging */
//    @RawQuery(observedEntities = [PhotoPressPost::class])
//    abstract fun getPhotoPressPostsDataSourceForQuery(query: SupportSQLiteQuery): DataSource.Factory<Int, PhotoPressPost>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(conversion: PhotoPressPost)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(vararg blog_posts: PhotoPressPost)
    
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertAll(blog_posts: List<PhotoPressPost>)
    
    @Update
    abstract suspend fun update(conversion: PhotoPressPost)
    
    @Delete
    abstract suspend fun delete(conversion: PhotoPressPost)
    
    /**
     * Used only when logging user out
     */
    @Transaction
    @Query("delete FROM blog_posts")
    abstract suspend fun deleteAllHistory()
    
    
}