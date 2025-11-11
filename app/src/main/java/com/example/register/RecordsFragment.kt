package com.example.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment

class RecordsFragment : Fragment() {

    private lateinit var recordsListView: ListView
    private lateinit var emptyTextView: TextView
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recordsListView = view.findViewById(R.id.recordsListView)
        emptyTextView = view.findViewById(R.id.emptyTextView)

        databaseHelper = DatabaseHelper(requireContext())
        loadTopScores()
    }

    private fun loadTopScores() {
        val topScores = databaseHelper.getTopScores()

        if (topScores.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
            recordsListView.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            recordsListView.visibility = View.VISIBLE

            val adapter = RecordsAdapter(requireContext(), topScores) // ← передаем context
            recordsListView.adapter = adapter
        }
    }

    // Вложенный класс с конструктором, принимающим Context
    private class RecordsAdapter(
        private val context: android.content.Context, // ← получаем context извне
        private val scores: List<GameScore>
    ) : ArrayAdapter<GameScore>(context, 0, scores) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context) // ← используем context
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
}