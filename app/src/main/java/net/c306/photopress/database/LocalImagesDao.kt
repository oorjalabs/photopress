package net.c306.photopress.database

import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class LocalImagesDao: BaseDao<PostImage> {
    
    @Query("SELECT * FROM local_media ORDER BY id ASC")
    abstract suspend fun getAll(): List<PostImage>
    
    @Query("SELECT * FROM local_media WHERE id = :id")
    abstract suspend fun getById(id: Int): PostImage?
    
    @Query("SELECT * FROM local_media WHERE post_id = :postId")
    abstract suspend fun getByPostId(postId: Int): PostImage?
    
    @Query("SELECT * FROM local_media WHERE id IN (:imageIds)")
    abstract suspend fun loadAllByIds(imageIds: IntArray): List<PostImage>
    
}