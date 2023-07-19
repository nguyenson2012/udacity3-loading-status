package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var downloadID: Long = 0
    private var selectedURL: String? = null
    private var selectedURLName: String? = null
    private val NOTIFICATION_POLICY_REQUEST_CODE = 1100

    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        notificationManager = getSystemService(
            NotificationManager::class.java
        )

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        val loadingButton: LoadingButton = binding.root.findViewById(R.id.custom_button)
        loadingButton.setOnClickListener {
            Log.d("MainActivity", "click $selectedURL")
            selectedURL?.let {
                loadingButton.buttonState = ButtonState.Loading
                download(it)
            }
            if (selectedURL == null) {
                Toast.makeText(this, getString(R.string.please_choose_url), Toast.LENGTH_SHORT).show()
            }
        }
        requestNotificationPolicyAccess()
    }

    private fun createNotiChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH)

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_channel_description)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun isNotificationPolicyAccessGranted(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    private fun requestNotificationPolicyAccess() {
        val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivityForResult(intent, NOTIFICATION_POLICY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NOTIFICATION_POLICY_REQUEST_CODE) {
            if (isNotificationPolicyAccessGranted()) {
                // Notification policy access has been granted
                createNotiChannel(CHANNEL_ID, getString(R.string.notification_channel_name))
            } else {
                Log.d("MainActivity", "Notification not granted");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun clickRadioButton(view: View) {
        if (view is RadioButton && view.isChecked) {
            when (view.getId()) {
                R.id.radio_btn_glide ->
                    selectedURL = GLIDE_REPO_URL
                R.id.radio_btn_download_app ->
                    selectedURL = STARTER_APP_REPO_URL
                R.id.radio_btn_retrofit ->
                    selectedURL = RETROFIT_REPO_URL
            }
            selectedURLName = view.text.toString()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("Range")
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            val loadingButton: LoadingButton = binding.root.findViewById(R.id.custom_button);
            loadingButton.buttonState = ButtonState.Completed
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query()
            query.setFilterById(id!!)

            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                var downloadStatus = "Fail"
                if (DownloadManager.STATUS_SUCCESSFUL == status) {
                    downloadStatus = "Success"
                }

                Toast.makeText(applicationContext, getString(R.string.notification_description), Toast.LENGTH_LONG).show()
                if (isNotificationPolicyAccessGranted()) {
                    sendNotification(
                        notificationManager,
                        CHANNEL_ID,
                        getString(R.string.notification_description),
                        applicationContext,
                        downloadStatus,
                        selectedURLName!!
                    )
                }
            }
        }
    }

    private fun sendNotification(
        notificationManager: NotificationManager,
        channelId: String,
        messageBody: String,
        applicationContext: Context,
        downloadStatus: String,
        selectedURLName: String
    ) {
        val notificationRequestCode = 1
        val detailActivityIntent = Intent(applicationContext, DetailActivity::class.java)
        detailActivityIntent.putExtra(DetailActivity.DETAIL_INTENT_STATUS_KEY, downloadStatus)
        detailActivityIntent.putExtra(DetailActivity.DETAIL_INTENT_FILENAME_KEY, selectedURLName)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationRequestCode,
            detailActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_assistant_black_24dp,
                applicationContext.getString(R.string.notification_button),
                pendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(notificationRequestCode, builder.build())
    }


    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    companion object {
        private const val GLIDE_REPO_URL =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val STARTER_APP_REPO_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/masterXXX.zip"
        private const val RETROFIT_REPO_URL =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"
        private const val CHANNEL_ID = "channelId"
    }
}