package cn.stu.edu.sicnu.tay.lab06

import java.io.Serializable

/**
 * 翻牌游戏的模型（Model）。
 *
 * 实验六中整个游戏对象都从 Serializable 派生：退出程序前把本对象写入
 * 应用私有文件，再次打开时通过 ObjectInputStream 读回，得分、牌序、
 * 已翻开和已配对的牌都能完整恢复。
 */
class CardMatchingGame(
    val count: Int,
    val cardsPerMatch: Int = CARDS_PER_MATCH_TWO
) : Serializable {

    companion object {
        const val CARDS_PER_MATCH_TWO = 2
        const val CARDS_PER_MATCH_THREE = 3

        const val MISMATCH_PENALTY = 2
        const val MATCH_BONUS = 4
        const val COST_TO_CHOOSE = 1

        private const val serialVersionUID = 1L
    }

    var score = 0
        private set

    private val cards: MutableList<Card>

    init {
        require(count > 0 && count % cardsPerMatch == 0) { "卡牌数量必须能被每组牌数整除" }

        val deck = Deck()
        val list = mutableListOf<Card>()
        if (cardsPerMatch == CARDS_PER_MATCH_THREE) {
            // 三张牌模式：每组发出三张同点数的牌，保证每一组都存在配对
            repeat(count / CARDS_PER_MATCH_THREE) {
                val card = deck.drawRandomCard() ?: return@repeat
                repeat(CARDS_PER_MATCH_THREE) { list.add(Card(card.suit, card.rank)) }
            }
        } else {
            // 两张牌模式：每张牌复制一份，保证每张牌都有自己的配对牌
            repeat(count / CARDS_PER_MATCH_TWO) {
                val card = deck.drawRandomCard() ?: return@repeat
                list.add(card)
                list.add(Card(card.suit, card.rank))
            }
        }
        list.shuffle()
        cards = list
    }

    val cardCount: Int
        get() = cards.size

    fun cardAtIndex(index: Int): Card {
        return cards[index]
    }

    /**
     * 重新开始游戏：清空得分、翻回所有牌并重新洗牌。
     */
    fun reset() {
        score = 0
        for (card in cards) {
            card.isChosen = false
            card.isMatched = false
        }
        cards.shuffle()
    }

    fun chooseCardAtIndex(index: Int) {
        val card = cardAtIndex(index)
        if (card.isMatched) {
            return
        }
        if (card.isChosen) {
            // 已选中的牌再点一次则取消选择
            card.isChosen = false
            return
        }

        // 当前处于选中状态且尚未配对的牌
        val chosen = cards.filter { it.isChosen && !it.isMatched }

        if (chosen.size < cardsPerMatch - 1) {
            // 还没凑齐一组，直接选中这张牌
            score -= COST_TO_CHOOSE
            card.isChosen = true
        } else {
            // 凑齐一组：用新选的这张牌与之前选中的牌一起匹配
            val matchScore = card.match(chosen.toTypedArray())
            score -= COST_TO_CHOOSE
            card.isChosen = true
            if (matchScore > 0) {
                // 配对成功：发放奖励分，并把整组牌标记为已配对
                score += matchScore * MATCH_BONUS
                for (otherCard in chosen) {
                    otherCard.isMatched = true
                }
                card.isMatched = true
            } else {
                // 配对失败：扣除罚分，之前选中的牌全部翻回背面
                score -= MISMATCH_PENALTY
                for (otherCard in chosen) {
                    otherCard.isChosen = false
                }
            }
        }
    }
}
