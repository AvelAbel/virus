package com.abelflynn.vir

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("MY_PREFERENCES", Context.MODE_PRIVATE)
        val maxLevel = sharedPref.getInt("record", 1)

        val maxLevelTextView: TextView = findViewById(R.id.max_level)
        maxLevelTextView.text = "Рекорд: уровень $maxLevel"

        val playButton: Button = findViewById(R.id.play_button)
        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }
    }
}