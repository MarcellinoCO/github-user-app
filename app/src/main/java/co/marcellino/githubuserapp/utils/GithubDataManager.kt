package co.marcellino.githubuserapp.utils

import android.content.Context
import android.util.Log
import co.marcellino.githubuserapp.model.User
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlin.math.abs

object GithubDataManager {

    private const val TAG = "GithubDataManager"

    private const val BASE_URL = "https://api.github.com"
    private const val AUTH_TOKEN = "token 6336005359551b982c81ab522bda968e4fb79be8"

    private const val KEY_TOTAL_USERS_COUNT = "total_count"
    private const val KEY_ID = "id"
    private const val KEY_USERNAME = "login"
    private const val KEY_AVATAR = "avatar_url"
    private const val KEY_NAME = "name"
    private const val KEY_COMPANY = "company"
    private const val KEY_LOCATION = "location"
    private const val KEY_REPOSITORY = "public_repos"
    private const val KEY_FOLLOWER = "followers"
    private const val KEY_FOLLOWING = "following"

    private lateinit var requestQueue: RequestQueue

    //userNumPerGroup must be within the range of 1..20, and must be a multiples of userNumPerPage.
    private const val userNumPerGroup = 20
    private var currentGroupIndex = 0
    private var currentGroup: ArrayList<User> = arrayListOf()
    private var previousGroup: ArrayList<User> = arrayListOf()
    private var nextGroup: ArrayList<User> = arrayListOf()

    private const val userNumPerPage = 10
    private var currentPage: ArrayList<User> = arrayListOf()

    private var totalUsersCount: Int = -1

    fun setContext(context: Context) {
        requestQueue = Volley.newRequestQueue(context)
    }

    fun getUsersListAtPage(pageIndex: Int, listener: GithubDataListener) {
        if (pageIndex < 0) {
            listener.onFailure()
            return
        }

        val requestedGroupIndex = pageIndex / (userNumPerGroup / userNumPerPage)
        if (currentGroup.isNullOrEmpty() || abs(requestedGroupIndex - currentGroupIndex) > 1) { //First time initiation.
            if (requestedGroupIndex >= 0) requestCurrentGroup(
                requestedGroupIndex,
                pageIndex,
                listener
            )
            if (requestedGroupIndex - 1 >= 0) requestPreviousGroup(requestedGroupIndex - 1)
            requestNextGroup(requestedGroupIndex + 1)
        } else if (requestedGroupIndex == currentGroupIndex) { //Same group but different page.
            Log.d(TAG, "Change Page Same Group")
            returnUsersListAtPage(pageIndex, listener)
        } else if (requestedGroupIndex == currentGroupIndex - 1) { //Previous group.
            Log.d(TAG, "Change Page Previous Group")
            nextGroup = currentGroup
            currentGroup = previousGroup
            requestPreviousGroup(requestedGroupIndex)
            returnUsersListAtPage(pageIndex, listener)
        } else if (requestedGroupIndex == currentGroupIndex + 1) { //Next group.
            Log.d(TAG, "Change Page Next Group")
            previousGroup = currentGroup
            currentGroup = nextGroup
            requestNextGroup(requestedGroupIndex)
            returnUsersListAtPage(pageIndex, listener)
        }
        currentGroupIndex = requestedGroupIndex
    }

