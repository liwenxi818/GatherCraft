package com.gathercraft.gathercraft.skill;

import com.gathercraft.gathercraft.achievement.AchievementManager;
import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.SkillPointOfferPacket;
import com.gathercraft.gathercraft.network.packet.SkillPointStatSyncPacket;
import com.gathercraft.gathercraft.network.packet.SkillXpUpdatePacket;
import com.gathercraft.gathercraft.particle.ParticleUtil;
import com.gathercraft.gathercraft.skill.handler.PlayerTickHandler;
import com.gathercraft.gathercraft.title.TitleManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SkillManager {

    public static long xpToNextLevel(int currentLevel) {
        if (currentLevel >= SkillData.MAX_LEVEL) return Long.MAX_VALUE;
        if (currentLevel < 20)  return (currentLevel + 1) * 8L;
        if (currentLevel < 60)  return (currentLevel + 1) * 20L;
        return (currentLevel + 1) * 50L;
    }

    public static void addXP(Player player, SkillType skill, long amount) {
        if (!(player instanceof ServerPlayer sp)) return;

        int level = SkillData.getLevel(player, skill);
        if (level >= SkillData.MAX_LEVEL) return;

        long finalAmount = (long) (amount * TitleManager.getXPMultiplier(player));

        int originalLevel = level;
        long xp = SkillData.getXP(player, skill) + finalAmount;

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

        SkillData.updateSkill(player, skill, level, xp);

        boolean leveledUp = level > originalLevel;
        boolean tierUp = leveledUp && (level % 10 == 0);

        // S2C 패킷: 클라이언트 HUD 오버레이 갱신
        float progress = (float) getXPProgress(player, skill);
        PacketHandler.sendToPlayer(sp, new SkillXpUpdatePacket(skill, level, progress, leveledUp, tierUp));
    }

    /**
     * 레벨업 시 스킬 포인트 선택 offer를 대기열에 추가한다.
     * pending이 0이면 즉시 패킷을 전송하고, 이미 대기 중이면 큐잉만 한다.
     * 선택 완료 후 SkillPointChoicePacket 핸들러에서 다음 offer를 연속 전송한다.
     */
    private static void triggerSkillPointOffer(ServerPlayer sp, SkillType skill) {
        int pending = SkillData.getPendingCount(sp, skill);
        SkillData.setPendingCount(sp, skill, pending + 1);
        if (pending == 0) {
            sendSkillPointOffer(sp, skill);
        }
    }

    /**
     * 해당 스킬의 4가지 선택지 중 3개를 무작위로 골라 클라이언트에 전송한다.
     * PlayerTickHandler(로그인 이벤트)에서도 호출한다.
     */
    public static void sendSkillPointOffer(ServerPlayer sp, SkillType skill) {
        List<SkillPointStat> options = new ArrayList<>(SkillPointStat.getOptionsForSkill(skill));
        // 4개 중 3개 무작위 선택 (마지막 하나 제거)
        options.remove(ThreadLocalRandom.current().nextInt(options.size()));

        int level = SkillData.getLevel(sp, skill);
        int[]   ords = new int[3];
        float[] vals = new float[3];
        for (int i = 0; i < 3; i++) {
            SkillPointStat stat = options.get(i);
            ords[i] = stat.ordinal();
            vals[i] = SkillData.getStatValue(sp, stat);
        }
        PacketHandler.sendToPlayer(sp, new SkillPointOfferPacket(skill, level, ords, vals));
    }

    /**
     * MINING_SPEED/LUMBERJACK_SPEED 누적값을 클라이언트 로컬 플레이어 NBT에 동기화한다.
     * PlayerEvent.BreakSpeed의 클라이언트 예측이 서버 파괴 속도와 일치하도록 로그인 시 +
     * 두 스탯 중 하나를 선택할 때마다 호출한다.
     */
    public static void sendSpeedStatSync(ServerPlayer sp) {
        float mining = SkillData.getStatValue(sp, SkillPointStat.MINING_SPEED);
        float lumberjack = SkillData.getStatValue(sp, SkillPointStat.LUMBERJACK_SPEED);
        PacketHandler.sendToPlayer(sp, new SkillPointStatSyncPacket(mining, lumberjack));
    }

    private static void onLevelUp(Player player, SkillType skill, int newLevel) {
        SkillTier tier = SkillTier.fromLevel(newLevel);
        boolean isTierUp = (newLevel % 10 == 0);
        boolean isAwakening = (newLevel == SkillData.MAX_LEVEL);

        // 채팅 메시지
        String msg = "§a[" + skill.getKoreanName() + "] §f레벨 §e" + newLevel + "§f 달성!";
        if (isTierUp) {
            msg += " §6✦ " + tier.getDisplayName() + " 티어!";
        }
        player.sendSystemMessage(Component.literal(msg));

        if (!(player instanceof ServerPlayer sp)) return;

        // 스킬 포인트 offer 예약
        triggerSkillPointOffer(sp, skill);

        // 칭호 자동 해금 체크
        TitleManager.checkAndUnlock(sp);

        // 업적: 전 스킬 레벨 기반 종합 업적 체크 + 채광/사냥 100레벨 각성 업적
        AchievementManager.checkAllSkillLevel(sp);
        if (isAwakening) {
            if (skill == SkillType.MINING) AchievementManager.unlock(sp, "mining_max");
            if (skill == SkillType.HUNTING) AchievementManager.unlock(sp, "hunting_max");
        }

        // 방어 스킬: 체력/넉백 속성을 틱 주기 대신 즉시 갱신
        if (skill == SkillType.DEFENSE) {
            PlayerTickHandler.applyDefenseAttributesNow(sp);
        }
        ServerLevel serverLevel = (ServerLevel) player.level();

        double px = sp.getX();
        double py = sp.getY();
        double pz = sp.getZ();

        if (isAwakening) {
            // 100레벨 각성: 전 서버 브로드캐스트
            String broadcast = "§6§l[각성] §e" + sp.getName().getString()
                + " §f님이 §b" + skill.getKoreanName() + " §f100레벨 각성을 달성했습니다!";
            MinecraftServer server = sp.getServer();
            if (server != null) {
                server.getPlayerList().broadcastSystemMessage(Component.literal(broadcast), false);
            }

            // 초대형 파티클 연출
            ParticleUtil.spawnBurst(serverLevel, px, py, pz, ParticleTypes.EXPLOSION_EMITTER, 3, 1.0);
            ParticleUtil.spawnCircle(serverLevel, px, py, pz, ParticleTypes.TOTEM_OF_UNDYING, 3.0, 32, 0);
            ParticleUtil.spawnCircle(serverLevel, px, py, pz, ParticleTypes.FIREWORK, 5.0, 40, 1.0);
            ParticleUtil.spawnCircle(serverLevel, px, py, pz, ParticleUtil.getSkillColor(skill), 2.0, 24, 0.5);

        } else if (isTierUp) {
            // 티어업: 화려한 연출
            ParticleUtil.spawnBurst(serverLevel, px, py + 1, pz, ParticleTypes.TOTEM_OF_UNDYING, 50, 0.5);
            ParticleUtil.spawnCircle(serverLevel, px, py, pz, ParticleTypes.FIREWORK, 2.5, 24, 0.5);
            ParticleUtil.spawnCircle(serverLevel, px, py, pz, ParticleUtil.getSkillColor(skill), 1.5, 16, 0);

        } else {
            // 일반 레벨업
            ParticleUtil.spawnBurst(serverLevel, px, py + 1, pz, ParticleTypes.TOTEM_OF_UNDYING, 20, 0.3);
            ParticleUtil.spawnCircle(serverLevel, px, py, pz, ParticleUtil.getSkillColor(skill), 1.5, 16, 0);
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
