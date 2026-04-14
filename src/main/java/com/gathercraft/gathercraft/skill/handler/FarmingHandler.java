package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import com.gathercraft.gathercraft.skill.SkillType;
import com.gathercraft.gathercraft.skill.SkillUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 농사 스킬 핸들러
 * - 완전히 자란 작물 수확 시 XP 적립
 * - 레벨별 보너스: 추가 드롭, 자동 재식(40레벨), 5x5 범위 수확(100레벨)
 * - 모션: COMPOSTER (수확), HAPPY_VILLAGER (자동 재식)
 */
public class FarmingHandler {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        BlockState state = event.getState();

        if (!isFullyGrownCrop(state)) return;

        SkillManager.addXP(player, SkillType.FARMING, 6);

        int level = SkillData.getLevel(player, SkillType.FARMING);
        ServerLevel world = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        double bx = pos.getX() + 0.5;
        double by = pos.getY() + 0.5;
        double bz = pos.getZ() + 0.5;

        // 항상: COMPOSTER 파티클
        world.sendParticles(ParticleTypes.COMPOSTER, bx, by + 0.5, bz, 6, 0.3, 0.2, 0.3, 0.05);

        // 추가 드롭 보너스
        double extraDropChance = extraDropChance(level)
            + SkillData.getStatValue(player, SkillPointStat.FARMING_EXTRA_DROP);
        if (extraDropChance > 0 && ThreadLocalRandom.current().nextDouble() < extraDropChance) {
            SkillUtil.spawnExtraDrops(state, world, pos, player);
        }

        // 40레벨: 씨앗 자동 재식 (항상) / 미만: 스탯 포인트 확률로 재식
        float replantStat = SkillData.getStatValue(player, SkillPointStat.FARMING_REPLANT);
        boolean shouldReplant = level >= 40
            || (replantStat > 0 && ThreadLocalRandom.current().nextDouble() < replantStat);
        if (shouldReplant) {
            autoReplant(state, world, pos);
            // 자동 재식 시 초록 파티클
            world.sendParticles(ParticleTypes.HAPPY_VILLAGER, bx, by + 0.8, bz, 4, 0.2, 0.2, 0.2, 0.05);
        }

        // 100레벨 각성: 주변 5x5 범위 작물 동시 수확
        if (level >= 100 && ThreadLocalRandom.current().nextDouble() < 0.25) {
            triggerAreaHarvest(player, world, pos);
        }
    }

    private boolean isFullyGrownCrop(BlockState state) {
        if (!state.is(BlockTags.CROPS)) return false;
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        return false;
    }

    private double extraDropChance(int level) {
        if (level >= 90) return 0.50;
        if (level >= 60) return 0.30;
        if (level >= 30) return 0.15;
        if (level >= 10) return 0.05;
        return 0;
    }

    private void autoReplant(BlockState state, ServerLevel world, BlockPos pos) {
        if (state.getBlock() instanceof CropBlock crop) {
            BlockState sapling = crop.defaultBlockState();
            world.setBlock(pos, sapling, Block.UPDATE_ALL);
        }
    }

    private void triggerAreaHarvest(ServerPlayer player, ServerLevel world, BlockPos center) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos target = center.offset(dx, 0, dz);
                if (isFullyGrownCrop(world.getBlockState(target))) {
                    world.sendParticles(ParticleTypes.COMPOSTER,
                        target.getX() + 0.5, target.getY() + 0.8, target.getZ() + 0.5,
                        4, 0.2, 0.2, 0.2, 0.05);
                    world.destroyBlock(target, true, player);
                }
            }
        }
    }
}
