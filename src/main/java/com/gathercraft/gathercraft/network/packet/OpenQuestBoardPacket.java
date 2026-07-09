package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.client.gui.SkillBookScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 퀘스트 게시판 우클릭 시 스킬 책 GUI를 퀘스트 탭(4)으로 연다. */
public class OpenQuestBoardPacket {

    public static void encode(OpenQuestBoardPacket p, FriendlyByteBuf buf) {
        // 필드 없음
    }

    public static OpenQuestBoardPacket decode(FriendlyByteBuf buf) {
        return new OpenQuestBoardPacket();
    }

    public static void handle(OpenQuestBoardPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                SkillBookScreen.open(4)
            )
        );
        ctx.get().setPacketHandled(true);
    }
}
