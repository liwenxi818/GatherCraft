package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 대장장이 스킬 핸들러
 * - 철/금/다이아 도구 제작 및 수리 시 XP 적립
 * - 레벨별 보너스: 내구도(PlayerTickHandler), 모루 비용 감소, 재료 절약, 랜덤 인챈트, 각성 인챈트
 */
public class SmithingHandler {

    private static final Random RANDOM = new Random();

    // 제작대에서 도구/방어구 제작 시
    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack result = event.getCrafting();
        if (!isMetalTool(result)) return;

        SkillManager.addXP(player, SkillType.SMITHING, 15);

        int level = SkillData.getLevel(player, SkillType.SMITHING);

        // 40레벨: 제작 시 재료 절약 (결과물 추가 지급으로 구현)
        if (level >= 40 && RANDOM.nextDouble() < materialSaveChance(level)) {
            ItemStack bonus = result.copy();
            bonus.setCount(1);
            player.addItem(bonus);
        }

        // 50레벨: 제작 시 랜덤 인챈트 1개 자동 부여
        if (level >= 50 && result.isEnchantable()) {
            applyRandomEnchant(result, level);
        }
    }

    // 모루 수리 완료 이벤트 (결과물 취득 시)
    @SubscribeEvent
    public void onAnvilRepair(AnvilRepairEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack result = event.getOutput();
        if (!isMetalTool(result)) return;

        SkillManager.addXP(player, SkillType.SMITHING, 10);

        // 70레벨: 모루 내구도 감소 확률 50% 감소
        int level = SkillData.getLevel(player, SkillType.SMITHING);
        float costReduction = anvilCostReduction(level);
        if (costReduction > 0) {
            event.setBreakChance(event.getBreakChance() * (1 - costReduction));
        }
    }

    private void applyRandomEnchant(ItemStack stack, int level) {
        int enchantLevel = level >= 80 ? 30 : level >= 60 ? 20 : 10;
        List<EnchantmentInstance> enchants = EnchantmentHelper.getAvailableEnchantmentResults(
            enchantLevel, stack, false
        );
        if (enchants.isEmpty()) return;

        EnchantmentInstance chosen = enchants.get(RANDOM.nextInt(enchants.size()));
        Map<Enchantment, Integer> current = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        current.put(chosen.enchantment, chosen.level);
        EnchantmentHelper.setEnchantments(current, stack);
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
