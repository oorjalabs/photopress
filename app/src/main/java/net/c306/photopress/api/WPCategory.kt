package net.c306.photopress.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import net.c306.photopress.utils.Json

@Keep
data class WPCategory(
    @SerializedName("ID")
    val id: Int,
    val name: String,
    @SerializedName("post_count")
    val postCount: Int,
    /** id of parent category */
    val parent: Int,
    val slug: String,
    val isLocal: Boolean = false
) {
    companion object {
        
        const val FIELDS_STRING = "ID,name,post_count,parent,slug"
        
        fun fromJson(jsonString: String): WPCategory {
            return Json.getInstance().fromJson(jsonString, WPCategory::class.java)
        }
        
        const val ARG_ORDER_BY = "order_by"
        const val ARG_ORDER = "order"
        const val ARG_NUMBER = "number"
        const val ARG_FIELDS = "fields"
        
        const val VALUE_ORDER_BY = "count"
        const val VALUE_ORDER = "DESC"
        const val VALUE_NUMBER = 50
        
    }
    
    
    @Keep
    data class GetCategoriesResponse(
        /** The number of categories returned. */
        val found: Int,
        /** Array of [WPCategory] objects. */
        val categories: List<WPCategory>
    )
    
    fun toJson(): String {
        return Json.getInstance().toJson(this)
    }
    
}