package com.example.register

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.register.data.GoldRepository
import com.example.register.database.AppDatabase
import com.example.register.database.Score
import com.example.register.database.User
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random



class GameFragment : Fragment(), SensorEventListener {

    private lateinit var gameContainer: ViewGroup
    private lateinit var scoreTextView: TextView
    private lateinit var bugsCountTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var menuButton: Button

    private var score = 0
    private var bugsDestroyed = 0
    private var bugsMissed = 0
    private var timeLeft = 60
    private var gameActive = false
    private var currentBugs = mutableListOf<Bug>()
    private var currentBonus: Bonus? = null
    private var gamePaused = false

    private var gameSpeed = 5
    private var maxBugs = 10
    private var bonusInterval = 30
    private var roundDuration = 60

    private val handler = Handler(Looper.getMainLooper())
    private val bugTypes = listOf(
        R.drawable.bug1,
        R.drawable.bug2,
        R.drawable.bug3,
        R.drawable.bug4
    )

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private var isGravityActive = false
    private var gravityStartTime = 0L
    private val GRAVITY_DURATION = 10000L

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var vibrator: Vibrator

    private lateinit var database: AppDatabase
    private var currentUserId: Long = 0
    private var currentUserName = "Гость"

    private var goldBugRate: Double = 4500.0
    private var currentGoldBug: GoldBug? = null

    private data class GoldBug(
        val imageView: ImageView,
        val value: Int
    )

    private fun startGoldBugSpawning() {
        handler.post(object : Runnable {
            override fun run() {
                if (gameActive && !gamePaused && currentGoldBug == null) {
                    spawnGoldBug()
                    handler.postDelayed(this, 20000) // Каждые 20 секунд
                } else if (gameActive && !gamePaused) {
                    handler.postDelayed(this, 5000)
                }
            }
        })
    }

    private suspend fun loadGoldRate() {
        try {
            val repository = GoldRepository()
            goldBugRate = repository.getGoldRate()
        } catch (e: Exception) {
            goldBugRate = 4500.0 // Значение по умолчанию
        }
    }

    private fun spawnGoldBug() {
        val bugSize = Random.nextInt(150, 250)
        val bug = ImageView(requireContext())

        val layoutParams = ViewGroup.LayoutParams(bugSize, bugSize)
        bug.layoutParams = layoutParams

        bug.setImageResource(R.drawable.gold_bug) // Золотая картинка
        bug.scaleType = ImageView.ScaleType.FIT_CENTER

        val value = (goldBugRate / 100).toInt() // Очки пропорциональны курсу

        val x = Random.nextInt(gameContainer.width - bugSize).toFloat()
        val y = Random.nextInt(gameContainer.height - bugSize).toFloat()
        bug.x = x
        bug.y = y

        // Золотое свечение
        bug.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(1000).start()

        val glowAnimator = ObjectAnimator.ofFloat(bug, "alpha", 1f, 0.7f, 1f)
        glowAnimator.duration = 800
        glowAnimator.repeatCount = ObjectAnimator.INFINITE
        glowAnimator.start()

        bug.setOnClickListener {
            collectGoldBug(value)
        }

        gameContainer.addView(bug)
        currentGoldBug = GoldBug(bug, value)

        handler.postDelayed({
            if (currentGoldBug != null) {
                handleMiss()
            }
        }, 15000) // Исчезает через 15 секунд
    }

    private fun collectGoldBug(value: Int) {
        currentGoldBug?.let { goldBug ->
            score += value
            updateUI()

            showTemporaryMessage("+$value очков за золотого таракана!")

            goldBug.imageView.animate()
                .scaleX(2f).scaleY(2f).alpha(0f)
                .setDuration(500)
                .withEndAction {
                    gameContainer.removeView(goldBug.imageView)
                }.start()

            currentGoldBug = null
        }
    }

    private data class Bug(
        val imageView: ImageView,
        var currentX: Float,
        var currentY: Float,
        var targetX: Float,
        var targetY: Float,
        var speed: Float,
        var animator: ValueAnimator? = null
    )

