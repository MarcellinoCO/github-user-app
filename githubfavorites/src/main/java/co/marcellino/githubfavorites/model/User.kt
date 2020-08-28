package co.marcellino.githubfavorites.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var id: Int = 0,
    var username: String = "",
    var avatar: String = "",
    var name: String = "",
    var company: String = "",
    var location: String = "",
    var repository: Int = 0,
    var follower: Int = 0,
    var following: Int = 0,
    var isFavorite: Boolean = false
) : Parcelable