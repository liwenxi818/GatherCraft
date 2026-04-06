package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 요리 스킬 핸들러
 * - 화로/훈연기/용광로에서 음식 제련 완료 시 XP 적립 (ItemSmeltedEvent)
 * - 레벨별 보너스: 음식 버프 지속시간, 포화도 증가, 고유 버프, 체력 즉시 회복
 */
public class CookingHandler {

    @SubscribeEvent
    public void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack result = event.getSmelting();
        if (!isCookedFood(result)) return;

        SkillManager.addXP(player, SkillType.COOKING, 5);
    }

    // 음식 섭취 시 요리 레벨 보너스 적용
    @SubscribeEvent
    public void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItem();

        Item item = stack.getItem();
        if (!item.isEdible()) return;

        int level = SkillData.getLevel(player, SkillType.COOKING);
        if (level <= 0) return;

        // 50레벨: 음식 고유 버프 적용
        if (level >= 20) {
            applyFoodBuff(player, item, level);
        }

        // 100레벨 각성: 즉시 체력 회복 + 모든 디버프 제거
        if (level >= 100) {
            player.heal(4.0f);
            player.removeAllEffects();
            // 유익한 효과는 재적용 (제거 후 다시 적용)
        }
    }

    private void applyFoodBuff(ServerPlayer player, Item item, int level) {
        int duration = buffDuration(level);
        int amplifier = level >= 60 ? 1 : 0;

        // 음식 종류별 버프
        if (item == Items.COOKED_BEEF || item == Items.COOKED_PORKCHOP || item == Items.COOKED_MUTTON) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, amplifier, false, true));
        } else if (item == Items.COOKED_CHICKEN || item == Items.COOKED_RABBIT) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier, false, true));
        } else if (item == Items.COOKED_COD || item == Items.COOKED_SALMON) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, duration, 0, false, true));
        } else if (item == Items.BAKED_POTATO || item == Items.BREAD) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, amplifier, false, true));
        } else if (item == Items.PUMPKIN_PIE || item == Items.CAKE) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration / 4, 0, false, true));
        }
    }

    private int buffDuration(int level) {
        // 기본 60초, 레벨에 따라 증가
        if (level >= 60) return (int)(1200 * 1.5); // 30초 * 1.5
        if (level >= 30) return 1200;               // 60초
        if (level >= 10) return (int)(1200 * 1.1); // 60초 * 1.1
        return 1200;
    }

    private boolean isCookedFood(ItemStack stack) {
        return stack.is(Items.COOKED_BEEF)
            || stack.is(Items.COOKED_CHICKEN)
            || stack.is(Items.COOKED_COD)
            || stack.is(Items.COOKED_MUTTON)
            || stack.is(Items.COOKED_PORKCHOP)
            || stack.is(Items.COOKED_RABBIT)
            || stack.is(Items.COOKED_SALMON)
            || stack.is(Items.BAKED_POTATO)
            || stack.is(Items.DRIED_KELP)
            || stack.is(Items.BREAD);
    }
}
