package com.honorshots.screenshot.service

import android.util.Log
import com.honorshots.screenshot.data.*

/**
 * 阵容分析服务
 * 负责分析王者荣耀对局中的阵容优劣
 *
 * 功能：
 * 1. 识别加载界面中的英雄（TODO: 需要图像识别支持）
 * 2. 分析双方阵容的坦度、控制、开团、输出等能力
 * 3. 评估前期/后期强度
 * 4. 分析克制关系
 * 5. 提供取胜关键点和建议
 */
object MatchAnalysisService {

    private const val TAG = "MatchAnalysisService"

    /**
     * 分析对局阵容
     * @param blueTeamHeroes 蓝方英雄名称列表（5个）
     * @param redTeamHeroes 红方英雄名称列表（5个）
     * @param blueTeamRanks 蓝方段位列表
     * @param redTeamRanks 红方段位列表
     * @return 对局分析结果
     */
    fun analyzeMatch(
        blueTeamHeroes: List<String>,
        redTeamHeroes: List<String>,
        blueTeamRanks: List<String> = List(5) { "钻石" },
        redTeamRanks: List<String> = List(5) { "钻石" }
    ): MatchAnalysis {
        Log.d(TAG, "开始分析阵容...")
        Log.d(TAG, "蓝方英雄: $blueTeamHeroes")
        Log.d(TAG, "红方英雄: $redTeamHeroes")

        // 解析英雄数据
        val blueHeroes = blueTeamHeroes.mapIndexed { index, name ->
            HeroDatabase.getHeroByName(name) ?: createDefaultHero(name, Team.BLUE)
        }
        val redHeroes = redTeamHeroes.mapIndexed { index, name ->
            HeroDatabase.getHeroByName(name) ?: createDefaultHero(name, Team.RED)
        }

        // 创建玩家列表
        val bluePlayers = createPlayers(blueHeroes, blueTeamRanks, Team.BLUE)
        val redPlayers = createPlayers(redHeroes, redTeamRanks, Team.RED)

        // 分析双方阵容
        val blueAnalysis = analyzeTeam(Team.BLUE, bluePlayers)
        val redAnalysis = analyzeTeam(Team.RED, redPlayers)

        // 分析克制关系
        val counterAnalyses = analyzeCounters(blueHeroes, redHeroes)

        // 生成取胜关键点
        val keyFactors = generateKeyFactors(blueAnalysis, redAnalysis, counterAnalyses)

        // 计算胜率
        val winProbability = calculateWinProbability(blueAnalysis, redAnalysis)

        // 预估对局时长
        val estimatedDuration = estimateGameDuration(blueAnalysis, redAnalysis)

        Log.d(TAG, "分析完成，蓝方胜率: ${(winProbability * 100).toInt()}%")

        return MatchAnalysis(
            blueTeam = blueAnalysis,
            redTeam = redAnalysis,
            counterAnalyses = counterAnalyses,
            keyFactors = keyFactors,
            recommendedPlaystyle = generatePlaystyle(blueAnalysis, redAnalysis),
            warnings = generateWarnings(blueAnalysis, redAnalysis),
            winProbability = winProbability,
            estimatedDuration = estimatedDuration
        )
    }

    /**
     * 创建默认英雄（当数据库中不存在时使用）
     */
    private fun createDefaultHero(name: String, team: Team): Hero {
        return Hero(
            id = "unknown",
            name = name,
            role = "未知",
            lane = Lane.UNKNOWN,
            tankiness = 3,
            controlAbility = 3,
            earlyGameStrength = 3,
            lateGameStrength = 3,
            mobility = 3,
            pushAbility = 3,
            burstDamage = 3,
            sustainedDamage = 3
        )
    }

    /**
     * 创建玩家列表
     */
    private fun createPlayers(
        heroes: List<Hero>,
        ranks: List<String>,
        team: Team
    ): List<Player> {
        // 智能分配分路
        val assignedLanes = assignLanes(heroes)
        return heroes.mapIndexed { index, hero ->
            Player(
                hero = hero,
                rank = ranks.getOrElse(index) { "钻石" },
                lane = assignedLanes[index],
                team = team
            )
        }
    }

