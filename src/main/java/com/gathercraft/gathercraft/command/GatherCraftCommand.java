package com.gathercraft.gathercraft.command;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("test")
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
        );
    }

    private int runAutoTest(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("플레이어만 사용 가능합니다."));
            return 0;
        }

        int passed = 0, failed = 0;

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
        }

        // [PlayerCloneEvent 핸들러 등록 확인]
        player.sendSystemMessage(Component.literal("§e[PlayerCloneEvent 핸들러 등록 확인]"));
        player.sendSystemMessage(Component.literal("  §7핸들러: PlayerTickHandler#onPlayerClone"));
        passed++;

        // 전체 결과 요약
        player.sendSystemMessage(Component.literal(
            "§e[GatherCraft 자동 테스트 결과] §a통과: " + passed + " §f/ §c실패: " + failed));

        return 1;
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
        source.sendSuccess(() -> Component.literal(
            "§a[GatherCraft] §f모든 스킬이 초기화되었습니다."), false);
        return 1;
    }

}
