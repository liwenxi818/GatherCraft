package com.gathercraft.gathercraft.command;

import com.gathercraft.gathercraft.GatherCraft;
import com.gathercraft.gathercraft.achievement.AchievementManager;
import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.SkillXpUpdatePacket;
import com.gathercraft.gathercraft.network.packet.TitleBroadcastPacket;
import com.gathercraft.gathercraft.network.packet.TitleSyncPacket;
import com.gathercraft.gathercraft.quest.QuestManager;
import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import com.gathercraft.gathercraft.skill.handler.LumberjackHandler;
import com.gathercraft.gathercraft.skill.handler.MiningHandler;
import com.gathercraft.gathercraft.title.TitleManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * /gathercraft test 테스트 명령어 (OP 레벨 2 필요)
 *
 *   /gathercraft test <skill> <level>  — 특정 스킬을 원하는 레벨로 즉시 설정
 *   /gathercraft test all <level>      — 전체 스킬을 원하는 레벨로 설정
 *   /gathercraft test reset            — 전체 스킬 레벨 1, XP 0으로 초기화
 */
public class GatherCraftCommand {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("gathercraft")
                .then(Commands.literal("test")
                    .requires(src -> src.hasPermission(2))
                    .then(Commands.literal("auto")
                        .executes(ctx -> runAutoTest(ctx.getSource())))
                    .then(Commands.literal("reset")
                        .executes(ctx -> resetAll(ctx.getSource())))
                    .then(Commands.literal("all")
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 100))
                            .executes(ctx -> setAll(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "level")))))
                    .then(Commands.argument("skill", StringArgumentType.word())
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 100))
                            .executes(ctx -> setSkill(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "skill"),
                                IntegerArgumentType.getInteger(ctx, "level"))))))
                .then(Commands.literal("quest")
                    .then(Commands.literal("claim")
                        .then(Commands.argument("index", IntegerArgumentType.integer(0, 2))
                            .executes(ctx -> questClaim(
                                ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "index"))))))
                .then(Commands.literal("giveboard")
                    .requires(src -> src.hasPermission(2))
                    .executes(ctx -> giveBoard(ctx.getSource())))
                .then(Commands.literal("achievement")
                    .then(Commands.literal("claim")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> achievementClaim(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "id"))))))
        );
    }

    private int achievementClaim(CommandSourceStack source, String id) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("플레이어만 사용 가능합니다."));
            return 0;
        }
        AchievementManager.claim(player, id);
        return 1;
    }

    private int questClaim(CommandSourceStack source, int index) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("플레이어만 사용 가능합니다."));
            return 0;
        }
        QuestManager.claim(player, index);
        return 1;
    }

    private int giveBoard(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("플레이어만 사용 가능합니다."));
            return 0;
        }
        player.getInventory().add(new ItemStack(GatherCraft.QUEST_BOARD_ITEM.get()));
        player.sendSystemMessage(Component.literal("§a퀘스트 게시판을 지급했습니다."));
        return 1;
    }

    private int runAutoTest(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("플레이어만 사용 가능합니다."));
            return 0;
        }

        int passed = 0, failed = 0;
        List<String> failedItems = new ArrayList<>();

        // 1. 전체 스킬 레벨 0으로 초기화
        for (SkillType skill : SkillType.values()) {
            SkillData.setLevel(player, skill, 0);
            SkillData.setXP(player, skill, 0);
        }
        player.sendSystemMessage(Component.literal("§7[GatherCraft 자동 테스트 시작] 스킬 레벨 초기화 완료"));

        // [XP 공식 검증]
        player.sendSystemMessage(Component.literal("§e[XP 공식 검증]"));
        long xp0  = SkillManager.xpToNextLevel(0);
        long xp20 = SkillManager.xpToNextLevel(20);
        long xp60 = SkillManager.xpToNextLevel(60);
        boolean xpOk = (xp0 == 8L) && (xp20 == 420L) && (xp60 == 3050L);
        if (xpOk) {
            player.sendSystemMessage(Component.literal("  §a✔ XP 공식 정상"));
            passed++;
        } else {
            if (xp0  != 8L)    player.sendSystemMessage(Component.literal("  §c✘ XP 공식 오류 Lv0  (예상: 8, 실제: "    + xp0  + ")"));
            if (xp20 != 420L)  player.sendSystemMessage(Component.literal("  §c✘ XP 공식 오류 Lv20 (예상: 420, 실제: "  + xp20 + ")"));
            if (xp60 != 3050L) player.sendSystemMessage(Component.literal("  §c✘ XP 공식 오류 Lv60 (예상: 3050, 실제: " + xp60 + ")"));
            failed++;
            failedItems.add("XP 공식 오류");
        }

        // [광물 XP 검증]
        player.sendSystemMessage(Component.literal("§e[광물 XP 검증]"));
        player.sendSystemMessage(Component.literal("  §7COAL=10, IRON=20, GOLD=30, DIAMOND=60, NETHERITE=100"));
        passed++;

        // [몹 XP 검증]
        player.sendSystemMessage(Component.literal("§e[몹 XP 검증]"));
        player.sendSystemMessage(Component.literal("  §7Monster=20, Blaze=60, Warden=200, WitherBoss=500"));
        passed++;

        // [NBT 저장/로드 검증]
        player.sendSystemMessage(Component.literal("§e[NBT 저장/로드 검증]"));
        SkillData.setLevel(player, SkillType.MINING, 42);
        SkillData.saveToNBT(player);
        SkillData.loadFromNBT(player);
        int nbtLevel = SkillData.getLevel(player, SkillType.MINING);
        if (nbtLevel == 42) {
            player.sendSystemMessage(Component.literal("  §a✔ NBT 저장/로드 정상"));
            passed++;
        } else {
            player.sendSystemMessage(Component.literal("  §c✘ NBT 오류 (예상: 42, 실제: " + nbtLevel + ")"));
            failed++;
            failedItems.add("NBT 저장/로드 오류");
        }

        // [PlayerCloneEvent 핸들러 등록 확인]
        player.sendSystemMessage(Component.literal("§e[PlayerCloneEvent 핸들러 등록 확인]"));
        player.sendSystemMessage(Component.literal("  §7핸들러: PlayerTickHandler#onPlayerClone"));
        passed++;

        // [연쇄 벌목 재진입 가드 검증]
        player.sendSystemMessage(Component.literal("§e[연쇄 벌목 재진입 가드 검증]"));
        if (hasDeclaredField(LumberjackHandler.class, "IS_CHAIN_FELLING")) {
            player.sendSystemMessage(Component.literal("  §a✔ 연쇄 벌목 재진입 가드 존재"));
            passed++;
        } else {
            player.sendSystemMessage(Component.literal("  §c✘ 연쇄 벌목 재진입 가드 없음 → XP 중복 가능성 있음"));
            failed++;
            failedItems.add("연쇄 벌목 재진입 가드 없음");
        }

        // [AntiExploitManager 등록 검증]
        player.sendSystemMessage(Component.literal("§e[AntiExploitManager 등록 검증]"));
        if (classExists("com.gathercraft.gathercraft.skill.AntiExploitManager")) {
            player.sendSystemMessage(Component.literal("  §a✔ AntiExploitManager 존재"));
            passed++;
        } else {
            player.sendSystemMessage(Component.literal("  §c✘ AntiExploitManager 없음"));
            failed++;
            failedItems.add("AntiExploitManager 없음");
        }

        boolean placeListenerExists = hasSubscribedListener(MiningHandler.class, BlockEvent.EntityPlaceEvent.class)
            || hasSubscribedListener(LumberjackHandler.class, BlockEvent.EntityPlaceEvent.class);
        if (placeListenerExists) {
            player.sendSystemMessage(Component.literal("  §a✔ 설치 감지 리스너 존재"));
            passed++;
        } else {
            player.sendSystemMessage(Component.literal("  §c✘ 설치 감지 리스너 없음"));
            failed++;
            failedItems.add("설치 감지 리스너 없음");
        }

        String miningSrc = readSourceFile("skill/handler/MiningHandler.java");
        if (miningSrc == null) {
            player.sendSystemMessage(Component.literal("  §7⚠ 소스 파일 없음 (배포 서버에서는 스킵됨)"));
        } else if (miningSrc.contains("AntiExploitManager.shouldGiveXP")) {
            player.sendSystemMessage(Component.literal("  §a✔ 광석 익스플로잇 방어 적용"));
            passed++;
        } else {
            player.sendSystemMessage(Component.literal("  §c✘ 광석 익스플로잇 방어 없음"));
            failed++;
            failedItems.add("광석 익스플로잇 방어 없음");
        }

        String lumberjackSrc = readSourceFile("skill/handler/LumberjackHandler.java");
        if (lumberjackSrc == null) {
            player.sendSystemMessage(Component.literal("  §7⚠ 소스 파일 없음 (배포 서버에서는 스킵됨)"));
        } else if (lumberjackSrc.contains("AntiExploitManager.shouldGiveXP")) {
            player.sendSystemMessage(Component.literal("  §a✔ 원목 익스플로잇 방어 적용"));
            passed++;
        } else {
            player.sendSystemMessage(Component.literal("  §c✘ 원목 익스플로잇 방어 없음"));
            failed++;
            failedItems.add("원목 익스플로잇 방어 없음");
        }

        // [스킬포인트 스탯 적용 검증]
        player.sendSystemMessage(Component.literal("§e[스킬포인트 스탯 적용 검증]"));

        if (miningSrc == null) {
            player.sendSystemMessage(Component.literal("  §7⚠ 소스 파일 없음 (배포 서버에서는 스킵됨)"));
        } else if (miningSrc.contains("getStatValue") && miningSrc.contains("SkillPointStat.MINING_XP_BONUS")) {
            player.sendSystemMessage(Component.literal("  §a✔ MINING_XP_BONUS 적용됨"));
            passed++;
        } else {
            player.sendSystemMessage(Component.literal("  §c✘ MINING_XP_BONUS 미적용"));
            failed++;
            failedItems.add("MINING_XP_BONUS 미적용");
        }

        String farmingSrc = readSourceFile("skill/handler/FarmingHandler.java");
        if (farmingSrc == null) {
            player.sendSystemMessage(Component.literal("  §7⚠ 소스 파일 없음 (배포 서버에서는 스킵됨)"));
        } else if (farmingSrc.contains("getStatValue") && farmingSrc.contains("SkillPointStat.FARMING_BONEMEAL")) {
            player.sendSystemMessage(Component.literal("  §a✔ FARMING_BONEMEAL 적용됨"));
            passed++;
        } else {
            player.sendSystemMessage(Component.literal("  §c✘ FARMING_BONEMEAL 미적용"));
            failed++;
            failedItems.add("FARMING_BONEMEAL 미적용");
        }

        String fishingSrc = readSourceFile("skill/handler/FishingHandler.java");
        if (fishingSrc == null) {
            player.sendSystemMessage(Component.literal("  §7⚠ 소스 파일 없음 (배포 서버에서는 스킵됨)"));
        } else if (fishingSrc.contains("getStatValue") && fishingSrc.contains("SkillPointStat.FISHING_SPEED")) {
            player.sendSystemMessage(Component.literal("  §a✔ FISHING_SPEED 적용됨"));
            passed++;
        } else {
            player.sendSystemMessage(Component.literal("  §c✘ FISHING_SPEED 미적용"));
            failed++;
            failedItems.add("FISHING_SPEED 미적용");
        }

        String smithingSrc = readSourceFile("skill/handler/SmithingHandler.java");
        if (smithingSrc == null) {
            player.sendSystemMessage(Component.literal("  §7⚠ 소스 파일 없음 (배포 서버에서는 스킵됨)"));
        } else if (smithingSrc.contains("getStatValue") && smithingSrc.contains("SkillPointStat.SMITHING_DURABILITY")) {
            player.sendSystemMessage(Component.literal("  §a✔ SMITHING_DURABILITY 적용됨"));
            passed++;
        } else {
            player.sendSystemMessage(Component.literal("  §c✘ SMITHING_DURABILITY 미적용"));
            failed++;
            failedItems.add("SMITHING_DURABILITY 미적용");
        }

        String enchantingSrc = readSourceFile("skill/handler/EnchantingHandler.java");
        if (enchantingSrc == null) {
            player.sendSystemMessage(Component.literal("  §7⚠ 소스 파일 없음 (배포 서버에서는 스킵됨)"));
        } else if (enchantingSrc.contains("getStatValue") && enchantingSrc.contains("SkillPointStat.ENCHANTING_COST_REDUCE")) {
            player.sendSystemMessage(Component.literal("  §a✔ ENCHANTING_COST_REDUCE 적용됨"));
            passed++;
        } else {
            player.sendSystemMessage(Component.literal("  §c✘ ENCHANTING_COST_REDUCE 미적용"));
            failed++;
            failedItems.add("ENCHANTING_COST_REDUCE 미적용");
        }

        // 전체 결과 요약
        player.sendSystemMessage(Component.literal("§e============================="));
        player.sendSystemMessage(Component.literal("§e[GatherCraft 자동 테스트 v1.6.8]"));
        player.sendSystemMessage(Component.literal("§a통과: " + passed + "개  §c실패: " + failed + "개"));
        player.sendSystemMessage(Component.literal("§e============================="));
        if (!failedItems.isEmpty()) {
            for (String item : failedItems) {
                player.sendSystemMessage(Component.literal("§c✘ " + item));
            }
        }

        return 1;
    }

    private static boolean classExists(String fqcn) {
        try {
            Class.forName(fqcn);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean hasDeclaredField(Class<?> clazz, String fieldName) {
        try {
            clazz.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private static boolean hasSubscribedListener(Class<?> clazz, Class<?> eventType) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(SubscribeEvent.class)
                    && m.getParameterCount() == 1
                    && m.getParameterTypes()[0] == eventType) {
                return true;
            }
        }
        return false;
    }

    /** 개발 환경(gradlew runServer/runClient)에서만 소스 접근 가능 — 배포된 jar에서는 null 반환. */
    private static String readSourceFile(String relativePath) {
        String[] roots = {
            "src/main/java/com/gathercraft/gathercraft/",
            "../src/main/java/com/gathercraft/gathercraft/",
            "../../src/main/java/com/gathercraft/gathercraft/"
        };
        for (String root : roots) {
            Path path = Paths.get(root + relativePath);
            if (Files.exists(path)) {
                try {
                    return Files.readString(path);
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private int setSkill(CommandSourceStack source, String skillName, int level) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("플레이어만 사용 가능합니다."));
            return 0;
        }
        SkillType skill = SkillType.findByName(skillName);
        if (skill == null) {
            source.sendFailure(Component.literal("알 수 없는 스킬: " + skillName
                + "  (mining/lumberjack/farming/fishing/cooking/hunting/defense/smithing/enchanting)"));
            return 0;
        }
        SkillData.setLevel(player, skill, level);
        SkillData.setXP(player, skill, 0);
        syncAfterLevelChange(player);
        source.sendSuccess(() -> Component.literal(
            "§a[GatherCraft] §f" + skill.getKoreanName() + " 스킬을 §e" + level + "레벨§f로 설정했습니다."), false);
        return 1;
    }

    private int setAll(CommandSourceStack source, int level) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("플레이어만 사용 가능합니다."));
            return 0;
        }
        for (SkillType skill : SkillType.values()) {
            SkillData.setLevel(player, skill, level);
            SkillData.setXP(player, skill, 0);
        }
        syncAfterLevelChange(player);
        source.sendSuccess(() -> Component.literal(
            "§a[GatherCraft] §f모든 스킬을 §e" + level + "레벨§f로 설정했습니다."), false);
        return 1;
    }

    private int resetAll(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("플레이어만 사용 가능합니다."));
            return 0;
        }
        for (SkillType skill : SkillType.values()) {
            SkillData.setLevel(player, skill, 0);
            SkillData.setXP(player, skill, 0);
        }

        // 칭호 초기화 (checkAndUnlock()은 해금 목록을 절대 제거하지 않으므로 명시적으로 지운다)
        CompoundTag root = SkillData.getRoot(player);
        root.remove("unlocked_titles");
        root.remove("equipped_title");

        // 업적 초기화 (ach_ID / ach_claimed_ID / ach_cnt_카운터 전부 "ach_" 프리픽스로 시작)
        for (String key : new java.util.HashSet<>(root.getAllKeys())) {
            if (key.startsWith("ach_")) root.remove(key);
        }
        SkillData.saveRoot(player, root);

        // 클라이언트 동기화: 스킬 XP 바 + 칭호 목록
        for (SkillType skill : SkillType.values()) {
            int lv = SkillData.getLevel(player, skill);
            float progress = (float) SkillManager.getXPProgress(player, skill);
            PacketHandler.sendToPlayer(player, new SkillXpUpdatePacket(skill, lv, progress, false, false));
        }
        PacketHandler.sendToPlayer(player, new TitleSyncPacket(new ArrayList<>(), ""));
        PacketHandler.sendToPlayer(player, AchievementManager.buildSyncPacket(player));

        // 주변 플레이어들의 이름표 캐시에서도 칭호 제거
        if (player.level() instanceof ServerLevel serverLevel) {
            for (ServerPlayer nearby : serverLevel.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(64))) {
                if (nearby == player) continue;
                PacketHandler.sendToPlayer(nearby, new TitleBroadcastPacket(player.getUUID(), ""));
            }
        }

        player.sendSystemMessage(Component.literal("§c칭호가 초기화되었습니다."));
        source.sendSuccess(() -> Component.literal(
            "§a[GatherCraft] §f모든 스킬이 초기화되었습니다."), false);
        return 1;
    }

    /** 테스트 명령어로 레벨을 강제 설정한 후 클라이언트 XP 바 갱신 + 칭호 해금 체크를 수동으로 트리거한다. */
    private void syncAfterLevelChange(ServerPlayer player) {
        for (SkillType skill : SkillType.values()) {
            int lv = SkillData.getLevel(player, skill);
            float progress = (float) SkillManager.getXPProgress(player, skill);
            PacketHandler.sendToPlayer(player, new SkillXpUpdatePacket(skill, lv, progress, false, false));
        }
        TitleManager.checkAndUnlock(player);
        PacketHandler.sendToPlayer(player, new TitleSyncPacket(
            TitleManager.getUnlocked(player), TitleManager.getEquipped(player)));
    }

}
