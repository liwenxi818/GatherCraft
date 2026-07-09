package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.title.TitleNameTagCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TitleBroadcastPacket {

    public final UUID playerUUID;
    public final String equippedTitleId;

    public TitleBroadcastPacket(UUID playerUUID, String equippedTitleId) {
        this.playerUUID = playerUUID;
        this.equippedTitleId = equippedTitleId;
    }

    public static void encode(TitleBroadcastPacket p, FriendlyByteBuf buf) {
        buf.writeUUID(p.playerUUID);
        buf.writeUtf(p.equippedTitleId, 32);
    }

    public static TitleBroadcastPacket decode(FriendlyByteBuf buf) {
        return new TitleBroadcastPacket(buf.readUUID(), buf.readUtf(32));
    }

    public static void handle(TitleBroadcastPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                if (packet.equippedTitleId.isEmpty()) {
                    TitleNameTagCache.remove(packet.playerUUID);
                } else {
                    TitleNameTagCache.set(packet.playerUUID, packet.equippedTitleId);
                }
            })
        );
        ctx.get().setPacketHandled(true);
    }
}
