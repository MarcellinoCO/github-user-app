package co.marcellino.githubfavorites.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.marcellino.githubfavorites.R
import co.marcellino.githubfavorites.model.User
import com.bumptech.glide.Glide

class UserListAdapter(
    private val listUser: ArrayList<User>,
    private val context: Context
) :
    RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.iv_item_avatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tv_item_user_name)
        val tvRealName: TextView = itemView.findViewById(R.id.tv_item_name)
        val tvOrganization: TextView = itemView.findViewById(R.id.tv_item_company)
        val tvLocation: TextView = itemView.findViewById(R.id.tv_item_location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: User = listUser[position]

        Glide.with(holder.itemView.context).load(user.avatar)
            .placeholder(R.drawable.ic_person)
            .circleCrop()
            .into(holder.ivAvatar)

        holder.ivAvatar.transitionName = user.username

        holder.tvUserName.text = user.username
        holder.tvRealName.text = if (user.name != "null") user.name else "-"

        holder.tvOrganization.text = if (user.company != "null") context.resources.getString(
            R.string.format_company,
            user.company
        ) else "-"
        holder.tvLocation.text = if (user.location != "null") user.location else "-"

        holder.itemView.animation = AlphaAnimation(0.0f, 1.0f).apply { duration = 1500L }
    }

    override fun getItemCount(): Int {
        return listUser.size
    }
}