    private data class Bonus(
        val imageView: ImageView,
        val type: String = "gravity"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameContainer = view.findViewById(R.id.gameContainer)
        scoreTextView = view.findViewById(R.id.scoreTextView)
        bugsCountTextView = view.findViewById(R.id.bugsCountTextView)
        timerTextView = view.findViewById(R.id.timerTextView)
        menuButton = view.findViewById(R.id.menuButton)

        database = AppDatabase.getInstance(requireContext())

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER)
        if (sensors.isNotEmpty()) {
            accelerometer = sensors[0]
        } else {
            // Акселерометр не доступен
            Toast.makeText(requireContext(), "Акселерометр не доступен на этом устройстве", Toast.LENGTH_LONG).show()
            return
        }

        // Исправленная инициализация вибрации
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        lifecycleScope.launch {
            loadCurrentUser()
        }

        gameContainer.setOnClickListener {
            if (gameActive && !gamePaused) {
                handleMiss()
            }
        }

        menuButton.setOnClickListener {
            showGameMenu()
        }

        gameContainer.post {
            loadGameSettings()
            startGame()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isGravityActive || !gameActive || gamePaused) return

        event?.let { sensorEvent ->
            if (sensorEvent.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = sensorEvent.values[0]
                val y = sensorEvent.values[1]

                applyGravityToBugs(x, y)

                if (System.currentTimeMillis() - gravityStartTime > GRAVITY_DURATION) {
                    stopGravityEffect()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun applyGravityToBugs(x: Float, y: Float) {
        currentBugs.forEach { bug ->
            val gravityStrength = 8.0f
            val newX = bug.imageView.x + x * gravityStrength
            val newY = bug.imageView.y - y * gravityStrength

            val boundedX = newX.coerceIn(0f, (gameContainer.width - bug.imageView.width).toFloat())
            val boundedY = newY.coerceIn(0f, (gameContainer.height - bug.imageView.height).toFloat())

            bug.imageView.x = boundedX
            bug.imageView.y = boundedY
            bug.currentX = boundedX
            bug.currentY = boundedY
        }
    }

    private fun startGravityEffect() {
        if (isGravityActive) return

        isGravityActive = true
        gravityStartTime = System.currentTimeMillis()

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        playBugScream()

        if (vibrator.hasVibrator()) {
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }

        showTemporaryMessage("Гравитация активирована! Наклоняйте телефон!")
    }

    private fun stopGravityEffect() {
        if (!isGravityActive) return

        isGravityActive = false
        sensorManager.unregisterListener(this)

        showTemporaryMessage("Гравитация деактивирована")
    }

    private fun playBugScream() {
        try {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.bug_scream)
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startBonusSpawning() {
        handler.post(object : Runnable {
            override fun run() {
                if (gameActive && !gamePaused && currentBonus == null) {
                    spawnBonus()
                    handler.postDelayed(this, 15000)
                } else if (gameActive && !gamePaused) {
                    handler.postDelayed(this, 5000)
                }
            }
        })
    }

    private fun spawnBonus() {
        val bonusSize = 80
        val bonus = ImageView(requireContext())

        val layoutParams = ViewGroup.LayoutParams(bonusSize, bonusSize)
        bonus.layoutParams = layoutParams

        bonus.setImageResource(R.drawable.ic_launcher_foreground)
        bonus.scaleType = ImageView.ScaleType.FIT_CENTER

        val x = Random.nextInt(gameContainer.width - bonusSize).toFloat()
        val y = Random.nextInt(gameContainer.height - bonusSize).toFloat()

        bonus.x = x
        bonus.y = y

        bonus.alpha = 0f
        bonus.scaleX = 0f
        bonus.scaleY = 0f

        bonus.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .start()

        val blinkAnimator = ObjectAnimator.ofFloat(bonus, "alpha", 1f, 0.3f, 1f)
        blinkAnimator.duration = 1000
        blinkAnimator.repeatCount = ObjectAnimator.INFINITE
        blinkAnimator.start()

        bonus.setOnClickListener {
            collectBonus()
        }

        gameContainer.addView(bonus)
        currentBonus = Bonus(bonus, "gravity")

        handler.postDelayed({
            if (currentBonus != null) {
                removeBonus()
            }
        }, 10000)
    }

    private fun collectBonus() {
        currentBonus?.let { bonus ->
            val scaleX = ObjectAnimator.ofFloat(bonus.imageView, "scaleX", 1f, 2f, 0f)
            val scaleY = ObjectAnimator.ofFloat(bonus.imageView, "scaleY", 1f, 2f, 0f)
            val rotation = ObjectAnimator.ofFloat(bonus.imageView, "rotation", 0f, 360f)

            scaleX.duration = 500
            scaleY.duration = 500
            rotation.duration = 500

            scaleX.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    gameContainer.removeView(bonus.imageView)
                }
            })

            scaleX.start()
            scaleY.start()
            rotation.start()

            startGravityEffect()

            currentBonus = null

            score += 50
            updateUI()
            showTemporaryMessage("+50 очков за бонус!")
        }
    }

    private fun removeBonus() {
        currentBonus?.let { bonus ->
            bonus.imageView.animate()
                .alpha(0f)
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(500)
                .withEndAction {
                    gameContainer.removeView(bonus.imageView)
                }
                .start()
            currentBonus = null
        }
    }

    private suspend fun loadCurrentUser() {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getLong("current_user_id", 0L)

        if (userId > 0) {
            val user = database.userDao().getUserById(userId)
            if (user != null) {
                currentUserId = user.id
                currentUserName = user.fullName
            } else {
                createGuestUser()
            }
        } else {
            createGuestUser()
        }

        activity?.runOnUiThread {
            showTemporaryMessage("Добро пожаловать, $currentUserName!")
        }
    }

    private suspend fun createGuestUser() {
        currentUserId = database.userDao().insert(User(fullName = "Гость"))
        currentUserName = "Гость"

        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putLong("current_user_id", currentUserId).apply()
        sharedPref.edit().putString("current_user_name", currentUserName).apply()
    }

    private fun loadGameSettings() {
        val sharedPref = requireActivity().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        gameSpeed = sharedPref.getInt("game_speed", 5)
        maxBugs = sharedPref.getInt("max_cockroaches", 10)
        bonusInterval = sharedPref.getInt("bonus_interval", 30)
        roundDuration = sharedPref.getInt("round_duration", 60)
        timeLeft = roundDuration
    }

    private fun startGame() {
        gameActive = true
        gamePaused = false
        score = 0
        bugsDestroyed = 0
        bugsMissed = 0
        timeLeft = roundDuration

        updateUI()
        startTimer()
        startBugSpawning()
        startBonusSpawning()
        startGoldBugSpawning()
    }

    private fun pauseGame() {
        gamePaused = true
        currentBugs.forEach { it.animator?.pause() }
        if (isGravityActive) {
            sensorManager.unregisterListener(this)
        }
        handler.removeCallbacksAndMessages(null)
    }

    private fun resumeGame() {
        gamePaused = false
        currentBugs.forEach { it.animator?.resume() }
        if (isGravityActive) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
        startTimer()
        startBugSpawning()
        startBonusSpawning()
    }

    private fun handleMiss() {
        if (!gameActive || gamePaused) return

        score -= 3
        bugsMissed++

        showMissEffect()
        updateUI()

        if (bugsMissed % 5 == 0) {
            showTemporaryMessage("Промах! -3 очка")
        }
    }

    private fun showMissEffect() {
        val missText = TextView(requireContext())
        missText.text = "-3"
        missText.setTextColor(0xFFFF4444.toInt())
        missText.textSize = 24f
        missText.x = gameContainer.width / 2f - 50f
        missText.y = gameContainer.height / 2f - 50f

        gameContainer.addView(missText)

        val fadeOut = ObjectAnimator.ofFloat(missText, "alpha", 1f, 0f)
        val moveUp = ObjectAnimator.ofFloat(missText, "translationY", 0f, -100f)

        fadeOut.duration = 1000
        moveUp.duration = 1000

        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                gameContainer.removeView(missText)
            }
        })

        fadeOut.start()
        moveUp.start()
    }

    private fun showTemporaryMessage(message: String) {
        val messageText = TextView(requireContext())
        messageText.text = message
        messageText.setTextColor(0xFFFFFFFF.toInt())
        messageText.textSize = 18f
        messageText.setBackgroundColor(0xAA000000.toInt())
        messageText.setPadding(20, 10, 20, 10)

        messageText.x = gameContainer.width / 2f - 150f
        messageText.y = gameContainer.height / 4f

        gameContainer.addView(messageText)

        handler.postDelayed({
            val fadeOut = ObjectAnimator.ofFloat(messageText, "alpha", 1f, 0f)
            fadeOut.duration = 500
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    gameContainer.removeView(messageText)
                }
            })
            fadeOut.start()
        }, 2000)
    }

    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                if (gameActive && !gamePaused && timeLeft > 0) {
                    timeLeft--
                    updateUI()

                    if (timeLeft == 10) {
                        showTemporaryMessage("Осталось 10 секунд!")
                    }

                    handler.postDelayed(this, 1000)
                } else if (timeLeft <= 0) {
                    endGame()
                }
            }
        })
    }

    private fun startBugSpawning() {
        handler.post(object : Runnable {
            override fun run() {
                if (gameActive && !gamePaused && currentBugs.size < maxBugs) {
                    spawnBug()
                    val spawnDelay = (2000 - (gameSpeed * 150)).toLong()
                    handler.postDelayed(this, spawnDelay)
                } else if (gameActive && !gamePaused) {
                    handler.postDelayed(this, 500)
                }
            }
        })
    }

    private fun spawnBug() {
        val bugSize = Random.nextInt(120, 200)
        val bug = ImageView(requireContext())

        val layoutParams = ViewGroup.LayoutParams(bugSize, bugSize)
        bug.layoutParams = layoutParams

        val bugDrawable = bugTypes[Random.nextInt(bugTypes.size)]
        bug.setImageResource(bugDrawable)
        bug.scaleType = ImageView.ScaleType.FIT_CENTER

        bug.isClickable = true
        bug.isFocusable = true
        val touchPadding = 15
        bug.setPadding(touchPadding, touchPadding, touchPadding, touchPadding)

        val startX = when (Random.nextInt(4)) {
            0 -> -bugSize.toFloat()
            1 -> gameContainer.width.toFloat()
            else -> Random.nextInt(gameContainer.width - bugSize).toFloat()
        }

        val startY = when (Random.nextInt(4)) {
            0 -> -bugSize.toFloat()
            1 -> gameContainer.height.toFloat()
            else -> Random.nextInt(gameContainer.height - bugSize).toFloat()
        }

        bug.x = startX
        bug.y = startY

        bug.alpha = 0f
        bug.animate().alpha(1f).setDuration(500).start()

        val targetX = Random.nextInt(gameContainer.width - bugSize).toFloat()
        val targetY = Random.nextInt(gameContainer.height - bugSize).toFloat()

        val baseSpeed = 0.25f
        val speedFromSettings = gameSpeed * 0.03f
        val randomVariation = Random.nextFloat() * 0.15f
        val speed = baseSpeed + speedFromSettings + randomVariation

        val bugData = Bug(bug, startX, startY, targetX, targetY, speed)

        bug.setOnClickListener {
            if (!gamePaused) {
                destroyBug(bugData, true)
            }
        }

        gameContainer.addView(bug)
        currentBugs.add(bugData)

        startBugMovement(bugData)

        handler.postDelayed({
            if (currentBugs.contains(bugData)) {
                destroyBug(bugData, false)
                if (gameActive && !gamePaused) {
                    handleMiss()
                }
            }
        }, 7000)
    }

    private fun startBugMovement(bug: Bug) {
        val animator = ValueAnimator.ofFloat(0f, 1f)

        val baseDuration = 2500L
        val speedFactor = (1.0 / bug.speed.toDouble()).toFloat()
        val animationDuration = (baseDuration * speedFactor).toLong()

        animator.duration = animationDuration

        animator.addUpdateListener { animation ->
            if (!gameActive || !currentBugs.contains(bug) || gamePaused) {
                return@addUpdateListener
            }

            val fraction = animation.animatedValue as Float

            val movementSmoothness = 0.04f

            bug.currentX = bug.imageView.x + (bug.targetX - bug.imageView.x) * movementSmoothness
            bug.currentY = bug.imageView.y + (bug.targetY - bug.imageView.y) * movementSmoothness

            bug.currentX = bug.currentX.coerceIn(0f, (gameContainer.width - bug.imageView.width).toFloat())
            bug.currentY = bug.currentY.coerceIn(0f, (gameContainer.height - bug.imageView.height).toFloat())

            bug.imageView.x = bug.currentX
            bug.imageView.y = bug.currentY

            val distanceToTarget = sqrt(
                (bug.targetX - bug.currentX) * (bug.targetX - bug.currentX) +
                        (bug.targetY - bug.currentY) * (bug.targetY - bug.currentY)
            )

            if (distanceToTarget < 25 || fraction >= 0.99f) {
                bug.targetX = Random.nextInt(gameContainer.width - bug.imageView.width).toFloat()
                bug.targetY = Random.nextInt(gameContainer.height - bug.imageView.height).toFloat()

                animator.setFloatValues(0f, 1f)
                animator.start()
            }
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (gameActive && currentBugs.contains(bug) && !gamePaused) {
                    bug.targetX = Random.nextInt(gameContainer.width - bug.imageView.width).toFloat()
                    bug.targetY = Random.nextInt(gameContainer.height - bug.imageView.height).toFloat()
                    animator.setFloatValues(0f, 1f)
                    animator.start()
                }
            }
        })

        bug.animator = animator
        animator.start()
    }

    private fun destroyBug(bug: Bug, byPlayer: Boolean) {
        if (!currentBugs.contains(bug)) return

        currentBugs.remove(bug)
        bug.animator?.cancel()

        if (byPlayer && gameActive && !gamePaused) {
            score += 10
            bugsDestroyed++

            if (bugsDestroyed % 5 == 0) {
                score += 20
                showTemporaryMessage("Комбо x5! +20 очков!")
            }
        }

        val scaleX = ObjectAnimator.ofFloat(bug.imageView, "scaleX", 1f, 0f)
        val scaleY = ObjectAnimator.ofFloat(bug.imageView, "scaleY", 1f, 0f)
        val rotation = ObjectAnimator.ofFloat(bug.imageView, "rotation", 0f, 360f)

        scaleX.duration = 300
        scaleY.duration = 300
        rotation.duration = 300

        scaleX.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                gameContainer.removeView(bug.imageView)
            }
        })

        scaleX.start()
        scaleY.start()
        rotation.start()

        updateUI()
    }

    private fun updateUI() {
        scoreTextView.text = "Очки: $score"
        bugsCountTextView.text = "Уничтожено: $bugsDestroyed"
        timerTextView.text = "Время: ${timeLeft}с"
    }

    private suspend fun saveGameResult() {
        val accuracy = if (bugsDestroyed + bugsMissed > 0) {
            (bugsDestroyed.toFloat() / (bugsDestroyed + bugsMissed) * 100)
        } else {
            0f
        }

        val difficulty = when (gameSpeed) {
            in 1..3 -> "Легкий"
            in 4..7 -> "Средний"
            else -> "Сложный"
        }

        val score = Score(
            userId = currentUserId,
            score = this.score,
            difficulty = difficulty,
            bugsDestroyed = bugsDestroyed,
            accuracy = accuracy
        )

        database.scoreDao().insert(score)
    }

    private fun showGameMenu() {
        pauseGame()

        val menuItems = arrayOf(
            "Продолжить",
            "Начать заново",
            "Сменить пользователя",
            "Настройки",
            "Авторы",
            "Правила",
            "Выйти"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Игровое меню")
            .setItems(menuItems) { dialog, which ->
                when (which) {
                    0 -> {
                        resumeGame()
                        dialog.dismiss()
                    }
                    1 -> {
                        dialog.dismiss()
                        restartGame()
                    }
                    2 -> {
                        dialog.dismiss()
                        lifecycleScope.launch {
                            showUserSelectionDialog()
                        }
                    }
                    3 -> {
                        dialog.dismiss()
                        navigateToSettings()
                    }
                    4 -> {
                        dialog.dismiss()
                        navigateToAuthors()
                    }
                    5 -> {
                        dialog.dismiss()
                        navigateToRules()
                    }
                    6 -> {
                        dialog.dismiss()
                        exitGame()
                    }
                }
            }
            .setOnCancelListener {
                resumeGame()
            }
            .show()
    }

    private suspend fun showUserSelectionDialog() {
        val users = database.userDao().getAllUsers()

        if (users.isEmpty()) {
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Сначала зарегистрируйтесь во вкладке 'Регистрация'", Toast.LENGTH_LONG).show()
            }
            return
        }

        val userNames = users.map { it.fullName }.toTypedArray()

        activity?.runOnUiThread {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Выберите пользователя")
                .setItems(userNames) { dialog, which ->
                    val selectedUser = users[which]
                    currentUserId = selectedUser.id
                    currentUserName = selectedUser.fullName

                    val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putLong("current_user_id", selectedUser.id).apply()
                    sharedPref.edit().putString("current_user_name", selectedUser.fullName).apply()

                    showTemporaryMessage("Играем за: ${selectedUser.fullName}")
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun navigateToSettings() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.switchToTab(3)
    }

    private fun navigateToAuthors() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.switchToTab(4)
    }

    private fun navigateToRules() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.switchToTab(5)
    }

    private fun restartGame() {
        stopGravityEffect()

        gameActive = false
        gamePaused = false
        currentBugs.forEach { bug ->
            bug.animator?.cancel()
            gameContainer.removeView(bug.imageView)
        }
        currentBugs.clear()

        removeBonus()

        handler.removeCallbacksAndMessages(null)

        startGame()
    }

    private fun exitGame() {
        stopGravityEffect()
        gameActive = false
        gamePaused = false
        currentBugs.forEach { bug ->
            bug.animator?.cancel()
            gameContainer.removeView(bug.imageView)
        }
        currentBugs.clear()
        removeBonus()
        handler.removeCallbacksAndMessages(null)

        scoreTextView.text = "Игра завершена\nНажмите для новой игры"
    }

    private fun endGame() {
        gameActive = false
        gamePaused = false

        currentBugs.forEach { bug ->
            bug.animator?.cancel()
            gameContainer.removeView(bug.imageView)
        }
        currentBugs.clear()

        lifecycleScope.launch {
            saveGameResult()

            val accuracy = if (bugsDestroyed + bugsMissed > 0) {
                (bugsDestroyed.toFloat() / (bugsDestroyed + bugsMissed) * 100).toInt()
            } else {
                0
            }


            activity?.runOnUiThread {
                scoreTextView.text = "Игра окончена!\nФинальный счет: $score\nТочность: ${accuracy}%"
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (gameActive) {
            pauseGame()
        }
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (gameActive && gamePaused) {
            resumeGame()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gameActive = false
        stopGravityEffect()
        sensorManager.unregisterListener(this)
        mediaPlayer?.release()
        handler.removeCallbacksAndMessages(null)
        currentBugs.forEach { it.animator?.cancel() }
    }
}
