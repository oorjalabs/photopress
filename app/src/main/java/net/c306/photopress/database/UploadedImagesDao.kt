package net.c306.photopress.database

import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class UploadedImagesDao: BaseDao<UploadedMedia> {
    
    @Query("SELECT * FROM uploaded_media ORDER BY id ASC")
    abstract suspend fun getAll(): List<UploadedMedia>
    
    @Query("SELECT * FROM uploaded_media WHERE id IN (:imageIds)")
    abstract suspend fun loadAllByIds(imageIds: IntArray): List<UploadedMedia>
    
}