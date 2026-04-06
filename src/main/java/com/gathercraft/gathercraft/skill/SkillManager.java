package com.gathercraft.gathercraft.skill;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class SkillManager {

    // XP required to go from level N to N+1: (N+1) * 100
    // Level 0->1: 100 XP, Level 49->50: 5000 XP, Level 99->100: 10000 XP
    public static long xpToNextLevel(int currentLevel) {
        if (currentLevel >= SkillData.MAX_LEVEL) return Long.MAX_VALUE;
        return (currentLevel + 1) * 100L;
    }

    public static void addXP(Player player, SkillType skill, long amount) {
        if (!(player instanceof ServerPlayer sp)) return;

        int level = SkillData.getLevel(player, skill);
        if (level >= SkillData.MAX_LEVEL) return;

        int originalLevel = level;
        long xp = SkillData.getXP(player, skill) + amount;

        while (level < SkillData.MAX_LEVEL) {
            long needed = xpToNextLevel(level);
            if (xp >= needed) {
                xp -= needed;
                level++;
                onLevelUp(player, skill, level);
            } else {
                break;
            }
        }

        SkillData.setLevel(player, skill, level);
        SkillData.setXP(player, skill, xp);

        boolean leveledUp = level > originalLevel;
        SkillHUD.trigger(sp, skill, leveledUp);
    }

    private static void onLevelUp(Player player, SkillType skill, int newLevel) {
        SkillTier tier = SkillTier.fromLevel(newLevel);
        boolean isTierUp = (newLevel % 10 == 0);

        String msg = "§a[" + skill.getKoreanName() + "] §f레벨 §e" + newLevel + "§f 달성!";
        if (isTierUp) {
            msg += " §6✦ " + tier.getDisplayName() + " 티어!";
        }
        player.sendSystemMessage(Component.literal(msg));

        if (player instanceof ServerPlayer sp && player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                isTierUp ? ParticleTypes.TOTEM_OF_UNDYING : ParticleTypes.HAPPY_VILLAGER,
                sp.getX(), sp.getY() + 1.0, sp.getZ(),
                isTierUp ? 50 : 20,
                0.5, 0.5, 0.5, 0.1
            );
        }
    }

    public static double getXPProgress(Player player, SkillType skill) {
        int level = SkillData.getLevel(player, skill);
        if (level >= SkillData.MAX_LEVEL) return 1.0;
        long xp = SkillData.getXP(player, skill);
        long needed = xpToNextLevel(level);
        return (double) xp / needed;
    }
}
