package co.marcellino.githubuserapp.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import co.marcellino.githubuserapp.FollowerFragment
import co.marcellino.githubuserapp.R
import co.marcellino.githubuserapp.model.User

class FollowerPagerAdapter(
    private val context: Context,
    private val user: User,
    fm: FragmentManager
) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = FollowerFragment.newInstance(FollowerFragment.TYPE_FOLLOWER)
            1 -> fragment = FollowerFragment.newInstance(FollowerFragment.TYPE_FOLLOWING)
        }
        return fragment as Fragment
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int) = when (position) {
        0 -> context.resources.getQuantityString(R.plurals.format_title_follower, user.follower)
        1 -> context.resources.getString(R.string.format_title_following)
        else -> ""
    }
}