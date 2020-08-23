package co.marcellino.githubuserapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import co.marcellino.githubuserapp.adapter.ListUserAdapter
import co.marcellino.githubuserapp.db.AppDatabase
import co.marcellino.githubuserapp.model.User
import co.marcellino.githubuserapp.viewmodel.FavoritesViewModel
import kotlinx.android.synthetic.main.activity_favorites.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private lateinit var favoritesViewModel: FavoritesViewModel
    private lateinit var appDatabase: AppDatabase

    private lateinit var listFavoritesAdapter: ListUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        initializeAppbar()
        initializeRecyclerView(arrayListOf(), true)

        appDatabase = AppDatabase(applicationContext)
        initializeViewModel()
    }

    override fun onResume() {
        super.onResume()
        favoritesViewModel.loadFavoritesPage()
    }

    private fun initializeAppbar() {
        setSupportActionBar(appbar_favorites)
        appbar_favorites.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun initializeRecyclerView(favoritesList: ArrayList<User>, isFirstCreated: Boolean) {
        listFavoritesAdapter = ListUserAdapter(favoritesList, this)

        if (isFirstCreated) rv_favorites.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_favorites.adapter = listFavoritesAdapter

        listFavoritesAdapter.setOnItemClickCallback(object : ListUserAdapter.OnItemClickCallback {
            override fun onItemClicked(position: Int, user: User, sharedElement: View) {
                goToDetailActivity(user, sharedElement)
            }

            override fun onItemAddToFavorites(position: Int, user: User) {
                addToFavorites(position, user)
            }
        })
    }

    private fun initializeViewModel() {
        favoritesViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(FavoritesViewModel::class.java)

        favoritesViewModel.setAppDatabase(appDatabase)
        if (favoritesViewModel.getFavoritesPage().value == null) favoritesViewModel.loadFavoritesPage()
        favoritesViewModel.getFavoritesPage().observe(this, Observer { newFavoritesPage ->
            initializeRecyclerView(newFavoritesPage, false)

            container_favorite_loading.visibility = View.GONE
            container_favorite_error.visibility = View.GONE
            container_favorite_list.visibility = View.VISIBLE
        })

        favoritesViewModel.getTotalFavoritesCount().observe(this, Observer { totalFavoritesCount ->
            tv_favorites_count.text = resources.getQuantityString(
                R.plurals.format_favorites_count,
                totalFavoritesCount,
                totalFavoritesCount
            )
        })

        favoritesViewModel.isFavoritesLoading().observe(this, Observer { isFavoritesLoading ->
            if (isFavoritesLoading) setLoading()
        })

        favoritesViewModel.isFavoritesError().observe(this, Observer { isFavoritesError ->
            if (isFavoritesError) setError()
        })
    }

    private fun goToDetailActivity(user: User, sharedElement: View) {
        val intentDetailActivity = Intent(this, UserDetailActivity::class.java)
        intentDetailActivity.putExtra(UserDetailActivity.EXTRA_USER_DATA, user)

        val sharedElementTransition = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            sharedElement,
            user.username
        )

        startActivity(intentDetailActivity, sharedElementTransition.toBundle())
    }

    private fun addToFavorites(position: Int, user: User) {
        if (!user.isFavorite) {
            listFavoritesAdapter.removeItemAt(position)

            GlobalScope.launch {
                appDatabase.favoritesDao().delete(user)
                favoritesViewModel.loadTotalFavoritesCount()
            }
        }
    }

    private fun setLoading() {
        container_favorite_error.visibility = View.GONE
        container_favorite_list.visibility = View.GONE
        container_favorite_loading.visibility = View.VISIBLE
    }

    private fun setError() {
        container_favorite_list.visibility = View.GONE
        container_favorite_loading.visibility = View.GONE
        container_favorite_error.visibility = View.VISIBLE
    }
}