package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 마법부여 스킬 핸들러
 * - 인챈트 테이블 열기 감지로 XP 적립 (5분 쿨다운)
 * - 레벨별 보너스: 인챈트 비용 감소(TODO), 레벨 보너스(EnchantmentLevelSetEvent), 저주 면역(TODO)
 *
 * 참고: Forge 1.20.1에는 "플레이어가 인챈트 버튼을 클릭" 이벤트가 없음.
 *       PlayerContainerEvent.Open + 쿨다운으로 XP를 근사치 지급.
 */
public class EnchantingHandler {

    private static final long COOLDOWN_TICKS = 5 * 60 * 20L; // 5분 = 6000틱
    private final Map<UUID, Long>    lastXpGrantTime = new HashMap<>();
    private final Map<UUID, Integer> xpLevelOnOpen   = new HashMap<>();

    @SubscribeEvent
    public void onPlayerLoggedOut(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        lastXpGrantTime.remove(uuid);
        xpLevelOnOpen.remove(uuid);
    }

    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getContainer() instanceof EnchantmentMenu)) return;

        xpLevelOnOpen.put(player.getUUID(), player.experienceLevel);
    }

    @SubscribeEvent
    public void onContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getContainer() instanceof EnchantmentMenu)) return;

        UUID uuid = player.getUUID();
        Integer xpOnOpen = xpLevelOnOpen.remove(uuid);
        if (xpOnOpen == null) return;

        // XP 레벨이 줄었다면 인챈트를 수행한 것
        if (player.experienceLevel < xpOnOpen) {
            long now = player.level().getGameTime();
            if (now - lastXpGrantTime.getOrDefault(uuid, 0L) < COOLDOWN_TICKS) return;
            lastXpGrantTime.put(uuid, now);

            int xpConsumed = xpOnOpen - player.experienceLevel;
            SkillManager.addXP(player, SkillType.ENCHANTING, xpConsumed * 5L);
        }
    }

    /**
     * 인챈트 레벨 보너스 적용.
     * 테이블 위치에서 5블록 내 가장 가까운 플레이어의 스킬 레벨을 참조.
     * 20레벨 +1, 50레벨 +3, 80레벨 +5 슬롯 레벨 보너스.
     */
    @SubscribeEvent
    public void onEnchantmentLevelSet(EnchantmentLevelSetEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        BlockPos pos = event.getPos();
        Player nearestPlayer = serverLevel.getNearestPlayer(
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5.0, false);
        if (!(nearestPlayer instanceof ServerPlayer player)) return;

        int enchantSkillLevel = SkillData.getLevel(player, SkillType.ENCHANTING);
        // 레벨 기반 보너스 + 스탯 포인트 보너스 (0.5 단위로 누적, 정수로 반올림)
        float statBonus = SkillData.getStatValue(player, SkillPointStat.ENCHANTING_LEVEL_BONUS);
        int bonus = getEnchantBonus(enchantSkillLevel) + Math.round(statBonus);
        if (bonus > 0) {
            // 최대 30 + bonus 까지 허용 (풀 서가 기준 초과 가능)
            event.setEnchantLevel(Math.min(event.getOriginalLevel() + bonus, 30 + bonus));
        }
    }

    private int getEnchantBonus(int skillLevel) {
        if (skillLevel >= 80) return 5;
        if (skillLevel >= 50) return 3;
        if (skillLevel >= 20) return 1;
        return 0;
    }
}
