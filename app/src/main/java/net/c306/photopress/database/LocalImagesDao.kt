package net.c306.photopress.database

import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class LocalImagesDao: BaseDao<PostImage> {
    
    @Query("SELECT * FROM local_media ORDER BY id ASC")
    abstract suspend fun getAll(): List<PostImage>
    
    @Query("SELECT * FROM local_media WHERE id IN (:imageIds)")
    abstract suspend fun loadAllByIds(imageIds: IntArray): List<PostImage>
    
}