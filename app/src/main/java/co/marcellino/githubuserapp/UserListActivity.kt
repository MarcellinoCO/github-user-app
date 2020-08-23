package co.marcellino.githubuserapp

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import co.marcellino.githubuserapp.adapter.ListUserAdapter
import co.marcellino.githubuserapp.db.AppDatabase
import co.marcellino.githubuserapp.model.User
import co.marcellino.githubuserapp.utils.NetworkManager
import co.marcellino.githubuserapp.viewmodel.UserListViewModel
import kotlinx.android.synthetic.main.activity_user_list.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserListActivity : AppCompatActivity(), ExitDialogFragment.OnExitDialogListener,
    View.OnClickListener {

    private lateinit var userLisViewModel: UserListViewModel
    private lateinit var appDatabase: AppDatabase

    private lateinit var listUserAdapter: ListUserAdapter
    private lateinit var listSearchAdapter: ListUserAdapter
    private var totalPagesCount = 0

    private var isNextPageAvailable = false
    private var isNextPageRequested = false
    private var isPreviousPageAvailable = false
    private var isPreviousPageRequested = false

    private var listUser = arrayListOf<User>()

    private var isLoading = true
    private var isError = false

    private var isSearching = false
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        initializeAppBar()

        initializeListRecyclerView(arrayListOf(), true)
        initializeSearchRecyclerView(arrayListOf(), true)

        NetworkManager.getInstance(applicationContext)
        appDatabase = AppDatabase(applicationContext)
        initializeViewModel()

        btn_previous_page.setOnClickListener(this)
        btn_next_page.setOnClickListener(this)
        btn_error_retry.setOnClickListener(this)
        btn_list_to_top.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.apppbar_user_list, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search)
        val searchViewAction = searchView.actionView as SearchView

        searchViewAction.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchViewAction.queryHint = resources.getString(R.string.hint_enter_username)
        searchViewAction.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) userLisViewModel.loadSearchPage(newText)
                else userLisViewModel.cancelSearchPage()
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) userLisViewModel.loadSearchPage(query)
                else userLisViewModel.cancelSearchPage()
                return true
            }
        })
        searchView.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?) = true

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                userLisViewModel.cancelSearchPage()
                return true
            }
        })

        if (isSearching) {
            searchView.expandActionView()
            searchViewAction.setQuery(searchQuery, false)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        userLisViewModel.updateFavorites()
    }

    override fun onBackPressed() {
        ExitDialogFragment().show(supportFragmentManager, ExitDialogFragment::class.java.simpleName)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_next_page -> {
                if (!isNextPageAvailable) {
                    setLoading()
                    isNextPageRequested = true
                } else userLisViewModel.loadNextPage()
            }
            R.id.btn_previous_page -> {
                if (!isPreviousPageAvailable) {
                    setLoading()
                    isPreviousPageRequested = true
                } else userLisViewModel.loadPreviousPage()
            }
            R.id.btn_error_retry -> {
                if (isSearching) userLisViewModel.loadSearchPage(searchQuery)
                else userLisViewModel.loadCurrentPage()
            }
            R.id.btn_list_to_top -> {
                sv_list.smoothScrollTo(0, 0, 1000)
            }
        }
    }

    private fun initializeAppBar() {
        setSupportActionBar(appbar_user_list)
        appbar_user_list.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.favorite -> {
                    val intentFavoritesActivity = Intent(this, FavoritesActivity::class.java)
                    startActivity(intentFavoritesActivity)
                }
                R.id.language -> {
                    val intentChangeLanguage = Intent(Settings.ACTION_LOCALE_SETTINGS)
                    startActivity(intentChangeLanguage)
                }
            }
            true
        }
    }

    private fun initializeListRecyclerView(usersList: ArrayList<User>, isFirstCreated: Boolean) {
        listUserAdapter = ListUserAdapter(usersList, this)

        if (isFirstCreated) rv_user_list.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_user_list.adapter = listUserAdapter

        listUserAdapter.setOnItemClickCallback(object : ListUserAdapter.OnItemClickCallback {
            override fun onItemClicked(position: Int, user: User, sharedElement: View) {
                goToDetailActivity(user, sharedElement)
            }

            override fun onItemAddToFavorites(position: Int, user: User) {
                addToFavorites(user)
            }
        })
    }

    private fun initializeSearchRecyclerView(userList: ArrayList<User>, isFirstCreated: Boolean) {
        listSearchAdapter = ListUserAdapter(userList, this)

        if (isFirstCreated) rv_user_search.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_user_search.adapter = listSearchAdapter

        listSearchAdapter.setOnItemClickCallback(object : ListUserAdapter.OnItemClickCallback {
            override fun onItemClicked(position: Int, user: User, sharedElement: View) {
                goToDetailActivity(user, sharedElement)
            }

            override fun onItemAddToFavorites(position: Int, user: User) {
                addToFavorites(user)
            }
        })
    }

    private fun initializeViewModel() {
        userLisViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(UserListViewModel::class.java)
        userLisViewModel.setAppDatabase(appDatabase)

        if (userLisViewModel.getCurrentPage().value == null) userLisViewModel.loadCurrentPage()
        userLisViewModel.getCurrentPage().observe(this, Observer { newListUser ->
            initializeListRecyclerView(newListUser, false)

            container_search.visibility = View.GONE
            container_error.visibility = View.GONE
            container_loading.visibility = View.GONE
            container_list.visibility = View.VISIBLE

            this.listUser = newListUser
        })

        if (userLisViewModel.getTotalPagesCount().value == null) userLisViewModel.requestTotalPagesCount()
        userLisViewModel.getTotalPagesCount().observe(this, Observer { newTotalPagesCount ->
            totalPagesCount = newTotalPagesCount
            tv_page.text =
                resources.getString(
                    R.string.format_page,
                    userLisViewModel.getCurrentPageIndex().value,
                    newTotalPagesCount
                )
        })

        userLisViewModel.getCurrentPageIndex().observe(this, Observer { newCurrentPageIndex ->
            tv_page.text =
                resources.getString(R.string.format_page, newCurrentPageIndex, totalPagesCount)

            if (newCurrentPageIndex > 1) btn_previous_page.visibility = View.VISIBLE
            else btn_previous_page.visibility = View.GONE
        })

        userLisViewModel.isNextPageAvailable().observe(this, Observer { isNextPageAvailable ->
            this.isNextPageAvailable = isNextPageAvailable

            if (isNextPageAvailable && isNextPageRequested) {
                userLisViewModel.loadNextPage()
                isNextPageRequested = false
            }
        })

        userLisViewModel.isPreviousPageAvailable()
            .observe(this, Observer { isPreviousPageAvailable ->
                this.isPreviousPageAvailable = isPreviousPageAvailable

                if (isPreviousPageAvailable && isPreviousPageRequested) {
                    userLisViewModel.loadPreviousPage()
                    isPreviousPageRequested = false
                }
            })

        userLisViewModel.getSearchPage().observe(this, Observer { newSearchPage ->
            initializeSearchRecyclerView(newSearchPage, false)
            setSearch()
        })

        userLisViewModel.getSearchQuery().observe(this, Observer { newSearchQuery ->
            searchQuery = newSearchQuery
            tv_search_query.text = resources.getString(R.string.format_search_query, newSearchQuery)
        })

        userLisViewModel.getSearchCount().observe(this, Observer { newSearchCount ->
            tv_search_count.text = resources.getQuantityString(
                R.plurals.format_search_count,
                newSearchCount,
                newSearchCount
            )
        })

        userLisViewModel.isLoading().observe(this, Observer { isLoading ->
            this.isLoading = isLoading
            if (isLoading) setLoading()
        })

        userLisViewModel.isError().observe(this, Observer { isError ->
            this.isError = isError
            if (isError) setError()
        })

        userLisViewModel.isSearching().observe(this, Observer { isSearching ->
            this.isSearching = isSearching
            if (isSearching) setSearch()
            else {
                container_search.visibility = View.GONE
                container_error.visibility = View.GONE
                container_loading.visibility = View.GONE
                container_title.visibility = View.VISIBLE
                container_list.visibility = View.VISIBLE
            }
        })
    }

    private fun setLoading() {
        container_list.visibility = View.GONE
        container_search.visibility = View.GONE
        container_error.visibility = View.GONE
        container_loading.visibility = View.VISIBLE
    }

    private fun setError() {
        container_list.visibility = View.GONE
        container_search.visibility = View.GONE
        container_loading.visibility = View.GONE
        container_error.visibility = View.VISIBLE
    }

    private fun setSearch() {
        container_title.visibility = View.GONE
        container_list.visibility = View.GONE
        container_error.visibility = View.GONE
        container_loading.visibility = View.GONE
        container_search.visibility = View.VISIBLE
    }

    private fun goToDetailActivity(user: User, sharedElement: View) {
        val intentDetailActivity = Intent(this@UserListActivity, UserDetailActivity::class.java)
        intentDetailActivity.putExtra(UserDetailActivity.EXTRA_USER_DATA, user)

        val sharedElementTransition = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this@UserListActivity,
            sharedElement,
            user.username
        )

        startActivity(intentDetailActivity, sharedElementTransition.toBundle())
    }

    private fun addToFavorites(user: User) {
        GlobalScope.launch {
            if (user.isFavorite) appDatabase.favoritesDao().insert(user)
            else appDatabase.favoritesDao().delete(user)
        }
    }

    override fun onExitDialogActionChosen(exit: Boolean) {
        if (exit) finish()
    }
}
