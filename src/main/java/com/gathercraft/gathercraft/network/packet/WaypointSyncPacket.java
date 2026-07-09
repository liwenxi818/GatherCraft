package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.waypoint.WaypointClientCache;
import com.gathercraft.gathercraft.waypoint.WaypointData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class WaypointSyncPacket {

    public final List<WaypointData> waypoints;

    public WaypointSyncPacket(List<WaypointData> waypoints) {
        this.waypoints = waypoints;
    }

    public static void encode(WaypointSyncPacket p, FriendlyByteBuf buf) {
        ListTag list = new ListTag();
        for (WaypointData wp : p.waypoints) list.add(wp.toNBT());
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("list", list);
        buf.writeNbt(wrapper);
    }

    public static WaypointSyncPacket decode(FriendlyByteBuf buf) {
        List<WaypointData> waypoints = new ArrayList<>();
        CompoundTag wrapper = buf.readNbt();
        if (wrapper != null) {
            ListTag list = wrapper.getList("list", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                waypoints.add(WaypointData.fromNBT(list.getCompound(i)));
            }
        }
        return new WaypointSyncPacket(waypoints);
    }

    public static void handle(WaypointSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                WaypointClientCache.set(packet.waypoints)
            )
        );
        ctx.get().setPacketHandled(true);
    }
}
