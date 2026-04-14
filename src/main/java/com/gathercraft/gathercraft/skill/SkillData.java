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
