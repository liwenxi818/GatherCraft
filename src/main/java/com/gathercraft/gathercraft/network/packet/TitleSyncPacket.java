package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.title.TitleClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TitleSyncPacket {

    public final List<String> unlocked;
    public final String equipped;

    public TitleSyncPacket(List<String> unlocked, String equipped) {
        this.unlocked = unlocked;
        this.equipped = equipped;
    }

    public static void encode(TitleSyncPacket p, FriendlyByteBuf buf) {
        buf.writeVarInt(p.unlocked.size());
        for (String id : p.unlocked) {
            buf.writeUtf(id, 32);
        }
        buf.writeUtf(p.equipped, 32);
    }

    public static TitleSyncPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<String> unlocked = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            unlocked.add(buf.readUtf(32));
        }
        String equipped = buf.readUtf(32);
        return new TitleSyncPacket(unlocked, equipped);
    }

    public static void handle(TitleSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                TitleClientCache.set(packet.unlocked, packet.equipped)
            )
        );
        ctx.get().setPacketHandled(true);
    }
}
