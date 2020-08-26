package co.marcellino.githubuserapp.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import co.marcellino.githubuserapp.db.AppDatabase
import co.marcellino.githubuserapp.model.User
import kotlinx.coroutines.runBlocking

class FavoritesProvider : ContentProvider() {

    companion object {
        private const val AUTHORITY = "co.marcellino.githubuserapp"
        private const val SCHEME = "content"

        const val CODE_FAVORITE = 1
        const val CODE_FAVORITE_ID = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private lateinit var appDatabase: AppDatabase

        val CONTENT_URI: Uri =
            Uri.Builder().scheme(SCHEME).authority(AUTHORITY).appendPath(AppDatabase.TABLE_NAME_FAV)
                .build()

        init {
            uriMatcher.addURI(AUTHORITY, AppDatabase.TABLE_NAME_FAV, CODE_FAVORITE)
            uriMatcher.addURI(AUTHORITY, "${AppDatabase.TABLE_NAME_FAV}/#", CODE_FAVORITE_ID)
        }
    }

    override fun onCreate(): Boolean {
        appDatabase = AppDatabase(context as Context)
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? = runBlocking {
        if (uriMatcher.match(uri) == CODE_FAVORITE) {
            appDatabase.favoritesDao().getAllRaw()
        } else null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = runBlocking {
        val added: Long = if (uriMatcher.match(uri) == CODE_FAVORITE) {
            context?.contentResolver?.notifyChange(CONTENT_URI, null)
            appDatabase.favoritesDao().insert(User.fromContentValues(values))
        } else 0
        return@runBlocking Uri.parse("$CONTENT_URI/$added")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int =
        runBlocking {
            return@runBlocking if (uriMatcher.match(uri) == CODE_FAVORITE_ID) {
                context?.contentResolver?.notifyChange(CONTENT_URI, null)
                appDatabase.favoritesDao().deleteById(uri.lastPathSegment.toString().toInt())
            } else 0
        }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    override fun getType(uri: Uri): String? = null
}
