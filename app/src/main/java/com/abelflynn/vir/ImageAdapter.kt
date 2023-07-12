package com.abelflynn.vir

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

data class Cell(
    var image: Int,
    var number: Int
)

class ImageAdapter(private val context: Context, private val level: Int) : BaseAdapter() {
    private val emptyImage = R.drawable.empty
    private val computerImage = R.drawable.computer
    private val computerBlockedImage = R.drawable.computer_100
    private val playerImage = R.drawable.player
    private val playerBlockedImage = R.drawable.player_100
    private val computer2Image = R.drawable.computer_2
    private val computer2BlockedImage = R.drawable.computer_2_100
    private val computer3Image = R.drawable.computer_3
    private val computer3BlockedImage = R.drawable.computer_3_100
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    val gridSize = level + 3

    var cells = MutableList(gridSize * gridSize) { Cell(emptyImage, 0) }.apply {
        this[0] = Cell(computerImage, 1)
        this[gridSize - 1] = Cell(computer2Image, 1)
        this[this.size - gridSize] = Cell(computer3Image, 1)
        this[this.size - 1] = Cell(playerImage, 1)
    }

    override fun getCount(): Int = gridSize * gridSize

    override fun getItem(position: Int): Any? = null

    override fun getItemId(position: Int): Long = 0L

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val imageView: ImageView
        val textView: TextView

        if (convertView == null) {
            view = inflater.inflate(R.layout.grid_item, parent, false)
            imageView = view.findViewById(R.id.image)
            textView = view.findViewById(R.id.text)
            view.tag = ViewHolder(imageView, textView)
        } else {
            view = convertView
            val viewHolder = view.tag as ViewHolder
            imageView = viewHolder.image
            textView = viewHolder.text
        }

        val cell = cells[position]
        if (cell.image == R.drawable.player || cell.image == R.drawable.computer || cell.image == R.drawable.computer_2 || cell.image == R.drawable.computer_3) {
            imageView.setImageResource(if (hasEmptyNeighbor(position)) cell.image else getBlockedCellImage(cell.image))
        } else {
            imageView.setImageResource(cell.image)
        }

        val layoutParams = view.layoutParams
        layoutParams.height = parent.height / gridSize // Измените высоту в соответствии с уровнем
        view.layoutParams = layoutParams

        textView.text = if (cell.number > 0) cell.number.toString() else ""

        return view
    }

    private fun getBlockedCellImage(originalImage: Int): Int {
        return when (originalImage) {
            playerImage -> playerBlockedImage
            computerImage -> computerBlockedImage
            computer2Image -> computer2BlockedImage
            computer3Image -> computer3BlockedImage
            else -> originalImage
        }
    }

    fun hasEmptyNeighbor(position: Int): Boolean {
        val row = position / gridSize
        val col = position % gridSize
        val directions = listOf(-1, 0, 1, 0, -1)

        for (i in 0 until 4) {
            val newRow = row + directions[i]
            val newCol = col + directions[i + 1]

            if (newRow in 0 until gridSize && newCol in 0 until gridSize) {
                val newPosition = newRow * gridSize + newCol
                val newCell = cells[newPosition]

                if (newCell.image == emptyImage) {
                    return true
                }
            }
        }

        return false
    }

    private data class ViewHolder(val image: ImageView, val text: TextView)
}
