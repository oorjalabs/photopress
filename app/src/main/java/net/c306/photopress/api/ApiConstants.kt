package net.c306.photopress.api

import net.c306.photopress.BuildConfig

object ApiConstants {

    private const val AUTH_REDIRECT_URL = "https://c306.net/apps/auth/photopress.html"

    const val AUTHORISE_URL = "https://public-api.wordpress.com/oauth2/authorize?client_id=${BuildConfig.WP_ID}&response_type=token&redirect_uri=$AUTH_REDIRECT_URL&scope=global"

    const val BASE_URL = "https://baseurl.com/"
    const val LOGIN_URL = "auth/login"
    const val POSTS_URL = "posts"
}