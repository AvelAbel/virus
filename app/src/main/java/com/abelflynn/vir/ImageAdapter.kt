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


class ImageAdapter(private val context: Context) : BaseAdapter() {
    private val emptyImage = R.drawable.empty
    private val computerImage = R.drawable.computer
    private val computerBlockedImage = R.drawable.computer_100
    private val playerImage = R.drawable.player
    private val playerBlockedImage = R.drawable.player_100
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    var cells = MutableList(16) { Cell(emptyImage, 0) }.apply {
        this[0] = Cell(computerImage, 1)
        this[15] = Cell(playerImage, 1)
    }

    override fun getCount(): Int = 16

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
        if (hasEmptyNeighbor(position)) {
            imageView.setImageResource(cell.image)
        } else {
            when (cell.image) {
                playerImage -> imageView.setImageResource(playerBlockedImage)
                computerImage -> imageView.setImageResource(computerBlockedImage)
                else -> imageView.setImageResource(cell.image)
            }
        }

        val layoutParams = view.layoutParams
        layoutParams.height = parent.height / 4
        view.layoutParams = layoutParams

        textView.text = if (cell.number > 0) cell.number.toString() else ""

        return view
    }

    fun hasEmptyNeighbor(position: Int): Boolean {
        val row = position / 4
        val col = position % 4
        val directions = listOf(-1, 0, 1, 0, -1)

        for (i in 0 until 4) {
            val newRow = row + directions[i]
            val newCol = col + directions[i + 1]

            if (newRow in 0 until 4 && newCol in 0 until 4) {
                val newPosition = newRow * 4 + newCol
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