package co.marcellino.githubuserapp.widget

import android.content.Intent
import android.widget.RemoteViewsService

class FavoritesWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory =
        StackRemoteViewsFactory(this.applicationContext)
}
