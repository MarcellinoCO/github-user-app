package co.marcellino.githubuserapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.marcellino.githubuserapp.model.User
import co.marcellino.githubuserapp.utils.NetworkManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest

class UserDetailViewModel : ViewModel() {

    private val tag = "GithubUserApp"
    private val userPerPage = 10

    private val liveUser = MutableLiveData<User>()
    private var user = User()
    fun getUser(): LiveData<User> = liveUser
    fun setUser(user: User) {
        this.user = user
        liveUser.postValue(user)
    }

    private val isFollowerError = MutableLiveData<Boolean>()
    fun isFollowerError(): LiveData<Boolean> = isFollowerError
    private val isFollowingError = MutableLiveData<Boolean>()
    fun isFollowingError(): LiveData<Boolean> = isFollowingError

    private val isFollowerLoading = MutableLiveData<Boolean>()
    fun isFollowerLoading(): LiveData<Boolean> = isFollowerLoading
    private val isFollowingLoading = MutableLiveData<Boolean>()
    fun isFollowingLoading(): LiveData<Boolean> = isFollowingLoading

    private val liveFollowerPage = MutableLiveData<ArrayList<User>>()
    private val liveFollowingPage = MutableLiveData<ArrayList<User>>()
    fun getFollowerPage(): LiveData<ArrayList<User>> = liveFollowerPage
    fun getFollowingPage(): LiveData<ArrayList<User>> = liveFollowingPage
    fun loadFollowerPage() {
        isFollowerLoading.postValue(true)
        val followerPageRequest = object : JsonArrayRequest(
            Method.GET,
            "${NetworkManager.BASE_URL}/users/${user.username}/followers?per_page=$userPerPage",
            null,
            Response.Listener { followerPageArray ->
                try {
                    val tempFollowerPage = arrayListOf<User>()

                    for (i in 0 until userPerPage.coerceAtMost(followerPageArray.length())) {
                        val userObject = followerPageArray.getJSONObject(i)
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

                                tempFollowerPage.add(tempUser)

                                if (i == (kotlin.math.min(
                                        userPerPage,
                                        followerPageArray.length()
                                    ) - 1)
                                ) {
                                    tempFollowerPage.sortBy { it.id }
                                    liveFollowerPage.postValue(tempFollowerPage)

                                    isFollowerError.postValue(false)
                                    isFollowerLoading.postValue(false)
                                }
                            },
                            Response.ErrorListener {
                                Log.d(tag, "Volley error - FollowerUserDetails: ${it.message}")
                                isFollowerError.postValue(true)
                                isFollowerLoading.postValue(false)
                            }) {
                            override fun getHeaders() = NetworkManager.headers
                        }.also { it.tag = NetworkManager.TAG_FOLLOWER }
                        NetworkManager.getInstance(null).addToRequestQueue(userDetailsRequest)
                    }
                } catch (e: Exception) {
                    Log.d(tag, "Parse error - FollowerPage: ${e.message}")
                    isFollowerError.postValue(true)
                    isFollowerLoading.postValue(false)
                }
            },
            Response.ErrorListener {
                Log.d(tag, "Volley error - FollowerPage: ${it.message}")
                isFollowerError.postValue(true)
                isFollowerLoading.postValue(false)
            }) {
            override fun getHeaders() = NetworkManager.headers
        }.also { it.tag = NetworkManager.TAG_FOLLOWER }
        NetworkManager.getInstance(null).addToRequestQueue(followerPageRequest)
    }

    fun loadFollowingPage() {
        isFollowingLoading.postValue(true)
        val followingPageRequest = object : JsonArrayRequest(
            Method.GET,
            "${NetworkManager.BASE_URL}/users/${user.username}/following?per_page=$userPerPage",
            null,
            Response.Listener { followingPageArray ->
                try {
                    val tempFollowingPage = arrayListOf<User>()

                    for (i in 0 until userPerPage.coerceAtMost(followingPageArray.length())) {
                        val userObject = followingPageArray.getJSONObject(i)
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

                                tempFollowingPage.add(tempUser)

                                if (i == (kotlin.math.min(
                                        userPerPage,
                                        followingPageArray.length()
                                    ) - 1)
                                ) {
                                    tempFollowingPage.sortBy { it.id }
                                    liveFollowingPage.postValue(tempFollowingPage)

                                    isFollowingError.postValue(false)
                                    isFollowingLoading.postValue(false)
                                }
                            },
                            Response.ErrorListener {
                                Log.d(tag, "Volley error - FollowingUserDetails: ${it.message}")
                                isFollowingError.postValue(true)
                                isFollowingLoading.postValue(false)
                            }) {
                            override fun getHeaders() = NetworkManager.headers
                        }.also { it.tag = NetworkManager.TAG_FOLLOWING }
                        NetworkManager.getInstance(null).addToRequestQueue(userDetailsRequest)
                    }
                } catch (e: Exception) {
                    Log.d(tag, "Parse error - FollowingPage: ${e.message}")
                    isFollowingError.postValue(true)
                    isFollowingLoading.postValue(false)
                }
            },
            Response.ErrorListener {
                Log.d(tag, "Volley error - FollowingPage: ${it.message}")
                isFollowingError.postValue(true)
                isFollowingLoading.postValue(false)
            }) {
            override fun getHeaders() = NetworkManager.headers
        }.also { it.tag = NetworkManager.TAG_FOLLOWING }
        NetworkManager.getInstance(null).addToRequestQueue(followingPageRequest)
    }
}