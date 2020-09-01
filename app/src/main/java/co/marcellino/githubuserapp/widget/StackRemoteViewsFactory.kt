package co.marcellino.githubuserapp.widget

import android.content.Context
import android.content.Intent
import android.os.Binder
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import co.marcellino.githubuserapp.R
import co.marcellino.githubuserapp.db.AppDatabase
import co.marcellino.githubuserapp.model.User
import co.marcellino.githubuserapp.provider.FavoritesProvider
import co.marcellino.githubuserapp.utils.MappingHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

internal class StackRemoteViewsFactory(private val context: Context) :
    RemoteViewsService.RemoteViewsFactory {

    private var favoritesList = ArrayList<User>()

    override fun onDataSetChanged() {
        val identityToken = Binder.clearCallingIdentity()
        val cursor =
            context.contentResolver?.query(FavoritesProvider.CONTENT_URI, null, null, null, null)
        favoritesList = MappingHelper.mapCursorToArrayList(cursor)

        Binder.restoreCallingIdentity(identityToken)
    }

    override fun getViewAt(position: Int): RemoteViews {
        val user = favoritesList[position]

        val remoteViews = RemoteViews(context.packageName, R.layout.widget_item_favorites).apply {
            val avatar = Glide.with(context.applicationContext).load(favoritesList[position].avatar)
                .apply(RequestOptions.circleCropTransform()).submit().get().toBitmap()
            setImageViewBitmap(R.id.iv_widget_avatar, avatar)

            setTextViewText(R.id.tv_widget_username, user.username)
            setTextViewText(R.id.tv_widget_name, user.name)
            setTextViewText(
                R.id.tv_widget_company,
                if (user.company != "null") user.company else "-"
            )
            setTextViewText(
                R.id.tv_widget_location,
                if (user.location != "null") user.location else "-"
            )
        }

        val extrasUser = bundleOf(
            "${FavoritesWidget.EXTRA_ITEM}.${AppDatabase.COLUMN_ID}" to user.id,
            "${FavoritesWidget.EXTRA_ITEM}.${AppDatabase.COLUMN_USERNAME}" to user.username,
            "${FavoritesWidget.EXTRA_ITEM}.${AppDatabase.COLUMN_AVATAR}" to user.avatar,
            "${FavoritesWidget.EXTRA_ITEM}.${AppDatabase.COLUMN_NAME}" to user.name,
            "${FavoritesWidget.EXTRA_ITEM}.${AppDatabase.COLUMN_COMPANY}" to user.company,
            "${FavoritesWidget.EXTRA_ITEM}.${AppDatabase.COLUMN_LOCATION}" to user.location,
            "${FavoritesWidget.EXTRA_ITEM}.${AppDatabase.COLUMN_REPOSITORY}" to user.repository,
            "${FavoritesWidget.EXTRA_ITEM}.${AppDatabase.COLUMN_FOLLOWER}" to user.follower,
            "${FavoritesWidget.EXTRA_ITEM}.${AppDatabase.COLUMN_FOLLOWING}" to user.following,
            "${FavoritesWidget.EXTRA_ITEM}.${AppDatabase.COLUMN_IS_FAVORITE}" to user.isFavorite
        )
        val fillInIntent = Intent().putExtras(extrasUser)
        remoteViews.setOnClickFillInIntent(R.id.ll_widget, fillInIntent)

        return remoteViews
    }

    override fun onCreate() {}

    override fun getLoadingView(): RemoteViews? = null

    override fun getItemId(position: Int): Long = 0

    override fun hasStableIds(): Boolean = false

    override fun getCount(): Int = favoritesList.size

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {}
}