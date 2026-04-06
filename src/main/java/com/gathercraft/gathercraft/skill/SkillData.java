package com.gathercraft.gathercraft.skill;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class SkillData {

    private static final String ROOT_KEY = "GatherCraft";
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

    private static CompoundTag getRoot(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(ROOT_KEY)) {
            persistentData.put(ROOT_KEY, new CompoundTag());
        }
        return persistentData.getCompound(ROOT_KEY);
    }

    private static void saveRoot(Player player, CompoundTag tag) {
        player.getPersistentData().put(ROOT_KEY, tag);
    }
}
