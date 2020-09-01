package co.marcellino.githubuserapp.widget

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.net.toUri
import co.marcellino.githubuserapp.R
import co.marcellino.githubuserapp.UserDetailActivity
import co.marcellino.githubuserapp.db.AppDatabase
import co.marcellino.githubuserapp.model.User

class FavoritesWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_DETAIL = "co.marcellino.githubuserapp.ACTION_DETAIL"
        const val EXTRA_ITEM = "co.marcellino.githubuserapp.EXTRA_ITEM"

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, FavoritesWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = this.toUri(Intent.URI_INTENT_SCHEME).toUri()
            }

            val detailIntent = Intent(context, FavoritesWidget::class.java).apply {
                action = ACTION_DETAIL
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = this.toUri(Intent.URI_INTENT_SCHEME).toUri()
            }
            val detailPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                detailIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val views = RemoteViews(context.packageName, R.layout.widget_favorites).apply {
                setRemoteAdapter(R.id.sv_widget_favorites, intent)
                setEmptyView(R.id.sv_widget_favorites, R.id.empty_view)
                setPendingIntentTemplate(R.id.sv_widget_favorites, detailPendingIntent)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAppWidget(context: Context) {
            val manager = AppWidgetManager.getInstance(context)

            val widget = ComponentName(context, FavoritesWidget::class.java)
            val views = RemoteViews(context.packageName, R.layout.widget_favorites)

            manager.updateAppWidget(widget, views)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ACTION_DETAIL) {
            val user = User(
                id = intent.getIntExtra("$EXTRA_ITEM.${AppDatabase.COLUMN_ID}", 0),
                username = intent.getStringExtra("$EXTRA_ITEM.${AppDatabase.COLUMN_USERNAME}")
                    ?: "",
                avatar = intent.getStringExtra("$EXTRA_ITEM.${AppDatabase.COLUMN_AVATAR}") ?: "",
                name = intent.getStringExtra("$EXTRA_ITEM.${AppDatabase.COLUMN_NAME}") ?: "",
                company = intent.getStringExtra("$EXTRA_ITEM.${AppDatabase.COLUMN_COMPANY}") ?: "",
                location = intent.getStringExtra("$EXTRA_ITEM.${AppDatabase.COLUMN_LOCATION}")
                    ?: "",
                repository = intent.getIntExtra("$EXTRA_ITEM.${AppDatabase.COLUMN_REPOSITORY}", 0),
                follower = intent.getIntExtra("$EXTRA_ITEM.${AppDatabase.COLUMN_FOLLOWER}", 0),
                following = intent.getIntExtra("$EXTRA_ITEM.${AppDatabase.COLUMN_FOLLOWING}", 0),
                isFavorite = intent.getBooleanExtra(
                    "$EXTRA_ITEM.${AppDatabase.COLUMN_IS_FAVORITE}",
                    true
                )
            )
            val detailIntent = Intent(context, UserDetailActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(UserDetailActivity.EXTRA_USER_DATA, user)
            }

            TaskStackBuilder.create(context).addParentStack(UserDetailActivity::class.java)
                .addNextIntent(detailIntent).startActivities()
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}