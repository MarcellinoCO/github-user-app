package co.marcellino.githubuserapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.marcellino.githubuserapp.adapter.ListUserAdapter
import co.marcellino.githubuserapp.model.User

class FollowerFragment : Fragment() {

    private var userList: ArrayList<User>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userList = it.getParcelableArrayList("userList")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_follower, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.rv_follower)
        val rvAdapter = ListUserAdapter(userList ?: arrayListOf(), context as Context)
        rv.layoutManager =
            LinearLayoutManager(context as Context, LinearLayoutManager.VERTICAL, false)
        rv.adapter = rvAdapter
    }

    companion object {
        fun newInstance(userList: ArrayList<User>) =
            FollowerFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("userList", userList)
                }
            }
    }
}