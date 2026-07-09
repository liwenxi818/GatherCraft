package com.gathercraft.gathercraft.waypoint;

import com.gathercraft.gathercraft.particle.ParticleUtil;
import com.gathercraft.gathercraft.skill.SkillData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WaypointManager {

    public static final int MAX_WAYPOINTS = 10;
    private static final String KEY = "waypoints";

    public static List<WaypointData> getAll(Player player) {
        CompoundTag root = SkillData.getRoot(player);
        ListTag list = root.getList(KEY, Tag.TAG_COMPOUND);
        List<WaypointData> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(WaypointData.fromNBT(list.getCompound(i)));
        }
        return result;
    }

    /** 이미 10개가 저장되어 있으면 false를 반환한다. */
    public static boolean add(Player player, WaypointData data) {
        CompoundTag root = SkillData.getRoot(player);
        ListTag list = root.getList(KEY, Tag.TAG_COMPOUND);
        if (list.size() >= MAX_WAYPOINTS) return false;
        list.add(data.toNBT());
        root.put(KEY, list);
        SkillData.saveRoot(player, root);
        return true;
    }

    /** index는 클라이언트가 보낸 값이라 신뢰할 수 없으므로 범위 밖이면 조용히 무시한다. */
    public static void delete(Player player, int index) {
        CompoundTag root = SkillData.getRoot(player);
        ListTag list = root.getList(KEY, Tag.TAG_COMPOUND);
        if (index < 0 || index >= list.size()) return;
        list.remove(index);
        root.put(KEY, list);
        SkillData.saveRoot(player, root);
    }

    public static void teleport(ServerPlayer player, int index) {
        List<WaypointData> waypoints = getAll(player);
        if (index < 0 || index >= waypoints.size()) return;
        WaypointData wp = waypoints.get(index);

        MinecraftServer server = player.getServer();
        if (server == null) return;

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(wp.dimension));
        ServerLevel targetLevel = server.getLevel(dimKey);
        if (targetLevel == null) {
            player.sendSystemMessage(Component.literal("§c해당 차원에 접근할 수 없습니다."));
            return;
        }

        boolean sameDimension = targetLevel == player.serverLevel();
        if (sameDimension) {
            player.teleportTo(wp.x, wp.y, wp.z);
        } else {
            player.teleportTo(targetLevel, wp.x, wp.y, wp.z, Set.of(), wp.yaw, 0f);
        }

        ParticleUtil.spawnCircle(targetLevel, wp.x, wp.y, wp.z, ParticleTypes.PORTAL, 0.8, 24, 1.0);

        player.sendSystemMessage(Component.literal(
            "§a[웨이포인트] §f'" + wp.name + "'(으)로 이동했습니다. §7("
                + dimensionDisplayName(wp.dimension) + " "
                + (int) wp.x + " " + (int) wp.y + " " + (int) wp.z + ")"
        ));
    }

    /** 채팅 메시지 전용 차원명 헬퍼 (클라이언트 UI 배지 매핑과는 별개). */
    private static String dimensionDisplayName(String dim) {
        return switch (dim) {
            case "minecraft:overworld" -> "오버월드";
            case "minecraft:the_nether" -> "네더";
            case "minecraft:the_end" -> "엔드";
            default -> dim;
        };
    }
}
