package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.achievement.AchievementManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AchievementClaimPacket {

    public final String achievementId;

    public AchievementClaimPacket(String achievementId) {
        this.achievementId = achievementId;
    }

    public static void encode(AchievementClaimPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.achievementId, 32);
    }

    public static AchievementClaimPacket decode(FriendlyByteBuf buf) {
        return new AchievementClaimPacket(buf.readUtf(32));
    }

    public static void handle(AchievementClaimPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            AchievementManager.claim(sp, packet.achievementId);
        });
        ctx.get().setPacketHandled(true);
    }
}
