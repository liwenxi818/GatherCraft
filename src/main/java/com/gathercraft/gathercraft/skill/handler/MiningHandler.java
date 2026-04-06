package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;

/**
 * 채광 스킬 핸들러
 * - 광석 채굴 시 XP 적립
 * - 레벨별 보너스: 추가 드롭, Haste(PlayerTickHandler), XP 오브, 3x3 채굴
 */
public class MiningHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        BlockState state = event.getState();
        if (!state.is(Tags.Blocks.ORES)) return;

        SkillManager.addXP(player, SkillType.MINING, 10);

        int level = SkillData.getLevel(player, SkillType.MINING);
        ServerLevel world = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();

        // 추가 드롭 보너스 (10/30/60/90레벨)
        double extraDropChance = extraDropChance(level);
        if (extraDropChance > 0 && RANDOM.nextDouble() < extraDropChance) {
            spawnExtraDrops(state, world, pos, player);
        }

        // 50레벨: 희귀 광석 추가 드롭
        if (level >= 50) {
            if (isRareOre(state) && RANDOM.nextDouble() < 0.20) {
                spawnExtraDrops(state, world, pos, player);
            }
        }

        // 70레벨: XP 오브 추가 드롭
        if (level >= 70) {
            world.addFreshEntity(new ExperienceOrb(world,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                RANDOM.nextInt(3) + 1));
        }

        // 100레벨 각성: 15% 확률로 주변 3x3 광석 동시 채굴
        if (level >= 100 && RANDOM.nextDouble() < 0.15) {
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

    private void spawnExtraDrops(BlockState state, ServerLevel world, BlockPos pos, ServerPlayer player) {
        List<ItemStack> drops = Block.getDrops(state, world, pos, null, player, player.getMainHandItem());
        for (ItemStack drop : drops) {
            Block.popResource(world, pos, drop);
        }
    }

    private void triggerAreaMining(ServerPlayer player, ServerLevel world, BlockPos center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos target = center.offset(dx, dy, dz);
                    if (world.getBlockState(target).is(Tags.Blocks.ORES)) {
                        world.destroyBlock(target, true, player);
                    }
                }
            }
        }
    }
}
