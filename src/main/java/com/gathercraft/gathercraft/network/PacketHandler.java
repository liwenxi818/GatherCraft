package com.gathercraft.gathercraft.network;

import com.gathercraft.gathercraft.GatherCraft;
import com.gathercraft.gathercraft.network.packet.AchievementClaimPacket;
import com.gathercraft.gathercraft.network.packet.AchievementSyncPacket;
import com.gathercraft.gathercraft.network.packet.DamageTextPacket;
import com.gathercraft.gathercraft.network.packet.DashRequestPacket;
import com.gathercraft.gathercraft.network.packet.DashSyncPacket;
import com.gathercraft.gathercraft.network.packet.OpenQuestBoardPacket;
import com.gathercraft.gathercraft.network.packet.QuestClaimPacket;
import com.gathercraft.gathercraft.network.packet.QuestSyncPacket;
import com.gathercraft.gathercraft.network.packet.ScreenFlashPacket;
import com.gathercraft.gathercraft.network.packet.SkillPointChoicePacket;
import com.gathercraft.gathercraft.network.packet.SkillPointOfferPacket;
import com.gathercraft.gathercraft.network.packet.SkillPointStatSyncPacket;
import com.gathercraft.gathercraft.network.packet.SkillXpUpdatePacket;
import com.gathercraft.gathercraft.network.packet.TitleBroadcastPacket;
import com.gathercraft.gathercraft.network.packet.TitleEquipPacket;
import com.gathercraft.gathercraft.network.packet.TitleSyncPacket;
import com.gathercraft.gathercraft.network.packet.TpaAskPacket;
import com.gathercraft.gathercraft.network.packet.TpaRequestPacket;
import com.gathercraft.gathercraft.network.packet.TpaResponsePacket;
import com.gathercraft.gathercraft.network.packet.WaypointDeletePacket;
import com.gathercraft.gathercraft.network.packet.WaypointSavePacket;
import com.gathercraft.gathercraft.network.packet.WaypointSyncPacket;
import com.gathercraft.gathercraft.network.packet.WaypointTeleportPacket;
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

        CHANNEL.messageBuilder(WaypointSavePacket.class, 7, NetworkDirection.PLAY_TO_SERVER)
            .encoder(WaypointSavePacket::encode)
            .decoder(WaypointSavePacket::decode)
            .consumerMainThread(WaypointSavePacket::handle)
            .add();

        CHANNEL.messageBuilder(WaypointDeletePacket.class, 8, NetworkDirection.PLAY_TO_SERVER)
            .encoder(WaypointDeletePacket::encode)
            .decoder(WaypointDeletePacket::decode)
            .consumerMainThread(WaypointDeletePacket::handle)
            .add();

        CHANNEL.messageBuilder(WaypointTeleportPacket.class, 9, NetworkDirection.PLAY_TO_SERVER)
            .encoder(WaypointTeleportPacket::encode)
            .decoder(WaypointTeleportPacket::decode)
            .consumerMainThread(WaypointTeleportPacket::handle)
            .add();

        CHANNEL.messageBuilder(WaypointSyncPacket.class, 10, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(WaypointSyncPacket::encode)
            .decoder(WaypointSyncPacket::decode)
            .consumerMainThread(WaypointSyncPacket::handle)
            .add();

        CHANNEL.messageBuilder(TitleSyncPacket.class, 11, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(TitleSyncPacket::encode)
            .decoder(TitleSyncPacket::decode)
            .consumerMainThread(TitleSyncPacket::handle)
            .add();

        CHANNEL.messageBuilder(TitleEquipPacket.class, 12, NetworkDirection.PLAY_TO_SERVER)
            .encoder(TitleEquipPacket::encode)
            .decoder(TitleEquipPacket::decode)
            .consumerMainThread(TitleEquipPacket::handle)
            .add();

        CHANNEL.messageBuilder(TpaRequestPacket.class, 13, NetworkDirection.PLAY_TO_SERVER)
            .encoder(TpaRequestPacket::encode)
            .decoder(TpaRequestPacket::decode)
            .consumerMainThread(TpaRequestPacket::handle)
            .add();

        CHANNEL.messageBuilder(TpaResponsePacket.class, 14, NetworkDirection.PLAY_TO_SERVER)
            .encoder(TpaResponsePacket::encode)
            .decoder(TpaResponsePacket::decode)
            .consumerMainThread(TpaResponsePacket::handle)
            .add();

        CHANNEL.messageBuilder(TpaAskPacket.class, 15, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(TpaAskPacket::encode)
            .decoder(TpaAskPacket::decode)
            .consumerMainThread(TpaAskPacket::handle)
            .add();

        CHANNEL.messageBuilder(TitleBroadcastPacket.class, 16, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(TitleBroadcastPacket::encode)
            .decoder(TitleBroadcastPacket::decode)
            .consumerMainThread(TitleBroadcastPacket::handle)
            .add();

        CHANNEL.messageBuilder(QuestSyncPacket.class, 17, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(QuestSyncPacket::encode)
            .decoder(QuestSyncPacket::decode)
            .consumerMainThread(QuestSyncPacket::handle)
            .add();

        CHANNEL.messageBuilder(OpenQuestBoardPacket.class, 18, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(OpenQuestBoardPacket::encode)
            .decoder(OpenQuestBoardPacket::decode)
            .consumerMainThread(OpenQuestBoardPacket::handle)
            .add();

        CHANNEL.messageBuilder(AchievementSyncPacket.class, 19, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(AchievementSyncPacket::encode)
            .decoder(AchievementSyncPacket::decode)
            .consumerMainThread(AchievementSyncPacket::handle)
            .add();

        CHANNEL.messageBuilder(QuestClaimPacket.class, 20, NetworkDirection.PLAY_TO_SERVER)
            .encoder(QuestClaimPacket::encode)
            .decoder(QuestClaimPacket::decode)
            .consumerMainThread(QuestClaimPacket::handle)
            .add();

        CHANNEL.messageBuilder(AchievementClaimPacket.class, 21, NetworkDirection.PLAY_TO_SERVER)
            .encoder(AchievementClaimPacket::encode)
            .decoder(AchievementClaimPacket::decode)
            .consumerMainThread(AchievementClaimPacket::handle)
            .add();

        CHANNEL.messageBuilder(SkillPointStatSyncPacket.class, 22, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(SkillPointStatSyncPacket::encode)
            .decoder(SkillPointStatSyncPacket::decode)
            .consumerMainThread(SkillPointStatSyncPacket::handle)
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
