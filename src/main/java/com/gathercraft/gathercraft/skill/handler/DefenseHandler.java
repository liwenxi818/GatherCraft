package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.ScreenFlashPacket;
import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 방어 스킬 핸들러
 * - 몬스터에게 데미지 받을 시 XP 적립
 * - 레벨별 보너스: 데미지 감소, 무효화 확률, 독/화염 면역, 치명타 무효화
 * - 모션: 피격 DAMAGE_INDICATOR, 큰 데미지 화면 빨간 플래시, 방패 막기 BLOCK+CRIT
 */
public class DefenseHandler {

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DamageSource source = event.getSource();

        // 몬스터에게 받은 데미지만 XP 적립
        if (source.getEntity() instanceof Monster) {
            SkillManager.addXP(player, SkillType.DEFENSE, 2);
        }

        // 피격 파티클: DAMAGE_INDICATOR
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                player.getX(), player.getY() + 1.0, player.getZ(),
                5, 0.3, 0.3, 0.3, 0.1);
        }

        // 큰 데미지(체력 20% 이상): 화면 가장자리 빨간 플래시
        if (event.getAmount() >= player.getMaxHealth() * 0.2f) {
            PacketHandler.sendToPlayer(player, new ScreenFlashPacket(0.85f));
        }

        int level = SkillData.getLevel(player, SkillType.DEFENSE);
        if (level <= 0) return;

        float originalDamage = event.getAmount();
        float damage = originalDamage;

        // 80레벨: 독/화염 데미지 면역
        // 독 효과 데미지는 MAGIC 타입 + 공격자 엔티티 없음으로 식별
        if (level >= 80) {
            boolean isFire = source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE);
            boolean isPoison = source.is(DamageTypes.MAGIC) && source.getEntity() == null
                && player.hasEffect(net.minecraft.world.effect.MobEffects.POISON);
            if (isFire || isPoison) {
                event.setCanceled(true);
                return;
            }
        }

        float dmgReduceStat = SkillData.getStatValue(player, SkillPointStat.DEFENSE_DAMAGE_REDUCE);
        float totalReduction = Math.min(damageReduction(level) + dmgReduceStat, 0.90f);
        damage *= (1.0f - totalReduction);

        // 무적 시간 연장 (스탯 포인트)
        float invulStat = SkillData.getStatValue(player, SkillPointStat.DEFENSE_INVULNERABLE);
        if (invulStat > 0) {
            int baseTime = 20; // 바닐라 기본 무적 시간 (1초)
            player.invulnerableTime = Math.max(player.invulnerableTime,
                baseTime + (int)(baseTime * invulStat));
        }

        if (level >= 50 && source.getEntity() instanceof Monster && ThreadLocalRandom.current().nextDouble() < 0.10) {
            event.setCanceled(true);
            return;
        }

        if (level >= 100 && isCriticalDamage(originalDamage) && ThreadLocalRandom.current().nextDouble() < 0.30) {
            event.setCanceled(true);
            return;
        }

        event.setAmount(damage);
    }

    @SubscribeEvent
    public void onShieldBlock(ShieldBlockEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        Vec3 look = player.getLookAngle();
        double bx = player.getX() + look.x * 0.8;
        double by = player.getY() + 0.8;
        double bz = player.getZ() + look.z * 0.8;

        // 방패 소재 블록 파티클 + 불꽃 효과
        serverLevel.sendParticles(
            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()),
            bx, by, bz, 20, 0.2, 0.2, 0.2, 0.1);
        serverLevel.sendParticles(ParticleTypes.CRIT, bx, by, bz, 10, 0.3, 0.3, 0.3, 0.2);
    }

    private float damageReduction(int level) {
        if (level >= 90) return 0.50f;
        if (level >= 60) return 0.30f;
        if (level >= 30) return 0.15f;
        if (level >= 10) return 0.05f;
        return 0;
    }

    private boolean isCriticalDamage(float originalDamage) {
        return originalDamage >= 6.0f;
    }
}
