package com.gathercraft.gathercraft.title;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.TitleSyncPacket;
import com.gathercraft.gathercraft.particle.ParticleUtil;
import com.gathercraft.gathercraft.skill.SkillData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class TitleManager {

    private static final String UNLOCKED_KEY = "unlocked_titles";
    private static final String EQUIPPED_KEY = "equipped_title";

    public static List<String> getUnlocked(Player player) {
        CompoundTag root = SkillData.getRoot(player);
        ListTag list = root.getList(UNLOCKED_KEY, Tag.TAG_STRING);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(list.getString(i));
        }
        return result;
    }

    public static String getEquipped(Player player) {
        return SkillData.getRoot(player).getString(EQUIPPED_KEY);
    }

    /** id가 해금 목록에 없으면 무시한다. 이미 착용 중인 id를 다시 보내면 해제된다. */
    public static void equip(ServerPlayer player, String id) {
        CompoundTag root = SkillData.getRoot(player);
        ListTag list = root.getList(UNLOCKED_KEY, Tag.TAG_STRING);
        boolean owns = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(id)) {
                owns = true;
                break;
            }
        }
        if (!owns) return;

        String current = root.getString(EQUIPPED_KEY);
        root.putString(EQUIPPED_KEY, current.equals(id) ? "" : id);
        SkillData.saveRoot(player, root);
    }

    /** 조건 충족한 신규 칭호를 해금하고 채팅/파티클 연출 후 동기화 패킷을 전송한다. */
    public static void checkAndUnlock(ServerPlayer player) {
        CompoundTag root = SkillData.getRoot(player);
        ListTag list = root.getList(UNLOCKED_KEY, Tag.TAG_STRING);

        List<String> owned = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            owned.add(list.getString(i));
        }

        boolean changed = false;
        for (Title title : Title.values()) {
            if (owned.contains(title.id)) continue;
            if (!title.isUnlocked(player)) continue;

            list.add(StringTag.valueOf(title.id));
            changed = true;

            player.sendSystemMessage(Component.literal(
                "§6[칭호 해금] §f'" + title.displayName + "§f' 칭호를 획득했습니다!"
            ));
            if (player.level() instanceof ServerLevel serverLevel) {
                ParticleUtil.spawnCircle(serverLevel, player.getX(), player.getY(), player.getZ(),
                    ParticleTypes.TOTEM_OF_UNDYING, 1.2, 16, 0.5);
            }
        }

        if (changed) {
            root.put(UNLOCKED_KEY, list);
            SkillData.saveRoot(player, root);
            PacketHandler.sendToPlayer(player, new TitleSyncPacket(getUnlocked(player), getEquipped(player)));
        }
    }

    public static String getDisplayName(String id) {
        Title title = Title.byId(id);
        return title != null ? title.displayName : "";
    }

    public static String getConditionText(String id) {
        Title title = Title.byId(id);
        return title != null ? title.conditionText() : "";
    }

    /** 착용 여부와 무관하게 해금(보유)만 해도 적용되는 패시브 효과 판정용. */
    public static boolean hasTitle(Player player, String id) {
        return getUnlocked(player).contains(id);
    }

    /** 전 스킬 공통 XP 배율 (all_50/all_100 보유 시). 스킬별 보너스(miner_3 등)는 별도 처리. */
    public static float getXPMultiplier(Player player) {
        if (hasTitle(player, "all_100")) return 1.15f;
        if (hasTitle(player, "all_50")) return 1.05f;
        return 1.0f;
    }
}
