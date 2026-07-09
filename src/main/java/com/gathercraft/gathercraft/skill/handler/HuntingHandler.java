package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.achievement.AchievementManager;
import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.DamageTextPacket;
import com.gathercraft.gathercraft.particle.ParticleUtil;
import com.gathercraft.gathercraft.quest.QuestManager;
import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import com.gathercraft.gathercraft.title.TitleManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 사냥 스킬 핸들러
 * - 몬스터 처치 시 XP 적립 (등급별)
 * - 레벨별 보너스: 공격력 증가(PlayerTickHandler), 크리티컬 확률, 희귀 드롭, 체력 회복, 즉사 공격
 * - 모션: 공격 SWEEP_ATTACK/CRIT, 처치 EXPLOSION_EMITTER/SOUL, 보스 firework
 */
public class HuntingHandler {

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) return;
        if (event.getEntity() instanceof Player) return;

        LivingEntity killed = event.getEntity();
        SkillManager.addXP(player, SkillType.HUNTING, getMobXP(killed));

        String mobId = ForgeRegistries.ENTITY_TYPES.getKey(killed.getType()).getPath().toUpperCase();
        QuestManager.progress(player, "hunt", mobId, 1);
        QuestManager.progress(player, "hunt", "ANY", 1);
        boolean isBossKill = killed instanceof WitherBoss || killed instanceof EnderDragon;
        if (isBossKill) {
            QuestManager.progress(player, "hunt", "BOSS", 1);
            AchievementManager.incrementAndCheck(player, "boss", 1, "first_boss", "boss_10");
        } else {
            AchievementManager.incrementAndCheck(player, "mob", 1, "mob_1000");
        }

        int level = SkillData.getLevel(player, SkillType.HUNTING);
        ServerLevel world = (ServerLevel) killed.level();
        double ex = killed.getX();
        double ey = killed.getY();
        double ez = killed.getZ();

        // 보스 처치 시 화려한 firework 연출
        if (killed instanceof WitherBoss || killed instanceof EnderDragon) {
            for (int i = 0; i < 2; i++) {
                double ox = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2;
                double oz = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2;
                ParticleUtil.spawnBurst(world, ex + ox, ey + 1, ez + oz,
                    ParticleTypes.EXPLOSION, 1, 0);
            }
            world.sendParticles(ParticleTypes.FIREWORK, ex, ey + 2, ez, 25, 1.0, 1.0, 1.0, 0.2);
            ParticleUtil.spawnCircle(world, ex, ey, ez, ParticleTypes.TOTEM_OF_UNDYING, 3.0, 16, 0.5);
        } else {
            world.sendParticles(ParticleTypes.POOF, ex, ey + 0.5, ez, 5, 0.2, 0.2, 0.2, 0.05);
            world.sendParticles(ParticleTypes.SOUL, ex, ey + 1, ez, 6, 0.2, 0.3, 0.2, 0.04);
        }

        // HUNTING_HEAL: 20/50/80레벨 확률 체력 회복
        if (level >= 20) {
            double healChance;
            float healAmount;
            if (level >= 80) { healChance = 0.40; healAmount = 4.0f; }
            else if (level >= 50) { healChance = 0.25; healAmount = 2.0f; }
            else { healChance = 0.10; healAmount = 1.0f; }

            if (ThreadLocalRandom.current().nextDouble() < healChance) {
                player.heal(healAmount);
                world.sendParticles(ParticleTypes.HEART,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    5, 0.3, 0.3, 0.3, 0.05);
            }
        }

        // HUNTING_RARE_DROP: 40/70레벨 확률 (보스는 확정)
        applyRareDrop(killed, level);
    }

    private void applyRareDrop(LivingEntity killed, int level) {
        boolean isBoss = killed instanceof WitherBoss || killed instanceof EnderDragon;
        boolean isElite = killed instanceof ElderGuardian || killed instanceof Warden;

        if (isBoss) {
            killed.spawnAtLocation(new ItemStack(Items.NETHER_STAR));
            return;
        }

        double chance;
        if (level >= 70) chance = 0.15;
        else if (level >= 40) chance = 0.05;
        else return;

        if (ThreadLocalRandom.current().nextDouble() >= chance) return;

        if (isElite) {
            killed.spawnAtLocation(new ItemStack(Items.ECHO_SHARD));
        } else if (killed instanceof Blaze) {
            killed.spawnAtLocation(new ItemStack(Items.BLAZE_ROD, 2));
        } else if (killed instanceof EnderMan) {
            killed.spawnAtLocation(new ItemStack(Items.ENDER_PEARL, 2));
        } else {
            int roll = ThreadLocalRandom.current().nextInt(3);
            Item drop = switch (roll) {
                case 0 -> Items.NAME_TAG;
                case 1 -> Items.SADDLE;
                default -> Items.EMERALD;
            };
            killed.spawnAtLocation(new ItemStack(drop));
        }
    }

    private long getMobXP(LivingEntity entity) {
        if (entity instanceof WitherBoss || entity instanceof EnderDragon) return 500L;
        if (entity instanceof ElderGuardian || entity instanceof Warden)   return 200L;
        if (entity instanceof Blaze || entity instanceof Ghast
         || entity instanceof EnderMan || entity instanceof Shulker)       return 60L;
        if (entity instanceof Monster)                                     return 20L;
        return 10L;
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getEntity() instanceof Player) return;

        int level = SkillData.getLevel(player, SkillType.HUNTING);
        boolean crit = isCritical(player);

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
        damage *= attackMultiplier(level);
        if (TitleManager.hasTitle(player, "hunter_3")) {
            damage *= 1.05f;
        }

        boolean isCritHit = false;
        if (level >= 40 && crit) {
            damage *= 1.20f;
            isCritHit = true;
        } else {
            double critChance = level >= 80 ? 0.20 : level >= 20 ? 0.05 : 0;
            if (critChance > 0 && ThreadLocalRandom.current().nextDouble() < critChance) {
                damage *= 1.5f;
                isCritHit = true;
            }
        }

        if (level >= 100 && ThreadLocalRandom.current().nextDouble() < 0.05) {
            LivingEntity tgt = event.getEntity();
            PacketHandler.sendToPlayer(player, new DamageTextPacket(
                tgt.getHealth(), true,
                tgt.getX(), tgt.getY() + tgt.getBbHeight() + 0.2, tgt.getZ()
            ));
            tgt.kill();
            return;
        }

        event.setAmount(damage);

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
