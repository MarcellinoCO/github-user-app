package co.marcellino.githubuserapp.utils

import android.content.Context
import android.util.Log
import co.marcellino.githubuserapp.model.PageData
import co.marcellino.githubuserapp.model.User
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

object GithubDataManager {

    private const val TAG = "GithubDataManager"
    private const val TAG_REQUEST_SEARCH = "request_search"

    private const val BASE_URL = "https://api.github.com"
    private const val AUTH_TOKEN = "token 6336005359551b982c81ab522bda968e4fb79be8"

    private const val KEY_TOTAL_COUNT = "total_count"
    private const val KEY_ID = "id"
    private const val KEY_USERNAME = "login"
    private const val KEY_AVATAR = "avatar_url"
    private const val KEY_NAME = "name"
    private const val KEY_COMPANY = "company"
    private const val KEY_LOCATION = "location"
    private const val KEY_REPOSITORY = "public_repos"
    private const val KEY_FOLLOWER = "followers"
    private const val KEY_FOLLOWING = "following"
    private const val KEY_SEARCH_ITEMS = "items"

    private lateinit var requestQueue: RequestQueue

    private lateinit var listener: GithubDataListener

    const val userNumPerPage = 10
    private var currentPageIndex = 0
    private var currentPage: ArrayList<User> = arrayListOf()
    private var previousPage: ArrayList<User> = arrayListOf()
    private var nextPage: ArrayList<User> = arrayListOf()
    private var pagesData: ArrayList<PageData> = arrayListOf()

    private var totalUsersCount: Int = -1

    fun setContext(context: Context): GithubDataManager {
        requestQueue = Volley.newRequestQueue(context)
        return this
    }

    fun setListener(listener: GithubDataListener): GithubDataManager {
        this.listener = listener
        return this
    }

    fun reset(): GithubDataManager {
        currentPageIndex = 0
        currentPage = arrayListOf()
        previousPage = arrayListOf()
        nextPage = arrayListOf()
        pagesData = arrayListOf()
        return this
    }

    //Only called when the app is first created.
    fun getCurrentPage() {
        if (currentPage.isNullOrEmpty()) {
            val currentPageRequest = object : JsonArrayRequest(
                Method.GET,
                "$BASE_URL/users?per_page=$userNumPerPage",
                null,
                Response.Listener { usersListArray ->
                    currentPage = arrayListOf()
                    val currentPageData = PageData()

                    for (i in 0 until userNumPerPage) {
                        val userObject = usersListArray.getJSONObject(i)
                        val user = User()

                        try {
                            user.id = userObject.getInt(KEY_ID)
                            if (i == 0) currentPageData.startId = user.id
                            else if (i == userNumPerPage - 1) currentPageData.endId = user.id

                            user.username = userObject.getString(KEY_USERNAME)
                            user.avatar = userObject.getString(KEY_AVATAR)
                        } catch (e: Exception) {
                            Log.d(TAG, "Parse Error - User ${user.username}: ${e.message}")
                            listener.onFailure()
                        }

                        val userDetailsRequest = object : JsonObjectRequest(
                            Method.GET,
                            "$BASE_URL/users/${user.username}",
                            null,
                            Response.Listener { userDetailsObject ->
                                try {
                                    user.name = userDetailsObject.getString(KEY_NAME)
                                    user.company = userDetailsObject.getString(KEY_COMPANY)
                                    user.location = userDetailsObject.getString(KEY_LOCATION)
                                    user.repository = userDetailsObject.getInt(KEY_REPOSITORY)
                                    user.follower = userDetailsObject.getInt(KEY_FOLLOWER)
                                    user.following = userDetailsObject.getInt(KEY_FOLLOWING)
                                } catch (e: Exception) {
                                    Log.d(
                                        TAG,
                                        "Parse Error - UserDetails ${user.username}: ${e.message}"
                                    )
                                    listener.onFailure()
                                }
                                currentPage.add(user)

                                if (i == userNumPerPage - 1) {
                                    currentPage.sortBy { it.id }
                                    pagesData.add(currentPageData)

                                    listener.onPageSuccess(currentPage)
                                    loadNextPage()

                                    Log.d("HAI", "CURRENT")
                                    Log.d("HAI", "- Previous: $previousPage")
                                    Log.d("HAI", "- Current : $currentPage")
                                    Log.d("HAI", "- Next    : $nextPage")
                                }
                            }, Response.ErrorListener { userDetailsVolleyError ->
                                Log.d(
                                    TAG,
                                    "Volley Error - UserDetailsRequest: ${userDetailsVolleyError.message}"
                                )
                                listener.onFailure()
                            }) {
                            override fun getHeaders(): MutableMap<String, String> =
                                this@GithubDataManager.getHeaders()
                        }
                        requestQueue.add(userDetailsRequest)
                    }
                },
                Response.ErrorListener { currentPageVolleyError ->
                    Log.d(
                        TAG,
                        "Volley Error - CurrentPageRequest: ${currentPageVolleyError.message}"
                    )
                    listener.onFailure()
                }) {
                override fun getHeaders(): MutableMap<String, String> =
                    this@GithubDataManager.getHeaders()
            }
            requestQueue.add(currentPageRequest)
        } else listener.onPageSuccess(currentPage)
    }

