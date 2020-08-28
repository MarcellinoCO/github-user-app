package co.marcellino.githubfavorites.viewmodel

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.marcellino.githubfavorites.db.AppDatabase
import co.marcellino.githubfavorites.model.User
import co.marcellino.githubfavorites.utils.MappingHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private var contentResolver: ContentResolver? = null
    fun setContentResolver(contentResolver: ContentResolver) {
        this.contentResolver = contentResolver
    }

    private var favoritesList = ArrayList<User>()
    private val liveFavoritesList = MutableLiveData<ArrayList<User>>()
    fun getFavoritesList(): LiveData<ArrayList<User>> = liveFavoritesList
    fun loadFavoritesList() {
        GlobalScope.launch(Dispatchers.Main) {
            val deferredFavoritesList = async(Dispatchers.Main) {
                val cursor = contentResolver?.query(AppDatabase.CONTENT_URI, null, null, null, null)
                MappingHelper.mapCursorToArrayList(cursor)
            }

            favoritesList = deferredFavoritesList.await()
            liveFavoritesList.postValue(favoritesList)
        }
    }
}