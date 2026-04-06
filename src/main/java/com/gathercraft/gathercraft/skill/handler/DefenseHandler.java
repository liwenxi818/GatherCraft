package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

/**
 * 방어 스킬 핸들러
 * - 몬스터에게 데미지 받을 시 XP 적립
 * - 레벨별 보너스: 데미지 감소, 무효화 확률, 독/화염 면역, 치명타 무효화
 * - 넉백 저항, 체력 최대치는 PlayerTickHandler에서 처리
 */
public class DefenseHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DamageSource source = event.getSource();

        // 몬스터에게 받은 데미지만 XP 적립
        if (source.getEntity() instanceof Monster) {
            SkillManager.addXP(player, SkillType.DEFENSE, 2);
        }

        int level = SkillData.getLevel(player, SkillType.DEFENSE);
        if (level <= 0) return;

        float damage = event.getAmount();

        // 80레벨: 독/화염 데미지 면역
        if (level >= 80) {
            if (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE)
                || (source.is(DamageTypes.MAGIC) && player.hasEffect(net.minecraft.world.effect.MobEffects.POISON))) {
                event.setCanceled(true);
                return;
            }
        }

        // 데미지 감소 보너스 (10/30/60/90레벨)
        damage *= (1.0f - damageReduction(level));

        // 50레벨: 데미지 받을 시 일정 확률 완전 무효화
        if (level >= 50 && source.getEntity() instanceof Monster && RANDOM.nextDouble() < 0.10) {
            event.setCanceled(true);
            return;
        }

        // 100레벨 각성: 치명타 데미지 완전 무효화 확률
        if (level >= 100 && isCriticalDamage(damage, event.getAmount()) && RANDOM.nextDouble() < 0.30) {
            event.setCanceled(true);
            return;
        }

        event.setAmount(damage);
    }

    private float damageReduction(int level) {
        if (level >= 90) return 0.50f;
        if (level >= 60) return 0.30f;
        if (level >= 30) return 0.15f;
        if (level >= 10) return 0.05f;
        return 0;
    }

    private boolean isCriticalDamage(float reducedDamage, float originalDamage) {
        // 원래 데미지가 일정 이상이면 치명타로 간주
        return originalDamage >= 6.0f;
    }
}
