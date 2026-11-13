package cn.stu.edu.sicnu.tay.cardgame04

class CardMatchingGame(count: Int) {
    var score = 0
        private set

    private val cards: MutableList<Card>

    init {
        require(count % 2 == 0 && count > 0) { "卡牌数量必须是正偶数" }

        val deck = Deck()
        val list = mutableListOf<Card>()
        // 每张牌复制一份，保证每张牌都有自己的配对牌，不会剩下无法匹配的牌
        repeat(count / 2) {
            val card = deck.drawRandomCard() ?: return@repeat
            list.add(card)
            list.add(Card(suit = card.suit, rank = card.rank))
        }
        list.shuffle()
        cards = list
    }

    fun cardAtIndex(index: Int): Card {
        return cards[index]
    }

    val MISMATCH_PENALTY = 2
    val MATCH_BONUS = 4
    val COST_TO_CHOOSE = 1

    fun chooseCardAtIndex(index: Int) {
        val card = cardAtIndex(index)
        if (!card.isMatched) {
            if (card.isChosen) {
                card.isChosen = false
            } else {
                for (otherCard in cards) {
                    if (otherCard.isChosen && !otherCard.isMatched) {
                        val matchScore = card.match(arrayOf(otherCard))
                        if (matchScore > 0) {
                            score += matchScore * MATCH_BONUS
                            otherCard.isMatched = true
                            card.isMatched = true
                        } else {
                            score -= MISMATCH_PENALTY
                            otherCard.isChosen = false
                        }
                        break
                    }
                }
                score -= COST_TO_CHOOSE
                card.isChosen = true
            }
        }
    }
}
