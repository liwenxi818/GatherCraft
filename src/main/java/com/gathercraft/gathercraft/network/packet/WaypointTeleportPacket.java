package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.waypoint.WaypointManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WaypointTeleportPacket {

    public final int index;

    public WaypointTeleportPacket(int index) {
        this.index = index;
    }

    public static void encode(WaypointTeleportPacket p, FriendlyByteBuf buf) {
        buf.writeInt(p.index);
    }

    public static WaypointTeleportPacket decode(FriendlyByteBuf buf) {
        return new WaypointTeleportPacket(buf.readInt());
    }

    public static void handle(WaypointTeleportPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            WaypointManager.teleport(sp, packet.index);
        });
        ctx.get().setPacketHandled(true);
    }
}
