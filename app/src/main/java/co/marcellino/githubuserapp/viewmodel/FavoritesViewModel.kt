package co.marcellino.githubuserapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.marcellino.githubuserapp.db.AppDatabase
import co.marcellino.githubuserapp.model.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {

    private val TAG = "GithubUserApp"

    private var appDatabase: AppDatabase? = null
    fun setAppDatabase(appDatabase: AppDatabase) {
        this.appDatabase = appDatabase
    }

    private val isFavoritesLoading = MutableLiveData<Boolean>()
    fun isFavoritesLoading(): LiveData<Boolean> = isFavoritesLoading
    private val isFavoritesError = MutableLiveData<Boolean>()
    fun isFavoritesError(): LiveData<Boolean> = isFavoritesError

    private val totalFavoritesCount = MutableLiveData<Int>()
    fun getTotalFavoritesCount(): LiveData<Int> = totalFavoritesCount
    fun loadTotalFavoritesCount() {
        GlobalScope.launch {
            val count = appDatabase?.favoritesDao()?.getAll()?.size ?: 0
            totalFavoritesCount.postValue(count)
        }
    }

    private var liveFavoritesPage = MutableLiveData<ArrayList<User>>()
    fun getFavoritesPage(): LiveData<ArrayList<User>> = liveFavoritesPage
    fun loadFavoritesPage() {
        isFavoritesLoading.postValue(true)
        GlobalScope.launch {
            val currentFavoritesPage = appDatabase?.favoritesDao()?.getAll() ?: listOf()
            totalFavoritesCount.postValue(currentFavoritesPage.size)
            liveFavoritesPage.postValue(ArrayList(currentFavoritesPage))

            isFavoritesLoading.postValue(false)
            isFavoritesError.postValue(false)
        }
    }
}