    fun getTotalUsersCount(listener: GithubDataListener) {
        if (totalUsersCount != -1) {
            listener.onTotalUsersCountSuccess(totalUsersCount)
            return
        }

        val objectRequest = object : JsonObjectRequest(
            Method.GET,
            "$BASE_URL/search/users?q=type%3Auser",
            null,
            Response.Listener { objectTotalUsersCount ->
                try {
                    totalUsersCount = objectTotalUsersCount.getInt(KEY_TOTAL_USERS_COUNT)
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
    }

    //Parameter group starts from 0.
    private fun requestCurrentGroup(groupIndex: Int, pageIndex: Int, listener: GithubDataListener) {
        val startId = groupIndex * userNumPerGroup
        val groupArrayRequest = object : JsonArrayRequest(
            Method.GET,
            "$BASE_URL/users?since=$startId",
            null,
            Response.Listener { arrayGroups ->
                try {
                    currentGroup = arrayListOf()

                    for (i in 0 until userNumPerGroup) {
                        val userObject = arrayGroups.getJSONObject(i)
                        val user = User()

                        user.id = userObject.getInt(KEY_ID)
                        user.username = userObject.getString(KEY_USERNAME)
                        user.avatar = userObject.getString(KEY_AVATAR)

                        val userObjectRequest = object : JsonObjectRequest(
                            Method.GET,
                            "$BASE_URL/users/${user.username}",
                            null,
                            Response.Listener { userDetailObject ->
                                try {
                                    user.name = userDetailObject.getString(KEY_NAME)
                                    user.company = userDetailObject.getString(KEY_COMPANY)
                                    user.location = userDetailObject.getString(KEY_LOCATION)
                                    user.repository = userDetailObject.getInt(KEY_REPOSITORY)
                                    user.follower = userDetailObject.getInt(KEY_FOLLOWER)
                                    user.following = userDetailObject.getInt(KEY_FOLLOWING)
                                    currentGroup.add(user)

                                    if (i == userNumPerGroup - 1) returnUsersListAtPage(
                                        pageIndex,
                                        listener
                                    )
                                } catch (e: Exception) {
                                    Log.d(TAG, "Parse Error - CurrentGroupUser: ${e.message}")
                                    listener.onFailure()
                                }
                            },
                            Response.ErrorListener {
                                Log.d(TAG, "Volley Error - CurrentGroupUser: ${it.message}")
                                listener.onFailure()
                            }) {
                            override fun getHeaders(): MutableMap<String, String> =
                                this@GithubDataManager.getHeaders()
                        }
                        requestQueue.add(userObjectRequest)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Parse Error - CurrentGroup: ${e.message}")
                    listener.onFailure()
                }
            }, Response.ErrorListener {
                Log.d(TAG, "Volley Error - CurrentGroup: ${it.message}")
                listener.onFailure()
            }) {
            override fun getHeaders(): MutableMap<String, String> =
                this@GithubDataManager.getHeaders()
        }
        requestQueue.add(groupArrayRequest)
    }

    private fun requestPreviousGroup(groupIndex: Int) {
        val startId = groupIndex * userNumPerGroup
        val previousGroupArrayRequest = object : JsonArrayRequest(
            Method.GET,
            "$BASE_URL/users?since=$startId",
            null,
            Response.Listener { arrayGroups ->
                try {
                    previousGroup = arrayListOf()

                    for (i in 0 until userNumPerGroup) {
                        val userObject = arrayGroups.getJSONObject(i)
                        val user = User()

                        user.id = userObject.getInt(KEY_ID)
                        user.username = userObject.getString(KEY_USERNAME)
                        user.avatar = userObject.getString(KEY_AVATAR)

                        val userObjectRequest = object : JsonObjectRequest(
                            Method.GET,
                            "$BASE_URL/users/${user.username}",
                            null,
                            Response.Listener { userDetailObject ->
                                try {
                                    user.name = userDetailObject.getString(KEY_NAME)
                                    user.company = userDetailObject.getString(KEY_COMPANY)
                                    user.location = userDetailObject.getString(KEY_LOCATION)
                                    user.repository = userDetailObject.getInt(KEY_REPOSITORY)
                                    user.follower = userDetailObject.getInt(KEY_FOLLOWER)
                                    user.following = userDetailObject.getInt(KEY_FOLLOWING)
                                    previousGroup.add(user)

                                    if (i == userNumPerGroup - 1) previousGroup.sortBy { it.id }
                                } catch (e: Exception) {
                                    Log.d(TAG, "Parse Error - PreviousGroupUser: ${e.message}")
                                }
                            },
                            Response.ErrorListener {
                                Log.d(TAG, "Volley Error - PreviousGroupUser: ${it.message}")
                            }) {
                            override fun getHeaders(): MutableMap<String, String> =
                                this@GithubDataManager.getHeaders()
                        }
                        requestQueue.add(userObjectRequest)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Parse Error - PreviousGroup: ${e.message}")
                }
            }, Response.ErrorListener {
                Log.d(TAG, "Volley Error - PreviousGroup: ${it.message}")
            }) {
            override fun getHeaders(): MutableMap<String, String> =
                this@GithubDataManager.getHeaders()
        }
        requestQueue.add(previousGroupArrayRequest)
    }

    private fun requestNextGroup(groupIndex: Int) {
        val startId = groupIndex * userNumPerGroup
        val nextGroupArrayRequest = object : JsonArrayRequest(
            Method.GET,
            "$BASE_URL/users?since=$startId",
            null,
            Response.Listener { arrayGroups ->
                try {
                    nextGroup = arrayListOf()

                    for (i in 0 until userNumPerGroup) {
                        val userObject = arrayGroups.getJSONObject(i)
                        val user = User()

                        user.id = userObject.getInt(KEY_ID)
                        user.username = userObject.getString(KEY_USERNAME)
                        user.avatar = userObject.getString(KEY_AVATAR)

                        val userObjectRequest = object : JsonObjectRequest(
                            Method.GET,
                            "$BASE_URL/users/${user.username}",
                            null,
                            Response.Listener { userDetailObject ->
                                try {
                                    user.name = userDetailObject.getString(KEY_NAME)
                                    user.company = userDetailObject.getString(KEY_COMPANY)
                                    user.location = userDetailObject.getString(KEY_LOCATION)
                                    user.repository = userDetailObject.getInt(KEY_REPOSITORY)
                                    user.follower = userDetailObject.getInt(KEY_FOLLOWER)
                                    user.following = userDetailObject.getInt(KEY_FOLLOWING)
                                    nextGroup.add(user)

                                    if (i == userNumPerGroup - 1) nextGroup.sortBy { it.id }
                                } catch (e: Exception) {
                                    Log.d(TAG, "Parse Error - PreviousGroupUser: ${e.message}")
                                }
                            },
                            Response.ErrorListener {
                                Log.d(TAG, "Volley Error - PreviousGroupUser: ${it.message}")
                            }) {
                            override fun getHeaders(): MutableMap<String, String> =
                                this@GithubDataManager.getHeaders()
                        }
                        requestQueue.add(userObjectRequest)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Parse Error - PreviousGroup: ${e.message}")
                }
            }, Response.ErrorListener {
                Log.d(TAG, "Volley Error - PreviousGroup: ${it.message}")
            }) {
            override fun getHeaders(): MutableMap<String, String> =
                this@GithubDataManager.getHeaders()
        }
        requestQueue.add(nextGroupArrayRequest)
    }

    private fun returnUsersListAtPage(pageIndex: Int, listener: GithubDataListener) {
        currentPage = arrayListOf()
        for (i in ((pageIndex % (userNumPerGroup / userNumPerPage)) * userNumPerPage)
                until (((pageIndex % (userNumPerGroup / userNumPerPage)) * userNumPerPage) + userNumPerPage)) {
            currentPage.add(currentGroup[i])
        }
        currentPage.sortBy { it.id }

        listener.onUsersListPageSuccess(currentPage)
    }

    private fun getHeaders(): MutableMap<String, String> {
        val headers = HashMap<String, String>()
        headers["Authorization"] = AUTH_TOKEN

        return headers
    }

    interface GithubDataListener {
        fun onFailure() {}

        fun onTotalUsersCountSuccess(totalUsersCount: Int) {}
        fun onUsersListPageSuccess(usersList: ArrayList<User>) {}
    }
}