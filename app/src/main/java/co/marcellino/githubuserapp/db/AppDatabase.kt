package co.marcellino.githubuserapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import co.marcellino.githubuserapp.model.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao

    companion object {
        const val TABLE_NAME_FAV = "table_favorites"

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

        @Volatile
        private var INSTANCE: AppDatabase? = null

        operator fun invoke(context: Context) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context, AppDatabase::class.java, TABLE_NAME_FAV)
                .build()
        }
    }
}