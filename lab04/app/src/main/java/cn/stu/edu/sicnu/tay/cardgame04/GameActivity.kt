package cn.stu.edu.sicnu.tay.cardgame04

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout

class GameActivity : AppCompatActivity() {

    companion object {
        private const val CARD_COUNT = 16
    }

    private lateinit var boardContainer: FrameLayout
    private lateinit var gridLayout: GridLayout
    private lateinit var tvScore: TextView
    private lateinit var game: CardMatchingGame
    private val cardViews = mutableListOf<PlayingCardView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        applySystemBarInsets(findViewById(R.id.root), findViewById(R.id.appBar))

        boardContainer = findViewById(R.id.boardContainer)
        gridLayout = findViewById(R.id.gridLayout)
        tvScore = findViewById(R.id.tvScore)

        findViewById<Button>(R.id.btnReset).setOnClickListener { startNewGame() }

        findViewById<Button>(R.id.btnExit).setOnClickListener {
            val resultIntent = Intent().putExtra("score", game.score)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        // 等棋盘区域测量完成后再按可用空间计算牌面大小
        boardContainer.doOnLayout { startNewGame() }
    }

    private fun startNewGame() {
        val spacing = dp(10)
        val boardWidth = boardContainer.width - boardContainer.paddingLeft - boardContainer.paddingRight
        val boardHeight = boardContainer.height - boardContainer.paddingTop - boardContainer.paddingBottom
        if (boardWidth <= 0 || boardHeight <= 0) return

        // 竖屏 4 列、横屏 8 列，保证牌面和屏幕比例协调
        val columns = if (boardWidth > boardHeight) 8 else 4
        val rows = (CARD_COUNT + columns - 1) / columns

        var cardWidth = (boardWidth - spacing * columns) / columns
        var cardHeight = (cardWidth * 7f / 5f).toInt()

        // 高度放不下时，以高度为准缩放，始终保持 5:7 的扑克牌比例
        if (cardHeight * rows + spacing * rows > boardHeight) {
            cardHeight = (boardHeight - spacing * rows) / rows
            cardWidth = (cardHeight * 5f / 7f).toInt()
        }
        cardWidth = minOf(cardWidth, dp(130))
        cardHeight = (cardWidth * 7f / 5f).toInt()
        if (cardWidth <= 0 || cardHeight <= 0) return

        gridLayout.columnCount = columns
        gridLayout.rowCount = rows
        gridLayout.removeAllViews()
        cardViews.clear()
        gridLayout.layoutParams = FrameLayout.LayoutParams(
            columns * (cardWidth + spacing),
            rows * (cardHeight + spacing),
            Gravity.CENTER
        )

        game = CardMatchingGame(CARD_COUNT)

        for (i in 0 until CARD_COUNT) {
            val cardView = PlayingCardView(this).apply {
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(i / columns),
                    GridLayout.spec(i % columns)
                ).apply {
                    width = cardWidth
                    height = cardHeight
                    setMargins(spacing / 2, spacing / 2, spacing / 2, spacing / 2)
                }
                setOnClickListener { onCardClicked(i) }
            }
            gridLayout.addView(cardView)
            cardViews.add(cardView)
        }
        updateUI()
    }

    private fun onCardClicked(index: Int) {
        game.chooseCardAtIndex(index)
        updateUI()
    }

    private fun updateUI() {
        for (i in 0 until CARD_COUNT) {
            val card = game.cardAtIndex(i)
            cardViews[i].bind(card, card.isChosen || card.isMatched, card.isMatched)
        }
        tvScore.text = getString(R.string.score_label, game.score)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
