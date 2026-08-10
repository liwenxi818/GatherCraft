package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.achievement.AchievementManager;
import com.gathercraft.gathercraft.particle.ParticleUtil;
import com.gathercraft.gathercraft.quest.QuestManager;
import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 요리 스킬 핸들러
 * - 화로/훈연기/용광로에서 음식 제련 완료 시 XP 적립
 * - 레벨별 보너스: 음식 버프 지속시간/강도(90레벨 최대), 포화도 증가(40/70레벨), 고유 버프 개수(20/50/80레벨 1→2→3개), 체력 즉시 회복
 * - 모션: ENTITY_EFFECT 원형 (버프 음식 섭취 시)
 */
public class CookingHandler {

    // 음식 카테고리별 버프 후보 (우선순위 순서, buffCount()만큼 앞에서부터 동시 적용)
    private static final MobEffect[] MEAT_BUFFS    = {MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED, MobEffects.HEALTH_BOOST};
    private static final MobEffect[] POULTRY_BUFFS = {MobEffects.MOVEMENT_SPEED, MobEffects.JUMP, MobEffects.REGENERATION};
    private static final MobEffect[] FISH_BUFFS    = {MobEffects.WATER_BREATHING, MobEffects.DOLPHINS_GRACE, MobEffects.CONDUIT_POWER};
    private static final MobEffect[] STAPLE_BUFFS  = {MobEffects.ABSORPTION, MobEffects.HEALTH_BOOST, MobEffects.DAMAGE_RESISTANCE};
    private static final MobEffect[] DESSERT_BUFFS = {MobEffects.REGENERATION, MobEffects.ABSORPTION, MobEffects.HEALTH_BOOST};

    @SubscribeEvent
    public void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack result = event.getSmelting();
        if (!isCookedFood(result)) return;

        SkillManager.addXP(player, SkillType.COOKING, 5L * Math.max(1, result.getCount()));

        QuestManager.progress(player, "cook", "ANY", result.getCount());
        AchievementManager.incrementAndCheck(player, "cook", result.getCount(), "cook_500");
    }

    @SubscribeEvent
    public void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItem();

        Item item = stack.getItem();
        if (!item.isEdible()) return;

        int level = SkillData.getLevel(player, SkillType.COOKING);
        if (level <= 0) return;

        MobEffect[] buffs = buffsFor(item);
        int buffCount = buffCount(level);
        float extraBuffStat = SkillData.getStatValue(player, SkillPointStat.COOKING_EXTRA_BUFF);
        if (extraBuffStat >= 0.06f) {
            buffCount = Math.min(buffCount + 1, 4);
        }
        if (buffs != null && buffCount > 0) {
            int duration  = buffDuration(level);
            int amplifier = buffAmplifier(level);
            for (int i = 0; i < Math.min(buffCount, buffs.length); i++) {
                applyBuffEffect(player, buffs[i], duration, amplifier);
            }

            if (player.level() instanceof ServerLevel serverLevel) {
                ParticleUtil.spawnCircle(serverLevel,
                    player.getX(), player.getY(), player.getZ(),
                    ParticleTypes.ENTITY_EFFECT, 1.0, 12, 0.5);
            }
        }

        // COOKING_SATURATION: 40/70레벨 음식 포화도 20%/50% 증가
        double saturationBonusPercent = saturationBonusPercent(level)
            + SkillData.getStatValue(player, SkillPointStat.COOKING_SATURATION);
        if (saturationBonusPercent > 0) {
            FoodProperties food = item.getFoodProperties(stack, player);
            if (food != null) {
                float baseSaturation = food.getNutrition() * food.getSaturationModifier() * 2.0f;
                player.getFoodData().eat(0, (float) (baseSaturation * saturationBonusPercent));
            }
        }

        if (level >= 100) {
            player.heal(4.0f);
            player.removeAllEffects();
        }
    }

    private void applyBuffEffect(ServerPlayer player, MobEffect effect, int duration, int amplifier) {
        // 재생 효과는 다른 버프보다 훨씬 강력하므로 지속시간을 1/4로 제한
        int finalDuration = effect == MobEffects.REGENERATION ? Math.max(20, duration / 4) : duration;
        player.addEffect(new MobEffectInstance(effect, finalDuration, amplifier, false, true));
    }

    private MobEffect[] buffsFor(Item item) {
        if (item == Items.COOKED_BEEF || item == Items.COOKED_PORKCHOP || item == Items.COOKED_MUTTON) return MEAT_BUFFS;
        if (item == Items.COOKED_CHICKEN || item == Items.COOKED_RABBIT) return POULTRY_BUFFS;
        if (item == Items.COOKED_COD || item == Items.COOKED_SALMON) return FISH_BUFFS;
        if (item == Items.BAKED_POTATO || item == Items.BREAD) return STAPLE_BUFFS;
        if (item == Items.PUMPKIN_PIE || item == Items.CAKE) return DESSERT_BUFFS;
        return null;
    }

    // COOKING_BUFF_COUNT: 20레벨 1개, 50레벨 2개, 80레벨 3개 동시 적용
    private int buffCount(int level) {
        if (level >= 80) return 3;
        if (level >= 50) return 2;
        if (level >= 20) return 1;
        return 0;
    }

    // COOKING_BUFF_MAX: 90레벨부터 모든 음식 버프 강도 최대(amplifier 2 = III)
    private int buffAmplifier(int level) {
        if (level >= 90) return 2;
        if (level >= 60) return 1;
        return 0;
    }

    private double saturationBonusPercent(int level) {
        if (level >= 70) return 0.50;
        if (level >= 40) return 0.20;
        return 0;
    }

    private int buffDuration(int level) {
        if (level >= 60) return (int)(1200 * 1.5);
        if (level >= 30) return 1200;
        if (level >= 10) return (int)(1200 * 1.1);
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
