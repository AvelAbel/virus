package com.abelflynn.vir

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Random
import androidx.appcompat.app.AlertDialog


class GameActivity : AppCompatActivity() {

    private var computerScore = 1  // начальные очки компьютера
    private var playerScore = 1  // начальные очки игрока
    private val random = Random()
    private var level = 1

    // функция для проверки окончания игры и отображения результатов
    private fun checkGameOver(movesAvailable: Boolean, currentPlayer: String) {
        var winner: String
        // Если ходов больше нет
        if (!movesAvailable) {
            winner = if (currentPlayer == "player") {
                "computer" // Если ходы закончились у игрока, побеждает компьютер
            } else {
                "player" // Если ходы закончились у компьютера, побеждает игрок
            }
        } else {
            winner = if (playerScore > computerScore) {
                "player" // Игрок выиграл
            } else {
                "computer" // Компьютер выиграл
            }
        }

        val message = if (winner == "player") {
            "Вы выиграли с счетом $playerScore против $computerScore"
        } else {
            "Вы проиграли. Счет $computerScore против $playerScore"
        }

        val intent: Intent
        val okHandler: DialogInterface.OnClickListener
        if (winner == "player") {
            // Переходим к следующему уровню
            intent = Intent(this, GameActivity::class.java)
            intent.putExtra("level", level + 1)
            okHandler = DialogInterface.OnClickListener { _, _ ->
                startActivity(intent)
                finish()
            }
        } else {
            // Возвращаемся к главному экрану
            intent = Intent(this, MainActivity::class.java)
            val sharedPref = getSharedPreferences("MY_PREFERENCES", Context.MODE_PRIVATE)
            val record = sharedPref.getInt("record", 1)
            if (level > record) {
                with (sharedPref.edit()) {
                    putInt("record", level)
                    apply()
                }
            }
            okHandler = DialogInterface.OnClickListener { _, _ ->
                startActivity(intent)
                finish()
            }
        }
        showGameOverDialog(message, okHandler)
    }


    private fun showGameOverDialog(message: String, okHandler: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle("Игра окончена")
            .setMessage(message)
            .setPositiveButton("OK", okHandler)
            .setCancelable(false)
            .show()
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
                // Проверяем, есть ли пустые ячейки и доступные ходы у игрока в начале его хода
                var emptyCells = imageAdapter.cells.count { it.image == R.drawable.empty }
                if (emptyCells == 0 || !hasPlayerAvailableMoves(imageAdapter)) {
                    // Если нет пустых ячеек или доступных ходов у игрока, игра окончена
                    checkGameOver(emptyCells != 0 && hasPlayerAvailableMoves(imageAdapter), "player")
                    return@setOnItemClickListener
                }

                updateCells(position, imageAdapter, R.drawable.player)

                // Суммируем значения всех ячеек игрока
                playerScore = imageAdapter.cells.filter { it.image == R.drawable.player }.sumBy { it.number }
                playerScoreTextView.text = "Игрок: $playerScore"

                // Проверяем, есть ли пустые ячейки и доступные ходы у компьютера в начале его хода
                emptyCells = imageAdapter.cells.count { it.image == R.drawable.empty }
                if (emptyCells == 0 || !hasComputerAvailableMoves(imageAdapter)) {
                    // Если нет пустых ячеек или доступных ходов у компьютера, игра окончена
                    checkGameOver(emptyCells != 0 && hasComputerAvailableMoves(imageAdapter), "computer")
                    return@setOnItemClickListener
                }

                // Ход компьютера
                val availableCells = getAvailableComputerCells(imageAdapter)
                val computerPosition = availableCells[random.nextInt(availableCells.size)]
                updateCells(computerPosition, imageAdapter, R.drawable.computer)

                // Суммируем значения всех ячеек компьютера
                computerScore = imageAdapter.cells.filter { it.image == R.drawable.computer }.sumBy { it.number }
                computerScoreTextView.text = "Компьютер: $computerScore"

                // Проверяем, есть ли пустые ячейки и доступные ходы у игрока в начале его хода после хода компьютера
                emptyCells = imageAdapter.cells.count { it.image == R.drawable.empty }
                if (emptyCells == 0 || !hasPlayerAvailableMoves(imageAdapter)) {
                    // Если нет пустых ячеек или доступных ходов у игрока, игра окончена
                    checkGameOver(emptyCells != 0 && hasPlayerAvailableMoves(imageAdapter), "player")
                    return@setOnItemClickListener
                }
            }
            imageAdapter.notifyDataSetChanged()
        }

    }

    private fun hasPlayerAvailableMoves(imageAdapter: ImageAdapter): Boolean {
        return imageAdapter.cells.indices.any { imageAdapter.cells[it].image == R.drawable.player && imageAdapter.hasEmptyNeighbor(it) }
    }

    private fun hasComputerAvailableMoves(imageAdapter: ImageAdapter): Boolean {
        return imageAdapter.cells.indices.any { imageAdapter.cells[it].image == R.drawable.computer && imageAdapter.hasEmptyNeighbor(it) }
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
        val availableCells = imageAdapter.cells.indices.filter { imageAdapter.cells[it].image == R.drawable.computer && imageAdapter.hasEmptyNeighbor(it) }
        if (availableCells.isEmpty()) {
            return availableCells
        }
        val maxValue = availableCells.maxOf { imageAdapter.cells[it].number }
        return availableCells.filter { imageAdapter.cells[it].number == maxValue }
    }
}

