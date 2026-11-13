package cn.stu.edu.sicnu.tay.cardgame04

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 游戏控制器（MVC 中的 Controller）：
 * 实验五使用 RecyclerView 替换原来的 GridLayout 展示 24 张牌，
 * 通过 CardAdapter 的回调处理玩家的翻牌操作，并在菜单中支持
 * 二张牌 / 三张牌两种配对模式的切换与重新开始；横屏竖屏通过
 * 资源限定符提供不同的每行牌数（spanCount）。
 */
class GameActivity : AppCompatActivity() {

    companion object {
        // 游戏共 24 张牌
        private const val CARD_COUNT = 24
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var game: CardMatchingGame

    // 当前配对模式：每组 2 张或 3 张
    private var mode = CardMatchingGame.CARDS_PER_MATCH_TWO
    private var adapter: CardAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        applySystemBarInsets(findViewById(R.id.root), findViewById(R.id.appBar))

        // 使用工具栏承载菜单（游戏模式切换 / 重新开始）
        setSupportActionBar(findViewById(R.id.toolbar))

        recyclerView = findViewById(R.id.recyclerView)
        game = CardMatchingGame(CARD_COUNT, mode)

        findViewById<View>(R.id.btnReset).setOnClickListener { resetGame() }
        findViewById<View>(R.id.btnExit).setOnClickListener {
            val resultIntent = Intent().putExtra("score", game.score)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

        // 等 RecyclerView 测量完成后再按可用空间计算每张牌的大小
        recyclerView.doOnLayout { setupAdapter() }
        updateScore()
    }

    /**
     * 根据 RecyclerView 的实际大小与横竖屏的列数，
     * 计算出统一的牌面尺寸并创建适配器。
     */
    private fun setupAdapter() {
        val spacing = dp(8)
        val boardWidth = recyclerView.width - recyclerView.paddingLeft - recyclerView.paddingRight
        val boardHeight = recyclerView.height - recyclerView.paddingTop - recyclerView.paddingBottom
        if (boardWidth <= 0 || boardHeight <= 0) return

        // 竖屏每行 4 张（6 行）、横屏每行 6 张（4 行），由 integers.xml 资源提供
        val columns = resources.getInteger(R.integer.grid_span_count)
        val rows = (CARD_COUNT + columns - 1) / columns

        var cardWidth = (boardWidth - spacing * columns) / columns
        var cardHeight = cardWidth * 7 / 5

        // 高度放不下时以高度为准缩放，始终保持 5:7 的扑克牌比例
        if (cardHeight * rows + spacing * rows > boardHeight) {
            cardHeight = (boardHeight - spacing * rows) / rows
            cardWidth = cardHeight * 5 / 7
        }
        if (cardWidth <= 0 || cardHeight <= 0) return

        recyclerView.layoutManager = GridLayoutManager(this, columns)
        adapter = CardAdapter(game, cardWidth, cardHeight, spacing / 2) { position ->
            onCardClicked(position)
        }
        recyclerView.adapter = adapter
    }

    /**
     * 适配器回调：玩家点击某张牌后交给模型处理，再刷新牌面与分数。
     */
    private fun onCardClicked(position: Int) {
        game.chooseCardAtIndex(position)
        adapter?.notifyDataSetChanged()
        updateScore()
    }

    /**
     * 重新开始：调用模型新增的 reset 方法并刷新界面。
     */
    private fun resetGame() {
        game.reset()
        adapter?.notifyDataSetChanged()
        updateScore()
    }

    /**
     * 切换二张牌 / 三张牌模式：按新模式重新发一局。
     */
    private fun switchMode(newMode: Int) {
        if (newMode == mode) return
        mode = newMode
        game = CardMatchingGame(CARD_COUNT, mode)
        adapter?.notifyDataSetChanged()
        updateScore()
        invalidateOptionsMenu()
    }

    private fun updateScore() {
        supportActionBar?.subtitle = getString(R.string.score_label, game.score)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.game_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // 让菜单中的单选状态与当前模式保持一致
        menu.findItem(R.id.action_mode_two)?.isChecked =
            mode == CardMatchingGame.CARDS_PER_MATCH_TWO
        menu.findItem(R.id.action_mode_three)?.isChecked =
            mode == CardMatchingGame.CARDS_PER_MATCH_THREE
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reset -> {
                resetGame()
                true
            }
            R.id.action_mode_two -> {
                switchMode(CardMatchingGame.CARDS_PER_MATCH_TWO)
                true
            }
            R.id.action_mode_three -> {
                switchMode(CardMatchingGame.CARDS_PER_MATCH_THREE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
