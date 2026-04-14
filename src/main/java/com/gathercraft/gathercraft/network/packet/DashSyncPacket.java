package com.gathercraft.gathercraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DashSyncPacket {

    private final long cooldownTicks;

    public DashSyncPacket(long cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public static void encode(DashSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.cooldownTicks);
    }

    public static DashSyncPacket decode(FriendlyByteBuf buf) {
        return new DashSyncPacket(buf.readLong());
    }

    public static void handle(DashSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.gathercraft.gathercraft.client.overlay.SkillBarOverlay.onDashActivated(packet.cooldownTicks)
            )
        );
        ctx.get().setPacketHandled(true);
    }
}
