package net.keyfc.api.model.uc

data class UserStats(
    val score: Int, // 积分
    val experience: Int, // 经验
    val popularity: Int, // 人气
    val kp: Int, // KP
    val credits: Int, // 学分
    val goodPersonCards: Int, // 好人卡
    val favorability: Int, // 好感度
)