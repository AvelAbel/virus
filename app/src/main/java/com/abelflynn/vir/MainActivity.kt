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

        // Получаем максимальный уровень из SharedPreferences
        val prefs = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        val maxLevel = prefs.getInt("MAX_LEVEL", 1)

        // Находим TextView для отображения максимального уровня и обновляем его текст
        val maxLevelTextView: TextView = findViewById(R.id.max_level)
        maxLevelTextView.text = "Рекорд: уровень $maxLevel"

        val playButton: Button = findViewById(R.id.play_button)
        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            // Передаем уровень в GameActivity
            intent.putExtra("LEVEL", 1)
            startActivity(intent)
        }
    }
}
