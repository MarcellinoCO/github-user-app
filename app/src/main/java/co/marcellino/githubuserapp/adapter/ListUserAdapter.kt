package co.marcellino.githubuserapp.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.marcellino.githubuserapp.R
import co.marcellino.githubuserapp.model.User
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

open class ListUserAdapter(private val listUser: ArrayList<User>, private val context: Context) :
    RecyclerView.Adapter<ListUserAdapter.ViewHolder>() {

    private var onItemClickCallback: OnItemClickCallback? = null

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
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean = false

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.itemView.setOnClickListener {
                        onItemClickCallback?.onItemClicked(
                            holder.adapterPosition,
                            user,
                            holder.ivAvatar
                        )
                    }
                    return false
                }
            })
            .into(holder.ivAvatar)

        holder.ivAvatar.transitionName = user.username

        holder.tvUserName.text = user.username
        holder.tvRealName.text = if (user.name != "null") user.name else "-"

        holder.tvOrganization.text = if (user.company != "null") context.resources.getString(
            R.string.format_company,
            user.company
        ) else "-"
        holder.tvLocation.text = if (user.location != "null") user.location else "-"

        holder.itemView.animation = AlphaAnimation(0.0f, 1.0f).apply { duration = 1000L }
    }

    override fun getItemCount(): Int {
        return listUser.size
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(position: Int, user: User, sharedElement: View)
    }
}