package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.waypoint.WaypointManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WaypointDeletePacket {

    public final int index;

    public WaypointDeletePacket(int index) {
        this.index = index;
    }

    public static void encode(WaypointDeletePacket p, FriendlyByteBuf buf) {
        buf.writeInt(p.index);
    }

    public static WaypointDeletePacket decode(FriendlyByteBuf buf) {
        return new WaypointDeletePacket(buf.readInt());
    }

    public static void handle(WaypointDeletePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            WaypointManager.delete(sp, packet.index);
            PacketHandler.sendToPlayer(sp, new WaypointSyncPacket(WaypointManager.getAll(sp)));
        });
        ctx.get().setPacketHandled(true);
    }
}
