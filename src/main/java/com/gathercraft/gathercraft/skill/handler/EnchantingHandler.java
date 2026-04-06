package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.server.level.ServerPlayer;
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

    private static final long COOLDOWN_MS = 5 * 60 * 1000L;
    private final Map<UUID, Long> lastXpGrantTime = new HashMap<>();
    // 인챈트 테이블 열었을 때 XP 레벨 기록 (닫을 때 비교용)
    private final Map<UUID, Integer> xpLevelOnOpen = new HashMap<>();

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
            long now = System.currentTimeMillis();
            if (now - lastXpGrantTime.getOrDefault(uuid, 0L) < COOLDOWN_MS) return;
            lastXpGrantTime.put(uuid, now);

            int xpConsumed = xpOnOpen - player.experienceLevel;
            SkillManager.addXP(player, SkillType.ENCHANTING, xpConsumed * 5L);
        }
    }

    // 인챈트 레벨 보너스 적용
    @SubscribeEvent
    public void onEnchantmentLevelSet(EnchantmentLevelSetEvent event) {
        // EnchantmentLevelSetEvent에는 플레이어 참조가 없어 글로벌 레벨 적용 불가
        // TODO: 플레이어별 레벨 보너스 적용 방법 조사 (CapabilityPlayer 등 활용 고려)
        // event.setLevel(event.getLevel() + bonus);
    }
}
