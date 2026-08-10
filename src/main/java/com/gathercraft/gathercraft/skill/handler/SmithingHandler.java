package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 대장장이 스킬 핸들러
 * - 철/금/다이아 도구 제작 및 수리 시 XP 적립
 * - 레벨별 보너스: 내구도(PlayerTickHandler), 모루 비용 감소, 재료 절약, 랜덤 인챈트, 각성 인챈트
 */
public class SmithingHandler {

    // 모루 열었을 때 플레이어 XP 레벨 추적 (환급 계산용)
    private final Map<UUID, Integer> xpLevelOnAnvilOpen = new HashMap<>();

    // [6] 금속 도구 내구도 추적 (TickEvent 기반)
    private final Map<UUID, Integer> prevToolDamage = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTickSmithing(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        ItemStack stack = player.getMainHandItem();
        UUID uuid = player.getUUID();

        if (!stack.isEmpty() && stack.isDamageableItem() && isMetalTool(stack)) {
            int currentDamage = stack.getDamageValue();
            Integer prev = prevToolDamage.get(uuid);
            if (prev != null && currentDamage > prev) {
                int level = SkillData.getLevel(player, SkillType.SMITHING);
                double chance;
                if (level >= 80) chance = 0.75;
                else if (level >= 60) chance = 0.50;
                else if (level >= 30) chance = 0.25;
                else if (level >= 10) chance = 0.10;
                else chance = 0.0;
                float durabilityStat = SkillData.getStatValue(player, SkillPointStat.SMITHING_DURABILITY);
                chance = Math.min(chance + durabilityStat, 0.95);
                if (chance > 0 && ThreadLocalRandom.current().nextDouble() < chance) {
                    stack.setDamageValue(prev);
                }
            }
            prevToolDamage.put(uuid, stack.getDamageValue());
        } else {
            prevToolDamage.remove(uuid);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        xpLevelOnAnvilOpen.remove(event.getEntity().getUUID());
    }

    // 제작대에서 도구/방어구 제작 시
    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack result = event.getCrafting();
        if (!isMetalTool(result)) return;

        SkillManager.addXP(player, SkillType.SMITHING, 15);

        int level = SkillData.getLevel(player, SkillType.SMITHING);

        // 재료 절약 (40레벨 기본 + 스탯 포인트)
        float matSaveStat = SkillData.getStatValue(player, SkillPointStat.SMITHING_MATERIAL_SAVE);
        double saveChance = (level >= 40 ? materialSaveChance(level) : 0) + matSaveStat;
        if (saveChance > 0 && ThreadLocalRandom.current().nextDouble() < saveChance) {
            ItemStack bonus = result.copy();
            bonus.setCount(1);
            player.addItem(bonus);
        }

        // 랜덤 인챈트 (50레벨 기본 + 스탯 포인트)
        float enchantStat = SkillData.getStatValue(player, SkillPointStat.SMITHING_ENCHANT_CHANCE);
        boolean doEnchant = (level >= 50) || (enchantStat > 0 && ThreadLocalRandom.current().nextDouble() < enchantStat);
        if (doEnchant && result.isEnchantable()) {
            applyRandomEnchant(result, level);
        }

        // [7a] 100레벨 각성: 제작 도구에 각성 태그 + 이름 접두사
        if (level >= 100) {
            result.getOrCreateTag().putBoolean("gathercraft_awakened", true);
            result.setHoverName(Component.literal("§6§l[각성] ").append(result.getHoverName()));
        }
    }

    // 모루 열릴 때 XP 레벨 기록
    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getContainer() instanceof AnvilMenu) {
            xpLevelOnAnvilOpen.put(player.getUUID(), player.experienceLevel);
        }
    }

    // 모루 수리 완료 이벤트 (결과물 취득 시)
    @SubscribeEvent
    public void onAnvilRepair(AnvilRepairEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack result = event.getOutput();
        if (!isMetalTool(result)) return;

        SkillManager.addXP(player, SkillType.SMITHING, 10);

        int level = SkillData.getLevel(player, SkillType.SMITHING);

        // 20레벨/70레벨: 소모 XP 환급으로 경험치 비용 감소 구현 (스탯 포인트 포함)
        float repairStat = SkillData.getStatValue(player, SkillPointStat.SMITHING_REPAIR_COST);
        float refundRate = Math.min(xpRefundRate(level) + repairStat, 0.80f);
        if (refundRate > 0) {
            UUID uuid = player.getUUID();
            Integer xpBefore = xpLevelOnAnvilOpen.remove(uuid);
            if (xpBefore != null) {
                int xpConsumed = xpBefore - player.experienceLevel;
                if (xpConsumed > 0) {
                    int refundLevels = Math.round(xpConsumed * refundRate);
                    if (refundLevels > 0) {
                        player.giveExperienceLevels(refundLevels);
                    }
                }
            }
        }

        // 모루 내구도 감소 확률 추가 감소
        float breakReduction = anvilCostReduction(level);
        if (breakReduction > 0) {
            event.setBreakChance(event.getBreakChance() * (1 - breakReduction));
        }
    }

    private void applyRandomEnchant(ItemStack stack, int level) {
        int enchantLevel = level >= 80 ? 30 : level >= 60 ? 20 : 10;
        List<EnchantmentInstance> enchants = EnchantmentHelper.getAvailableEnchantmentResults(
            enchantLevel, stack, false
        );
        if (enchants.isEmpty()) return;

        EnchantmentInstance chosen = enchants.get(ThreadLocalRandom.current().nextInt(enchants.size()));
        Map<Enchantment, Integer> current = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        current.put(chosen.enchantment, chosen.level);
        EnchantmentHelper.setEnchantments(current, stack);
    }

    private float xpRefundRate(int level) {
        if (level >= 70) return 0.50f;
        if (level >= 20) return 0.20f;
        return 0;
    }

    private float anvilCostReduction(int level) {
        if (level >= 70) return 0.50f;
        if (level >= 20) return 0.20f;
        return 0;
    }

    private double materialSaveChance(int level) {
        if (level >= 90) return 0.40;
        if (level >= 40) return 0.15;
        return 0;
    }

    private boolean isMetalTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof TieredItem tiered && (
            tiered.getTier() == Tiers.IRON
            || tiered.getTier() == Tiers.GOLD
            || tiered.getTier() == Tiers.DIAMOND
            || tiered.getTier() == Tiers.NETHERITE
        );
    }
}
