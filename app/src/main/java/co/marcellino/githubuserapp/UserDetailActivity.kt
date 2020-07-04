package co.marcellino.githubuserapp

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.palette.graphics.Palette
import co.marcellino.githubuserapp.model.User
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_user_detail.*

class UserDetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_USER_DATA = "extra_user_data"
    }

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        supportPostponeEnterTransition()

        initializeAppBar()

        user = intent.extras?.getParcelable(EXTRA_USER_DATA) ?: User()
        displaySharedElementTransition()
        displayData()
    }

    private fun initializeAppBar() {
        setSupportActionBar(appbar_user_detail)
        appbar_user_detail.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun displaySharedElementTransition() {
        val avatarResourceID = this.resources.getIdentifier(user.avatar, "drawable", packageName)
        iv_avatar.transitionName = user.username
        Glide.with(this).load(avatarResourceID).circleCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    supportStartPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    supportStartPostponedEnterTransition()
                    return false
                }
            }).into(iv_avatar)

        Palette.from(BitmapFactory.decodeResource(resources, avatarResourceID))
            .generate { palette ->
                val vibrantSwatch = palette?.vibrantSwatch
                val mutedSwatch = palette?.mutedSwatch
                val darkMutedSwatch = palette?.darkMutedSwatch
                this@UserDetailActivity.container_avatar.setBackgroundColor(
                    vibrantSwatch?.rgb ?: mutedSwatch?.rgb ?: darkMutedSwatch?.rgb
                    ?: ResourcesCompat.getColor(resources, R.color.colorPrimaryDark, null)
                )
            }
    }

    private fun displayData() {
        tv_name.text = user.name
        tv_user_name.text = user.username

        val repositoryPlural: String = if (user.repository > 1) "ies" else "y"
        tv_repository.text =
            resources.getString(R.string.format_repository, user.repository, repositoryPlural)

        val followerPlural: String = if (user.follower > 1) "s" else ""
        val followingPlural: String = if (user.following > 1) "s" else ""
        tv_follower.text = resources.getString(
            R.string.format_follower,
            user.follower, followerPlural,
            user.following, followingPlural
        )

        tv_company.text = resources.getString(R.string.format_company, user.company)
        tv_location.text = user.location
    }
}
