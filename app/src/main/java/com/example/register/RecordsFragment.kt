package com.example.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.register.database.AppDatabase
import kotlinx.coroutines.launch

class RecordsFragment : Fragment() {

    private lateinit var recordsListView: ListView
    private lateinit var emptyTextView: TextView
    private lateinit var database: AppDatabase

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

        database = AppDatabase.getInstance(requireContext())
        loadTopScores()
    }

    private fun loadTopScores() {
        lifecycleScope.launch {
            val topScores = database.scoreDao().getTopScoresWithUsers(20)

            activity?.runOnUiThread {
                if (topScores.isEmpty()) {
                    emptyTextView.visibility = View.VISIBLE
                    recordsListView.visibility = View.GONE
                } else {
                    emptyTextView.visibility = View.GONE
                    recordsListView.visibility = View.VISIBLE

                    val adapter = RecordsAdapter(requireContext(), topScores)
                    recordsListView.adapter = adapter
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTopScores() // Обновляем при каждом открытии вкладки
    }
}