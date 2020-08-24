package co.marcellino.githubuserapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import co.marcellino.githubuserapp.utils.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.*

class PreferenceFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var notificationsEnabledKey: String
    private lateinit var notificationsTimeKey: String
    private lateinit var languageSetKey: String

    private lateinit var notificationsEnabledPreference: SwitchPreferenceCompat
    private lateinit var notificationsTimePreference: Preference
    private lateinit var languageSetPreference: Preference

    private var isNotificationEnabled = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val sharedPreferences = preferenceManager.sharedPreferences
        initialize(sharedPreferences)
        loadPreferences(sharedPreferences)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == notificationsEnabledKey) {
            isNotificationEnabled =
                sharedPreferences?.getBoolean(notificationsEnabledKey, false) ?: false

            if (isNotificationEnabled) {
                val timeString = sharedPreferences?.getString(
                    notificationsTimeKey,
                    resources.getString(R.string.preference_notifications_time_default)
                ) ?: resources.getString(R.string.preference_notifications_time_default)
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val calendar = Calendar.getInstance().also {
                    it.time = timeFormat.parse(timeString) as Date
                }

                setReminder(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
            } else cancelReminder()
        }

        if (key == languageSetKey) {
            languageSetPreference.summary =
                if (Locale.getDefault().language == "id") resources.getStringArray(R.array.language_list)[1]
                else resources.getStringArray(R.array.language_list)[0]
        }
    }

    override fun onResume() {
        super.onResume()

        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()

        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun initialize(sharedPreferences: SharedPreferences) {
        notificationsEnabledKey =
            resources.getString(R.string.preference_notifications_title)
        notificationsTimeKey = resources.getString(R.string.preference_notifications_time_title)
        languageSetKey = resources.getString(R.string.preference_language_title)

        notificationsEnabledPreference =
            findPreference<SwitchPreferenceCompat>(notificationsEnabledKey) as SwitchPreferenceCompat

        notificationsTimePreference = findPreference<Preference>(notificationsTimeKey) as Preference
        notificationsTimePreference.setOnPreferenceClickListener {
            val calendar = Calendar.getInstance()

            TimePickerDialog(
                context as Context,
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    val c = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hourOfDay)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                    }
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val timeString = timeFormat.format(c.timeInMillis)

                    sharedPreferences.edit {
                        putString(notificationsTimeKey, timeString)
                    }
                    notificationsTimePreference.summary = resources.getString(
                        R.string.preference_notifications_time_summary,
                        timeString
                    )

                    if (isNotificationEnabled) {
                        setReminder(hourOfDay, minute)
                    } else cancelReminder()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()

            true
        }

        languageSetPreference = findPreference<Preference>(languageSetKey) as Preference
    }

    private fun loadPreferences(sharedPreferences: SharedPreferences) {
        isNotificationEnabled = sharedPreferences.getBoolean(notificationsEnabledKey, false)
        notificationsEnabledPreference.isChecked = isNotificationEnabled

        notificationsTimePreference.summary = resources.getString(
            R.string.preference_notifications_time_summary,
            sharedPreferences.getString(
                notificationsTimeKey,
                resources.getString(R.string.preference_notifications_time_default)
            )
        )

        languageSetPreference.summary =
            if (Locale.getDefault().language == "in") resources.getStringArray(R.array.language_list)[1]
            else resources.getStringArray(R.array.language_list)[0]
    }

    private fun setReminder(hourOfDay: Int, minute: Int) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelReminder() {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

        alarmManager.cancel(pendingIntent)
    }
}