package net.c306.photopress.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import net.c306.photopress.utils.Json

@Keep
data class WPTag(
    @SerializedName("ID")
    val id: Int,
    val name: String,
    @SerializedName("post_count")
    val postCount: Int,
    val slug: String
) {
    companion object {
        
        const val FIELDS_STRING = "ID,name,post_count,slug"
        
        fun fromJson(jsonString: String): WPTag {
            return Json.getInstance().fromJson(jsonString, WPTag::class.java)
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
    data class TagsResponse(
        /** The number of tags returned. */
        val  found: Int,
        /** Array of tag objects. */
        val tags: List<WPTag>
    )
    
    fun toJson(): String {
        return Json.getInstance().toJson(this)
    }
    
}