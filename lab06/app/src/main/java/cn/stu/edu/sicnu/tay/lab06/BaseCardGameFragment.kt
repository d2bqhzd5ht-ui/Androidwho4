package cn.stu.edu.sicnu.tay.lab06

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * 第 2、3 个 Fragment 的公共父类：翻牌游戏。
 *
 * 子类只需要指定「每组几张牌」和「状态文件名」：
 *  - TwoCardGameFragment：2 张牌配对
 *  - ThreeCardGameFragment：3 张牌配对
 *
 * 整个 CardMatchingGame 对象实现了 Serializable，每次翻牌、重新开始
 * 以及 Fragment 暂停时都会用 openFileOutput + ObjectOutputStream 保存；
 * Fragment 创建时用 openFileInput + ObjectInputStream 恢复上次的对局。
 */
abstract class BaseCardGameFragment : Fragment(R.layout.fragment_card_game) {

    /** 每组配对的牌数：2 或 3 */
    protected abstract val cardsPerMatch: Int

    /** 当前 Fragment 对应的序列化状态文件名 */
    protected abstract val stateFileName: String

    private lateinit var game: CardMatchingGame
    private lateinit var tvScore: TextView
    private lateinit var tvBest: TextView
    private lateinit var recyclerView: RecyclerView
    private var adapter: CardAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvScore = view.findViewById(R.id.tvScore)
        tvBest = view.findViewById(R.id.tvBest)
        recyclerView = view.findViewById(R.id.recyclerView)

        val restored = loadGame()
        if (restored != null) {
            game = restored
            Toast.makeText(requireContext(), R.string.restored_toast, Toast.LENGTH_SHORT).show()
        } else {
            game = CardMatchingGame(CARD_COUNT, cardsPerMatch)
        }

        view.findViewById<View>(R.id.btnReset).setOnClickListener { resetGame() }

        // 先设置布局管理器，布局完成后再创建适配器
        recyclerView.layoutManager = GridLayoutManager(
            requireContext(), resources.getInteger(R.integer.grid_span_count)
        )
        recyclerView.post {
            if (adapter == null) setupAdapter()
        }
        updateScore()
    }

    /**
     * 根据 RecyclerView 的实际大小与横竖屏的列数，计算统一的牌面尺寸并创建适配器。
     */
    private fun setupAdapter() {
        if (adapter != null) return
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

        recyclerView.layoutManager = GridLayoutManager(requireContext(), columns)
        adapter = CardAdapter(game, cardWidth, cardHeight, spacing / 2) { position ->
            onCardClicked(position)
        }
        recyclerView.adapter = adapter
        recyclerView.requestLayout()
    }

    /**
     * 适配器回调：玩家点击某张牌后交给模型处理，再刷新牌面与分数。
     */
    private fun onCardClicked(position: Int) {
        game.chooseCardAtIndex(position)
        adapter?.notifyDataSetChanged()
        updateScore()
        saveGame()
        saveHighScore()
    }

    private fun resetGame() {
        game.reset()
        adapter?.notifyDataSetChanged()
        updateScore()
        saveGame()
    }

    private fun updateScore() {
        tvScore.text = getString(R.string.score_label, game.score)
        val profile = ProfileStore.load(requireContext())
        val best = if (cardsPerMatch == CardMatchingGame.CARDS_PER_MATCH_THREE) {
            profile.highScoreThree
        } else {
            profile.highScoreTwo
        }
        tvBest.text = getString(R.string.best_score_label, best)
    }

    /**
     * 得分超过历史最高分时，把最新最高分写入个人信息文件。
     */
    private fun saveHighScore() {
        val profile = ProfileStore.load(requireContext())
        if (cardsPerMatch == CardMatchingGame.CARDS_PER_MATCH_THREE) {
            if (game.score <= profile.highScoreThree) return
            profile.highScoreThree = game.score
        } else {
            if (game.score <= profile.highScoreTwo) return
            profile.highScoreTwo = game.score
        }
        ProfileStore.save(requireContext(), profile)
        updateScore()
    }

    /**
     * 序列化保存游戏状态：openFileOutput + ObjectOutputStream。
     */
    private fun saveGame() {
        try {
            requireContext().openFileOutput(stateFileName, Context.MODE_PRIVATE).use { output ->
                ObjectOutputStream(output).use { stream ->
                    stream.writeObject(game)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 序列化读取游戏状态：openFileInput + ObjectInputStream。
     * 没有历史记录时返回 null。
     */
    private fun loadGame(): CardMatchingGame? {
        return try {
            requireContext().openFileInput(stateFileName).use { input ->
                ObjectInputStream(input).use { stream ->
                    stream.readObject() as CardMatchingGame
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onPause() {
        super.onPause()
        if (::game.isInitialized) {
            saveGame()
        }
    }

    override fun onDestroyView() {
        recyclerView.adapter = null
        adapter = null
        super.onDestroyView()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    companion object {
        // 游戏共 24 张牌
        private const val CARD_COUNT = 24
    }
}
