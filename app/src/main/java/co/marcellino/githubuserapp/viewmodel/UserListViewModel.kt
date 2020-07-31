package co.marcellino.githubuserapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.marcellino.githubuserapp.model.PageData
import co.marcellino.githubuserapp.model.User
import co.marcellino.githubuserapp.utils.NetworkManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import kotlin.math.min

class UserListViewModel : ViewModel() {

    private val tag = "GithubUserApp"
    private val userPerPage = 10

    private val isError = MutableLiveData<Boolean>()
    fun isError(): LiveData<Boolean> = isError
    private val isLoading = MutableLiveData<Boolean>()
    fun isLoading(): LiveData<Boolean> = isLoading
    private val isSearching = MutableLiveData<Boolean>()
    fun isSearching(): LiveData<Boolean> = isSearching

    private val totalPagesCount = MutableLiveData<Int>()
    fun getTotalPagesCount(): LiveData<Int> = totalPagesCount
    fun requestTotalPagesCount() {
        isLoading.postValue(true)
        val totalUsersRequest = object : JsonObjectRequest(
            Method.GET,
            "${NetworkManager.BASE_URL}/search/users?q=type%3Auser&per_page=1",
            null,
            Response.Listener { totalUsersObject ->
                try {
                    val tempTotalPagesCount = totalUsersObject.getInt("total_count") / userPerPage
                    totalPagesCount.postValue(tempTotalPagesCount)

                    isError.postValue(false)
                    isLoading.postValue(false)
                } catch (e: Exception) {
                    Log.d(tag, "Parse error - TotalPagesCount: ${e.message}")
                    isError.postValue(true)
                    isLoading.postValue(false)
                }
            },
            Response.ErrorListener {
                Log.d(tag, "Volley error - TotalPagesCount: ${it.message}")
                isError.postValue(true)
                isLoading.postValue(false)
            }) {
            override fun getHeaders() = NetworkManager.headers
        }
        NetworkManager.getInstance(null).addToRequestQueue(totalUsersRequest)
    }

    private val liveCurrentPageIndex = MutableLiveData<Int>().also { it.postValue(1) }
    private val liveCurrentPage = MutableLiveData<ArrayList<User>>()
    private val liveIsNextPageAvailable = MutableLiveData<Boolean>().also { it.postValue(false) }
    private val liveIsPreviousPageAvailable =
        MutableLiveData<Boolean>().also { it.postValue(false) }
    private var currentPageIndex = 1
    private var currentPage = ArrayList<User>()
    private var previousPage = ArrayList<User>()
    private var nextPage = ArrayList<User>()
    private val pagesData = ArrayList<PageData>()
    fun getCurrentPageIndex(): LiveData<Int> = liveCurrentPageIndex
    fun getCurrentPage(): LiveData<ArrayList<User>> = liveCurrentPage
    fun isNextPageAvailable(): LiveData<Boolean> = liveIsNextPageAvailable
    fun isPreviousPageAvailable(): LiveData<Boolean> = liveIsPreviousPageAvailable
    fun loadCurrentPage() {
        isLoading.postValue(true)
        val currentPageRequest = object : JsonArrayRequest(
            Method.GET,
            "${NetworkManager.BASE_URL}/users?per_page=$userPerPage",
            null,
            Response.Listener { currentPageArray ->
                try {
                    val tempCurrentPage = arrayListOf<User>()
                    val tempPageData = PageData()

                    for (i in 0 until userPerPage) {
                        val userObject = currentPageArray.getJSONObject(i)
                        val tempUser = User()

                        tempUser.id = userObject.getInt("id")
                        if (i == 0) tempPageData.startId = tempUser.id
                        else if (i == userPerPage - 1) tempPageData.endId = tempUser.id

                        tempUser.username = userObject.getString("login")
                        tempUser.avatar = userObject.getString("avatar_url")
                        val userDetailsRequest = object : JsonObjectRequest(
                            Method.GET,
                            "${NetworkManager.BASE_URL}/users/${tempUser.username}",
                            null,
                            Response.Listener { userDetailsObject ->
                                tempUser.name = userDetailsObject.getString("name")
                                tempUser.company = userDetailsObject.getString("company")
                                tempUser.location = userDetailsObject.getString("location")
                                tempUser.repository = userDetailsObject.getInt("public_repos")
                                tempUser.follower = userDetailsObject.getInt("followers")
                                tempUser.following = userDetailsObject.getInt("following")

                                tempCurrentPage.add(tempUser)

                                if (i == userPerPage - 1) {
                                    tempCurrentPage.sortBy { it.id }
                                    if (!pagesData.contains(tempPageData))
                                        pagesData.add(tempPageData)

                                    currentPage = tempCurrentPage
                                    liveCurrentPage.postValue(tempCurrentPage)

                                    isError.postValue(false)
                                    isLoading.postValue(false)

                                    requestNextPage()
                                }
                            },
                            Response.ErrorListener {
                                Log.d(tag, "Volley error - CurrentUserDetails: ${it.message}")
                                isError.postValue(true)
                                isLoading.postValue(false)
                            }) {
                            override fun getHeaders() = NetworkManager.headers
                        }.also { it.tag = NetworkManager.TAG_CURRENT_PAGE }
                        NetworkManager.getInstance(null).addToRequestQueue(userDetailsRequest)
                    }
                } catch (e: Exception) {
                    Log.d(tag, "Parse error - CurrentUser: ${e.message}")
                    isError.postValue(true)
                    isLoading.postValue(false)
                }
            },
            Response.ErrorListener {
                Log.d(tag, "Volley error - CurrentPage: ${it.message}")
                isError.postValue(true)
                isLoading.postValue(false)
            }) {
            override fun getHeaders() = NetworkManager.headers
        }.also { it.tag = NetworkManager.TAG_CURRENT_PAGE }
        NetworkManager.getInstance(null).addToRequestQueue(currentPageRequest)
    }

