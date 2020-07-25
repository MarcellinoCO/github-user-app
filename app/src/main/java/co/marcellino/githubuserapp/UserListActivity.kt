package co.marcellino.githubuserapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import co.marcellino.githubuserapp.adapter.ListUserAdapter
import co.marcellino.githubuserapp.model.User
import co.marcellino.githubuserapp.utils.GithubDataManager
import kotlinx.android.synthetic.main.activity_user_list.*

class UserListActivity : AppCompatActivity(), ExitDialogFragment.OnExitDialogListener {
    companion object {
        const val SORT_NAME_ASC = 0
        const val SORT_NAME_DES = 1
        const val SORT_REPOSITORY_ASC = 2
        const val SORT_REPOSITORY_DES = 4
        const val SORT_FOLLOWER_ASC = 5
        const val SORT_FOLLOWER_DES = 6
    }

    private var listUser: ArrayList<User> = arrayListOf()
    private var currentPageIndex: Int = 0

    private lateinit var listUserAdapter: ListUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        initializeAppBar()

        GithubDataManager.setContext(applicationContext)

        // listUser = UserDataReader.getUserList(this, R.raw.githubuser)
        initializeRecyclerView()
        GithubDataManager.getUsersListAtPage(
            currentPageIndex,
            object : GithubDataManager.GithubDataListener {
                override fun onUsersListPageSuccess(usersList: ArrayList<User>) {
                    listUser = usersList
                    Log.d("GithubUserApp", usersList.toString())
                    initializeRecyclerView()
                }
            })

        GithubDataManager.getTotalUsersCount(object : GithubDataManager.GithubDataListener {
            override fun onTotalUsersCountSuccess(totalUsersCount: Int) {
                Log.d("GithubUserApp", totalUsersCount.toString())
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.apppbar_user_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        ExitDialogFragment().show(supportFragmentManager, ExitDialogFragment::class.java.simpleName)
    }

    private fun initializeAppBar() {
        setSupportActionBar(appbar_user_list)
        appbar_user_list.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.btn_menu_sort_name_asc -> sortUpdateUserList(SORT_NAME_ASC)
                R.id.btn_menu_sort_name_des -> sortUpdateUserList(SORT_NAME_DES)
                R.id.btn_menu_sort_repository_asc -> sortUpdateUserList(SORT_REPOSITORY_ASC)
                R.id.btn_menu_sort_repository_des -> sortUpdateUserList(SORT_REPOSITORY_DES)
                R.id.btn_menu_sort_follower_asc -> sortUpdateUserList(SORT_FOLLOWER_ASC)
                R.id.btn_menu_sort_follower_des -> sortUpdateUserList(SORT_FOLLOWER_DES)
            }
            true
        }
    }

    private fun initializeRecyclerView() {
        listUserAdapter = ListUserAdapter(listUser, this)

        rv_user_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_user_list.adapter = listUserAdapter

        listUserAdapter.setOnItemClickCallback(object : ListUserAdapter.OnItemClickCallback {
            override fun onItemClicked(position: Int, user: User, sharedElement: View) {
                goToDetailActivity(user, sharedElement)
            }
        })
    }

    private fun goToDetailActivity(user: User, sharedElement: View) {
        val intentDetailActivity = Intent(this@UserListActivity, UserDetailActivity::class.java)
        intentDetailActivity.putExtra(UserDetailActivity.EXTRA_USER_DATA, user)

        val sharedElementTransition = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this@UserListActivity,
            sharedElement,
            user.username
        )

        startActivity(intentDetailActivity, sharedElementTransition.toBundle())
    }

    private fun sortUpdateUserList(sort: Int) {
        listUser.apply {
            when (sort) {
                SORT_NAME_ASC -> sortBy { it.name }
                SORT_NAME_DES -> sortByDescending { it.name }
                SORT_REPOSITORY_ASC -> sortBy { it.repository }
                SORT_REPOSITORY_DES -> sortByDescending { it.repository }
                SORT_FOLLOWER_ASC -> sortBy { it.follower }
                SORT_FOLLOWER_DES -> sortByDescending { it.follower }
            }
            initializeRecyclerView()
        }
    }

    override fun onExitDialogActionChosen(exit: Boolean) {
        if (exit) finish()
    }
}
