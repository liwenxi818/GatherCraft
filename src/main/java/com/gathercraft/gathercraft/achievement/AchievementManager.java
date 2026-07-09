package com.gathercraft.gathercraft.achievement;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.AchievementSyncPacket;
import com.gathercraft.gathercraft.particle.ParticleUtil;
import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 업적 15종 정의 및 해금/수령/카운터 관리. NBT는 SkillData.getRoot() 하위 "ach_*"에 저장. */
public class AchievementManager {

    public enum Category { MINING, HUNTING, LIVING, ALL }

    public record Achievement(String id, String displayName, String condition, SkillType relatedSkill,
                               long rewardXP, int rewardBottles, String counterKey, int counterGoal,
                               Category category) {}

    public static final List<Achievement> ALL = List.of(
        new Achievement("first_diamond", "§b첫 다이아", "다이아몬드 광석 첫 채굴",
            SkillType.MINING, 500, 1, "diamond", 1, Category.MINING),
        new Achievement("diamond_100", "§e다이아 수집가", "다이아몬드 100개 채굴",
            SkillType.MINING, 3000, 3, "diamond", 100, Category.MINING),
        new Achievement("ancient_10", "§6고대의 탐험가", "고대 잔해 10개 채굴",
            SkillType.MINING, 5000, 5, "ancient", 10, Category.MINING),
        new Achievement("mining_max", "§6§l광맥의 지배자", "채광 스킬 100레벨 달성",
            SkillType.MINING, 8000, 5, null, 0, Category.MINING),

        new Achievement("first_boss", "§c첫 보스 처치", "보스 몬스터 첫 처치",
            SkillType.HUNTING, 1000, 2, "boss", 1, Category.HUNTING),
        new Achievement("boss_10", "§4보스 슬레이어", "보스 몬스터 10마리 처치",
            SkillType.HUNTING, 8000, 5, "boss", 10, Category.HUNTING),
        new Achievement("mob_1000", "§c천인참", "몬스터 1000마리 처치",
            SkillType.HUNTING, 5000, 4, "mob", 1000, Category.HUNTING),
        new Achievement("hunting_max", "§4§l각성 사냥꾼", "사냥 스킬 100레벨 달성",
            SkillType.HUNTING, 8000, 5, null, 0, Category.HUNTING),

        new Achievement("fish_100", "§b낚시왕", "물고기 100마리 낚기",
            SkillType.FISHING, 2000, 3, "fish", 100, Category.LIVING),
        new Achievement("harvest_1000", "§a대농부", "작물 1000개 수확",
            SkillType.FARMING, 2000, 3, "harvest", 1000, Category.LIVING),
        new Achievement("cook_500", "§6미슐랭", "음식 500개 요리",
            SkillType.COOKING, 2000, 3, "cook", 500, Category.LIVING),
        new Achievement("log_500", "§2벌목왕", "원목 500개 채굴",
            SkillType.LUMBERJACK, 2000, 3, "log", 500, Category.LIVING),

        new Achievement("all_skill_30", "§e성장하는 모험가", "모든 스킬 30레벨 달성",
            null, 3000, 3, null, 0, Category.ALL),
        new Achievement("all_skill_50", "§6숙련된 모험가", "모든 스킬 50레벨 달성",
            null, 6000, 4, null, 0, Category.ALL),
        new Achievement("all_skill_100", "§b§l각성왕", "모든 스킬 100레벨 달성",
            null, 20000, 5, null, 0, Category.ALL)
    );

    private static final Map<String, Achievement> BY_ID =
        ALL.stream().collect(Collectors.toMap(Achievement::id, a -> a));

    public static Achievement byId(String id) {
        return BY_ID.get(id);
    }

    public static boolean has(Player player, String id) {
        return SkillData.getRoot(player).getBoolean("ach_" + id);
    }

    public static boolean isClaimed(Player player, String id) {
        return SkillData.getRoot(player).getBoolean("ach_claimed_" + id);
    }

    public static String getCondition(String id) {
        Achievement ach = BY_ID.get(id);
        return ach != null ? ach.condition() : "";
    }

    public static int getGoal(String id) {
        Achievement ach = BY_ID.get(id);
        return (ach != null && ach.counterKey() != null) ? ach.counterGoal() : 1;
    }

    public static String getCounterKey(String id) {
        Achievement ach = BY_ID.get(id);
        return ach != null ? ach.counterKey() : null;
    }

    /** "[스킬명] XP +n §7/ 경험치 병 n개" 형태의 보상 설명 텍스트를 반환한다. */
    public static String getRewardText(String id) {
        Achievement ach = BY_ID.get(id);
        if (ach == null) return "";
        String skillPart = ach.relatedSkill() != null
            ? ach.relatedSkill().getKoreanName() + " XP +" + ach.rewardXP()
            : "전 스킬 균등분배 XP +" + ach.rewardXP();
        return skillPart + " §7/ 경험치 병 " + ach.rewardBottles() + "개";
    }

