package com.udacity

import android.app.NotificationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()

        val tvFileName: TextView = binding.root.findViewById(R.id.tv_filename_value)
        val tvStatus: TextView = binding.root.findViewById(R.id.tv_status_value)
        val btnOk: Button = binding.root.findViewById(R.id.button_ok);

        tvFileName.text = intent.getStringExtra(DETAIL_INTENT_FILENAME_KEY)
        tvStatus.text = intent.getStringExtra(DETAIL_INTENT_STATUS_KEY)
        btnOk.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val DETAIL_INTENT_STATUS_KEY = "status"
        const val DETAIL_INTENT_FILENAME_KEY = "filename"
    }
}
