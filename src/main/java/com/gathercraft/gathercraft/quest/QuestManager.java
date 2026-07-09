package com.gathercraft.gathercraft.quest;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.QuestSyncPacket;
import com.gathercraft.gathercraft.particle.ParticleUtil;
import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** 일일 퀘스트 관리: 갱신/진행도 적립/보상 수령. NBT는 SkillData.getRoot() 하위에 저장. */
public class QuestManager {

    private static final String KEY_DATE = "quest_date";
    private static final String KEY_QUEST_PREFIX = "quest_";

    /** quest_date가 오늘과 다르면 자동 갱신 후 오늘의 퀘스트 3개를 반환한다. */
    public static List<QuestData> getQuests(Player player) {
        CompoundTag root = SkillData.getRoot(player);
        long today = todaySeed();
        if (root.getLong(KEY_DATE) != today) {
            if (player instanceof ServerPlayer sp) {
                refreshQuests(sp);
                root = SkillData.getRoot(player);
            }
        }

        List<QuestData> quests = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            CompoundTag tag = root.getCompound(KEY_QUEST_PREFIX + i);
            if (tag.contains("id")) {
                quests.add(QuestData.fromNBT(tag));
            }
        }
        return quests;
    }

    public static void refreshQuests(ServerPlayer player) {
        long today = todaySeed();
        List<QuestData> daily = QuestPool.getDailyQuests(today);

        CompoundTag root = SkillData.getRoot(player);
        for (int i = 0; i < daily.size(); i++) {
            root.put(KEY_QUEST_PREFIX + i, daily.get(i).toNBT());
        }
        root.putLong(KEY_DATE, today);
        SkillData.saveRoot(player, root);

        player.sendSystemMessage(Component.literal("§e[퀘스트] 오늘의 퀘스트가 갱신되었습니다!"));
    }

    /** actionType + target이 일치하는 미완료 퀘스트의 진행도를 올린다. 완료 시 클릭 가능한 채팅 메시지를 보낸다. */
    public static void progress(ServerPlayer player, String actionType, String target, int amount) {
        CompoundTag root = SkillData.getRoot(player);
        boolean changed = false;

        for (int i = 0; i < 3; i++) {
            String key = KEY_QUEST_PREFIX + i;
            CompoundTag tag = root.getCompound(key);
            if (!tag.contains("id")) continue;

            QuestData quest = QuestData.fromNBT(tag);
            if (quest.completed) continue;
            if (!quest.actionType.equals(actionType)) continue;
            if (!matchesTarget(quest.targetBlock, target)) continue;

            quest.progress = Math.min(quest.goal, quest.progress + amount);
            if (quest.progress >= quest.goal) {
                quest.completed = true;
                sendCompletionMessage(player, quest, i);
            }
            root.put(key, quest.toNBT());
            changed = true;
        }

        if (changed) {
            SkillData.saveRoot(player, root);
            PacketHandler.sendToPlayer(player, new QuestSyncPacket(getQuests(player)));
        }
    }

    /** 심층암 변종(DEEPSLATE_DIAMOND_ORE 등) 및 ANY 매칭을 모두 처리한다. */
    private static boolean matchesTarget(String targetBlock, String target) {
        if (targetBlock.equals("ANY")) return true;
        return target.contains(targetBlock) || targetBlock.contains(target);
    }

    private static void sendCompletionMessage(ServerPlayer player, QuestData quest, int index) {
        MutableComponent msg = Component.literal("§a[퀘스트 완료] '" + quest.description + "' 완료! ");
        MutableComponent claimLink = Component.literal("§e[보상 수령 클릭]")
            .withStyle(style -> style.withClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gathercraft quest claim " + index)));
        msg.append(claimLink);
        player.sendSystemMessage(msg);
    }

    public static void claim(ServerPlayer player, int index) {
        if (index < 0 || index >= 3) return;
        CompoundTag root = SkillData.getRoot(player);
        String key = KEY_QUEST_PREFIX + index;
        CompoundTag tag = root.getCompound(key);
        if (!tag.contains("id")) {
            player.sendSystemMessage(Component.literal("§c해당 퀘스트가 존재하지 않습니다."));
            return;
        }

        QuestData quest = QuestData.fromNBT(tag);
        if (!quest.completed) {
            player.sendSystemMessage(Component.literal("§c아직 완료하지 않은 퀘스트입니다."));
            return;
        }
        if (quest.claimed) {
            player.sendSystemMessage(Component.literal("§c이미 보상을 수령했습니다."));
            return;
        }

        quest.claimed = true;
        root.put(key, quest.toNBT());
        SkillData.saveRoot(player, root);

        SkillType skill = SkillType.valueOf(quest.skillType);
        SkillManager.addXP(player, skill, quest.rewardXP);

        if (player.level() instanceof ServerLevel level) {
            ItemStack bottles = new ItemStack(Items.EXPERIENCE_BOTTLE, quest.rewardExpBottles);
            ItemEntity entity = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), bottles);
            level.addFreshEntity(entity);
            ParticleUtil.spawnBurst(level, player.getX(), player.getY() + 1, player.getZ(),
                ParticleTypes.TOTEM_OF_UNDYING, 30, 0.5);
        }

        player.sendSystemMessage(Component.literal(
            "§6[보상] §f" + skill.getKoreanName() + " XP +" + quest.rewardXP
                + " + 경험치 병 " + quest.rewardExpBottles + "개!"));

        PacketHandler.sendToPlayer(player, new QuestSyncPacket(getQuests(player)));
    }

    private static long todaySeed() {
        return Long.parseLong(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
    }
}
