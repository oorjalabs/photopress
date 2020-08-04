package net.c306.photopress.database

import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class PostsDao : BaseDao<PhotoPressPost> {
    
    @Query("SELECT * FROM blog_posts ORDER BY timestamp DESC")
    abstract suspend fun getAll(): List<PhotoPressPost>
    
    @Query("SELECT * FROM blog_posts WHERE id IN (:postIds)")
    abstract suspend fun loadAllByIds(postIds: IntArray): List<PhotoPressPost>
    
}