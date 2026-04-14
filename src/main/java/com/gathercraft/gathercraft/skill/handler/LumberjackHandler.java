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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 벌목 스킬 핸들러
 * - 나무 원목 채굴 시 XP 적립
 * - 레벨별 보너스: 추가 드롭, 내구도 감소 절약(PlayerTickHandler), 묘목 심기(TODO), 연쇄 벌목
 * - 모션: CHERRY_LEAVES 나뭇잎 낙하, 연쇄 벌목 시 TOTEM
 */
public class LumberjackHandler {

    private static final int MAX_CHAIN_BLOCKS = 64;

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        BlockState state = event.getState();
        if (!state.is(BlockTags.LOGS)) return;

        SkillManager.addXP(player, SkillType.LUMBERJACK, 8);

        int level = SkillData.getLevel(player, SkillType.LUMBERJACK);
        ServerLevel world = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        double bx = pos.getX() + 0.5;
        double by = pos.getY() + 0.5;
        double bz = pos.getZ() + 0.5;

        // 항상: 나뭇잎 낙하 파티클
        world.sendParticles(ParticleTypes.CHERRY_LEAVES, bx, by + 0.5, bz, 8, 0.4, 0.3, 0.4, 0.05);

        // 추가 드롭 보너스
        double extraDropChance = extraDropChance(level)
            + SkillData.getStatValue(player, SkillPointStat.LUMBERJACK_EXTRA_DROP);
        if (extraDropChance > 0 && ThreadLocalRandom.current().nextDouble() < extraDropChance) {
            SkillUtil.spawnExtraDrops(state, world, pos, player);
        }

        // 묘목 추가 드롭 (스탯 포인트)
        float saplingChance = SkillData.getStatValue(player, SkillPointStat.LUMBERJACK_SAPLING);
        if (saplingChance > 0 && ThreadLocalRandom.current().nextDouble() < saplingChance) {
            Block sapling = getSaplingForLog(state);
            if (sapling != null) {
                world.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                    world, bx, by, bz, new ItemStack(sapling.asItem())
                ));
            }
        }

        // 50레벨: 자동 묘목 심기
        // server.execute()로 현재 이벤트 처리 완료 후 블록이 공기로 바뀐 뒤 심기
        if (level >= 50) {
            Block sapling = getSaplingForLog(state);
            if (sapling != null) {
                BlockState saplingState = sapling.defaultBlockState();
                world.getServer().execute(() -> {
                    if (world.getBlockState(pos).isAir()
                            && saplingState.canSurvive(world, pos)) {
                        world.setBlock(pos, saplingState, 3);
                        world.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            bx, by + 0.3, bz, 5, 0.3, 0.3, 0.3, 0.0);
                    }
                });
            }
        }

        // 100레벨 각성: 연결된 나무 전체 동시 채굴
        if (level >= 100 && ThreadLocalRandom.current().nextDouble() < 0.20) {
            triggerChainFelling(player, world, pos);
        }
    }

    /**
     * 나무 종류별 묘목 매핑. 나무 종류를 BlockTag로 판별.
     */
    @Nullable
    private Block getSaplingForLog(BlockState state) {
        if (state.is(BlockTags.OAK_LOGS))      return Blocks.OAK_SAPLING;
        if (state.is(BlockTags.SPRUCE_LOGS))   return Blocks.SPRUCE_SAPLING;
        if (state.is(BlockTags.BIRCH_LOGS))    return Blocks.BIRCH_SAPLING;
        if (state.is(BlockTags.JUNGLE_LOGS))   return Blocks.JUNGLE_SAPLING;
        if (state.is(BlockTags.ACACIA_LOGS))   return Blocks.ACACIA_SAPLING;
        if (state.is(BlockTags.DARK_OAK_LOGS)) return Blocks.DARK_OAK_SAPLING;
        if (state.is(BlockTags.MANGROVE_LOGS)) return Blocks.MANGROVE_PROPAGULE;
        if (state.is(BlockTags.CHERRY_LOGS))   return Blocks.CHERRY_SAPLING;
        return null;
    }

    private double extraDropChance(int level) {
        if (level >= 90) return 0.50;
        if (level >= 70) return 0.35;
        if (level >= 40) return 0.20;
        if (level >= 10) return 0.05;
        return 0;
    }

    private void triggerChainFelling(ServerPlayer player, ServerLevel world, BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        List<BlockPos> toBreak = new ArrayList<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && toBreak.size() < MAX_CHAIN_BLOCKS) {
            BlockPos current = queue.poll();
            toBreak.add(current);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos neighbor = current.offset(dx, dy, dz);
                        if (!visited.contains(neighbor) && world.getBlockState(neighbor).is(BlockTags.LOGS)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        // 시작 블록은 이미 파괴되므로 제외
        for (BlockPos pos : toBreak) {
            if (!pos.equals(start)) {
                // 각 블록에 나뭇잎 + TOTEM 파티클
                world.sendParticles(ParticleTypes.CHERRY_LEAVES,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    4, 0.3, 0.3, 0.3, 0.05);
                world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    2, 0.2, 0.2, 0.2, 0.1);
                world.destroyBlock(pos, true, player);
            }
        }
    }
}
