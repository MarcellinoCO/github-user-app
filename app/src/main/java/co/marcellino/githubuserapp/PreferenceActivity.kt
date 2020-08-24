package co.marcellino.githubuserapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import kotlinx.android.synthetic.main.activity_preference.*

class PreferenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        initializeAppBar()

        supportFragmentManager.commit {
            replace(R.id.holder_preference, PreferenceFragment())
        }
    }

    private fun initializeAppBar() {
        setSupportActionBar(appbar_preferences)
        appbar_preferences.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}