    fun loadNextPage() {
        if (nextPage.isNullOrEmpty()) {
            requestNextPage()
        } else {
            liveCurrentPageIndex.postValue(++currentPageIndex)
            liveCurrentPage.postValue(nextPage)

            previousPage = currentPage
            currentPage = nextPage
            nextPage = arrayListOf()

            liveIsPreviousPageAvailable.postValue(true)

            requestNextPage()
        }
    }

    private fun requestNextPage() {
        liveIsNextPageAvailable.postValue(false)
        val nextPageRequest = object : JsonArrayRequest(
            Method.GET,
            "${NetworkManager.BASE_URL}/users?per_page=$userPerPage&since=${pagesData[currentPageIndex - 1].endId}",
            null,
            Response.Listener { nextPageArray ->
                try {
                    val tempNextPage = arrayListOf<User>()
                    val tempPageData = PageData()

                    for (i in 0 until userPerPage) {
                        val userObject = nextPageArray.getJSONObject(i)
                        val tempUser = User()

                        tempUser.id = userObject.getInt("id")
                        if (i == 0) tempPageData.startId = tempUser.id
                        else if (i == userPerPage - 1) tempPageData.endId = tempUser.id

                        tempUser.username = userObject.getString("login")
                        tempUser.avatar = userObject.getString("avatar_url")
                        val userDetailsRequest = object : JsonObjectRequest(
                            Method.GET,
                            "${NetworkManager.BASE_URL}/users/${tempUser.username}",
                            null,
                            Response.Listener { userDetailsObject ->
                                tempUser.name = userDetailsObject.getString("name")
                                tempUser.company = userDetailsObject.getString("company")
                                tempUser.location = userDetailsObject.getString("location")
                                tempUser.repository = userDetailsObject.getInt("public_repos")
                                tempUser.follower = userDetailsObject.getInt("followers")
                                tempUser.following = userDetailsObject.getInt("following")

                                tempNextPage.add(tempUser)

                                if (i == userPerPage - 1) {
                                    tempNextPage.sortBy { it.id }
                                    if (!pagesData.contains(tempPageData))
                                        pagesData.add(tempPageData)

                                    nextPage = tempNextPage
                                    liveIsNextPageAvailable.postValue(true)
                                }
                            },
                            Response.ErrorListener {
                                Log.d(tag, "Volley error - NextUserDetails: ${it.message}")
                                isError.postValue(true)
                                isLoading.postValue(false)
                            }) {
                            override fun getHeaders() = NetworkManager.headers
                        }.also { it.tag = NetworkManager.TAG_CURRENT_PAGE }
                        NetworkManager.getInstance(null).addToRequestQueue(userDetailsRequest)
                    }
                } catch (e: Exception) {
                    Log.d(tag, "Parse error - NextUser: ${e.message}")
                    isError.postValue(true)
                    isLoading.postValue(false)
                }
            },
            Response.ErrorListener {
                Log.d(tag, "Volley error - NextPage: ${it.message}")
                isError.postValue(true)
                isLoading.postValue(false)
            }
        ) {
            override fun getHeaders() = NetworkManager.headers
        }.also { it.tag = NetworkManager.TAG_NEXT_PAGE }
        NetworkManager.getInstance(null).addToRequestQueue(nextPageRequest)
    }

