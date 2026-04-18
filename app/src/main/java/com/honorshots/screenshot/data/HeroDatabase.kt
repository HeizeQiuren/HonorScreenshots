package com.honorshots.screenshot.data

/**
 * 英雄数据库
 * 包含王者荣耀常见英雄的属性数据
 * TODO: 可扩展为从服务器获取或使用机器学习模型识别
 */
object HeroDatabase {

    /**
     * 获取所有英雄列表
     */
    fun getAllHeroes(): Map<String, Hero> = heroes

    /**
     * 根据英雄名称获取英雄数据
     */
    fun getHeroByName(name: String): Hero? {
        return heroes[name] ?: heroes.values.find {
            it.name.contains(name, ignoreCase = true) ||
            name.contains(it.name, ignoreCase = true)
        }
    }

    /**
     * 根据英雄ID获取英雄数据
     */
    fun getHeroById(id: String): Hero? = heroes[id]

    /**
     * 英雄数据库
     */
    private val heroes = mapOf(
        // ===== 坦克英雄 =====
        "猪八戒" to Hero(
            id = "113", name = "猪八戒", role = "坦克/战士",
            lane = Lane.TOP, isTank = true, isFighter = true,
            tankiness = 5, controlAbility = 4, earlyGameStrength = 4, lateGameStrength = 3,
            mobility = 2, pushAbility = 3, burstDamage = 2, sustainedDamage = 4
        ),
        "白起" to Hero(
            id = "114", name = "白起", role = "坦克",
            lane = Lane.TOP, isTank = true,
            tankiness = 5, controlAbility = 5, earlyGameStrength = 3, lateGameStrength = 4,
            mobility = 2, pushAbility = 2, burstDamage = 2, sustainedDamage = 3
        ),
        "廉颇" to Hero(
            id = "115", name = "廉颇", role = "坦克",
            lane = Lane.TOP, isTank = true,
            tankiness = 5, controlAbility = 5, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 2, pushAbility = 3, burstDamage = 2, sustainedDamage = 3
        ),
        "张飞" to Hero(
            id = "116", name = "张飞", role = "坦克/辅助",
            lane = Lane.SUPPORT, isTank = true, isSupport = true,
            tankiness = 5, controlAbility = 4, earlyGameStrength = 3, lateGameStrength = 4,
            mobility = 2, pushAbility = 2, burstDamage = 2, sustainedDamage = 3
        ),
        "程咬金" to Hero(
            id = "117", name = "程咬金", role = "坦克/战士",
            lane = Lane.TOP, isTank = true, isFighter = true,
            tankiness = 4, controlAbility = 2, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 3, pushAbility = 3, burstDamage = 3, sustainedDamage = 5
        ),

        // ===== 战士英雄 =====
        "铠" to Hero(
            id = "118", name = "铠", role = "战士/刺客",
            lane = Lane.TOP, isFighter = true,
            tankiness = 3, controlAbility = 2, earlyGameStrength = 4, lateGameStrength = 5,
            mobility = 3, pushAbility = 3, burstDamage = 5, sustainedDamage = 4
        ),
        "曹操" to Hero(
            id = "119", name = "曹操", role = "战士",
            lane = Lane.TOP, isFighter = true,
            tankiness = 3, controlAbility = 2, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 4, pushAbility = 3, burstDamage = 4, sustainedDamage = 5
        ),
        "老夫子" to Hero(
            id = "120", name = "老夫子", role = "战士/坦克",
            lane = Lane.TOP, isFighter = true, isTank = true,
            tankiness = 4, controlAbility = 3, earlyGameStrength = 3, lateGameStrength = 5,
            mobility = 2, pushAbility = 3, burstDamage = 4, sustainedDamage = 4
        ),
        "马超" to Hero(
            id = "121", name = "马超", role = "战士/刺客",
            lane = Lane.TOP, isFighter = true, isAssassin = true,
            tankiness = 2, controlAbility = 2, earlyGameStrength = 5, lateGameStrength = 4,
            mobility = 5, pushAbility = 3, burstDamage = 5, sustainedDamage = 3
        ),
        "夏洛特" to Hero(
            id = "122", name = "夏洛特", role = "战士",
            lane = Lane.TOP, isFighter = true,
            tankiness = 3, controlAbility = 3, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 4, pushAbility = 3, burstDamage = 4, sustainedDamage = 3
        ),

        // ===== 刺客英雄 =====
        "孙悟空" to Hero(
            id = "123", name = "孙悟空", role = "刺客/战士",
            lane = Lane.JUNGLE, isAssassin = true, isFighter = true,
            tankiness = 2, controlAbility = 2, earlyGameStrength = 3, lateGameStrength = 5,
            mobility = 4, pushAbility = 3, burstDamage = 5, sustainedDamage = 3
        ),
        "娜可露露" to Hero(
            id = "124", name = "娜可露露", role = "刺客",
            lane = Lane.JUNGLE, isAssassin = true,
            tankiness = 1, controlAbility = 1, earlyGameStrength = 3, lateGameStrength = 5,
            mobility = 5, pushAbility = 2, burstDamage = 5, sustainedDamage = 2
        ),
        "兰陵王" to Hero(
            id = "125", name = "兰陵王", role = "刺客",
            lane = Lane.JUNGLE, isAssassin = true,
            tankiness = 1, controlAbility = 2, earlyGameStrength = 5, lateGameStrength = 2,
            mobility = 4, pushAbility = 2, burstDamage = 5, sustainedDamage = 2
        ),
        "镜" to Hero(
            id = "126", name = "镜", role = "刺客",
            lane = Lane.JUNGLE, isAssassin = true,
            tankiness = 2, controlAbility = 2, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 5, pushAbility = 3, burstDamage = 5, sustainedDamage = 3
        ),
        "澜" to Hero(
            id = "127", name = "澜", role = "刺客",
            lane = Lane.JUNGLE, isAssassin = true,
            tankiness = 2, controlAbility = 2, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 5, pushAbility = 2, burstDamage = 5, sustainedDamage = 3
        ),

        // ===== 法师英雄 =====
        "安琪拉" to Hero(
            id = "128", name = "安琪拉", role = "法师",
            lane = Lane.MID, isMage = true,
            tankiness = 1, controlAbility = 4, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 2, pushAbility = 4, burstDamage = 5, sustainedDamage = 3
        ),
        "妲己" to Hero(
            id = "129", name = "妲己", role = "法师",
            lane = Lane.MID, isMage = true,
            tankiness = 1, controlAbility = 3, earlyGameStrength = 3, lateGameStrength = 4,
            mobility = 2, pushAbility = 3, burstDamage = 5, sustainedDamage = 2
        ),
        "王昭君" to Hero(
            id = "130", name = "王昭君", role = "法师",
            lane = Lane.MID, isMage = true,
            tankiness = 2, controlAbility = 5, earlyGameStrength = 3, lateGameStrength = 4,
            mobility = 2, pushAbility = 4, burstDamage = 4, sustainedDamage = 3
        ),
        "不知火舞" to Hero(
            id = "131", name = "不知火舞", role = "法师/刺客",
            lane = Lane.MID, isMage = true, isAssassin = true,
            tankiness = 1, controlAbility = 4, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 5, pushAbility = 3, burstDamage = 5, sustainedDamage = 2
        ),
        "上官婉儿" to Hero(
            id = "132", name = "上官婉儿", role = "法师/刺客",
            lane = Lane.MID, isMage = true, isAssassin = true,
            tankiness = 1, controlAbility = 2, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 5, pushAbility = 3, burstDamage = 5, sustainedDamage = 2
        ),
        "沈梦溪" to Hero(
            id = "133", name = "沈梦溪", role = "法师",
            lane = Lane.MID, isMage = true,
            tankiness = 1, controlAbility = 3, earlyGameStrength = 5, lateGameStrength = 3,
            mobility = 3, pushAbility = 5, burstDamage = 4, sustainedDamage = 3
        ),
        "干将莫邪" to Hero(
            id = "134", name = "干将莫邪", role = "法师",
            lane = Lane.MID, isMage = true,
            tankiness = 1, controlAbility = 2, earlyGameStrength = 4, lateGameStrength = 5,
            mobility = 2, pushAbility = 3, burstDamage = 5, sustainedDamage = 2
        ),
        "武则天" to Hero(
            id = "135", name = "武则天", role = "法师",
            lane = Lane.MID, isMage = true,
            tankiness = 1, controlAbility = 5, earlyGameStrength = 3, lateGameStrength = 5,
            mobility = 2, pushAbility = 4, burstDamage = 4, sustainedDamage = 4
        ),

        // ===== 射手英雄 =====
        "后羿" to Hero(
            id = "136", name = "后羿", role = "射手",
            lane = Lane.ADC, isMarksman = true,
            tankiness = 1, controlAbility = 2, earlyGameStrength = 3, lateGameStrength = 5,
            mobility = 2, pushAbility = 4, burstDamage = 3, sustainedDamage = 5
        ),
        "鲁班七号" to Hero(
            id = "137", name = "鲁班七号", role = "射手",
            lane = Lane.ADC, isMarksman = true,
            tankiness = 1, controlAbility = 2, earlyGameStrength = 3, lateGameStrength = 5,
            mobility = 1, pushAbility = 5, burstDamage = 4, sustainedDamage = 4
        ),
        "马可波罗" to Hero(
            id = "138", name = "马可波罗", role = "射手",
            lane = Lane.ADC, isMarksman = true,
            tankiness = 2, controlAbility = 2, earlyGameStrength = 3, lateGameStrength = 4,
            mobility = 4, pushAbility = 3, burstDamage = 4, sustainedDamage = 5
        ),
        "孙尚香" to Hero(
            id = "139", name = "孙尚香", role = "射手",
            lane = Lane.ADC, isMarksman = true,
            tankiness = 1, controlAbility = 1, earlyGameStrength = 3, lateGameStrength = 5,
            mobility = 4, pushAbility = 3, burstDamage = 5, sustainedDamage = 4
        ),
        "虞姬" to Hero(
            id = "140", name = "虞姬", role = "射手",
            lane = Lane.ADC, isMarksman = true,
            tankiness = 1, controlAbility = 1, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 3, pushAbility = 3, burstDamage = 4, sustainedDamage = 4
        ),
        "狄仁杰" to Hero(
            id = "141", name = "狄仁杰", role = "射手",
            lane = Lane.ADC, isMarksman = true,
            tankiness = 2, controlAbility = 3, earlyGameStrength = 3, lateGameStrength = 4,
            mobility = 2, pushAbility = 4, burstDamage = 3, sustainedDamage = 5
        ),
        "黄忠" to Hero(
            id = "142", name = "黄忠", role = "射手",
            lane = Lane.ADC, isMarksman = true,
            tankiness = 2, controlAbility = 2, earlyGameStrength = 2, lateGameStrength = 5,
            mobility = 1, pushAbility = 4, burstDamage = 3, sustainedDamage = 5
        ),
        "蒙犽" to Hero(
            id = "143", name = "蒙犽", role = "射手",
            lane = Lane.ADC, isMarksman = true,
            tankiness = 2, controlAbility = 2, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 3, pushAbility = 4, burstDamage = 4, sustainedDamage = 4
        ),

        // ===== 辅助英雄 =====
        "张飞" to Hero(
            id = "144", name = "张飞", role = "辅助/坦克",
            lane = Lane.SUPPORT, isSupport = true, isTank = true,
            tankiness = 5, controlAbility = 4, earlyGameStrength = 3, lateGameStrength = 4,
            mobility = 2, pushAbility = 2, burstDamage = 2, sustainedDamage = 3
        ),
        "牛魔" to Hero(
            id = "145", name = "牛魔", role = "辅助/坦克",
            lane = Lane.SUPPORT, isSupport = true, isTank = true,
            tankiness = 5, controlAbility = 5, earlyGameStrength = 3, lateGameStrength = 4,
            mobility = 2, pushAbility = 2, burstDamage = 2, sustainedDamage = 3
        ),
        "孙膑" to Hero(
            id = "146", name = "孙膑", role = "辅助",
            lane = Lane.SUPPORT, isSupport = true,
            tankiness = 2, controlAbility = 3, earlyGameStrength = 3, lateGameStrength = 5,
            mobility = 4, pushAbility = 3, burstDamage = 2, sustainedDamage = 4
        ),
        "大乔" to Hero(
            id = "147", name = "大乔", role = "辅助",
            lane = Lane.SUPPORT, isSupport = true,
            tankiness = 2, controlAbility = 3, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 5, pushAbility = 4, burstDamage = 2, sustainedDamage = 3
        ),
        "太乙真人" to Hero(
            id = "148", name = "太乙真人", role = "辅助",
            lane = Lane.SUPPORT, isSupport = true,
            tankiness = 3, controlAbility = 4, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 3, pushAbility = 2, burstDamage = 3, sustainedDamage = 3
        ),
        "明世隐" to Hero(
            id = "149", name = "明世隐", role = "辅助",
            lane = Lane.SUPPORT, isSupport = true,
            tankiness = 2, controlAbility = 2, earlyGameStrength = 4, lateGameStrength = 4,
            mobility = 2, pushAbility = 2, burstDamage = 2, sustainedDamage = 4
        ),
        "东皇太一" to Hero(
            id = "150", name = "东皇太一", role = "辅助/坦克",
            lane = Lane.SUPPORT, isSupport = true, isTank = true,
            tankiness = 4, controlAbility = 5, earlyGameStrength = 5, lateGameStrength = 3,
            mobility = 2, pushAbility = 2, burstDamage = 3, sustainedDamage = 4
        ),
        "盾山" to Hero(
            id = "151", name = "盾山", role = "辅助/坦克",
            lane = Lane.SUPPORT, isSupport = true, isTank = true,
            tankiness = 5, controlAbility = 5, earlyGameStrength = 3, lateGameStrength = 3,
            mobility = 1, pushAbility = 2, burstDamage = 1, sustainedDamage = 2
        ),
        "蔡文姬" to Hero(
            id = "152", name = "蔡文姬", role = "辅助",
            lane = Lane.SUPPORT, isSupport = true,
            tankiness = 2, controlAbility = 2, earlyGameStrength = 3, lateGameStrength = 4,
            mobility = 2, pushAbility = 2, burstDamage = 1, sustainedDamage = 5
        ),
        "瑶" to Hero(
            id = "153", name = "瑶", role = "辅助",
            lane = Lane.SUPPORT, isSupport = true,
            tankiness = 1, controlAbility = 2, earlyGameStrength = 3, lateGameStrength = 4,
            mobility = 4, pushAbility = 2, burstDamage = 1, sustainedDamage = 3
        )
    )
}