    /**
     * 智能分配分路
     */
    private fun assignLanes(heroes: List<Hero>): List<Lane> {
        val assigned = mutableListOf<Lane>()
        val availableLanes = mutableListOf(Lane.TOP, Lane.JUNGLE, Lane.MID, Lane.ADC, Lane.SUPPORT)
        val usedLanes = mutableSetOf<Lane>()

        for (hero in heroes) {
            val preferredLane = hero.lane
            if (preferredLane != Lane.UNKNOWN && preferredLane !in usedLanes) {
                assigned.add(preferredLane)
                usedLanes.add(preferredLane)
            } else {
                // 找到第一个未使用的分路
                val lane = availableLanes.firstOrNull { it !in usedLanes } ?: Lane.UNKNOWN
                assigned.add(lane)
                usedLanes.add(lane)
            }
        }

        // 如果分配不足5个，补充未使用的
        while (assigned.size < 5) {
            val lane = availableLanes.firstOrNull { it !in usedLanes } ?: Lane.UNKNOWN
            if (lane !in assigned) {
                assigned.add(lane)
                usedLanes.add(lane)
            } else {
                break
            }
        }

        return assigned
    }

    /**
     * 分析单支队伍
     */
    private fun analyzeTeam(team: Team, players: List<Player>): TeamAnalysis {
        val heroes = players.map { it.hero }

        // 计算各项属性总和
        val totalTankiness = heroes.sumOf { it.tankiness }
        val totalControl = heroes.sumOf { it.controlAbility }
        val totalBurstDamage = heroes.sumOf { it.burstDamage }
        val totalSustainedDamage = heroes.sumOf { it.sustainedDamage }
        val earlyGameStrength = heroes.sumOf { it.earlyGameStrength }
        val lateGameStrength = heroes.sumOf { it.lateGameStrength }
        val totalMobility = heroes.sumOf { it.mobility }
        val totalPushAbility = heroes.sumOf { it.pushAbility }

        // 计算开团能力（坦克+控制）
        val tankCount = heroes.count { it.isTank || it.isFighter }
        val initiationAbility = (totalControl + tankCount * 2).coerceAtMost(25)

        // 计算保护能力（辅助+坦克）
        val supportCount = heroes.count { it.isSupport || it.isTank }
        val peelAbility = (supportCount * 3 + heroes.sumOf { it.controlAbility }).coerceAtMost(25)

        // 计算团战强度
        val teamFightStrength = (
            totalBurstDamage * 0.3 +
            totalSustainedDamage * 0.2 +
            totalTankiness * 0.2 +
            totalControl * 0.2 +
            supportCount * 0.1
        ).toInt().coerceIn(0, 25)

        // 生成优势点
        val strengths = mutableListOf<String>()
        if (totalTankiness >= 18) strengths.add("前排够硬，团战站得住")
        if (totalControl >= 18) strengths.add("控制充足，可先手开团")
        if (totalBurstDamage >= 20) strengths.add("爆发伤害高，可快速击杀")
        if (earlyGameStrength >= 20) strengths.add("前期强势，可滚雪球")
        if (lateGameStrength >= 20) strengths.add("后期强势，可拖发育")
        if (tankCount >= 2) strengths.add("双前排阵容，坦度优秀")
        if (supportCount >= 1) strengths.add("有辅助位，保护能力强")

        // 生成劣势点
        val weaknesses = mutableListOf<String>()
        if (totalTankiness < 12) weaknesses.add("前排较脆，容易被秒")
        if (totalControl < 12) weaknesses.add("缺少控制技能")
        if (heroes.count { it.isMarksman } >= 2) weaknesses.add("双射手阵容，前期较弱")
        if (earlyGameStrength < 15) weaknesses.add("前期弱势，需避战发育")
        if (lateGameStrength < 15) weaknesses.add("后期乏力，需要速战速决")
        if (supportCount == 0) weaknesses.add("缺少辅助位，视野和保护不足")

        // 计算整体强度
        val overallStrength = (
            teamFightStrength * 0.4 +
            earlyGameStrength * 0.2 +
            lateGameStrength * 0.2 +
            peelAbility * 0.1 +
            totalPushAbility * 0.1
        ).toInt().coerceIn(1, 10)

        // 生成推荐策略
        val recommendedStrategy = when {
            earlyGameStrength >= 20 -> "前期强势，建议积极入侵野区、争夺资源"
            lateGameStrength >= 20 -> "后期强势，建议稳健发育、避免团战"
            teamFightStrength >= 20 -> "团战强势，建议抱团推进"
            else -> "均衡阵容，根据对局情况灵活应对"
        }

        return TeamAnalysis(
            team = team,
            players = players,
            totalTankiness = totalTankiness,
            totalControl = totalControl,
            totalBurstDamage = totalBurstDamage,
            totalSustainedDamage = totalSustainedDamage,
            earlyGameStrength = earlyGameStrength,
            lateGameStrength = lateGameStrength,
            initiationAbility = initiationAbility,
            peelAbility = peelAbility,
            pushAbility = totalPushAbility,
            teamFightStrength = teamFightStrength,
            strengths = strengths.ifEmpty { listOf("阵容较为均衡") },
            weaknesses = weaknesses.ifEmpty { listOf("无明显劣势") },
            overallStrength = overallStrength,
            recommendedStrategy = recommendedStrategy
        )
    }

