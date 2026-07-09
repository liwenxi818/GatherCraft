package com.gathercraft.gathercraft.block;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.OpenQuestBoardPacket;
import com.gathercraft.gathercraft.network.packet.QuestSyncPacket;
import com.gathercraft.gathercraft.quest.QuestManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;

/** 우클릭 시 오늘의 퀘스트를 갱신·동기화하고 스킬 책 퀘스트 탭을 연다. */
public class QuestBoardBlock extends Block {

    public QuestBoardBlock() {
        super(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.WOOD).noOcclusion());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer sp) {
            PacketHandler.sendToPlayer(sp, new QuestSyncPacket(QuestManager.getQuests(sp)));
            PacketHandler.sendToPlayer(sp, new OpenQuestBoardPacket());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
