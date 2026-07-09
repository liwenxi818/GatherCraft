package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.quest.QuestManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class QuestClaimPacket {

    public final int index;

    public QuestClaimPacket(int index) {
        this.index = index;
    }

    public static void encode(QuestClaimPacket p, FriendlyByteBuf buf) {
        buf.writeInt(p.index);
    }

    public static QuestClaimPacket decode(FriendlyByteBuf buf) {
        return new QuestClaimPacket(buf.readInt());
    }

    public static void handle(QuestClaimPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            QuestManager.claim(sp, packet.index);
        });
        ctx.get().setPacketHandled(true);
    }
}
