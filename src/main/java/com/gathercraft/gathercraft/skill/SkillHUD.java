package com.gathercraft.gathercraft.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XP 획득 시 ActionBar에 스킬 경험치 바를 잠시 표시하는 HUD 관리자.
 * - 일반 XP 획득: 20틱(1초) 표시
 * - 레벨업: 30틱(1.5초) 표시, 레벨업 연출 포함
 */
public class SkillHUD {

    private static final Map<UUID, HUDEntry> ACTIVE = new ConcurrentHashMap<>();

    private static final int SHOW_TICKS = 20;
    private static final int LEVELUP_TICKS = 30;

    /**
     * XP 획득 또는 레벨업 시 호출. SkillManager.addXP에서 호출됨.
     */
    public static void trigger(ServerPlayer player, SkillType skill, boolean leveledUp) {
        HUDEntry entry = ACTIVE.computeIfAbsent(player.getUUID(), id -> new HUDEntry());
        entry.skill = skill;
        if (leveledUp) {
            entry.ticksLeft = LEVELUP_TICKS;
            entry.levelUpFlash = true;
            entry.newLevel = SkillData.getLevel(player, skill);
        } else {
            // 이미 레벨업 연출 중이면 덮어쓰지 않음
            if (!entry.levelUpFlash) {
                entry.ticksLeft = SHOW_TICKS;
                entry.levelUpFlash = false;
            }
        }
    }

    /**
     * 플레이어가 퇴장할 때 메모리 정리.
     */
    public static void remove(UUID uuid) {
        ACTIVE.remove(uuid);
    }

    /**
     * PlayerTickHandler에서 매 틱 호출.
     * ActionBar 메시지를 전송하고 타이머를 감소시킴.
     */
    public static void tick(ServerPlayer player) {
        HUDEntry entry = ACTIVE.get(player.getUUID());
        if (entry == null) return;

        if (entry.ticksLeft <= 0) {
            ACTIVE.remove(player.getUUID());
            return;
        }

        player.displayClientMessage(buildMessage(player, entry), true);
        entry.ticksLeft--;

        // 레벨업 연출이 끝나면 플래그 초기화
        if (entry.levelUpFlash && entry.ticksLeft == 0) {
            entry.levelUpFlash = false;
        }
    }

    private static Component buildMessage(ServerPlayer player, HUDEntry entry) {
        SkillType skill = entry.skill;

        if (entry.levelUpFlash) {
            // 레벨업 연출: 꽉 찬 바 + 레벨업 메시지
            String msg = "§e[" + skill.getKoreanName() + "] §6✦ LEVEL UP! Lv." + entry.newLevel
                    + " §7(" + SkillTier.fromLevel(entry.newLevel).getDisplayName() + ")  "
                    + buildBar(1.0);
            return Component.literal(msg);
        }

        int level = SkillData.getLevel(player, skill);
        double progress = SkillManager.getXPProgress(player, skill);
        long xp = SkillData.getXP(player, skill);
        long needed = SkillManager.xpToNextLevel(level);

        String xpStr = level >= SkillData.MAX_LEVEL ? "MAX" : (xp + "/" + needed);
        String msg = "§e[" + skill.getKoreanName() + "] §aLv." + level + "  "
                + buildBar(progress) + "  §f" + xpStr;
        return Component.literal(msg);
    }

    private static String buildBar(double progress) {
        int filled = (int) Math.min(10, Math.round(progress * 10));
        StringBuilder sb = new StringBuilder("§a");
        for (int i = 0; i < 10; i++) {
            if (i == filled) sb.append("§7");
            sb.append("█");
        }
        return sb.toString();
    }

    private static class HUDEntry {
        SkillType skill;
        int ticksLeft;
        boolean levelUpFlash;
        int newLevel;
    }
}
