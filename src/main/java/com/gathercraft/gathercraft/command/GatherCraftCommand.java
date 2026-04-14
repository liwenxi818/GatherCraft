package com.gathercraft.gathercraft.command;

import com.gathercraft.gathercraft.skill.SkillData;
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
