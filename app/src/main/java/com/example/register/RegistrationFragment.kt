package com.example.register

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.register.database.AppDatabase
import com.example.register.database.User
import kotlinx.coroutines.launch
import java.util.*

class RegistrationFragment : Fragment() {

    private lateinit var fullNameEditText: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var courseSpinner: Spinner
    private lateinit var difficultySeekBar: SeekBar
    private lateinit var difficultyValueTextView: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var zodiacSignImageView: ImageView
    private lateinit var registerButton: Button
    private lateinit var displayTextView: TextView

    private lateinit var database: AppDatabase
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация базы данных
        database = AppDatabase.getInstance(requireContext())

        // Initialize views
        fullNameEditText = view.findViewById(R.id.fullNameEditText)
        genderRadioGroup = view.findViewById(R.id.genderRadioGroup)
        courseSpinner = view.findViewById(R.id.courseSpinner)
        difficultySeekBar = view.findViewById(R.id.difficultySeekBar)
        difficultyValueTextView = view.findViewById(R.id.difficultyValueTextView)
        calendarView = view.findViewById(R.id.calendarView)
        zodiacSignImageView = view.findViewById(R.id.zodiacSignImageView)
        registerButton = view.findViewById(R.id.registerButton)
        displayTextView = view.findViewById(R.id.displayTextView)

        // Populate course spinner
        val courses = arrayOf("1st", "2nd", "3rd", "4th")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, courses)
        courseSpinner.adapter = adapter

        // Difficulty SeekBar listener
        difficultySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                difficultyValueTextView.text = "Value: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // CalendarView listener
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            updateZodiacSign()
        }

        // Register button click listener
        registerButton.setOnClickListener {
            registerPlayer()
        }

        // Initialize the difficulty value
        difficultyValueTextView.text = "Value: ${difficultySeekBar.progress}"

        updateZodiacSign() // Initialize zodiac sign on startup
    }

    private fun registerPlayer() {
        val fullName = fullNameEditText.text.toString().trim()

        if (fullName.isEmpty()) {
            Toast.makeText(requireContext(), "Введите имя", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            // Проверяем, существует ли пользователь с таким именем
            val existingUser = database.userDao().getUserByName(fullName)

            if (existingUser != null) {
                // Пользователь уже существует - устанавливаем его как текущего
                setCurrentUser(existingUser.id, existingUser.fullName)
                activity?.runOnUiThread {
                    displayRegistrationInfo(existingUser.fullName)
                    Toast.makeText(requireContext(), "Добро пожаловать обратно, ${existingUser.fullName}!", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Создаем нового пользователя
                val userId = database.userDao().insert(User(fullName = fullName))
                setCurrentUser(userId, fullName)
                activity?.runOnUiThread {
                    displayRegistrationInfo(fullName)
                    Toast.makeText(requireContext(), "Пользователь $fullName зарегистрирован!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setCurrentUser(userId: Long, userName: String) {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putLong("current_user_id", userId).apply()
        sharedPref.edit().putString("current_user_name", userName).apply()
    }

    private fun displayRegistrationInfo(userName: String) {
        val genderId = genderRadioGroup.checkedRadioButtonId
        val gender = when (genderId) {
            R.id.maleRadioButton -> "Male"
            R.id.femaleRadioButton -> "Female"
            else -> "Unknown"
        }
        val course = courseSpinner.selectedItem.toString()
        val difficulty = difficultySeekBar.progress
        val zodiacSign = getZodiacSign(selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH))

        val displayText = """
            Игрок: $userName
            Пол: $gender
            Курс: $course
            Сложность: $difficulty
            Знак зодиака: $zodiacSign
            Готов к игре! 🎮
        """.trimIndent()

        displayTextView.text = displayText
    }

    private fun updateZodiacSign() {
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)
        val zodiacSign = getZodiacSign(month, day)

        val imageResource = when (zodiacSign) {
            "Aries" -> R.drawable.aries
            "Taurus" -> R.drawable.taurus
            "Gemini" -> R.drawable.gemini
            "Cancer" -> R.drawable.cancer
            "Leo" -> R.drawable.leo
            "Virgo" -> R.drawable.virgo
            "Libra" -> R.drawable.libra
            "Scorpio" -> R.drawable.scorpio
            "Sagittarius" -> R.drawable.sagittarius
            "Capricorn" -> R.drawable.capricorn
            "Aquarius" -> R.drawable.aquarius
            "Pisces" -> R.drawable.pisces
            else -> R.drawable.ic_launcher_background
        }

        zodiacSignImageView.setImageResource(imageResource)
    }

    private fun getZodiacSign(month: Int, day: Int): String {
        return when (month) {
            0 -> if (day <= 19) "Capricorn" else "Aquarius"
            1 -> if (day <= 18) "Aquarius" else "Pisces"
            2 -> if (day <= 20) "Pisces" else "Aries"
            3 -> if (day <= 19) "Aries" else "Taurus"
            4 -> if (day <= 20) "Taurus" else "Gemini"
            5 -> if (day <= 20) "Gemini" else "Cancer"
            6 -> if (day <= 22) "Cancer" else "Leo"
            7 -> if (day <= 22) "Leo" else "Virgo"
            8 -> if (day <= 22) "Virgo" else "Libra"
            9 -> if (day <= 22) "Libra" else "Scorpio"
            10 -> if (day <= 21) "Scorpio" else "Sagittarius"
            11 -> if (day <= 21) "Sagittarius" else "Capricorn"
            else -> "Unknown"
        }
    }
}