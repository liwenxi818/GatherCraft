package com.gathercraft.gathercraft.title;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.world.entity.player.Player;

/**
 * 칭호 17종 정의. requiredSkill이 null이면 전 스킬(9개) 조건으로 취급한다.
 */
public enum Title {
    MINER_1    ("miner_1",     "§7[광부]",          SkillType.MINING,      30),
    MINER_2    ("miner_2",     "§e[숙련 광부]",      SkillType.MINING,      70),
    MINER_3    ("miner_3",     "§6[전설의 광부]",    SkillType.MINING,      100),
    HUNTER_1   ("hunter_1",    "§c[사냥꾼]",         SkillType.HUNTING,     30),
    HUNTER_2   ("hunter_2",    "§4[맹수]",           SkillType.HUNTING,     70),
    HUNTER_3   ("hunter_3",    "§c§l[각성 사냥꾼]",  SkillType.HUNTING,     100),
    FARMER_1   ("farmer_1",    "§a[농부]",           SkillType.FARMING,     30),
    FARMER_2   ("farmer_2",    "§2[대농부]",         SkillType.FARMING,     70),
    FISHER_1   ("fisher_1",    "§b[낚시꾼]",         SkillType.FISHING,     50),
    FISHER_2   ("fisher_2",    "§3[심해 낚시꾼]",    SkillType.FISHING,     100),
    CHEF_1     ("chef_1",      "§e[요리사]",         SkillType.COOKING,     50),
    CHEF_2     ("chef_2",      "§6[명인 요리사]",    SkillType.COOKING,     100),
    SMITH_1    ("smith_1",     "§7[대장장이]",       SkillType.SMITHING,    50),
    ENCHANTER_1("enchanter_1", "§5[마법사]",         SkillType.ENCHANTING,  50),
    ENCHANTER_2("enchanter_2", "§d§l[대마법사]",     SkillType.ENCHANTING,  100),
    ALL_50     ("all_50",      "§6[모험가]",         null,                  50),
    ALL_100    ("all_100",     "§b§l[각성왕]",       null,                  100);

    public final String id;
    public final String displayName;
    public final SkillType requiredSkill;
    public final int requiredLevel;

    Title(String id, String displayName, SkillType requiredSkill, int requiredLevel) {
        this.id = id;
        this.displayName = displayName;
        this.requiredSkill = requiredSkill;
        this.requiredLevel = requiredLevel;
    }

    /** 조건 충족 여부를 확인한다. requiredSkill이 null이면 전 스킬 조건. */
    public boolean isUnlocked(Player player) {
        if (requiredSkill != null) {
            return SkillData.getLevel(player, requiredSkill) >= requiredLevel;
        }
        for (SkillType skill : SkillType.values()) {
            if (SkillData.getLevel(player, skill) < requiredLevel) return false;
        }
        return true;
    }

    /** "채광 30레벨" 또는 "전 스킬 50레벨" 형태의 조건 텍스트. */
    public String conditionText() {
        if (requiredSkill != null) {
            return requiredSkill.getKoreanName() + " " + requiredLevel + "레벨";
        }
        return "전 스킬 " + requiredLevel + "레벨";
    }

    public static Title byId(String id) {
        for (Title title : values()) {
            if (title.id.equals(id)) return title;
        }
        return null;
    }
}
