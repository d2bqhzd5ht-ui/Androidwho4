package cn.stu.edu.sicnu.tay.lab06

import org.junit.Assert.assertEquals
import org.junit.Test

class CardGameUnitTest {

    @Test
    fun testChooseCard_decreasesScore() {
        // 初始化一个16张牌的游戏
        val game = CardMatchingGame(16)
        val initialScore = game.score

        // 模拟点击第0张牌
        game.chooseCardAtIndex(0)

        // 验证得分是否扣了1分
        assertEquals(initialScore - 1, game.score)
    }
}