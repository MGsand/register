package com.example.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
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
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Заменяем setContentView на inflate
        val view = inflater.inflate(R.layout.fragment_registration, container, false)

        // Initialize views - теперь через view.findViewById
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

        return view
    }

    private fun registerPlayer() {
        val fullName = fullNameEditText.text.toString()
        val genderId = genderRadioGroup.checkedRadioButtonId
        val gender = when (genderId) {
            R.id.maleRadioButton -> "Male"
            R.id.femaleRadioButton -> "Female"
            else -> "Unknown"
        }
        val course = courseSpinner.selectedItem.toString()
        val difficulty = difficultySeekBar.progress
        val zodiacSign = getZodiacSign(selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH))

        val playerData = PlayerData(fullName, gender, course, difficulty, selectedDate, zodiacSign)

        val displayText = """
            Full Name: ${playerData.fullName}
            Gender: ${playerData.gender}
            Course: ${playerData.course}
            Difficulty: ${playerData.difficulty}
            Date of Birth: ${selectedDate.get(Calendar.DAY_OF_MONTH)}/${selectedDate.get(Calendar.MONTH) + 1}/${selectedDate.get(Calendar.YEAR)}
            Zodiac Sign: ${playerData.zodiacSign}
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

// Data class остается как есть
data class PlayerData(
    val fullName: String,
    val gender: String,
    val course: String,
    val difficulty: Int,
    val dob: Calendar,
    val zodiacSign: String
)