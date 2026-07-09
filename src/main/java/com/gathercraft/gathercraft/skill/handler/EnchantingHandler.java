package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 마법부여 스킬 핸들러
 * - 인챈트 테이블 열기 감지로 XP 적립 (5분 쿨다운)
 * - 레벨별 보너스: 인챈트 레벨 보너스, XP 비용 환급, 저주 면역, 추가 인챈트
 */
public class EnchantingHandler {

    private static final long COOLDOWN_TICKS = 5 * 60 * 20L;
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

        if (player.experienceLevel >= xpOnOpen) return;

        long now = player.level().getGameTime();
        if (now - lastXpGrantTime.getOrDefault(uuid, 0L) < COOLDOWN_TICKS) return;
        lastXpGrantTime.put(uuid, now);

        int xpConsumed = xpOnOpen - player.experienceLevel;
        SkillManager.addXP(player, SkillType.ENCHANTING, xpConsumed * 5L);

        int enchLevel = SkillData.getLevel(player, SkillType.ENCHANTING);

        // ENCHANTING_COST_REDUCE: 10/30/40/70/90레벨 XP 비용 일부 환급
        float costReduceRate;
        if (enchLevel >= 90) costReduceRate = 0.45f;
        else if (enchLevel >= 70) costReduceRate = 0.30f;
        else if (enchLevel >= 40) costReduceRate = 0.20f;
        else if (enchLevel >= 30) costReduceRate = 0.15f;
        else if (enchLevel >= 10) costReduceRate = 0.08f;
        else costReduceRate = 0f;

        if (costReduceRate > 0) {
            int refund = (int)(xpConsumed * costReduceRate);
            if (refund > 0) player.giveExperienceLevels(refund);
        }

        // ENCHANTING_CURSE_IMMUNE: 50/80/100레벨 저주 인챈트 제거
        double curseImmuneChance;
        if (enchLevel >= 100) curseImmuneChance = 1.0;
        else if (enchLevel >= 80) curseImmuneChance = 0.60;
        else if (enchLevel >= 50) curseImmuneChance = 0.30;
        else curseImmuneChance = 0;

        if (curseImmuneChance > 0 && ThreadLocalRandom.current().nextDouble() < curseImmuneChance) {
            removeCurseEnchants(player);
        }

        // ENCHANTING_EXTRA: 60/80/100레벨 추가 인챈트
        double extraChance;
        if (enchLevel >= 100) extraChance = 0.35;
        else if (enchLevel >= 80) extraChance = 0.20;
        else if (enchLevel >= 60) extraChance = 0.10;
        else extraChance = 0;

        if (extraChance > 0 && ThreadLocalRandom.current().nextDouble() < extraChance) {
            tryAddExtraEnchant(player, xpOnOpen);
        }
    }

    private void removeCurseEnchants(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            removeCursesFromStack(stack);
        }
        for (ItemStack stack : player.getInventory().armor) {
            removeCursesFromStack(stack);
        }
    }

    private void removeCursesFromStack(ItemStack stack) {
        if (stack.isEmpty()) return;
        Map<Enchantment, Integer> enchants = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        boolean changed = enchants.remove(Enchantments.BINDING_CURSE) != null;
        changed |= enchants.remove(Enchantments.VANISHING_CURSE) != null;
        if (changed) EnchantmentHelper.setEnchantments(enchants, stack);
    }

    private void tryAddExtraEnchant(ServerPlayer player, int targetLevel) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            Map<Enchantment, Integer> enchants = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
            if (enchants.size() != 1) continue;

            List<EnchantmentInstance> candidates = EnchantmentHelper.selectEnchantment(
                RandomSource.create(), stack, targetLevel, false);
            for (EnchantmentInstance inst : candidates) {
                if (!enchants.containsKey(inst.enchantment)) {
                    enchants.put(inst.enchantment, inst.level);
                    EnchantmentHelper.setEnchantments(enchants, stack);
                    break;
                }
            }
            break;
        }
    }

    /**
     * 인챈트 레벨 보너스: 20레벨 +1, 50레벨 +3, 80레벨 +5
     */
    @SubscribeEvent
    public void onEnchantmentLevelSet(EnchantmentLevelSetEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        BlockPos pos = event.getPos();
        Player nearestPlayer = serverLevel.getNearestPlayer(
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5.0, false);
        if (!(nearestPlayer instanceof ServerPlayer player)) return;

        int enchantSkillLevel = SkillData.getLevel(player, SkillType.ENCHANTING);
        int bonus = getEnchantBonus(enchantSkillLevel);
        if (bonus > 0) {
            event.setEnchantLevel(Math.min(event.getOriginalLevel() + bonus, 30 + bonus));
        }

        // [9] 100레벨 각성: 최고 등급 인챈트 보장
        if (enchantSkillLevel >= 100) {
            event.setEnchantLevel(30);
        }
    }

    private int getEnchantBonus(int skillLevel) {
        if (skillLevel >= 80) return 5;
        if (skillLevel >= 50) return 3;
        if (skillLevel >= 20) return 1;
        return 0;
    }
}
