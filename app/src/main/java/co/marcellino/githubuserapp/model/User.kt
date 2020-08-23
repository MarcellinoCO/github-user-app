package co.marcellino.githubuserapp.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import co.marcellino.githubuserapp.db.AppDatabase
import kotlinx.android.parcel.Parcelize

@Entity(tableName = AppDatabase.TABLE_NAME_FAV)
@Parcelize
data class User(
    @PrimaryKey @ColumnInfo(name = AppDatabase.COLUMN_ID) var id: Int = 0,
    @ColumnInfo(name = AppDatabase.COLUMN_USERNAME) var username: String = "",
    @ColumnInfo(name = AppDatabase.COLUMN_AVATAR) var avatar: String = "",
    @ColumnInfo(name = AppDatabase.COLUMN_NAME) var name: String = "",
    @ColumnInfo(name = AppDatabase.COLUMN_COMPANY) var company: String = "",
    @ColumnInfo(name = AppDatabase.COLUMN_LOCATION) var location: String = "",
    @ColumnInfo(name = AppDatabase.COLUMN_REPOSITORY) var repository: Int = 0,
    @ColumnInfo(name = AppDatabase.COLUMN_FOLLOWER) var follower: Int = 0,
    @ColumnInfo(name = AppDatabase.COLUMN_FOLLOWING) var following: Int = 0,
    @ColumnInfo(name = AppDatabase.COLUMN_IS_FAVORITE) var isFavorite: Boolean = false
) : Parcelable