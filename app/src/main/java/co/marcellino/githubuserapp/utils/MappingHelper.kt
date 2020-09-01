package co.marcellino.githubuserapp.utils

import android.database.Cursor
import co.marcellino.githubuserapp.db.AppDatabase
import co.marcellino.githubuserapp.model.User

object MappingHelper {

    fun mapCursorToArrayList(userCursor: Cursor?): ArrayList<User> {
        val userList = arrayListOf<User>()

        userCursor?.apply {
            while (moveToNext()) {
                userList.add(
                    User(
                        getInt(getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                        getString(getColumnIndexOrThrow(AppDatabase.COLUMN_USERNAME)),
                        getString(getColumnIndexOrThrow(AppDatabase.COLUMN_AVATAR)),
                        getString(getColumnIndexOrThrow(AppDatabase.COLUMN_NAME)),
                        getString(getColumnIndexOrThrow(AppDatabase.COLUMN_COMPANY)),
                        getString(getColumnIndexOrThrow(AppDatabase.COLUMN_LOCATION)),
                        getInt(getColumnIndexOrThrow(AppDatabase.COLUMN_REPOSITORY)),
                        getInt(getColumnIndexOrThrow(AppDatabase.COLUMN_FOLLOWER)),
                        getInt(getColumnIndexOrThrow(AppDatabase.COLUMN_FOLLOWING)),
                        true
                    )
                )
            }
        }
        return userList
    }
}