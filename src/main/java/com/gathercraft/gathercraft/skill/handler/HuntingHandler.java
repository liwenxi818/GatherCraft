package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.DamageTextPacket;
import com.gathercraft.gathercraft.particle.ParticleUtil;
import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 사냥 스킬 핸들러
 * - 몬스터 처치 시 XP 적립
 * - 레벨별 보너스: 공격력 증가(PlayerTickHandler), 크리티컬 확률, 추가 드롭, 체력 회복, 즉사 공격
 * - 모션: 공격 SWEEP_ATTACK/CRIT, 처치 EXPLOSION_EMITTER/SOUL, 보스 firework
 * - 크리티컬 시스템: 낙하 크리티컬(40레벨+20%) OR 레벨 기반 크리티컬(20레벨 5%/80레벨 20%) - 중복 미적용
 */
public class HuntingHandler {

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        // 플레이어 자신 및 다른 플레이어 처치는 제외
        if (event.getEntity() instanceof Player) return;

        SkillManager.addXP(player, SkillType.HUNTING, 20);

        int level = SkillData.getLevel(player, SkillType.HUNTING);
        ServerLevel world = (ServerLevel) event.getEntity().level();
        double ex = event.getEntity().getX();
        double ey = event.getEntity().getY();
        double ez = event.getEntity().getZ();

        // 보스 처치 시 화려한 firework 연출
        if (event.getEntity() instanceof WitherBoss || event.getEntity() instanceof EnderDragon) {
            for (int i = 0; i < 2; i++) {
                double ox = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2;
                double oz = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2;
                ParticleUtil.spawnBurst(world, ex + ox, ey + 1, ez + oz,
                    ParticleTypes.EXPLOSION, 1, 0);
            }
            world.sendParticles(ParticleTypes.FIREWORK, ex, ey + 2, ez, 25, 1.0, 1.0, 1.0, 0.2);
            ParticleUtil.spawnCircle(world, ex, ey, ez, ParticleTypes.TOTEM_OF_UNDYING, 3.0, 16, 0.5);
        } else {
            // 일반 몬스터 처치
            world.sendParticles(ParticleTypes.POOF, ex, ey + 0.5, ez, 5, 0.2, 0.2, 0.2, 0.05);
            world.sendParticles(ParticleTypes.SOUL, ex, ey + 1, ez, 6, 0.2, 0.3, 0.2, 0.04);
        }

        // 50레벨: 처치 시 일정 확률 체력 회복 (스탯 보너스 반영)
        float healStat = SkillData.getStatValue(player, SkillPointStat.HUNTING_HEAL);
        if (level >= 50 && ThreadLocalRandom.current().nextDouble() < 0.20) {
            player.heal(2.0f + healStat);
        }

        // 70레벨: 희귀 드롭 확률 증가 (스탯 보너스 반영)
        float rareDropStat = SkillData.getStatValue(player, SkillPointStat.HUNTING_RARE_DROP);
        if (level >= 70 && ThreadLocalRandom.current().nextDouble() < 0.10 + rareDropStat) {
            event.getEntity().spawnAtLocation(
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BONE)
            );
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getEntity() instanceof Player) return;

        int level = SkillData.getLevel(player, SkillType.HUNTING);
        boolean crit = isCritical(player);

        // 파티클 연출 (레벨 무관)
        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            double tx = event.getEntity().getX();
            double ty = event.getEntity().getY() + event.getEntity().getBbHeight() * 0.5;
            double tz = event.getEntity().getZ();

            ItemStack held = player.getMainHandItem();
            boolean isSwordOrAxe = held.getItem() instanceof SwordItem || held.getItem() instanceof AxeItem;

            if (isSwordOrAxe) {
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, tx, ty, tz, 5, 0.3, 0.3, 0.3, 0);
            }
            if (crit) {
                serverLevel.sendParticles(ParticleTypes.CRIT, tx, ty, tz, 8, 0.3, 0.3, 0.3, 0.2);
            }
        }

        if (level <= 0) return;

        float damage = event.getAmount();

        float attackStat = SkillData.getStatValue(player, SkillPointStat.HUNTING_ATTACK);
        damage *= (attackMultiplier(level) + attackStat);

        // 크리티컬: 낙하 크리티컬(40레벨) OR 레벨 기반 크리티컬 — 중복 적용 방지
        boolean isCritHit = false;
        float critStat = SkillData.getStatValue(player, SkillPointStat.HUNTING_CRITICAL);
        if (level >= 40 && crit) {
            damage *= 1.20f;
            isCritHit = true;
        } else {
            double critChance = (level >= 80 ? 0.20 : level >= 20 ? 0.05 : 0) + critStat;
            if (critChance > 0 && ThreadLocalRandom.current().nextDouble() < critChance) {
                damage *= 1.5f;
                isCritHit = true;
            }
        }

        if (level >= 100 && ThreadLocalRandom.current().nextDouble() < 0.05) {
            event.getEntity().kill();
            return;
        }

        event.setAmount(damage);

        // 부유 데미지 텍스트 패킷 전송
        LivingEntity target = event.getEntity();
        PacketHandler.sendToPlayer(player, new DamageTextPacket(
            damage, isCritHit,
            target.getX(),
            target.getY() + target.getBbHeight() + 0.2,
            target.getZ()
        ));
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