    fun getNextPage() {
        Log.d("HAI", "NEXT")
        Log.d("HAI", "- Previous: $previousPage")
        Log.d("HAI", "- Current : $currentPage")
        Log.d("HAI", "- Next    : $nextPage")
        listener.onPageSuccess(nextPage)

        previousPage = currentPage
        currentPage = nextPage
        nextPage = arrayListOf()

        currentPageIndex++
        loadNextPage()

        listener.onPreviousPageAvailable(true)

        Log.d("HAI", "- Previous: $previousPage")
        Log.d("HAI", "- Current : $currentPage")
        Log.d("HAI", "- Next    : $nextPage")
    }

    private fun loadNextPage() {
        listener.onNextPageAvailable(false)

        val nextPageRequest = object : JsonArrayRequest(
            Method.GET,
            "$BASE_URL/users?per_page=$userNumPerPage&since=${pagesData[currentPageIndex].endId}",
            null,
            Response.Listener { usersListArray ->
                nextPage = arrayListOf()
                val nextPageData = PageData()

                for (i in 0 until userNumPerPage) {
                    val userObject = usersListArray.getJSONObject(i)
                    val user = User()

                    try {
                        user.id = userObject.getInt(KEY_ID)
                        if (i == 0) nextPageData.startId = user.id
                        else if (i == userNumPerPage - 1) nextPageData.endId = user.id

                        user.username = userObject.getString(KEY_USERNAME)
                        user.avatar = userObject.getString(KEY_AVATAR)
                    } catch (e: Exception) {
                        Log.d(TAG, "Parse Error - NextUser ${user.username}: ${e.message}")
                    }

                    val userDetailsRequest = object : JsonObjectRequest(
                        Method.GET,
                        "$BASE_URL/users/${user.username}",
                        null,
                        Response.Listener { userDetailsObject ->
                            try {
                                user.name = userDetailsObject.getString(KEY_NAME)
                                user.company = userDetailsObject.getString(KEY_COMPANY)
                                user.location = userDetailsObject.getString(KEY_LOCATION)
                                user.repository = userDetailsObject.getInt(KEY_REPOSITORY)
                                user.follower = userDetailsObject.getInt(KEY_FOLLOWER)
                                user.following = userDetailsObject.getInt(KEY_FOLLOWING)
                            } catch (e: Exception) {
                                Log.d(
                                    TAG,
                                    "Parse Error - NextUserDetails ${user.username}: ${e.message}"
                                )
                            }

                            nextPage.add(user)

                            if (i == userNumPerPage - 1) {
                                nextPage.sortBy { it.id }
                                pagesData.add(nextPageData)

                                listener.onNextPageAvailable(true)
                            }
                        }, Response.ErrorListener { userDetailsVolleyError ->
                            Log.d(
                                TAG,
                                "Volley Error - NextUserDetailsRequest: ${userDetailsVolleyError.message}"
                            )
                        }) {
                        override fun getHeaders(): MutableMap<String, String> =
                            this@GithubDataManager.getHeaders()
                    }
                    requestQueue.add(userDetailsRequest)
                }
            },
            Response.ErrorListener { nextPageVolleyError ->
                Log.d(
                    TAG,
                    "Volley Error - NextPageRequest: ${nextPageVolleyError.message}"
                )
            }) {
            override fun getHeaders(): MutableMap<String, String> =
                this@GithubDataManager.getHeaders()
        }
        requestQueue.add(nextPageRequest)
    }

