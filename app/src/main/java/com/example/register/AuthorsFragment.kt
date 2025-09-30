package com.example.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment

class AuthorsFragment : Fragment() {


    private val authorsList = listOf(
        Author("Майоров Глеб", "Тимлид, разраб и тестировщик", R.drawable.me),

    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_authors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView = view.findViewById<ListView>(R.id.listViewAuthors)
        val adapter = AuthorAdapter(requireContext(), authorsList)
        listView.adapter = adapter


        listView.setOnItemClickListener { _, _, position, _ ->
            val author = authorsList[position]
            showAuthorDetails(author)
        }
    }

    private fun showAuthorDetails(author: Author) {
        // Можно сделать диалог или Toast с дополнительной информацией
        android.widget.Toast.makeText(
            requireContext(),
            "${author.name}\n${author.role}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    // Data class для авторов
    data class Author(
        val name: String,
        val role: String,
        val photoRes: Int
    )

    // Кастомизированный адаптер для списка
    class AuthorAdapter(
        private val context: android.content.Context,
        private val authors: List<Author>
    ) : ArrayAdapter<Author>(context, 0, authors) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            val holder: ViewHolder

            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_author, parent, false)
                holder = ViewHolder()
                holder.photoImageView = view.findViewById(R.id.ivAuthorPhoto)
                holder.nameTextView = view.findViewById(R.id.tvAuthorName)
                holder.roleTextView = view.findViewById(R.id.tvAuthorRole)
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }

            val author = authors[position]

            holder.photoImageView.setImageResource(author.photoRes)
            holder.nameTextView.text = author.name
            holder.roleTextView.text = author.role

            return view!!
        }

        private class ViewHolder {
            lateinit var photoImageView: ImageView
            lateinit var nameTextView: TextView
            lateinit var roleTextView: TextView
        }
    }
}