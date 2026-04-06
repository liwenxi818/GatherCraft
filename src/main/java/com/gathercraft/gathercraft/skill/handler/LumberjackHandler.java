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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * 벌목 스킬 핸들러
 * - 나무 원목 채굴 시 XP 적립
 * - 레벨별 보너스: 추가 드롭, 내구도 감소 절약(PlayerTickHandler), 묘목 심기(TODO), 연쇄 벌목
 */
public class LumberjackHandler {

    private static final Random RANDOM = new Random();
    private static final int MAX_CHAIN_BLOCKS = 64;

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        BlockState state = event.getState();
        if (!state.is(BlockTags.LOGS)) return;
        // 맨손(ItemStack.EMPTY)으로 채굴해도 XP 적립됨 — 도구 체크 없음

        SkillManager.addXP(player, SkillType.LUMBERJACK, 8);

        int level = SkillData.getLevel(player, SkillType.LUMBERJACK);
        ServerLevel world = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();

        // 추가 드롭 보너스 (10/40/70/90레벨)
        double extraDropChance = extraDropChance(level);
        if (extraDropChance > 0 && RANDOM.nextDouble() < extraDropChance) {
            spawnExtraDrops(state, world, pos, player);
        }

        // 50레벨: 자동 묘목 심기 (TODO: 나무 종류 별 묘목 매핑 구현)

        // 100레벨 각성: 연결된 나무 전체 동시 채굴
        if (level >= 100 && RANDOM.nextDouble() < 0.20) {
            triggerChainFelling(player, world, pos);
        }
    }

    private double extraDropChance(int level) {
        if (level >= 90) return 0.50;
        if (level >= 70) return 0.35;
        if (level >= 40) return 0.20;
        if (level >= 10) return 0.05;
        return 0;
    }

    private void spawnExtraDrops(BlockState state, ServerLevel world, BlockPos pos, ServerPlayer player) {
        List<ItemStack> drops = Block.getDrops(state, world, pos, null, player, player.getMainHandItem());
        for (ItemStack drop : drops) {
            Block.popResource(world, pos, drop);
        }
    }

    // BFS로 연결된 원목 블록 전체 채굴
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
                world.destroyBlock(pos, true, player);
            }
        }
    }
}