    fun loadPreviousPage() {
        Log.d(tag, "INDEX: $currentPageIndex")

        if (previousPage.isNullOrEmpty()) {
            requestPreviousPage()
        } else {
            liveCurrentPageIndex.postValue(--currentPageIndex)
            liveCurrentPage.postValue(previousPage)

            nextPage = currentPage
            currentPage = previousPage
            previousPage = arrayListOf()

            liveIsNextPageAvailable.postValue(true)

            requestPreviousPage()
        }
    }

    private fun requestPreviousPage() {
        if (currentPageIndex <= 1) return

        liveIsPreviousPageAvailable.postValue(false)
        val previousPageRequest = object : JsonArrayRequest(
            Method.GET,
            "${NetworkManager.BASE_URL}/users?per_page=$userPerPage&since=${pagesData[currentPageIndex - 2].startId - 1}",
            null,
            Response.Listener { previousPageArray ->
                try {
                    val tempPreviousPage = arrayListOf<User>()

                    for (i in 0 until userPerPage) {
                        val userObject = previousPageArray.getJSONObject(i)
                        val tempUser = User()

                        tempUser.id = userObject.getInt("id")
                        tempUser.username = userObject.getString("login")
                        tempUser.avatar = userObject.getString("avatar_url")
                        val userDetailsRequest = object : JsonObjectRequest(
                            Method.GET,
                            "${NetworkManager.BASE_URL}/users/${tempUser.username}",
                            null,
                            Response.Listener { userDetailsObject ->
                                tempUser.name = userDetailsObject.getString("name")
                                tempUser.company = userDetailsObject.getString("company")
                                tempUser.location = userDetailsObject.getString("location")
                                tempUser.repository = userDetailsObject.getInt("public_repos")
                                tempUser.follower = userDetailsObject.getInt("followers")
                                tempUser.following = userDetailsObject.getInt("following")

                                tempPreviousPage.add(tempUser)

                                if (i == userPerPage - 1) {
                                    tempPreviousPage.sortBy { it.id }
                                    previousPage = tempPreviousPage

                                    liveIsPreviousPageAvailable.postValue(true)
                                }
                            },
                            Response.ErrorListener {
                                Log.d(tag, "Volley error - PreviousUserDetails: ${it.message}")
                                isError.postValue(true)
                                isLoading.postValue(false)
                            }) {
                            override fun getHeaders() = NetworkManager.headers
                        }.also { it.tag = NetworkManager.TAG_PREVIOUS_PAGE }
                        NetworkManager.getInstance(null).addToRequestQueue(userDetailsRequest)
                    }
                } catch (e: Exception) {
                    Log.d(tag, "Parse error - PreviousUser: ${e.message}")
                    isError.postValue(true)
                    isLoading.postValue(false)
                }
            },
            Response.ErrorListener {
                Log.d(tag, "Volley error - PreviousUser: ${it.message}")
                isError.postValue(true)
                isLoading.postValue(false)
            }
        ) {
            override fun getHeaders() = NetworkManager.headers
        }.also { it.tag = NetworkManager.TAG_PREVIOUS_PAGE }
        NetworkManager.getInstance(null).addToRequestQueue(previousPageRequest)
    }

