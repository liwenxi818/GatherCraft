package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.tpa.TpaManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TpaRequestPacket {

    public final String targetName;

    public TpaRequestPacket(String targetName) {
        this.targetName = targetName;
    }

    public static void encode(TpaRequestPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.targetName, 16);
    }

    public static TpaRequestPacket decode(FriendlyByteBuf buf) {
        return new TpaRequestPacket(buf.readUtf(16));
    }

    public static void handle(TpaRequestPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            TpaManager.request(sp, packet.targetName);
        });
        ctx.get().setPacketHandled(true);
    }
}
