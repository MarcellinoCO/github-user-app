package co.marcellino.githubfavorites

import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import co.marcellino.githubfavorites.adapter.UserListAdapter
import co.marcellino.githubfavorites.db.AppDatabase
import co.marcellino.githubfavorites.model.User
import co.marcellino.githubfavorites.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var userListAdapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(appbar_favorites)
        initializeRecyclerView()

        initializeViewModel()
        initializeContentResolver()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.loadFavoritesList()
    }

    private fun initializeRecyclerView(
        favoritesList: ArrayList<User> = arrayListOf(),
        isFirstCreated: Boolean = true
    ) {
        userListAdapter = UserListAdapter(favoritesList, this)

        if (isFirstCreated) rv_favorites.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_favorites.adapter = userListAdapter
    }

    private fun initializeViewModel() {
        mainViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
            .get(MainViewModel::class.java)
        mainViewModel.setContentResolver(contentResolver)

        if (mainViewModel.getFavoritesList().value == null) mainViewModel.loadFavoritesList()
        mainViewModel.getFavoritesList().observe(this, Observer { newFavoritesList ->
            initializeRecyclerView(newFavoritesList, false)

            tv_favorites_count.text = resources.getQuantityString(
                R.plurals.format_favorites_count,
                newFavoritesList.size,
                newFavoritesList.size
            )
        })
    }

    private fun initializeContentResolver() {
        val handlerThread = HandlerThread("DataObserver").also { it.start() }
        val handler = Handler(handlerThread.looper)
        val observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                mainViewModel.loadFavoritesList()
            }
        }
        contentResolver.registerContentObserver(AppDatabase.CONTENT_URI, true, observer)
    }
}
