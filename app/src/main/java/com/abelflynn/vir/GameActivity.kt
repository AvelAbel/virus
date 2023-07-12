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
    private var computer2Score = 1
    private var computer3Score = 1
    private val random = Random()
    private var level = 1
    private lateinit var imageAdapter: ImageAdapter


    // функция для проверки окончания игры и отображения результатов
    private fun checkGameOver() {
        val scores = listOf(playerScore, computerScore, computer2Score, computer3Score)
        val maxScore = scores.maxOrNull() ?: 0
        val winner = when (maxScore) {
            playerScore -> "player"
            computerScore -> "computer1"
            computer2Score -> "computer2"
            computer3Score -> "computer3"
            else -> "draw"  // на случай ничейного результата
        }

        val message = when (winner) {
            "player" -> "Вы выиграли с счетом $playerScore против $computerScore, $computer2Score, $computer3Score"
            "computer1" -> "Robot выиграл. Счет $computerScore против $playerScore, $computer2Score, $computer3Score"
            "computer2" -> "Terminator выиграл. Счет $computer2Score против $playerScore, $computerScore, $computer3Score"
            "computer3" -> "Transformer выиграл. Счет $computer3Score против $playerScore, $computerScore, $computer2Score"
            else -> "Ничья."
        }

        val intent: Intent
        val okHandler: DialogInterface.OnClickListener
        if (winner == "player") {
            intent = Intent(this, GameActivity::class.java)
            intent.putExtra("level", level + 1)
            okHandler = DialogInterface.OnClickListener { _, _ ->
                startActivity(intent)
                finish()
            }
        } else {
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
        supportActionBar?.hide()
        setContentView(R.layout.activity_game)
        val computer2ScoreTextView: TextView = findViewById(R.id.computer2_score)
        val computer3ScoreTextView: TextView = findViewById(R.id.computer3_score)
        imageAdapter = ImageAdapter(this, level)

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
        computerScoreTextView.text = "Robot: $computerScore"
        playerScoreTextView.text = "Human: $playerScore"

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
                playerScoreTextView.text = "Human: $playerScore"

                // Проверяем, есть ли пустые ячейки и доступные ходы у компьютера в начале его хода
                var emptyCells = imageAdapter.cells.count { it.image == R.drawable.empty }
                if (emptyCells == 0) {
                    checkGameOver()
                    return@setOnItemClickListener
                }

                // Ходы компьютеров
                val availableCellsComputer1 = getAvailableComputerCells(imageAdapter, R.drawable.computer)
                val availableCellsComputer2 = getAvailableComputerCells(imageAdapter, R.drawable.computer_2)
                val availableCellsComputer3 = getAvailableComputerCells(imageAdapter, R.drawable.computer_3)

                if (availableCellsComputer1.isNotEmpty()) {
                    val computer1Position = availableCellsComputer1[random.nextInt(availableCellsComputer1.size)]
                    updateCells(computer1Position, imageAdapter, R.drawable.computer)

                    // Суммируем значения всех ячеек компьютера
                    computerScore = imageAdapter.cells.filter { it.image == R.drawable.computer }.sumBy { it.number }
                    computerScoreTextView.text = "Robot: $computerScore"
                }

                if (availableCellsComputer2.isNotEmpty()) {
                    val computer2Position = availableCellsComputer2[random.nextInt(availableCellsComputer2.size)]
                    updateCells(computer2Position, imageAdapter, R.drawable.computer_2)

                    // Суммируем значения всех ячеек компьютера 2
                    computer2Score = imageAdapter.cells.filter { it.image == R.drawable.computer_2 }.sumBy { it.number }
                    computer2ScoreTextView.text = "Terminator: $computer2Score"
                }

                if (availableCellsComputer3.isNotEmpty()) {
                    val computer3Position = availableCellsComputer3[random.nextInt(availableCellsComputer3.size)]
                    updateCells(computer3Position, imageAdapter, R.drawable.computer_3)

                    // Суммируем значения всех ячеек компьютера 3
                    computer3Score = imageAdapter.cells.filter { it.image == R.drawable.computer_3 }.sumBy { it.number }
                    computer3ScoreTextView.text = "Transformer: $computer3Score"
                }

                // Проверяем, есть ли пустые ячейки и доступные ходы у игрока в начале его хода после хода компьютера
                emptyCells = imageAdapter.cells.count { it.image == R.drawable.empty }
                if (emptyCells == 0) {
                    checkGameOver()
                } else if (!hasPlayerAvailableMoves(imageAdapter)) {
                    // Если есть пустые ячейки, но у игрока нет доступных ходов, компьютер выигрывает
                    checkGameOver()
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

    private fun getAvailableComputerCells(imageAdapter: ImageAdapter, imageResource: Int): List<Int> {
        val availableCells = imageAdapter.cells.indices.filter { imageAdapter.cells[it].image == imageResource && imageAdapter.hasEmptyNeighbor(it) }
        if (availableCells.isEmpty()) {
            return availableCells
        }
        val maxValue = availableCells.maxOf { imageAdapter.cells[it].number }
        return availableCells.filter { imageAdapter.cells[it].number == maxValue }
    }
}

