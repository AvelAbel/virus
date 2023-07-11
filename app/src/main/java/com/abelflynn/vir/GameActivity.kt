package com.abelflynn.vir

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Random

class GameActivity : AppCompatActivity() {

    private var computerScore = 1  // начальные очки компьютера
    private var playerScore = 1  // начальные очки игрока
    private val random = Random()
    private var level = 1

    // функция для проверки окончания игры и отображения результатов
    private fun checkGameOver() {
        if (playerScore > computerScore) {
            // Игрок выиграл, переходим к следующему уровню
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("level", level + 1)
            startActivity(intent)
        } else {
            // Компьютер выиграл, возвращаемся к главному экрану
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("record", level)
            startActivity(intent)
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        level = intent.getIntExtra("level", 1) // получите уровень из Intent
        val levelTextView: TextView = findViewById(R.id.level_text_view)
        levelTextView.text = "Уровень: $level"


        val gridView: SquareGridView = findViewById(R.id.grid_view)
        gridView.numColumns = level + 3  // Количество столбцов зависит от уровня
        val imageAdapter = ImageAdapter(this, level)
        gridView.adapter = imageAdapter

        val computerScoreTextView: TextView = findViewById(R.id.computer_score)
        val playerScoreTextView: TextView = findViewById(R.id.player_score)

        // Инициализируем начальные очки на экране
        computerScoreTextView.text = "Компьютер: $computerScore"
        playerScoreTextView.text = "Игрок: $playerScore"

        gridView.setOnItemClickListener { _, _, position, _ ->
            val cell = imageAdapter.cells[position]

            // Проверяем, соприкасается ли ячейка с пустой ячейкой
            if (!imageAdapter.hasEmptyNeighbor(position)) {
                // Если ячейка не соприкасается с пустой ячейкой, мы не позволяем пользователю на неё нажимать
                return@setOnItemClickListener
            }

            // Игрок может нажимать только на свои клетки
            if (cell.image == R.drawable.player) {
                updateCells(position, imageAdapter, R.drawable.player)

                // Суммируем значения всех ячеек игрока
                playerScore = imageAdapter.cells.filter { it.image == R.drawable.player }.sumBy { it.number }
                playerScoreTextView.text = "Игрок: $playerScore"

                // Проверяем, есть ли пустые ячейки
                var emptyCells = imageAdapter.cells.count { it.image == R.drawable.empty }
                if (emptyCells == 0) {
                    // Игра окончена, нет пустых ячеек
                    checkGameOver()
                    return@setOnItemClickListener
                }

                // Ход компьютера
                val availableCells = getAvailableComputerCells(imageAdapter)
                if (availableCells.isNotEmpty()) {
                    val computerPosition = availableCells[random.nextInt(availableCells.size)]
                    updateCells(computerPosition, imageAdapter, R.drawable.computer)

                    // Суммируем значения всех ячеек компьютера
                    computerScore = imageAdapter.cells.filter { it.image == R.drawable.computer }.sumBy { it.number }
                    computerScoreTextView.text = "Компьютер: $computerScore"
                }

                // Проверяем, есть ли пустые ячейки
                emptyCells = imageAdapter.cells.count { it.image == R.drawable.empty }
                if (emptyCells == 0) {
                    // Игра окончена, нет пустых ячеек
                    checkGameOver()
                }
            }
            imageAdapter.notifyDataSetChanged()
        }
    }

    private fun updateCells(position: Int, imageAdapter: ImageAdapter, imageResource: Int) {
        val gridSize = level + 3
        val row = position / gridSize
        val col = position % gridSize
        val directions = listOf(-1, 0, 1, 0, -1)
        val cellValue = imageAdapter.cells[position].number

        for (i in 0 until 4) {
            val newRow = row + directions[i]
            val newCol = col + directions[i + 1]

            if (newRow in 0 until gridSize && newCol in 0 until gridSize) {
                val newPosition = newRow * gridSize + newCol
                val newCell = imageAdapter.cells[newPosition]

                if (newCell.image == imageResource || newCell.image == R.drawable.empty) {
                    newCell.number += cellValue
                    newCell.image = imageResource  // Обновляем изображение ячейки
                }
            }
        }
        imageAdapter.notifyDataSetChanged() // Уведомляем адаптер об изменении данных
    }

    private fun getAvailableComputerCells(imageAdapter: ImageAdapter): List<Int> {
        return imageAdapter.cells.indices.filter { imageAdapter.cells[it].image == R.drawable.computer && imageAdapter.hasEmptyNeighbor(it) }
    }
}
