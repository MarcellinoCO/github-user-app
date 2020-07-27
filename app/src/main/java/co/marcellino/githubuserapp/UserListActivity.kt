package co.marcellino.githubuserapp

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import co.marcellino.githubuserapp.adapter.ListUserAdapter
import co.marcellino.githubuserapp.model.User
import co.marcellino.githubuserapp.utils.GithubDataManager
import kotlinx.android.synthetic.main.activity_user_list.*

class UserListActivity : AppCompatActivity(), ExitDialogFragment.OnExitDialogListener,
    View.OnClickListener, GithubDataManager.GithubDataListener {

    private lateinit var listUserAdapter: ListUserAdapter
    private lateinit var listSearchAdapter: ListUserAdapter

    private var listUser: ArrayList<User> = arrayListOf()
    private var currentPageIndex: Int = 0
    private var totalUsersCount: Int = 0

    private var isLoading = true
    private var isLoadingError = false

    private var isLoadingNextPage = false
    private var isLoadingPreviousPage = false
    private var isNextPageAvailable = false
    private var isPreviousPageAvailable = false

    private var listSearch: ArrayList<User> = arrayListOf()

    private var isSearchError = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        initializeAppBar()

        initializeListRecyclerView()
        initializeSearchRecyclerView()
        GithubDataManager.setContext(applicationContext).setListener(this).getTotalUsersCount()
            .getCurrentPage()

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
                if (!newText.isNullOrBlank()) {
                    switchLoading(true, isSuccess = true, isDoneSearching = false)
                    GithubDataManager.searchUsername(newText.trim())
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    switchLoading(true, isSuccess = true, isDoneSearching = false)
                    GithubDataManager.searchUsername(query.trim())
                }
                return true
            }
        })
        searchView.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                switchSearch(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                switchSearch(false)
                GithubDataManager.cancelSearchUsername()
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        ExitDialogFragment().show(supportFragmentManager, ExitDialogFragment::class.java.simpleName)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_previous_page -> {
                if (currentPageIndex == 0) return

                currentPageIndex--

                isLoadingPreviousPage = true
                if (!isPreviousPageAvailable) {
                    switchLoading(true, isSuccess = true, isDoneSearching = false)
                } else {
                    GithubDataManager.getPreviousPage()
                    isLoadingPreviousPage = false
                }

                if (currentPageIndex == 0) btn_previous_page.visibility = View.GONE
            }
            R.id.btn_next_page -> {
                currentPageIndex++

                isLoadingNextPage = true
                if (!isNextPageAvailable) {
                    switchLoading(true, isSuccess = true, isDoneSearching = false)
                } else {
                    GithubDataManager.getNextPage()
                    isLoadingNextPage = false
                }

                if (currentPageIndex > 0) btn_previous_page.visibility = View.VISIBLE
            }
            R.id.btn_error_retry -> {
                currentPageIndex = 0

                switchLoading(true, isSuccess = true, isDoneSearching = false)
                isLoadingNextPage = false
                isLoadingPreviousPage = false
                isNextPageAvailable = false
                isPreviousPageAvailable = false

                GithubDataManager.reset().getCurrentPage()
            }
            R.id.btn_list_to_top -> {
                sv_list.smoothScrollTo(0, 0, 1000)
            }
        }
    }

    private fun initializeAppBar() {
        setSupportActionBar(appbar_user_list)
        appbar_user_list.setOnMenuItemClickListener {
            true
        }
    }

    private fun initializeListRecyclerView() {
        listUserAdapter = ListUserAdapter(listUser, this)

        rv_user_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_user_list.adapter = listUserAdapter

        listUserAdapter.setOnItemClickCallback(object : ListUserAdapter.OnItemClickCallback {
            override fun onItemClicked(position: Int, user: User, sharedElement: View) {
                goToDetailActivity(user, sharedElement)
            }
        })
    }

    private fun updateListRecyclerView() {
        listUserAdapter = ListUserAdapter(listUser, this)
        rv_user_list.adapter = listUserAdapter

        listUserAdapter.setOnItemClickCallback(object : ListUserAdapter.OnItemClickCallback {
            override fun onItemClicked(position: Int, user: User, sharedElement: View) {
                goToDetailActivity(user, sharedElement)
            }
        })
    }

    private fun initializeSearchRecyclerView() {
        listSearchAdapter = ListUserAdapter(listSearch, this)

        rv_user_search.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_user_search.adapter = listSearchAdapter

        listSearchAdapter.setOnItemClickCallback(object : ListUserAdapter.OnItemClickCallback {
            override fun onItemClicked(position: Int, user: User, sharedElement: View) {
                goToDetailActivity(user, sharedElement)
            }
        })
    }

    private fun updateSearchRecyclerView() {
        listSearchAdapter = ListUserAdapter(listSearch, this)
        rv_user_search.adapter = listSearchAdapter

        listSearchAdapter.setOnItemClickCallback(object : ListUserAdapter.OnItemClickCallback {
            override fun onItemClicked(position: Int, user: User, sharedElement: View) {
                goToDetailActivity(user, sharedElement)
            }
        })
    }

    private fun switchLoading(isLoading: Boolean, isSuccess: Boolean, isDoneSearching: Boolean) {
        this.isLoading = isLoading

        container_loading.visibility = View.GONE
        container_list.visibility = View.GONE
        container_error.visibility = View.GONE
        container_search.visibility = View.GONE

        when {
            isLoading -> container_loading.visibility = View.VISIBLE
            isSuccess -> container_list.visibility = View.VISIBLE
            isDoneSearching -> container_search.visibility = View.VISIBLE
            else -> container_error.visibility = View.VISIBLE
        }
    }

    private fun switchSearch(isSearching: Boolean) {
        if (isSearching) {
            container_title.visibility = View.GONE
            container_list.visibility = View.GONE
            container_loading.visibility = View.GONE
            container_error.visibility = View.GONE
            container_search.visibility = View.VISIBLE
        } else {
            container_search.visibility = View.GONE
            container_title.visibility = View.VISIBLE
            when {
                isLoading -> container_loading.visibility = View.VISIBLE
                isLoadingError -> container_error.visibility = View.VISIBLE
                else -> container_list.visibility = View.VISIBLE
            }
        }
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

    override fun onExitDialogActionChosen(exit: Boolean) {
        if (exit) finish()
    }

    override fun onFailure() {
        isLoadingError = true
        switchLoading(false, isSuccess = false, isDoneSearching = false)
    }

    override fun onPageSuccess(usersList: ArrayList<User>) {
        isLoadingError = false

        listUser = usersList
        updateListRecyclerView()

        val totalPage = totalUsersCount / GithubDataManager.userNumPerPage
        tv_page.text = resources.getString(R.string.format_page, currentPageIndex + 1, totalPage)

        switchLoading(false, isSuccess = true, isDoneSearching = false)
    }

    override fun onTotalUsersCountSuccess(totalUsersCount: Int) {
        this.totalUsersCount = totalUsersCount

        val totalPage = totalUsersCount / GithubDataManager.userNumPerPage
        tv_page.text = resources.getString(R.string.format_page, currentPageIndex + 1, totalPage)
    }

    override fun onNextPageAvailable(isAvailable: Boolean) {
        isNextPageAvailable = isAvailable

        if (isAvailable && isLoadingNextPage) {
            GithubDataManager.getNextPage()

            isLoadingNextPage = false
        }
    }

    override fun onPreviousPageAvailable(isAvailable: Boolean) {
        isPreviousPageAvailable = isAvailable

        if (isAvailable && isLoadingPreviousPage) {
            GithubDataManager.getPreviousPage()

            isLoadingPreviousPage = false
        }
    }

    override fun onSearchSuccess(
        totalResultsCount: Int,
        listUserSearch: ArrayList<User>,
        query: String
    ) {
        isSearchError = false
        switchLoading(false, isSuccess = false, isDoneSearching = true)

        tv_search_query.text = resources.getString(R.string.format_search_query, query)
        tv_search_count.text = resources.getQuantityString(
            R.plurals.format_search_count,
            totalResultsCount,
            totalResultsCount
        )
        listSearch = listUserSearch

        updateSearchRecyclerView()
    }

    override fun onSearchFailure() {
        isSearchError = true
        switchLoading(false, isSuccess = false, isDoneSearching = false)
    }
}
