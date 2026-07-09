package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.achievement.AchievementClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class AchievementSyncPacket {

    public final List<String> unlockedIds;
    public final List<String> claimed;
    public final Map<String, Integer> counters;

    public AchievementSyncPacket(List<String> unlockedIds, List<String> claimed, Map<String, Integer> counters) {
        this.unlockedIds = unlockedIds;
        this.claimed = claimed;
        this.counters = counters;
    }

    public static void encode(AchievementSyncPacket p, FriendlyByteBuf buf) {
        buf.writeVarInt(p.unlockedIds.size());
        for (String id : p.unlockedIds) {
            buf.writeUtf(id, 32);
        }
        buf.writeVarInt(p.claimed.size());
        for (String id : p.claimed) {
            buf.writeUtf(id, 32);
        }
        buf.writeVarInt(p.counters.size());
        for (Map.Entry<String, Integer> entry : p.counters.entrySet()) {
            buf.writeUtf(entry.getKey(), 32);
            buf.writeVarInt(entry.getValue());
        }
    }

    public static AchievementSyncPacket decode(FriendlyByteBuf buf) {
        int unlockedSize = buf.readVarInt();
        List<String> unlockedIds = new ArrayList<>();
        for (int i = 0; i < unlockedSize; i++) {
            unlockedIds.add(buf.readUtf(32));
        }
        int claimedSize = buf.readVarInt();
        List<String> claimed = new ArrayList<>();
        for (int i = 0; i < claimedSize; i++) {
            claimed.add(buf.readUtf(32));
        }
        int counterSize = buf.readVarInt();
        Map<String, Integer> counters = new HashMap<>();
        for (int i = 0; i < counterSize; i++) {
            String key = buf.readUtf(32);
            int value = buf.readVarInt();
            counters.put(key, value);
        }
        return new AchievementSyncPacket(unlockedIds, claimed, counters);
    }

    public static void handle(AchievementSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                AchievementClientCache.set(packet.unlockedIds, packet.claimed, packet.counters)
            )
        );
        ctx.get().setPacketHandled(true);
    }
}