    /**
     * 分析克制关系
     */
    private fun analyzeCounters(
        blueHeroes: List<Hero>,
        redHeroes: List<Hero>
    ): List<CounterAnalysis> {
        val analyses = mutableListOf<CounterAnalysis>()

        // 简单克制关系分析
        for (hero in blueHeroes + redHeroes) {
            val counteredBy = mutableListOf<String>()
            val counters = mutableListOf<String>()
            val synergyPartners = mutableListOf<String>()

            // 基于英雄类型判断克制关系
            if (hero.isMarksman) {
                counteredBy.addAll(redHeroes.filter { it.isAssassin }.map { it.name })
                synergyPartners.addAll(blueHeroes.filter { it.isSupport || it.isTank }.map { it.name })
            }
            if (hero.isTank) {
                counteredBy.addAll(redHeroes.filter { it.isMarksman }.map { it.name })
                synergyPartners.addAll(blueHeroes.filter { it.isMarksman }.map { it.name })
            }
            if (hero.isAssassin) {
                counteredBy.addAll(redHeroes.filter { it.isTank }.map { it.name })
                synergyPartners.addAll(blueHeroes.filter { it.isFighter }.map { it.name })
            }

            analyses.add(
                CounterAnalysis(
                    heroName = hero.name,
                    counteredBy = counteredBy.distinct().take(3),
                    counters = counters.distinct().take(3),
                    synergyPartners = synergyPartners.distinct().take(3)
                )
            )
        }

        return analyses
    }

    /**
     * 生成取胜关键点
     */
    private fun generateKeyFactors(
        blueAnalysis: TeamAnalysis,
        redAnalysis: TeamAnalysis,
        counterAnalyses: List<CounterAnalysis>
    ): List<String> {
        val factors = mutableListOf<String>()

        // 坦度对比
        val tankDiff = blueAnalysis.totalTankiness - redAnalysis.totalTankiness
        when {
            tankDiff >= 5 -> factors.add("蓝方前排优势明显，团战更容易站住脚")
            tankDiff <= -5 -> factors.add("红方前排更硬，蓝方需要远程消耗")
        }

        // 控制对比
        val controlDiff = blueAnalysis.totalControl - redAnalysis.totalControl
        when {
            controlDiff >= 5 -> factors.add("蓝方控制更多，可主动开团")
            controlDiff <= -5 -> factors.add("红方控制更强，蓝方需谨慎走位")
        }

        // 前期对比
        val earlyDiff = blueAnalysis.earlyGameStrength - redAnalysis.earlyGameStrength
        when {
            earlyDiff >= 5 -> factors.add("蓝方前期强势，应积极入侵/速推")
            earlyDiff <= -5 -> factors.add("红方前期压制，蓝方需稳健发育")
        }

        // 后期对比
        val lateDiff = blueAnalysis.lateGameStrength - redAnalysis.lateGameStrength
        when {
            lateDiff >= 5 -> factors.add("蓝方后期更强，可拖到大后期")
            lateDiff <= -5 -> factors.add("红方后期强势，蓝方需速战速决")
        }

        // 开团能力
        if (blueAnalysis.initiationAbility > redAnalysis.peelAbility + 5) {
            factors.add("蓝方开团能力强，可主动寻找团战机会")
        }
        if (redAnalysis.initiationAbility > blueAnalysis.peelAbility + 5) {
            factors.add("红方开团能力强，蓝方需分散站位")
        }

        return factors.ifEmpty { listOf("双方阵容较为均衡，细节决定成败") }
    }

