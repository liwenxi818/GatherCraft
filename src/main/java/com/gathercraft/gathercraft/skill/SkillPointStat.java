package com.gathercraft.gathercraft.skill;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 레벨업 시 제시되는 스탯 포인트 선택지.
 * 스킬당 4개씩, 총 36개 정의.
 * NBT 키: "sp_" + stat.name() (예: "sp_MINING_EXTRA_DROP")
 */
public enum SkillPointStat {

    // === 채광 (MINING) ===
    MINING_EXTRA_DROP  (SkillType.MINING,     "추가 드롭 확률",      "+1%",    0.01f, "광석 채굴 시 아이템을 추가로 드롭"),
    MINING_SPEED       (SkillType.MINING,     "채굴 속도",           "6회마다 성급함 +1", 0.03f, "6번 선택마다 채굴 성급함 레벨 +1 (현재 {n}/6)"),
    MINING_RARE_DROP   (SkillType.MINING,     "희귀 광석 드롭 확률", "+1%",    0.01f, "희귀 광석에서 추가 드롭 확률 증가"),
    MINING_XP_BONUS    (SkillType.MINING,     "채굴 XP 보너스",      "+5%",    0.05f, "채굴로 얻는 XP에 배율 적용"),

    // === 벌목 (LUMBERJACK) ===
    LUMBERJACK_EXTRA_DROP (SkillType.LUMBERJACK, "원목 추가 드롭 확률", "+1%",  0.01f, "원목 채굴 시 추가 드롭 확률 증가"),
    LUMBERJACK_DURABILITY (SkillType.LUMBERJACK, "도끼 내구도 소모",   "-3%",   0.03f, "도끼 내구도 소모 취소 확률 증가"),
    LUMBERJACK_SAPLING    (SkillType.LUMBERJACK, "묘목 드롭 확률",    "+2%",    0.02f, "나뭇잎 파괴 시 묘목 드롭 확률 증가"),
    LUMBERJACK_SPEED      (SkillType.LUMBERJACK, "벌목 속도",         "6회마다 성급함 +1", 0.03f, "6번 선택마다 벌목 성급함 레벨 +1 (현재 {n}/6)"),

    // === 농사 (FARMING) ===
    FARMING_EXTRA_DROP (SkillType.FARMING,    "작물 추가 드롭",       "+1%",    0.01f, "작물 수확 시 추가 드롭 확률 증가"),
    FARMING_BONEMEAL   (SkillType.FARMING,    "뼛가루 효율",          "+5%",    0.05f, "뼛가루 즉시 완숙 확률 추가 증가"),
    FARMING_GROWTH     (SkillType.FARMING,    "작물 성장 속도",       "+2%",    0.02f, "작물 자연 성장 추가 트리거 확률 증가"),
    FARMING_REPLANT    (SkillType.FARMING,    "씨앗 자동 재식 확률",  "+3%",    0.03f, "작물 수확 후 씨앗 자동 재식 확률 증가"),

    // === 낚시 (FISHING) ===
    FISHING_RARE       (SkillType.FISHING,    "희귀 아이템 확률",     "+1%",    0.01f, "보물·희귀 아이템 낚을 확률 증가"),
    FISHING_SPEED      (SkillType.FISHING,    "낚시 속도",            "+3%",    0.03f, "낚시 입질 대기시간 단축"),
    FISHING_JUNK_REDUCE(SkillType.FISHING,    "쓰레기 드롭 확률",     "-2%",    0.02f, "쓰레기 아이템 낚일 확률 감소"),
    FISHING_FISH_DROP  (SkillType.FISHING,    "물고기 추가 드롭",     "+1%",    0.01f, "물고기 추가 드롭 확률 증가"),

    // === 요리 (COOKING) ===
    COOKING_BUFF_DURATION  (SkillType.COOKING, "버프 지속시간",        "+5%",   0.05f, "음식 섭취 시 버프 지속시간 증가"),
    COOKING_BUFF_AMPLIFIER (SkillType.COOKING, "버프 강도",            "+1단계", 0.5f, "음식 버프 강도 증가"),
    COOKING_SATURATION     (SkillType.COOKING, "음식 포화도",          "+3%",   0.03f, "음식 섭취 시 포만감 추가 회복"),
    COOKING_EXTRA_BUFF     (SkillType.COOKING, "추가 버프 발동 확률",  "+2%",   0.02f, "음식 섭취 시 추가 버프 발동"),