    fun getPreviousPage() {
        Log.d("HAI", "PREVIOUS")
        Log.d("HAI", "- Previous: $previousPage")
        Log.d("HAI", "- Current : $currentPage")
        Log.d("HAI", "- Next    : $nextPage")

        listener.onPageSuccess(previousPage)

        nextPage = currentPage
        currentPage = previousPage
        previousPage = arrayListOf()

        currentPageIndex--
        if (currentPageIndex > 0) loadPreviousPage()

        listener.onNextPageAvailable(true)

        Log.d("HAI", "- Previous: $previousPage")
        Log.d("HAI", "- Current : $currentPage")
        Log.d("HAI", "- Next    : $nextPage")
    }

    private fun loadPreviousPage() {
        listener.onPreviousPageAvailable(false)

        val previousPageRequest = object : JsonArrayRequest(
            Method.GET,
            "$BASE_URL/users?per_page=$userNumPerPage&since=${pagesData[currentPageIndex - 1].startId - 1}",
            null,
            Response.Listener { usersListArray ->
                previousPage = arrayListOf()

                for (i in 0 until userNumPerPage) {
                    val userObject = usersListArray.getJSONObject(i)
                    val user = User()

                    try {
                        user.id = userObject.getInt(KEY_ID)
                        user.username = userObject.getString(KEY_USERNAME)
                        user.avatar = userObject.getString(KEY_AVATAR)
                    } catch (e: Exception) {
                        Log.d(TAG, "Parse Error - PreviousUser ${user.username}: ${e.message}")
                    }

                    val userDetailsRequest = object : JsonObjectRequest(
                        Method.GET,
                        "$BASE_URL/users/${user.username}",
                        null,
                        Response.Listener { userDetailsObject ->
                            try {
                                user.name = userDetailsObject.getString(KEY_NAME)
                                user.company = userDetailsObject.getString(KEY_COMPANY)
                                user.location = userDetailsObject.getString(KEY_LOCATION)
                                user.repository = userDetailsObject.getInt(KEY_REPOSITORY)
                                user.follower = userDetailsObject.getInt(KEY_FOLLOWER)
                                user.following = userDetailsObject.getInt(KEY_FOLLOWING)
                            } catch (e: Exception) {
                                Log.d(
                                    TAG,
                                    "Parse Error -  PreviousUserDetails ${user.username}: ${e.message}"
                                )
                            }

                            previousPage.add(user)

                            if (i == userNumPerPage - 1) {
                                previousPage.sortBy { it.id }
                                listener.onPreviousPageAvailable(true)
                            }
                        }, Response.ErrorListener { userDetailsVolleyError ->
                            Log.d(
                                TAG,
                                "Volley Error - PreviousUserDetailsRequest: ${userDetailsVolleyError.message}"
                            )
                        }) {
                        override fun getHeaders(): MutableMap<String, String> =
                            this@GithubDataManager.getHeaders()
                    }
                    requestQueue.add(userDetailsRequest)
                }
            },
            Response.ErrorListener { previousPageVolleyError ->
                Log.d(
                    TAG,
                    "Volley Error - PreviousPageRequest: ${previousPageVolleyError.message}"
                )
            }) {
            override fun getHeaders(): MutableMap<String, String> =
                this@GithubDataManager.getHeaders()
        }
        requestQueue.add(previousPageRequest)
    }

    fun getTotalUsersCount(): GithubDataManager {
        if (totalUsersCount != -1) {
            listener.onTotalUsersCountSuccess(totalUsersCount)
            return this
        }

        val objectRequest = object : JsonObjectRequest(
            Method.GET,
            "$BASE_URL/search/users?q=type%3Auser",
            null,
            Response.Listener { objectTotalUsersCount ->
                try {
                    totalUsersCount = objectTotalUsersCount.getInt(KEY_TOTAL_COUNT)
                    listener.onTotalUsersCountSuccess(totalUsersCount)
                } catch (e: Exception) {
                    Log.d(TAG, "Parse Error - TotalUsersCount: ${e.message}")
                    listener.onFailure()
                }
            }, Response.ErrorListener { errorTotalUserCount ->
                Log.d(TAG, "Volley Error - TotalUsersCount: ${errorTotalUserCount.message}")
                listener.onFailure()
            }) {
            override fun getHeaders(): MutableMap<String, String> =
                this@GithubDataManager.getHeaders()
        }
        requestQueue.add(objectRequest)

        return this
    }

