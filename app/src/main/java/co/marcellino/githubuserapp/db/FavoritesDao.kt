package co.marcellino.githubuserapp.db

import androidx.room.*
import co.marcellino.githubuserapp.model.User

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM ${AppDatabase.TABLE_NAME_FAV}")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM ${AppDatabase.TABLE_NAME_FAV} WHERE ${AppDatabase.COLUMN_ID} LIKE :id")
    suspend fun findById(id: Int): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Delete
    suspend fun delete(user: User): Int
}