package com.honorshots.screenshot.data

/**
 * 英雄数据模型
 * @param id 英雄ID
 * @param name 英雄名称
 * @param role 职业定位
 * @param lane 推荐分路
 * @param isTank 是否为坦克
 * @param isFighter 是否为战士
 * @param isMage 是否为法师
 * @param isAssassin 是否为刺客
 * @param isMarksman 是否为射手
 * @param isSupport 是否为辅助
 * @param controlAbility 控制能力 (1-5)
 * @param burstDamage 爆发伤害 (1-5)
 * @param sustainedDamage 持续伤害 (1-5)
 * @param tankiness 坦度 (1-5)
 * @param earlyGameStrength 前期强度 (1-5)
 * @param lateGameStrength 后期强度 (1-5)
 * @param mobility 机动性 (1-5)
 * @param pushAbility 推线能力 (1-5)
 */
data class Hero(
    val id: String,
    val name: String,
    val role: String,
    val lane: Lane,
    val isTank: Boolean = false,
    val isFighter: Boolean = false,
    val isMage: Boolean = false,
    val isAssassin: Boolean = false,
    val isMarksman: Boolean = false,
    val isSupport: Boolean = false,
    val controlAbility: Int = 0,
    val burstDamage: Int = 0,
    val sustainedDamage: Int = 0,
    val tankiness: Int = 0,
    val earlyGameStrength: Int = 0,
    val lateGameStrength: Int = 0,
    val mobility: Int = 0,
    val pushAbility: Int = 0
)

/**
 * 分路枚举
 */
enum class Lane(val displayName: String) {
    TOP("对抗路"),
    JUNGLE("打野"),
    MID("中路"),
    ADC("发育路"),
    SUPPORT("辅助"),
    UNKNOWN("未知")
}

/**
 * 队伍枚举
 */
enum class Team(val displayName: String) {
    BLUE("蓝方"),
    RED("红方")
}

/**
 * 玩家数据模型
 * @param hero 所用英雄
 * @param rank 段位
 * @param lane 当前分路
 * @param team 所属队伍
 */
data class Player(
    val hero: Hero,
    val rank: String,
    val lane: Lane,
    val team: Team
)

/**
 * 队伍分析结果
 */
data class TeamAnalysis(
    val team: Team,
    val players: List<Player>,
    val totalTankiness: Int,           // 总坦度
    val totalControl: Int,              // 总控制能力
    val totalBurstDamage: Int,          // 总爆发伤害
    val totalSustainedDamage: Int,      // 总持续伤害
    val earlyGameStrength: Int,         // 前期强度
    val lateGameStrength: Int,          // 后期强度
    val initiationAbility: Int,         // 开团能力
    val peelAbility: Int,               // 保护能力
    val pushAbility: Int,               // 推塔能力
    val teamFightStrength: Int,         // 团战强度
    val strengths: List<String>,        // 优势点
    val weaknesses: List<String>,       // 劣势点
    val overallStrength: Int,           // 整体强度 1-10
    val recommendedStrategy: String     // 推荐策略
)

/**
 * 克制关系分析
 */
data class CounterAnalysis(
    val heroName: String,
    val counteredBy: List<String>,     // 被谁克制
    val counters: List<String>,         // 克制谁
    val synergyPartners: List<String>   // 最佳搭档
)

/**
 * 对局分析结果
 */
data class MatchAnalysis(
    val blueTeam: TeamAnalysis,
    val redTeam: TeamAnalysis,
    val counterAnalyses: List<CounterAnalysis>,
    val keyFactors: List<String>,       // 取胜关键点
    val recommendedPlaystyle: String,   // 推荐打法
    val warnings: List<String>,         // 需要注意的点
    val winProbability: Float,          // 胜率估算 (0-1)
    val estimatedDuration: String,       // 预估时长
    val generatedTime: Long = System.currentTimeMillis()
)
