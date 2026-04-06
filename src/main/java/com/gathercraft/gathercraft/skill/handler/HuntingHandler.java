package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

/**
 * 사냥 스킬 핸들러
 * - 몬스터 처치 시 XP 적립
 * - 레벨별 보너스: 공격력 증가(PlayerTickHandler), 크리티컬 확률, 추가 드롭, 체력 회복, 즉사 공격
 */
public class HuntingHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        // Slime/MagmaCube는 Monster를 상속하지 않으므로 별도 체크
        if (!(event.getEntity() instanceof Monster) && !(event.getEntity() instanceof Slime)) return;

        SkillManager.addXP(player, SkillType.HUNTING, 20);

        int level = SkillData.getLevel(player, SkillType.HUNTING);

        // 50레벨: 처치 시 일정 확률 체력 회복
        if (level >= 50 && RANDOM.nextDouble() < 0.20) {
            player.heal(2.0f);
        }

        // 70레벨: 희귀 드롭 확률 증가 (추가 드롭 생성)
        if (level >= 70 && RANDOM.nextDouble() < 0.10) {
            event.getEntity().spawnAtLocation(
                new ItemStack(net.minecraft.world.item.Items.BONE)
            );
        }
    }

    // 공격 시 크리티컬 보너스 및 즉사 처리
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getEntity() instanceof Monster) && !(event.getEntity() instanceof Slime)) return;

        int level = SkillData.getLevel(player, SkillType.HUNTING);
        if (level <= 0) return;

        float damage = event.getAmount();

        // 공격력 보너스 (10/30/60/90레벨)
        damage *= attackMultiplier(level);

        // 40레벨: 크리티컬 데미지 +20%
        if (level >= 40 && isCritical(player)) {
            damage *= 1.20f;
        }

        // 20레벨: 크리티컬 확률 +5%, 80레벨: +20%
        double critChance = level >= 80 ? 0.20 : level >= 20 ? 0.05 : 0;
        if (critChance > 0 && RANDOM.nextDouble() < critChance) {
            damage *= 1.5f;
        }

        // 100레벨 각성: 일정 확률 즉사 공격
        if (level >= 100 && RANDOM.nextDouble() < 0.05) {
            event.getEntity().kill();
            return;
        }

        event.setAmount(damage);
    }

    private float attackMultiplier(int level) {
        if (level >= 90) return 1.50f;
        if (level >= 60) return 1.30f;
        if (level >= 30) return 1.15f;
        if (level >= 10) return 1.05f;
        return 1.0f;
    }

    private boolean isCritical(ServerPlayer player) {
        return player.fallDistance > 0 && !player.onGround()
            && !player.isInWater() && !player.isPassenger();
    }
}
