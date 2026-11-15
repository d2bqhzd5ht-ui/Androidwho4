package cn.stu.edu.sicnu.tay.lab06

/**
 * 第 3 个 Fragment：三张牌的翻牌游戏。
 *
 * 每次翻 3 张牌，三张牌点数全部相同才配对成功；游戏状态保存在 game_three_tay.dat。
 */
class ThreeCardGameFragment : BaseCardGameFragment() {

    override val cardsPerMatch: Int = CardMatchingGame.CARDS_PER_MATCH_THREE

    override val stateFileName: String = "game_three_tay.dat"
}