    /**
     * 计算胜率
     */
    private fun calculateWinProbability(
        blueAnalysis: TeamAnalysis,
        redAnalysis: TeamAnalysis
    ): Float {
        var score = 50f

        // 整体强度
        score += (blueAnalysis.overallStrength - redAnalysis.overallStrength) * 2f

        // 坦度
        score += (blueAnalysis.totalTankiness - redAnalysis.totalTankiness) * 0.5f

        // 控制
        score += (blueAnalysis.totalControl - redAnalysis.totalControl) * 0.5f

        // 前期
        score += (blueAnalysis.earlyGameStrength - redAnalysis.earlyGameStrength) * 0.3f

        // 后期
        score += (blueAnalysis.lateGameStrength - redAnalysis.lateGameStrength) * 0.3f

        // 团战
        score += (blueAnalysis.teamFightStrength - redAnalysis.teamFightStrength) * 0.5f

        // 限制范围
        return (score / 100f).coerceIn(0.1f, 0.9f)
    }

    /**
     * 预估对局时长
     */
    private fun estimateGameDuration(
        blueAnalysis: TeamAnalysis,
        redAnalysis: TeamAnalysis
    ): String {
        val avgEarly = (blueAnalysis.earlyGameStrength + redAnalysis.earlyGameStrength) / 2
        val avgLate = (blueAnalysis.lateGameStrength + redAnalysis.lateGameStrength) / 2

        return when {
            avgEarly >= 20 -> "8-12分钟（快节奏）"
            avgLate >= 20 -> "15-20分钟（膀胱局）"
            else -> "10-15分钟（常规局）"
        }
    }

    /**
     * 生成推荐打法
     */
    private fun generatePlaystyle(
        blueAnalysis: TeamAnalysis,
        redAnalysis: TeamAnalysis
    ): String {
        val factors = mutableListOf<String>()

        if (blueAnalysis.earlyGameStrength >= 18) factors.add("前期入侵")
        if (blueAnalysis.lateGameStrength >= 18) factors.add("稳健发育")
        if (blueAnalysis.initiationAbility >= 18) factors.add("抱团推进")
        if (blueAnalysis.pushAbility >= 15) factors.add("速推战术")
        if (blueAnalysis.peelAbility >= 18) factors.add("保护后排")

        return when {
            factors.isEmpty() -> "均衡打法，灵活应对"
            else -> factors.joinToString("、")
        }
    }

    /**
     * 生成警告信息
     */
    private fun generateWarnings(
        blueAnalysis: TeamAnalysis,
        redAnalysis: TeamAnalysis
    ): List<String> {
        val warnings = mutableListOf<String>()

        // 检查蓝方劣势
        if (blueAnalysis.weaknesses.contains("前期弱势，需避战发育")) {
            warnings.add("注意：避免前期团战，专注发育")
        }
        if (blueAnalysis.weaknesses.contains("前排较脆，容易被秒")) {
            warnings.add("注意：后排容易被切，需注意站位")
        }
        if (blueAnalysis.weaknesses.contains("缺少控制技能")) {
            warnings.add("注意：容易被敌方先手开团")
        }

        // 检查红方优势
        if (redAnalysis.earlyGameStrength > blueAnalysis.earlyGameStrength + 3) {
            warnings.add("警告：红方前期压制力强")
        }
        if (redAnalysis.initiationAbility > blueAnalysis.peelAbility + 5) {
            warnings.add("警告：红方开团能力强，注意分散站位")
        }

        return warnings
    }

    /**
     * 生成演示数据（用于测试）
     */
    fun generateDemoAnalysis(): MatchAnalysis {
        return analyzeMatch(
            blueTeamHeroes = listOf("猪八戒", "娜可露露", "不知火舞", "后羿", "牛魔"),
            redTeamHeroes = listOf("铠", "孙悟空", "安琪拉", "马可波罗", "张飞"),
            blueTeamRanks = listOf("星耀", "王者", "星耀", "钻石", "钻石"),
            redTeamRanks = listOf("星耀", "星耀", "王者", "星耀", "钻石")
        )
    }
}