    /** 해금 기록 + 서버 공지 + 보상 수령 안내 채팅만 처리한다. 보상 지급은 claim()에서 별도로 처리한다. */
    public static void unlock(ServerPlayer player, String id) {
        if (has(player, id)) return;
        Achievement ach = BY_ID.get(id);
        if (ach == null) return;

        CompoundTag root = SkillData.getRoot(player);
        root.putBoolean("ach_" + id, true);
        SkillData.saveRoot(player, root);

        MinecraftServer server = player.getServer();
        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(Component.literal(
                "§6§l[업적] §e" + player.getName().getString() + " §f님이 §b" + ach.displayName()
                    + " §f달성!"), false);
        }

        MutableComponent msg = Component.literal("§a[업적] '" + ach.displayName() + "' §a달성! ");
        MutableComponent claimLink = Component.literal("§e[보상 수령] ←클릭")
            .withStyle(style -> style.withClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gathercraft achievement claim " + id)));
        msg.append(claimLink);
        player.sendSystemMessage(msg);

        PacketHandler.sendToPlayer(player, buildSyncPacket(player));
    }

    /** 해금된 업적의 보상을 수령한다 (XP + 경험치 병 + 파티클). */
    public static void claim(ServerPlayer player, String id) {
        if (!has(player, id)) {
            player.sendSystemMessage(Component.literal("§c달성하지 않은 업적입니다."));
            return;
        }
        if (isClaimed(player, id)) {
            player.sendSystemMessage(Component.literal("§c이미 보상을 수령했습니다."));
            return;
        }
        Achievement ach = BY_ID.get(id);
        if (ach == null) return;

        CompoundTag root = SkillData.getRoot(player);
        root.putBoolean("ach_claimed_" + id, true);
        SkillData.saveRoot(player, root);

        if (ach.relatedSkill() != null) {
            SkillManager.addXP(player, ach.relatedSkill(), ach.rewardXP());
        } else {
            long share = ach.rewardXP() / SkillType.values().length;
            for (SkillType skill : SkillType.values()) {
                SkillManager.addXP(player, skill, share);
            }
        }

        if (player.level() instanceof ServerLevel level) {
            ItemStack bottles = new ItemStack(Items.EXPERIENCE_BOTTLE, ach.rewardBottles());
            level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), bottles));
            ParticleUtil.spawnCircle(level, player.getX(), player.getY(), player.getZ(),
                ParticleTypes.TOTEM_OF_UNDYING, 2.0, 24, 0.5);
            ParticleUtil.spawnBurst(level, player.getX(), player.getY() + 1, player.getZ(),
                ParticleTypes.FIREWORK, 30, 0.5);
        }

        player.sendSystemMessage(Component.literal("§6[업적 보상] §f" + ach.displayName()));
        player.sendSystemMessage(Component.literal("§e" + getRewardText(id) + " §e지급!"));

        PacketHandler.sendToPlayer(player, buildSyncPacket(player));
    }

    /** counter 값을 amount만큼 증가시키고, checkIds 중 조건을 충족한 업적을 해금한다. */
    public static void incrementAndCheck(ServerPlayer player, String counter, int amount, String... checkIds) {
        CompoundTag root = SkillData.getRoot(player);
        String key = "ach_cnt_" + counter;
        int newValue = root.getInt(key) + amount;
        root.putInt(key, newValue);
        SkillData.saveRoot(player, root);

        for (String id : checkIds) {
            Achievement ach = BY_ID.get(id);
            if (ach == null || ach.counterKey() == null) continue;
            if (newValue >= ach.counterGoal()) {
                unlock(player, id);
            }
        }
        PacketHandler.sendToPlayer(player, buildSyncPacket(player));
    }

    public static void checkAllSkillLevel(ServerPlayer player) {
        checkAllAtLevel(player, 30, "all_skill_30");
        checkAllAtLevel(player, 50, "all_skill_50");
        checkAllAtLevel(player, 100, "all_skill_100");
    }

    private static void checkAllAtLevel(ServerPlayer player, int level, String id) {
        if (has(player, id)) return;
        for (SkillType skill : SkillType.values()) {
            if (SkillData.getLevel(player, skill) < level) return;
        }
        unlock(player, id);
    }

    public static int getCounter(Player player, String counter) {
        return SkillData.getRoot(player).getInt("ach_cnt_" + counter);
    }

    public static AchievementSyncPacket buildSyncPacket(Player player) {
        List<String> unlocked = new ArrayList<>();
        List<String> claimed = new ArrayList<>();
        for (Achievement ach : ALL) {
            if (has(player, ach.id())) unlocked.add(ach.id());
            if (isClaimed(player, ach.id())) claimed.add(ach.id());
        }
        Map<String, Integer> counters = new HashMap<>();
        for (Achievement ach : ALL) {
            if (ach.counterKey() != null) {
                counters.put(ach.counterKey(), getCounter(player, ach.counterKey()));
            }
        }
        return new AchievementSyncPacket(unlocked, claimed, counters);
    }
}
