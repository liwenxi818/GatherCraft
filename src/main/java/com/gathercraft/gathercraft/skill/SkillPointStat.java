package com.gathercraft.gathercraft.skill;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 레벨업 시 제시되는 스탯 포인트 선택지.
 * 스킬당 4개씩, 총 36개 정의.
 * NBT 키: "sp_" + stat.name() (예: "sp_MINING_EXTRA_DROP")
 * † 표시: 저장은 되지만 실제 적용은 아직 미구현
 */
public enum SkillPointStat {

    // === 채광 (MINING) ===
    MINING_EXTRA_DROP  (SkillType.MINING,     "추가 드롭 확률",      "+1%",    0.01f),
    MINING_SPEED       (SkillType.MINING,     "채굴 속도",           "+3%†",   0.03f),
    MINING_RARE_DROP   (SkillType.MINING,     "희귀 광석 드롭 확률", "+1%",    0.01f),
    MINING_XP_BONUS    (SkillType.MINING,     "채굴 XP 보너스",      "+5%†",   0.05f),

    // === 벌목 (LUMBERJACK) ===
    LUMBERJACK_EXTRA_DROP (SkillType.LUMBERJACK, "원목 추가 드롭 확률", "+1%",  0.01f),
    LUMBERJACK_DURABILITY (SkillType.LUMBERJACK, "도끼 내구도 소모",   "-3%†",  0.03f),
    LUMBERJACK_SAPLING    (SkillType.LUMBERJACK, "묘목 드롭 확률",    "+2%",    0.02f),
    LUMBERJACK_SPEED      (SkillType.LUMBERJACK, "벌목 속도",         "+3%†",   0.03f),

    // === 농사 (FARMING) ===
    FARMING_EXTRA_DROP (SkillType.FARMING,    "작물 추가 드롭",       "+1%",    0.01f),
    FARMING_BONEMEAL   (SkillType.FARMING,    "뼛가루 효율",          "+5%†",   0.05f),
    FARMING_GROWTH     (SkillType.FARMING,    "작물 성장 속도",       "+2%†",   0.02f),
    FARMING_REPLANT    (SkillType.FARMING,    "씨앗 자동 재식 확률",  "+3%",    0.03f),

    // === 낚시 (FISHING) ===
    FISHING_RARE       (SkillType.FISHING,    "희귀 아이템 확률",     "+1%",    0.01f),
    FISHING_SPEED      (SkillType.FISHING,    "낚시 속도",            "+3%†",   0.03f),
    FISHING_JUNK_REDUCE(SkillType.FISHING,    "쓰레기 드롭 확률",     "-2%",    0.02f),
    FISHING_FISH_DROP  (SkillType.FISHING,    "물고기 추가 드롭",     "+1%",    0.01f),

    // === 요리 (COOKING) ===
    COOKING_BUFF_DURATION  (SkillType.COOKING, "버프 지속시간",        "+5%",   0.05f),
    COOKING_BUFF_AMPLIFIER (SkillType.COOKING, "버프 강도",            "+1단계", 0.5f),
    COOKING_SATURATION     (SkillType.COOKING, "음식 포화도",          "+3%†",  0.03f),
    COOKING_EXTRA_BUFF     (SkillType.COOKING, "추가 버프 발동 확률",  "+2%†",  0.02f),

    // === 사냥 (HUNTING) ===
    HUNTING_ATTACK     (SkillType.HUNTING,    "공격력",               "+2%",    0.02f),
    HUNTING_CRITICAL   (SkillType.HUNTING,    "크리티컬 확률",        "+1%",    0.01f),
    HUNTING_HEAL       (SkillType.HUNTING,    "처치 시 체력 회복",    "+0.5HP", 0.5f),
    HUNTING_RARE_DROP  (SkillType.HUNTING,    "희귀 드롭 확률",       "+1%",    0.01f),

    // === 방어 (DEFENSE) ===
    DEFENSE_DAMAGE_REDUCE(SkillType.DEFENSE,  "데미지 감소",          "+1%",    0.01f),
    DEFENSE_MAX_HEALTH   (SkillType.DEFENSE,  "최대 체력",            "+1HP",   1.0f),
    DEFENSE_KNOCKBACK    (SkillType.DEFENSE,  "넉백 저항",            "+3%",    0.03f),
    DEFENSE_INVULNERABLE (SkillType.DEFENSE,  "무적 시간",            "+5%",    0.05f),

    // === 대장장이 (SMITHING) ===
    SMITHING_DURABILITY    (SkillType.SMITHING, "도구 내구도",         "+5%†",  0.05f),
    SMITHING_REPAIR_COST   (SkillType.SMITHING, "수리 비용 감소",      "+3%",   0.03f),
    SMITHING_MATERIAL_SAVE (SkillType.SMITHING, "재료 절약 확률",      "+1%",   0.01f),
    SMITHING_ENCHANT_CHANCE(SkillType.SMITHING, "랜덤 인챈트 확률",    "+2%",   0.02f),

    // === 마법부여 (ENCHANTING) ===
    ENCHANTING_COST_REDUCE (SkillType.ENCHANTING, "인챈트 비용 감소",  "+3%†",  0.03f),
    ENCHANTING_LEVEL_BONUS (SkillType.ENCHANTING, "인챈트 레벨 보너스","+0.5",  0.5f),
    ENCHANTING_EXTRA       (SkillType.ENCHANTING, "추가 인챈트 확률",  "+1%†",  0.01f),
    ENCHANTING_CURSE_IMMUNE(SkillType.ENCHANTING, "저주 면역 확률",    "+2%†",  0.02f);

    public final SkillType skill;
    public final String displayName;
    /** 선택지 버튼에 표시되는 증가량 텍스트 */
    public final String incrementText;
    /** addStatValue 시 1회 선택당 누적되는 수치 */
    public final float increment;

    SkillPointStat(SkillType skill, String displayName, String incrementText, float increment) {
        this.skill = skill;
        this.displayName = displayName;
        this.incrementText = incrementText;
        this.increment = increment;
    }

    /** NBT 저장 키: "sp_" + this.name() */
    public String getNbtKey() {
        return "sp_" + this.name();
    }

    /** 특정 스킬에 속하는 4개 선택지를 반환한다. */
    public static List<SkillPointStat> getOptionsForSkill(SkillType skill) {
        return Arrays.stream(values())
            .filter(s -> s.skill == skill)
            .collect(Collectors.toList());
    }
}
