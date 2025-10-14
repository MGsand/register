package com.example.register

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlin.math.sqrt
import kotlin.random.Random

class GameFragment : Fragment() {

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
    private var gamePaused = false

    private var gameSpeed = 1
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


    private data class Bug(
        val imageView: ImageView,
        var currentX: Float,
        var currentY: Float,
        var targetX: Float,
        var targetY: Float,
        var speed: Float,
        var animator: ValueAnimator? = null
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

    private fun loadGameSettings() {
        val sharedPref = requireActivity().getSharedPreferences("game_settings", android.content.Context.MODE_PRIVATE)
        gameSpeed = sharedPref.getInt("game_speed", 1)
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

    }

    private fun pauseGame() {
        gamePaused = true
        currentBugs.forEach { it.animator?.pause() }
        handler.removeCallbacksAndMessages(null)
    }

    private fun resumeGame() {
        gamePaused = false
        currentBugs.forEach { it.animator?.resume() }
        startTimer()
        startBugSpawning()
    }

    private fun handleMiss() {
        if (!gameActive || gamePaused) return

        // Штраф за промах
        score -= 3
        bugsMissed++


        showMissEffect()
        updateUI()


     //   if (bugsMissed % 5 == 0) {
       //     showTemporaryMessage("Промах! -3 очка")
      //  }
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



    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                if (gameActive && !gamePaused && timeLeft > 0) {
                    timeLeft--
                    updateUI()

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
        val bugSize = Random.nextInt(100, 180)
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

        // Анимация появления
        bug.alpha = 0f
        bug.animate().alpha(1f).setDuration(500).start()


        val targetX = Random.nextInt(gameContainer.width - bugSize).toFloat()
        val targetY = Random.nextInt(gameContainer.height - bugSize).toFloat()


        val baseSpeed = 0.3f
        val speedFromSettings = gameSpeed * 0.03f
        val randomVariation = Random.nextFloat() * 0.15f

        val speed = baseSpeed + speedFromSettings + randomVariation

        val bugData = Bug(bug, startX, startY, targetX, targetY, speed)

        // Обработчик клика
        bug.setOnClickListener {
            if (!gamePaused) {
                destroyBug(bugData, true)
            }
        }

        gameContainer.addView(bug)
        currentBugs.add(bugData)

        startBugMovement(bugData)

        // Увеличиваем время жизни жука
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


          //  if (bugsDestroyed % 5 == 0) {
         //       score += 20
         //       showTemporaryMessage("Комбо x5! +20 очков! 🎉")
         //   }
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

    private fun showGameMenu() {
        pauseGame()

        val menuItems = arrayOf("️ Продолжить", " Начать заново", " Правила", " Авторы", " Настройки", " Выйти")

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("🎮 Игровое меню")
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
                        navigateToSettings()
                    }
                    3 -> {
                        dialog.dismiss()
                        navigateToAuthors()
                    }
                    4 -> {
                        dialog.dismiss()
                        navigateToRules()
                    }
                    5 -> {
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

    private fun navigateToSettings() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.switchToTab(2) // Настройки - 3я вкладка (индекс 2)
    }

    private fun navigateToAuthors() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.switchToTab(3) // Авторы - 4я вкладка (индекс 3)
    }

    private fun navigateToRules() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.switchToTab(4) // Правила - 5я вкладка (индекс 4)
    }

    private fun restartGame() {

        gameActive = false
        gamePaused = false
        currentBugs.forEach { bug ->
            bug.animator?.cancel()
            gameContainer.removeView(bug.imageView)
        }
        currentBugs.clear()
        handler.removeCallbacksAndMessages(null)


        startGame()
    }

    private fun exitGame() {

        gameActive = false
        gamePaused = false
        currentBugs.forEach { bug ->
            bug.animator?.cancel()
            gameContainer.removeView(bug.imageView)
        }
        currentBugs.clear()
        handler.removeCallbacksAndMessages(null)


        scoreTextView.text = "Игра завершена\nНажмите 🎮 для новой игры"
    }

    private fun endGame() {
        gameActive = false
        gamePaused = false


        currentBugs.forEach { bug ->
            bug.animator?.cancel()
            gameContainer.removeView(bug.imageView)
        }
        currentBugs.clear()


        val accuracy = if (bugsDestroyed + bugsMissed > 0) {
            (bugsDestroyed.toFloat() / (bugsDestroyed + bugsMissed) * 100).toInt()
        } else {
            0
        }

        val resultText = when {
            score >= 200 -> "Отличный результат! "
            score >= 100 -> "Хорошая игра! "
            score >= 50 -> "Неплохо! "
            score >= 0 -> "Можно лучше! "
            else -> "Попробуйте еще раз! "
        }

        scoreTextView.text = "Игра окончена!\n$resultText\nФинальный счет: $score\nТочность: ${accuracy}%"
    }

    override fun onPause() {
        super.onPause()
        if (gameActive) {
            pauseGame()
        }
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
        handler.removeCallbacksAndMessages(null)
        currentBugs.forEach { it.animator?.cancel() }
    }
}