    fun searchUsername(query: String) {
        cancelSearchUsername()

        val searchObjectRequest = object : JsonObjectRequest(
            Method.GET,
            "$BASE_URL/search/users?q=$query",
            null,
            Response.Listener { searchResultObject ->
                val totalResultsCount = searchResultObject.getInt(KEY_TOTAL_COUNT)
                val listUserSearch = arrayListOf<User>()

                val usersArray = searchResultObject.getJSONArray(KEY_SEARCH_ITEMS)
                if (usersArray.length() == 0) listener.onSearchSuccess(0, arrayListOf(), query)
                for (i in 0 until kotlin.math.min(usersArray.length(), userNumPerPage)) {
                    val userObject = usersArray.getJSONObject(i)
                    val user = User()

                    try {
                        user.id = userObject.getInt(KEY_ID)
                        user.username = userObject.getString(KEY_USERNAME)
                        user.avatar = userObject.getString(KEY_AVATAR)
                    } catch (e: Exception) {
                        Log.d(TAG, "Parse Error - SearchUser: ${e.message}")
                        listener.onSearchFailure()
                    }

                    val userDetailsRequest = object : JsonObjectRequest(
                        Method.GET,
                        "$BASE_URL/users/${user.username}",
                        null,
                        Response.Listener { userDetailsObject ->
                            try {
                                user.name = userDetailsObject.getString(KEY_NAME)
                                user.company = userDetailsObject.getString(KEY_COMPANY)
                                user.location = userDetailsObject.getString(KEY_LOCATION)
                                user.repository = userDetailsObject.getInt(KEY_REPOSITORY)
                                user.follower = userDetailsObject.getInt(KEY_FOLLOWER)
                                user.following = userDetailsObject.getInt(KEY_FOLLOWING)
                            } catch (e: Exception) {
                                Log.d(
                                    TAG,
                                    "Parse Error -  SearchUserDetails ${user.username}: ${e.message}"
                                )
                                listener.onSearchFailure()
                            }

                            listUserSearch.add(user)

                            if (i == kotlin.math.min(usersArray.length(), userNumPerPage) - 1) {
                                listUserSearch.sortBy { it.id }
                                listener.onSearchSuccess(totalResultsCount, listUserSearch, query)
                            }
                        }, Response.ErrorListener { userDetailsVolleyError ->
                            Log.d(
                                TAG,
                                "Volley Error - PreviousUserDetailsRequest: ${userDetailsVolleyError.message}"
                            )
                            listener.onSearchFailure()
                        }) {
                        override fun getHeaders(): MutableMap<String, String> =
                            this@GithubDataManager.getHeaders()
                    }
                    requestQueue.add(userDetailsRequest)
                }
            },
            Response.ErrorListener { }) {
            override fun getHeaders(): MutableMap<String, String> =
                this@GithubDataManager.getHeaders()
        }
        searchObjectRequest.tag = TAG_REQUEST_SEARCH
        requestQueue.add(searchObjectRequest)
    }

    fun cancelSearchUsername() {
        requestQueue.cancelAll(TAG_REQUEST_SEARCH)
    }

    private fun getHeaders(): MutableMap<String, String> {
        val headers = HashMap<String, String>()
        headers["Authorization"] = AUTH_TOKEN

        return headers
    }

    interface GithubDataListener {
        fun onFailure() {}

        fun onPageSuccess(usersList: ArrayList<User>) {}

        fun onNextPageAvailable(isAvailable: Boolean) {}
        fun onPreviousPageAvailable(isAvailable: Boolean) {}

        fun onTotalUsersCountSuccess(totalUsersCount: Int) {}

        fun onSearchSuccess(
            totalResultsCount: Int,
            listUserSearch: ArrayList<User>,
            query: String
        ) {
        }

        fun onSearchFailure() {}
    }
}