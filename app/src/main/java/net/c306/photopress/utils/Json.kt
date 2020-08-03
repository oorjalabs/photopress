package net.c306.photopress.utils

import com.google.gson.Gson

object Json {
    
    private var gson: Gson? = null
    
    fun getInstance(): Gson {
        
        if (gson == null) {
//            // Source: https://static.javadoc.io/com.google.code.gson/gson/2.8.0/com/google/gson/TypeAdapterFactory.html
//            val builder = GsonBuilder()
//            builder.registerTypeAdapterFactory(LowercaseEnumTypeAdapterFactory())
//            gson = builder.create()
            gson = Gson()
        }
        
        return gson!!
    }
}