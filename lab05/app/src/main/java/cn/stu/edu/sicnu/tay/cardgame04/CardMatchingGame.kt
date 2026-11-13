package cn.stu.edu.sicnu.tay.cardgame04

/**
 * 翻牌游戏模型（MVC 中的 Model）：
 * 实验五在实验四基础上进行扩展——
 * 1. 新增 reset 方法，支持随时重新开始游戏；
 * 2. 支持三张牌的配对游戏：构造时传入 cardsPerMatch = 3，
 *    配对逻辑由 Card.match 与 chooseCardAtIndex 配合完成。
 */
class CardMatchingGame(count: Int, cardsPerMatch: Int = CARDS_PER_MATCH_TWO) {

    companion object {
        // 每组配对的牌数：2 张或 3 张
        const val CARDS_PER_MATCH_TWO = 2
        const val CARDS_PER_MATCH_THREE = 3

        const val MISMATCH_PENALTY = 2
        const val MATCH_BONUS = 4
        const val COST_TO_CHOOSE = 1
    }

    var score = 0
        private set

    // 实际生效的每组牌数（只允许 2 或 3）
    val cardsPerMatch: Int =
        if (cardsPerMatch == CARDS_PER_MATCH_THREE) CARDS_PER_MATCH_THREE else CARDS_PER_MATCH_TWO

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
     * 实验五新增：重新开始游戏，清空得分、翻回所有牌并重新洗牌。
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
