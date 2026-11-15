package cn.stu.edu.sicnu.tay.lab06

/**
 * 第 2 个 Fragment：二张牌的翻牌游戏。
 *
 * 每次翻 2 张牌，点数相同即配对成功；游戏状态保存在 game_two_tay.dat。
 */
class TwoCardGameFragment : BaseCardGameFragment() {

    override val cardsPerMatch: Int = CardMatchingGame.CARDS_PER_MATCH_TWO

    override val stateFileName: String = "game_two_tay.dat"
}
