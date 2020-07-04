package co.marcellino.githubuserapp.utils

import android.content.Context
import co.marcellino.githubuserapp.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

object UserDataReader {

    fun getUserList(context: Context, jsonResourceID: Int): ArrayList<User> {
        var listUser = arrayListOf<User>()

        val jsonString: String? = getJsonString(context, jsonResourceID)

        if (!jsonString.isNullOrEmpty()) {
            val gson = Gson()

            val arrayListType = object : TypeToken<ArrayList<User>>() {}.type
            listUser = gson.fromJson(jsonString, arrayListType)
        }

        return listUser
    }

    private fun getJsonString(context: Context, jsonResourceID: Int): String? {
        var jsonString: String? = null
        try {
            jsonString = context.resources.openRawResource(jsonResourceID).bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }

        return jsonString
    }
}