package cn.stu.edu.sicnu.tay.cardgame04

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 适配器（实验五核心要求）：
 * 从 RecyclerView.Adapter 派生，负责把 Model（CardMatchingGame）中的
 * 每一张牌绑定到 View（PlayingCardView）上，并把玩家的点击事件通过
 * onCardClick 回调交给 Controller（GameActivity）处理。
 *
 * @param game        游戏模型，条目数量与牌面数据都来自它
 * @param cardWidth   按屏幕可用空间计算出的统一牌宽（像素）
 * @param cardHeight  统一牌高（像素），保持 5:7 的扑克牌比例
 * @param margin      每张牌四周的间距（像素）
 * @param onCardClick 玩家点击某张牌时的回调，参数为牌的位置下标
 */
class CardAdapter(
    private val game: CardMatchingGame,
    private val cardWidth: Int,
    private val cardHeight: Int,
    private val margin: Int,
    private val onCardClick: (position: Int) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    /**
     * 条目持有者：缓存每个条目的牌视图，避免重复创建与查找。
     */
    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: PlayingCardView = itemView as PlayingCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        // 用统一计算好的尺寸摆放每一张牌，保证整版牌面大小一致、排列规整
        val params = FrameLayout.LayoutParams(cardWidth, cardHeight)
        params.setMargins(margin, margin, margin, margin)
        view.layoutParams = params
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return game.cardCount
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = game.cardAtIndex(position)
        // 已配对的牌翻开并淡出，选中的牌翻开，其余显示牌背
        holder.cardView.bind(card, card.isChosen || card.isMatched, card.isMatched)
        holder.cardView.setOnClickListener {
            // 回调：把点击位置交给控制器（GameActivity）处理
            onCardClick(position)
        }
    }
}
