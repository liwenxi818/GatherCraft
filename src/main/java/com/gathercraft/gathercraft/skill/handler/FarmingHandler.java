package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;

/**
 * 농사 스킬 핸들러
 * - 완전히 자란 작물 수확 시 XP 적립
 * - 레벨별 보너스: 추가 드롭, 자동 재식(40레벨), 5x5 범위 수확(100레벨)
 */
public class FarmingHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        BlockState state = event.getState();

        // 완전히 자란 작물인지 확인
        if (!isFullyGrownCrop(state)) return;

        SkillManager.addXP(player, SkillType.FARMING, 6);

        int level = SkillData.getLevel(player, SkillType.FARMING);
        ServerLevel world = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();

        // 추가 드롭 보너스 (10/30/60/90레벨)
        double extraDropChance = extraDropChance(level);
        if (extraDropChance > 0 && RANDOM.nextDouble() < extraDropChance) {
            spawnExtraDrops(state, world, pos, player);
        }

        // 40레벨: 씨앗 자동 재식 (수확 후 같은 위치에 씨앗 재식)
        if (level >= 40) {
            autoReplant(state, world, pos);
        }

        // 100레벨 각성: 주변 5x5 범위 작물 동시 수확
        if (level >= 100 && RANDOM.nextDouble() < 0.25) {
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

    private void spawnExtraDrops(BlockState state, ServerLevel world, BlockPos pos, ServerPlayer player) {
        List<ItemStack> drops = Block.getDrops(state, world, pos, null, player, player.getMainHandItem());
        for (ItemStack drop : drops) {
            Block.popResource(world, pos, drop);
        }
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
                    world.destroyBlock(target, true, player);
                }
            }
        }
    }
}
