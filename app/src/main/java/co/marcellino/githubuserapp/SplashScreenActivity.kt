package co.marcellino.githubuserapp

import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.activity_splash_screen_frame_0.*
import java.util.*
import kotlin.concurrent.schedule

class SplashScreenActivity : AppCompatActivity() {
    /** private val loadingDuration = 2000L
    private val splashScreenDuration = 3000L
    private val animationDuration = 1200L */
    private val loadingDuration = 100L
    private val splashScreenDuration = 100L
    private val animationDuration = 100L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen_frame_0)

        //Ceritanya sedang loading sesuatu... Siapa tahu nanti butuh :)
        Timer().schedule(loadingDuration) {
            this@SplashScreenActivity.runOnUiThread { startAnimation() }
            Timer().schedule(splashScreenDuration) {
                this@SplashScreenActivity.runOnUiThread { changeActivity() }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun startAnimation() {
        val constraintSet = ConstraintSet()
        constraintSet.load(this, R.layout.activity_splash_screen)

        val transitionSettings = ChangeBounds()
        transitionSettings.interpolator = OvershootInterpolator()
        transitionSettings.duration = animationDuration

        TransitionManager.beginDelayedTransition(cl_splash_screen_root, transitionSettings)
        constraintSet.applyTo(cl_splash_screen_root)
    }

    private fun changeActivity() {
        val intentMainActivity = Intent(this@SplashScreenActivity, UserListActivity::class.java)
        startActivity(intentMainActivity)

        finish()
    }
}
