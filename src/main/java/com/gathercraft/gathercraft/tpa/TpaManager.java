package com.gathercraft.gathercraft.tpa;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.TpaAskPacket;
import com.gathercraft.gathercraft.particle.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 플레이어 간 텔포 요청/수락/거절. NBT 저장 없이 런타임 static Map만 사용한다.
 */
public class TpaManager {

    private static final long COOLDOWN_MS = 60_000L;
    private static final long REQUEST_TIMEOUT_MS = 60_000L;

    private record PendingRequest(UUID requesterUUID, long timestamp) {}

    /** key = 응답자(요청을 받은 사람) UUID */
    private static final Map<UUID, PendingRequest> pendingRequests = new HashMap<>();
    /** key = 요청자 UUID */
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    public static void request(ServerPlayer from, String targetName) {
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(from.getUUID());
        if (last != null && now - last < COOLDOWN_MS) {
            long remain = (COOLDOWN_MS - (now - last)) / 1000L + 1;
            from.sendSystemMessage(Component.literal("§c재요청까지 " + remain + "초 남았습니다."));
            return;
        }

        MinecraftServer server = from.getServer();
        if (server == null) return;
        ServerPlayer target = server.getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            from.sendSystemMessage(Component.literal("§c해당 플레이어가 온라인 상태가 아닙니다."));
            return;
        }
        if (target == from) {
            from.sendSystemMessage(Component.literal("§c자기 자신에게 텔포할 수 없습니다."));
            return;
        }

        cooldowns.put(from.getUUID(), now);
        pendingRequests.put(target.getUUID(), new PendingRequest(from.getUUID(), now));

        PacketHandler.sendToPlayer(target, new TpaAskPacket(from.getName().getString()));

        from.sendSystemMessage(Component.literal(
            "§a[TPA] §f'" + target.getName().getString() + "'님에게 텔포 요청을 보냈습니다."));
    }

    public static void respond(ServerPlayer responder, boolean accept) {
        PendingRequest req = pendingRequests.remove(responder.getUUID());
        if (req == null) {
            responder.sendSystemMessage(Component.literal("§c만료된 요청입니다."));
            return;
        }

        long now = System.currentTimeMillis();
        if (now - req.timestamp() > REQUEST_TIMEOUT_MS) {
            responder.sendSystemMessage(Component.literal("§c만료된 요청입니다."));
            return;
        }

        MinecraftServer server = responder.getServer();
        if (server == null) return;
        ServerPlayer requester = server.getPlayerList().getPlayer(req.requesterUUID());
        if (requester == null) {
            responder.sendSystemMessage(Component.literal("§c요청을 보낸 플레이어가 접속 중이 아닙니다."));
            return;
        }

        if (!accept) {
            requester.sendSystemMessage(Component.literal("§c[TPA] §f텔포 요청이 거절되었습니다."));
            return;
        }

        ServerLevel targetLevel = responder.serverLevel();
        if (targetLevel == requester.serverLevel()) {
            requester.teleportTo(responder.getX(), responder.getY(), responder.getZ());
        } else {
            requester.teleportTo(targetLevel, responder.getX(), responder.getY(), responder.getZ(),
                Set.of(), responder.getYRot(), responder.getXRot());
        }

        ParticleUtil.spawnCircle(targetLevel, responder.getX(), responder.getY(), responder.getZ(),
            ParticleTypes.PORTAL, 0.8, 24, 1.0);

        requester.sendSystemMessage(Component.literal("§a[TPA] 텔포가 승인되었습니다!"));
        responder.sendSystemMessage(Component.literal(
            "§a[TPA] '" + requester.getName().getString() + "'님이 텔포했습니다."));
    }

    /** 로그아웃 시 위생 정리: 남아있는 요청/쿨다운을 제거한다. */
    public static void clearPlayer(UUID uuid) {
        pendingRequests.remove(uuid);
        pendingRequests.entrySet().removeIf(e -> e.getValue().requesterUUID().equals(uuid));
        cooldowns.remove(uuid);
    }
}
