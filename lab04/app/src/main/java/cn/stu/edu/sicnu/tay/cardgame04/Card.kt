package cn.stu.edu.sicnu.tay.cardgame04

class Card(val suit: String, val rank: String, var isChosen: Boolean = false, var isMatched: Boolean = false) {
    companion object {
        val rankStrings = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val validSuits = arrayOf("♥", "♦", "♠", "♣")
    }

    override fun toString(): String {
        return suit + rank
    }

    fun match(otherCards: Array<Card>): Int {
        // 只有点数相同才算配对：每张牌都有且只有一张完全相同的配对牌，
        // 避免按花色配对时把不同对子的牌消耗掉，导致最后剩下无法匹配的牌
        return if (otherCards.size == 1 && otherCards[0].rank == rank) 4 else 0
    }
}