    // === 사냥 (HUNTING) ===
    HUNTING_ATTACK     (SkillType.HUNTING,    "공격력",               "+2%",    0.02f, "몬스터에게 주는 공격력 증가"),
    HUNTING_CRITICAL   (SkillType.HUNTING,    "크리티컬 확률",        "+1%",    0.01f, "크리티컬 타격 발동 확률 증가"),
    HUNTING_HEAL       (SkillType.HUNTING,    "처치 시 체력 회복",    "+0.5HP", 0.5f, "처치 시 체력 회복량 증가"),
    HUNTING_RARE_DROP  (SkillType.HUNTING,    "희귀 드롭 확률",       "+1%",    0.01f, "몬스터 처치 시 희귀 아이템 드롭 확률 증가"),

    // === 방어 (DEFENSE) ===
    DEFENSE_DAMAGE_REDUCE(SkillType.DEFENSE,  "데미지 감소",          "+1%",    0.01f, "받는 데미지 감소율 증가"),
    DEFENSE_MAX_HEALTH   (SkillType.DEFENSE,  "최대 체력",            "+1HP",   1.0f, "최대 체력 추가 증가"),
    DEFENSE_KNOCKBACK    (SkillType.DEFENSE,  "넉백 저항",            "+3%",    0.03f, "넉백 저항 추가 증가"),
    DEFENSE_INVULNERABLE (SkillType.DEFENSE,  "무적 시간",            "+5%",    0.05f, "피격 후 무적 시간 증가"),

    // === 대장장이 (SMITHING) ===
    SMITHING_DURABILITY    (SkillType.SMITHING, "도구 내구도",         "+5%",   0.05f, "금속 도구 내구도 소모 취소 확률 증가"),
    SMITHING_REPAIR_COST   (SkillType.SMITHING, "수리 비용 감소",      "+3%",   0.03f, "모루 수리 경험치 비용 추가 감소"),
    SMITHING_MATERIAL_SAVE (SkillType.SMITHING, "재료 절약 확률",      "+1%",   0.01f, "도구 제작 시 재료 절약 확률 증가"),
    SMITHING_ENCHANT_CHANCE(SkillType.SMITHING, "랜덤 인챈트 확률",    "+2%",   0.02f, "제작 시 랜덤 인챈트 부여 확률 증가"),

    // === 마법부여 (ENCHANTING) ===
    ENCHANTING_COST_REDUCE (SkillType.ENCHANTING, "인챈트 비용 감소",  "+3%",   0.03f, "인챈트 소비 레벨 환급 비율 증가"),
    ENCHANTING_LEVEL_BONUS (SkillType.ENCHANTING, "인챈트 레벨 보너스","+0.5",  0.5f, "인챈트 테이블 레벨 보너스 증가"),
    ENCHANTING_EXTRA       (SkillType.ENCHANTING, "추가 인챈트 확률",  "+1%",   0.01f, "인챈트 후 추가 인챈트 부여 확률 증가"),
    ENCHANTING_CURSE_IMMUNE(SkillType.ENCHANTING, "저주 면역 확률",    "+2%",   0.02f, "저주 인챈트 자동 제거 확률 증가");

    /** MINING_SPEED / LUMBERJACK_SPEED 전용: 이 횟수만큼 선택할 때마다 Haste amplifier +1 */
    public static final int HASTE_PICKS_PER_LEVEL = 6;

    public final SkillType skill;
    public final String displayName;
    /** 선택지 버튼에 표시되는 증가량 텍스트 */
    public final String incrementText;
    /** addStatValue 시 1회 선택당 누적되는 수치 */
    public final float increment;
    /** 선택지 버튼/툴팁에 표시되는 설명 텍스트 */
    public final String description;

    SkillPointStat(SkillType skill, String displayName, String incrementText, float increment, String description) {
        this.skill = skill;
        this.displayName = displayName;
        this.incrementText = incrementText;
        this.increment = increment;
        this.description = description;
    }

    /** NBT 저장 키: "sp_" + this.name() */
    public String getNbtKey() {
        return "sp_" + this.name();
    }

    public String getDescription() {
        return description;
    }

    /** 특정 스킬에 속하는 4개 선택지를 반환한다. */
    public static List<SkillPointStat> getOptionsForSkill(SkillType skill) {
        return Arrays.stream(values())
            .filter(s -> s.skill == skill)
            .collect(Collectors.toList());
    }
}
