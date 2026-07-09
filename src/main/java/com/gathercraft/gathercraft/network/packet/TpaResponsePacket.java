package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.tpa.TpaManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TpaResponsePacket {

    public final boolean accept;

    public TpaResponsePacket(boolean accept) {
        this.accept = accept;
    }

    public static void encode(TpaResponsePacket p, FriendlyByteBuf buf) {
        buf.writeBoolean(p.accept);
    }

    public static TpaResponsePacket decode(FriendlyByteBuf buf) {
        return new TpaResponsePacket(buf.readBoolean());
    }

    public static void handle(TpaResponsePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            TpaManager.respond(sp, packet.accept);
        });
        ctx.get().setPacketHandled(true);
    }
}
