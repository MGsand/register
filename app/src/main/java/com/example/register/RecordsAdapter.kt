package com.example.register

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.register.database.ScoreWithUser

class RecordsAdapter(
    context: Context,
    private val scores: List<ScoreWithUser>
) : ArrayAdapter<ScoreWithUser>(context, 0, scores) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_record, parent, false)

        val score = scores[position]

        view.findViewById<TextView>(R.id.tvPosition).text = "${position + 1}."
        view.findViewById<TextView>(R.id.tvUserName).text = score.userName
        view.findViewById<TextView>(R.id.tvScore).text = "Очки: ${score.score}"
        view.findViewById<TextView>(R.id.tvDifficulty).text = "Сложность: ${score.difficulty}"
        view.findViewById<TextView>(R.id.tvAccuracy).text = "Точность: ${String.format("%.1f", score.accuracy)}%"
        view.findViewById<TextView>(R.id.tvBugsDestroyed).text = "Уничтожено: ${score.bugsDestroyed}"

        when (position) {
            0 -> view.findViewById<TextView>(R.id.tvPosition).setTextColor(0xFFFFD700.toInt())
            1 -> view.findViewById<TextView>(R.id.tvPosition).setTextColor(0xFFC0C0C0.toInt())
            2 -> view.findViewById<TextView>(R.id.tvPosition).setTextColor(0xFFCD7F32.toInt())
            else -> view.findViewById<TextView>(R.id.tvPosition).setTextColor(0xFF666666.toInt())
        }

        return view
    }

    override fun getCount(): Int = scores.size
}