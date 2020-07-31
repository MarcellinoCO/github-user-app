package co.marcellino.githubuserapp.utils

import android.content.Context
import co.marcellino.githubuserapp.BuildConfig
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class NetworkManager constructor(context: Context?) {

    companion object {
        const val BASE_URL = "https://api.github.com"
        private const val AUTH_TOKEN = BuildConfig.GITHUB_AUTH_TOKEN

        const val TAG_CURRENT_PAGE = "tag_current_page_request"
        const val TAG_NEXT_PAGE = "tag_next_page_request"
        const val TAG_PREVIOUS_PAGE = "tag_previous_page"
        const val TAG_SEARCH = "tag_search"
        const val TAG_FOLLOWER = "tag_follower"
        const val TAG_FOLLOWING = "tag_following"

        @Volatile
        private var INSTANCE: NetworkManager? = null

        fun getInstance(context: Context?) = INSTANCE ?: synchronized(this) {
            INSTANCE ?: NetworkManager(context).also { INSTANCE = it }
        }

        val headers = HashMap<String, String>().also { it["Authorization"] = AUTH_TOKEN }
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context?.applicationContext)
    }

    fun <T> addToRequestQueue(request: Request<T>) {
        requestQueue.add(request)
    }

    fun cancelRequestWithTag(tag: String) {
        requestQueue.cancelAll(tag)
    }
}