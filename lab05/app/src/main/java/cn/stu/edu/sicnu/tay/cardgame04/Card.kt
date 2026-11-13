package cn.stu.edu.sicnu.tay.cardgame04

class Card(val suit: String, val rank: String, var isChosen: Boolean = false, var isMatched: Boolean = false) {
    companion object {
        val rankStrings = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val validSuits = arrayOf("♥", "♦", "♠", "♣")

        // 单组配对的基础得分
        const val MATCH_SCORE = 4
    }

    override fun toString(): String {
        return suit + rank
    }

    /**
     * 匹配规则（实验五：支持三张牌的游戏）：
     * 两张牌模式：otherCards 为 1 张，点数相同即配对成功，得 4 分；
     * 三张牌模式：otherCards 为 2 张，三张牌点数全部相同才配对成功，
     *             难度更大，得分为配对牌数的 4 倍即 12 分；
     * 其余情况均无法配对，得 0 分。
     */
    fun match(otherCards: Array<Card>): Int {
        return when (otherCards.size) {
            1 -> if (otherCards[0].rank == rank) MATCH_SCORE else 0
            2 -> if (otherCards.all { it.rank == rank }) MATCH_SCORE * (otherCards.size + 1) else 0
            else -> 0
        }
    }
}
