package co.marcellino.githubuserapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.marcellino.githubuserapp.adapter.ListUserAdapter
import co.marcellino.githubuserapp.model.User
import co.marcellino.githubuserapp.viewmodel.UserDetailViewModel

class FollowerFragment : Fragment() {

    companion object {
        const val EXTRA_TYPE = "extra_type"

        const val TYPE_FOLLOWER = 0
        const val TYPE_FOLLOWING = 1

        fun newInstance(type: Int) =
            FollowerFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_TYPE, type)
                }
            }
    }

    private lateinit var userDetailViewModel: UserDetailViewModel

    private var type: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getInt(EXTRA_TYPE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_follower, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerView(arrayListOf(), true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializeViewModel()

        view?.findViewById<Button>(R.id.btn_error_retry)?.setOnClickListener {
            if (type == TYPE_FOLLOWER) userDetailViewModel.loadFollowerPage()
            else userDetailViewModel.loadFollowingPage()
        }
    }

    private fun initializeRecyclerView(userList: ArrayList<User>, isFirstCreated: Boolean) {
        val rv = view?.findViewById<RecyclerView>(R.id.rv_follow_list)

        if (isFirstCreated) rv?.layoutManager =
            LinearLayoutManager(context as Context, LinearLayoutManager.VERTICAL, false)
        rv?.adapter = object : ListUserAdapter(userList, context as Context, false) {
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                holder.ivAvatar.transitionName = ""
            }
        }
    }

    private fun initializeViewModel() {
        userDetailViewModel =
            ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory()).get(
                UserDetailViewModel::class.java
            )

        if (type == TYPE_FOLLOWER) {
            userDetailViewModel.getFollowerPage()
                .observe(requireActivity(), Observer { newFollowerPage ->
                    initializeRecyclerView(newFollowerPage, false)

                    view?.findViewById<ConstraintLayout>(R.id.container_error)?.visibility =
                        View.GONE
                    view?.findViewById<ConstraintLayout>(R.id.container_loading)?.visibility =
                        View.GONE
                    view?.findViewById<RecyclerView>(R.id.rv_follow_list)?.visibility =
                        View.VISIBLE
                })

            userDetailViewModel.isFollowerLoading()
                .observe(requireActivity(), Observer { isLoading ->
                    if (isLoading) {
                        view?.findViewById<ConstraintLayout>(R.id.container_error)?.visibility =
                            View.GONE
                        view?.findViewById<RecyclerView>(R.id.rv_follow_list)?.visibility =
                            View.GONE
                        view?.findViewById<ConstraintLayout>(R.id.container_loading)?.visibility =
                            View.VISIBLE
                    }
                })

            userDetailViewModel.isFollowerError().observe(requireActivity(), Observer { isError ->
                if (isError) {
                    view?.findViewById<ConstraintLayout>(R.id.container_error)?.visibility =
                        View.GONE
                    view?.findViewById<RecyclerView>(R.id.rv_follow_list)?.visibility =
                        View.GONE
                    view?.findViewById<ConstraintLayout>(R.id.container_loading)?.visibility =
                        View.VISIBLE
                }
            })
        } else {
            userDetailViewModel.getFollowingPage()
                .observe(requireActivity(), Observer { newFollowingPage ->
                    initializeRecyclerView(newFollowingPage, false)

                    view?.findViewById<ConstraintLayout>(R.id.container_error)?.visibility =
                        View.GONE
                    view?.findViewById<ConstraintLayout>(R.id.container_loading)?.visibility =
                        View.GONE
                    view?.findViewById<RecyclerView>(R.id.rv_follow_list)?.visibility =
                        View.VISIBLE
                })

            userDetailViewModel.isFollowingLoading()
                .observe(requireActivity(), Observer { isLoading ->
                    if (isLoading) {
                        view?.findViewById<ConstraintLayout>(R.id.container_error)?.visibility =
                            View.GONE
                        view?.findViewById<RecyclerView>(R.id.rv_follow_list)?.visibility =
                            View.GONE
                        view?.findViewById<ConstraintLayout>(R.id.container_loading)?.visibility =
                            View.VISIBLE
                    }
                })

            userDetailViewModel.isFollowingError().observe(requireActivity(), Observer { isError ->
                if (isError) {
                    view?.findViewById<ConstraintLayout>(R.id.container_error)?.visibility =
                        View.GONE
                    view?.findViewById<RecyclerView>(R.id.rv_follow_list)?.visibility =
                        View.GONE
                    view?.findViewById<ConstraintLayout>(R.id.container_loading)?.visibility =
                        View.VISIBLE
                }
            })
        }
    }
}