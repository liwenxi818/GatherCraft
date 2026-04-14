package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.skill.dash.DashManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DashRequestPacket {

    public static void encode(DashRequestPacket packet, FriendlyByteBuf buf) {
        // 빈 페이로드
    }

    public static DashRequestPacket decode(FriendlyByteBuf buf) {
        return new DashRequestPacket();
    }

    public static void handle(DashRequestPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                DashManager.tryDash(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
