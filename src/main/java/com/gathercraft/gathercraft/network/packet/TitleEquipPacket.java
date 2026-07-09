package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.title.TitleManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TitleEquipPacket {

    public final String titleId;

    public TitleEquipPacket(String titleId) {
        this.titleId = titleId;
    }

    public static void encode(TitleEquipPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.titleId, 32);
    }

    public static TitleEquipPacket decode(FriendlyByteBuf buf) {
        return new TitleEquipPacket(buf.readUtf(32));
    }

    public static void handle(TitleEquipPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;
            TitleManager.equip(sp, packet.titleId);
            PacketHandler.sendToPlayer(sp, new TitleSyncPacket(TitleManager.getUnlocked(sp), TitleManager.getEquipped(sp)));

            // 주변 64블록 플레이어들에게 착용 칭호 브로드캐스트 (이름표 표시용)
            String equippedId = TitleManager.getEquipped(sp);
            if (sp.level() instanceof ServerLevel serverLevel) {
                for (ServerPlayer nearby : serverLevel.getEntitiesOfClass(ServerPlayer.class, sp.getBoundingBox().inflate(64))) {
                    PacketHandler.sendToPlayer(nearby, new TitleBroadcastPacket(sp.getUUID(), equippedId));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
