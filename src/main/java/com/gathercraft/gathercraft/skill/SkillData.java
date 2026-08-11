package com.gathercraft.gathercraft.skill;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class SkillData {

    public static final String ROOT_KEY = "GatherCraft";
    public static final int MAX_LEVEL = 100;

    public static int getLevel(Player player, SkillType skill) {
        return getRoot(player).getInt(skill.name() + "_level");
    }

    public static long getXP(Player player, SkillType skill) {
        return getRoot(player).getLong(skill.name() + "_xp");
    }

    public static void setLevel(Player player, SkillType skill, int level) {
        CompoundTag tag = getRoot(player);
        tag.putInt(skill.name() + "_level", Math.min(level, MAX_LEVEL));
        saveRoot(player, tag);
    }

    public static void setXP(Player player, SkillType skill, long xp) {
        CompoundTag tag = getRoot(player);
        tag.putLong(skill.name() + "_xp", xp);
        saveRoot(player, tag);
    }

    /** 레벨과 XP를 한 번의 NBT 조작으로 원자적으로 갱신한다. */
    public static void updateSkill(Player player, SkillType skill, int level, long xp) {
        CompoundTag tag = getRoot(player);
        tag.putInt(skill.name() + "_level", Math.min(level, MAX_LEVEL));
        tag.putLong(skill.name() + "_xp", xp);
        saveRoot(player, tag);
    }

    /** GatherCraft NBT 루트 CompoundTag를 반환한다. 없으면 새로 생성. */
    public static CompoundTag getRoot(Player player) {
        CompoundTag pd = player.getPersistentData();
        if (!pd.contains(ROOT_KEY)) {
            pd.put(ROOT_KEY, new CompoundTag());
        }
        return pd.getCompound(ROOT_KEY);
    }

    /** GatherCraft NBT 루트 CompoundTag를 저장한다. */
    public static void saveRoot(Player player, CompoundTag tag) {
        player.getPersistentData().put(ROOT_KEY, tag);
    }

    /**
     * NBT에서 스킬 데이터 로드 (재접속·리스폰 시 호출).
     * 모든 읽기/쓰기가 getPersistentData()에 직접 반영되므로
     * 루트 CompoundTag 존재 여부만 확인한다.
     */
    public static void loadFromNBT(Player player) {
        getRoot(player);
    }

    /**
     * 스킬 데이터를 NBT에 명시 저장 (로그아웃 시 호출).
     * updateSkill/setLevel/setXP 모두 saveRoot()를 통해 즉시 반영되므로
     * 실질적으로 no-op이지만, 안전 보장을 위해 루트 태그를 재기록한다.
     */
    public static void saveToNBT(Player player) {
        CompoundTag tag = getRoot(player);
        player.getPersistentData().put(ROOT_KEY, tag);
    }

    // ---- 스킬 포인트 스탯 ----

    /** 누적 스탯 값을 반환한다. 기본값 0.0f. */
    public static float getStatValue(Player player, SkillPointStat stat) {
        return getRoot(player).getFloat(stat.getNbtKey());
    }

    /** 누적 스탯 값에 amount를 더해 저장한다. */
    public static void addStatValue(Player player, SkillPointStat stat, float amount) {
        CompoundTag tag = getRoot(player);
        tag.putFloat(stat.getNbtKey(), tag.getFloat(stat.getNbtKey()) + amount);
        saveRoot(player, tag);
    }

    /**
     * 선택 횟수(정수)를 반환한다. 누적 float 값은 반복 덧셈으로 인한 오차(예: 6번 선택 후
     * 0.18f가 아닌 0.17999999f)가 생길 수 있으므로, 반올림으로 실제 선택 횟수를 복원한다.
     * 이후 임계값 나눗셈(예: 6포인트마다 +1)은 이 정수값으로 해야 (int) 버림에 의한
     * 오프바이원 오류가 생기지 않는다.
     */
    public static int getStatPickCount(Player player, SkillPointStat stat) {
        return Math.round(getStatValue(player, stat) / stat.increment);
    }

    /** 레벨업 후 아직 선택하지 않은 스킬 포인트 수를 반환한다. */
    public static int getPendingCount(Player player, SkillType skill) {
        return getRoot(player).getInt("sp_pend_" + skill.name());
    }

    /** 대기 중인 스킬 포인트 수를 설정한다. */
    public static void setPendingCount(Player player, SkillType skill, int n) {
        CompoundTag tag = getRoot(player);
        tag.putInt("sp_pend_" + skill.name(), Math.max(0, n));
        saveRoot(player, tag);
    }
}
