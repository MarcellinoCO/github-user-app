package co.marcellino.githubuserapp

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.palette.graphics.Palette
import co.marcellino.githubuserapp.adapter.FollowerPagerAdapter
import co.marcellino.githubuserapp.model.User
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_user_detail.*

class UserDetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_USER_DATA = "extra_user_data"
    }

    private lateinit var user: User
    private lateinit var userList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        supportPostponeEnterTransition()

        initializeAppBar()

        user = intent.extras?.getParcelable(EXTRA_USER_DATA) ?: User()
        userList = intent.extras?.getParcelableArrayList("userList") ?: arrayListOf()
        displaySharedElementTransition()
        displayData()

        val followerPagerAdapter =
            FollowerPagerAdapter(this, user, userList, supportFragmentManager)
        vp_follower.adapter = followerPagerAdapter
        tabs_follower.setupWithViewPager(vp_follower)
    }

    private fun initializeAppBar() {
        setSupportActionBar(appbar_user_detail)
        appbar_user_detail.setNavigationOnClickListener {
            onBackPressed()
        }

        appbar_layout_user_detail.addOnOffsetChangedListener(object :
            AppBarLayout.OnOffsetChangedListener {
            var isShow = false
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) scrollRange = appBarLayout.totalScrollRange

                if (scrollRange + verticalOffset == 0) {
                    appbar_collapsing_user_detail.title = user.username
                    isShow = true
                } else if (isShow) {
                    appbar_collapsing_user_detail.title = ""
                    isShow = false
                }
            }
        })
    }

    private fun displaySharedElementTransition() {
        iv_avatar.transitionName = user.username
        Glide.with(this).load(user.avatar).circleCrop()
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

                    val avatarBitmap = (resource as BitmapDrawable).bitmap
                    Palette.from(avatarBitmap)
                        .generate { palette ->
                            val vibrantSwatch = palette?.vibrantSwatch
                            val mutedSwatch = palette?.mutedSwatch
                            val darkMutedSwatch = palette?.darkMutedSwatch
                            this@UserDetailActivity.container_avatar.setBackgroundColor(
                                vibrantSwatch?.rgb ?: mutedSwatch?.rgb ?: darkMutedSwatch?.rgb
                                ?: ResourcesCompat.getColor(
                                    resources,
                                    R.color.colorPrimaryDark,
                                    null
                                )
                            )

                            this@UserDetailActivity.appbar_user_detail.setBackgroundColor(
                                vibrantSwatch?.rgb ?: mutedSwatch?.rgb ?: darkMutedSwatch?.rgb
                                ?: ResourcesCompat.getColor(
                                    resources,
                                    R.color.colorPrimaryDark,
                                    null
                                )
                            )
                        }

                    return false
                }
            }).into(iv_avatar)
    }

    private fun displayData() {
        tv_name.text = if (user.name == "null") "-" else user.name
        tv_user_name.text = user.username

        tv_repository.text = resources.getQuantityString(
            R.plurals.format_repository,
            user.repository,
            user.repository
        )

        val followerPlural: String =
            resources.getQuantityString(R.plurals.format_follower, user.follower, user.follower)
        val followingPlural: String =
            resources.getQuantityString(R.plurals.format_following, user.following, user.following)
        tv_follower.text =
            resources.getString(R.string.format_follow, followerPlural, followingPlural)

        tv_company.text = if (user.company == "null") "-" else resources.getString(
            R.string.format_company,
            user.company
        )
        tv_location.text = if (user.location == "null") "-" else user.location
    }
}