    private val liveSearchQuery = MutableLiveData<String>()
    private val liveSearchCount = MutableLiveData<Int>()
    private val liveSearchPage = MutableLiveData<ArrayList<User>>()
    fun getSearchQuery(): LiveData<String> = liveSearchQuery
    fun getSearchCount(): LiveData<Int> = liveSearchCount
    fun getSearchPage(): LiveData<ArrayList<User>> = liveSearchPage
    fun loadSearchPage(query: String) {
        liveSearchQuery.postValue(query)
        isSearching.postValue(true)
        isLoading.postValue(true)

        NetworkManager.getInstance(null).cancelRequestWithTag(NetworkManager.TAG_SEARCH)
        val searchObjectRequest = object : JsonObjectRequest(Method.GET,
            "${NetworkManager.BASE_URL}/search/users?q=$query",
            null,
            Response.Listener { searchPageObject ->
                try {
                    val tempSearchCount = searchPageObject.getInt("total_count")
                    liveSearchCount.postValue(tempSearchCount)
                    if (tempSearchCount == 0) {
                        liveSearchPage.postValue(arrayListOf())
                        return@Listener
                    }

                    val tempSearchPage = arrayListOf<User>()
                    val searchUsersArray = searchPageObject.getJSONArray("items")
                    for (i in 0 until min(userPerPage, searchUsersArray.length())) {
                        val userObject = searchUsersArray.getJSONObject(i)
                        val tempUser = User()

                        tempUser.id = userObject.getInt("id")
                        tempUser.username = userObject.getString("login")
                        tempUser.avatar = userObject.getString("avatar_url")
                        val userDetailsRequest = object : JsonObjectRequest(
                            Method.GET,
                            "${NetworkManager.BASE_URL}/users/${tempUser.username}",
                            null,
                            Response.Listener { userDetailsObject ->
                                tempUser.name = userDetailsObject.getString("name")
                                tempUser.company = userDetailsObject.getString("company")
                                tempUser.location = userDetailsObject.getString("location")
                                tempUser.repository = userDetailsObject.getInt("public_repos")
                                tempUser.follower = userDetailsObject.getInt("followers")
                                tempUser.following = userDetailsObject.getInt("following")

                                tempSearchPage.add(tempUser)

                                if (i == (min(userPerPage, searchUsersArray.length()) - 1)) {
                                    tempSearchPage.sortBy { it.id }
                                    liveSearchPage.postValue(tempSearchPage)

                                    isError.postValue(false)
                                    isLoading.postValue(false)
                                }
                            },
                            Response.ErrorListener {
                                Log.d(tag, "Volley error - SearchUserDetails: ${it.message}")
                                isError.postValue(true)
                                isLoading.postValue(false)
                            }) {
                            override fun getHeaders() = NetworkManager.headers
                        }.also { it.tag = NetworkManager.TAG_SEARCH }
                        NetworkManager.getInstance(null).addToRequestQueue(userDetailsRequest)
                    }
                } catch (e: Exception) {
                    Log.d(tag, "Parse error - SearchPage: ${e.message}")
                    isError.postValue(true)
                    isLoading.postValue(false)
                }
            },
            Response.ErrorListener {
                Log.d(tag, "Volley error - SearchPage: ${it.message}")
                isError.postValue(true)
                isLoading.postValue(false)
            }) {
            override fun getHeaders() = NetworkManager.headers
        }.also { it.tag = NetworkManager.TAG_SEARCH }
        NetworkManager.getInstance(null).addToRequestQueue(searchObjectRequest)
    }

    fun cancelSearchPage() {
        isSearching.postValue(false)
        NetworkManager.getInstance(null).cancelRequestWithTag(NetworkManager.TAG_SEARCH)
    }
}