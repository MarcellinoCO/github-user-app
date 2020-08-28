package co.marcellino.githubfavorites.db

import android.net.Uri

class AppDatabase {
    companion object {
        private const val AUTHORITY = "co.marcellino.githubuserapp"
        private const val SCHEME = "content"
        private const val TABLE_NAME_FAV = "table_favorites"

        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_AVATAR = "avatar"
        const val COLUMN_NAME = "name"
        const val COLUMN_COMPANY = "company"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_REPOSITORY = "repository"
        const val COLUMN_FOLLOWER = "follower"
        const val COLUMN_FOLLOWING = "following"
        const val COLUMN_IS_FAVORITE = "is_favorite"

        val CONTENT_URI: Uri =
            Uri.Builder().scheme(SCHEME).authority(AUTHORITY).appendPath(TABLE_NAME_FAV).build()
    }
}