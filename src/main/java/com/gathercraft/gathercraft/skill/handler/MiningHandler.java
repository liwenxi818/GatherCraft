package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.particle.ParticleUtil;
import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import com.gathercraft.gathercraft.skill.SkillType;
import com.gathercraft.gathercraft.skill.SkillUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 채광 스킬 핸들러
 * - 광석 채굴 시 XP 적립
 * - 레벨별 보너스: 추가 드롭, Haste(PlayerTickHandler), XP 오브, 3x3 채굴
 * - 모션: CRIT 파티클 (항상), TOTEM (보너스 드롭 발생 시 1회)
 */
public class MiningHandler {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        BlockState state = event.getState();
        if (!state.is(Tags.Blocks.ORES)) return;

        SkillManager.addXP(player, SkillType.MINING, 10);

        int level = SkillData.getLevel(player, SkillType.MINING);
        ServerLevel world = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        double bx = pos.getX() + 0.5;
        double by = pos.getY() + 0.5;
        double bz = pos.getZ() + 0.5;

        // 항상: CRIT 파티클 소량
        world.sendParticles(ParticleTypes.CRIT, bx, by, bz, 3, 0.2, 0.2, 0.2, 0.1);

        // 추가 드롭 보너스 (일반 + 희귀 광석은 독립적으로 판정, TOTEM은 1회만)
        boolean extraDropped = false;
        double extraDropChance = extraDropChance(level)
            + SkillData.getStatValue(player, SkillPointStat.MINING_EXTRA_DROP);
        if (extraDropChance > 0 && ThreadLocalRandom.current().nextDouble() < extraDropChance) {
            SkillUtil.spawnExtraDrops(state, world, pos, player);
            extraDropped = true;
        }

        // 50레벨: 희귀 광석 추가 드롭 (일반 드롭과 독립)
        double rareDropChance = 0.20 + SkillData.getStatValue(player, SkillPointStat.MINING_RARE_DROP);
        if (level >= 50 && isRareOre(state) && ThreadLocalRandom.current().nextDouble() < rareDropChance) {
            SkillUtil.spawnExtraDrops(state, world, pos, player);
            extraDropped = true;
        }

        // TOTEM 파티클은 추가 드롭 발생 시 1회만
        if (extraDropped) {
            ParticleUtil.spawnBurst(world, bx, by, bz, ParticleTypes.TOTEM_OF_UNDYING, 20, 0.4);
        }

        // 70레벨: XP 오브 추가 드롭
        if (level >= 70) {
            world.addFreshEntity(new ExperienceOrb(world, bx, by, bz, ThreadLocalRandom.current().nextInt(3) + 1));
        }

        // 100레벨 각성: 15% 확률로 주변 3x3 광석 동시 채굴
        if (level >= 100 && ThreadLocalRandom.current().nextDouble() < 0.15) {
            triggerAreaMining(player, world, pos);
        }
    }

    private double extraDropChance(int level) {
        if (level >= 90) return 0.50;
        if (level >= 60) return 0.30;
        if (level >= 30) return 0.15;
        if (level >= 10) return 0.05;
        return 0;
    }

    private boolean isRareOre(BlockState state) {
        return state.is(Tags.Blocks.ORES_DIAMOND) || state.is(Tags.Blocks.ORES_EMERALD);
    }

    private void triggerAreaMining(ServerPlayer player, ServerLevel world, BlockPos center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos target = center.offset(dx, dy, dz);
                    if (world.getBlockState(target).is(Tags.Blocks.ORES)) {
                        world.sendParticles(ParticleTypes.CRIT,
                            target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                            2, 0.2, 0.2, 0.2, 0.1);
                        world.destroyBlock(target, true, player);
                    }
                }
            }
        }
    }
}
