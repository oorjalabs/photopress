package net.c306.photopress.api

import net.c306.photopress.BuildConfig

object ApiConstants {

    internal const val AUTH_REDIRECT_PATH = "/apps/auth/photopress.html"
    internal const val AUTH_REDIRECT_HOST = "c306.net"
    internal const val AUTH_REDIRECT_URL = "https://$AUTH_REDIRECT_HOST$AUTH_REDIRECT_PATH"

    @Suppress("unused")
    val AUTHORISE_URL_TOKEN = "https://public-api.wordpress.com/oauth2/authorize?client_id=${BuildConfig.WP_ID}&response_type=token&redirect_uri=$AUTH_REDIRECT_URL&scope=global"

    val AUTHORISE_URL = "https://public-api.wordpress.com/oauth2/authorize?client_id=${BuildConfig.WP_ID}&response_type=code&redirect_uri=$AUTH_REDIRECT_URL&scope=global"

    const val TOKEN_URL = "https://public-api.wordpress.com/oauth2/token"

    const val VALIDATE_URL = "https://public-api.wordpress.com/oauth2/token-info"


    const val BASE_URL = "https://public-api.wordpress.com/rest/v1.1/"

    const val ABOUT_ME_URL = "me"
    const val BLOG_LIST = "me/sites"
    const val CREATE_POST = "sites/{blog_id}/posts/new"
    const val UPDATE_POST = "sites/{blog_id}/posts/{post_id}"
    const val UPLOAD_MEDIA = "sites/{blog_id}/media/new"
    const val UPDATE_MEDIA_ATTRIBUTES = "/sites/{blog_id}/media/{media_id}"
    const val GET_TAGS_FOR_SITE = "sites/{blog_id}/tags"
    const val GET_CATEGORIES_FOR_SITE = "sites/{blog_id}/categories"
    const val CREATE_CATEGORY = "sites/{blog_id}/categories/new"



    const val ARG_FIELDS = "fields"
    const val ARG_OPTIONS = "options"
    const val ARG_ACCESS_TOKEN = "access_token"
    const val ARG_CLIENT_ID = "client_id"
    const val ARG_CLIENT_SECRET = "client_secret"
    const val ARG_CODE = "code"
    const val ARG_ERROR = "error"
    const val ARG_GRANT_TYPE = "grant_type"
    const val ARG_TOKEN = "token"
    const val ARG_USER_ID = "user_id"
    const val ARG_BLOG_ID = "blog_id"
    const val ARG_MEDIA_ID = "media_id"
    const val ARG_POST_ID = "post_id"
    const val ARG_BLOG_URL = "blog_url"
    const val ARG_TOKEN_TYPE = "token_type"
    const val ARG_REDIRECT_URI = "redirect_uri"
    const val ARG_USER_DISPLAY_NAME = "display_name"
    const val ARG_USER_AVATAR_URL = "avatar_URL"
    const val ARG_USER_PROFILE_URL = "profile_URL"
    const val ARG_PARENT = "parent"
    const val ARG_DESCRIPTION = "description"
    const val ARG_NAME = "name"
}