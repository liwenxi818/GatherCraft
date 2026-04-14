package com.gathercraft.gathercraft.skill;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * 스킬 핸들러 전반에서 공유되는 공통 유틸리티 메서드 모음.
 */
public final class SkillUtil {

    private SkillUtil() {}

    /**
     * 블록의 드롭 아이템을 계산하여 해당 위치에 다시 스폰한다.
     * Mining/Lumberjack/Farming 핸들러의 추가 드롭에 공통 사용.
     */
    public static void spawnExtraDrops(BlockState state, ServerLevel world,
                                        BlockPos pos, ServerPlayer player) {
        List<ItemStack> drops = Block.getDrops(state, world, pos, null, player, player.getMainHandItem());
        for (ItemStack drop : drops) {
            Block.popResource(world, pos, drop);
        }
    }
}
