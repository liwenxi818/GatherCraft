package com.gathercraft.gathercraft.network;

import com.gathercraft.gathercraft.GatherCraft;
import com.gathercraft.gathercraft.network.packet.DamageTextPacket;
import com.gathercraft.gathercraft.network.packet.DashRequestPacket;
import com.gathercraft.gathercraft.network.packet.DashSyncPacket;
import com.gathercraft.gathercraft.network.packet.ScreenFlashPacket;
import com.gathercraft.gathercraft.network.packet.SkillPointChoicePacket;
import com.gathercraft.gathercraft.network.packet.SkillPointOfferPacket;
import com.gathercraft.gathercraft.network.packet.SkillXpUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(GatherCraft.MOD_ID, "main"),
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    public static void register() {
        CHANNEL.messageBuilder(ScreenFlashPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(ScreenFlashPacket::encode)
            .decoder(ScreenFlashPacket::decode)
            .consumerMainThread(ScreenFlashPacket::handle)
            .add();

        CHANNEL.messageBuilder(DashRequestPacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
            .encoder(DashRequestPacket::encode)
            .decoder(DashRequestPacket::decode)
            .consumerMainThread(DashRequestPacket::handle)
            .add();

        CHANNEL.messageBuilder(DashSyncPacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(DashSyncPacket::encode)
            .decoder(DashSyncPacket::decode)
            .consumerMainThread(DashSyncPacket::handle)
            .add();

        CHANNEL.messageBuilder(SkillXpUpdatePacket.class, 3, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(SkillXpUpdatePacket::encode)
            .decoder(SkillXpUpdatePacket::decode)
            .consumerMainThread(SkillXpUpdatePacket::handle)
            .add();

        CHANNEL.messageBuilder(SkillPointOfferPacket.class, 4, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(SkillPointOfferPacket::encode)
            .decoder(SkillPointOfferPacket::decode)
            .consumerMainThread(SkillPointOfferPacket::handle)
            .add();

        CHANNEL.messageBuilder(SkillPointChoicePacket.class, 5, NetworkDirection.PLAY_TO_SERVER)
            .encoder(SkillPointChoicePacket::encode)
            .decoder(SkillPointChoicePacket::decode)
            .consumerMainThread(SkillPointChoicePacket::handle)
            .add();

        CHANNEL.messageBuilder(DamageTextPacket.class, 6, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(DamageTextPacket::encode)
            .decoder(DamageTextPacket::decode)
            .consumerMainThread(DamageTextPacket::handle)
            .add();
    }

    /** S2C: 특정 플레이어에게 패킷을 전송한다. */
    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    /** C2S: 서버로 패킷을 전송한다. */
    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
