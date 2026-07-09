package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.waypoint.WaypointData;
import com.gathercraft.gathercraft.waypoint.WaypointManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WaypointSavePacket {

    public final String name;
    public final String icon;

    public WaypointSavePacket(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    public static void encode(WaypointSavePacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.name, 32);
        buf.writeUtf(p.icon, 16);
    }

    public static WaypointSavePacket decode(FriendlyByteBuf buf) {
        return new WaypointSavePacket(buf.readUtf(32), buf.readUtf(16));
    }

    public static void handle(WaypointSavePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;

            WaypointData data = new WaypointData(
                packet.name, packet.icon,
                sp.level().dimension().location().toString(),
                sp.getX(), sp.getY(), sp.getZ(), sp.getYRot()
            );

            boolean added = WaypointManager.add(sp, data);
            if (added) {
                sp.sendSystemMessage(Component.literal("§a[웨이포인트] §f'" + packet.name + "' 저장 완료!"));
            } else {
                sp.sendSystemMessage(Component.literal("§c웨이포인트는 최대 " + WaypointManager.MAX_WAYPOINTS + "개까지 저장할 수 있습니다."));
            }

            PacketHandler.sendToPlayer(sp, new WaypointSyncPacket(WaypointManager.getAll(sp)));
        });
        ctx.get().setPacketHandled(true);
    }
}
