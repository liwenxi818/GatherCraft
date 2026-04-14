package com.gathercraft.gathercraft.command;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillTier;
import com.gathercraft.gathercraft.skill.SkillType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * /skill [스킬명] - 스킬 레벨 확인 명령어
 */
public class SkillCommand {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("skill")
                .executes(ctx -> showAllSkills(ctx.getSource()))
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> showSkill(ctx.getSource(), StringArgumentType.getString(ctx, "name"))))
        );
    }

    private int showAllSkills(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("플레이어만 사용 가능합니다."));
            return 0;
        }

        player.sendSystemMessage(Component.literal("§6=== GatherCraft 스킬 현황 ==="));
        for (SkillType skill : SkillType.values()) {
            player.sendSystemMessage(Component.literal(formatSkillLine(player, skill)));
        }
        return 1;
    }

    private int showSkill(CommandSourceStack source, String name) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("플레이어만 사용 가능합니다."));
            return 0;
        }

        SkillType skill = SkillType.findByName(name);
        if (skill == null) {
            source.sendFailure(Component.literal("알 수 없는 스킬: " + name));
            source.sendFailure(Component.literal("사용 가능: mining, lumberjack, farming, fishing, cooking, hunting, defense, smithing, enchanting"));
            return 0;
        }

        int level = SkillData.getLevel(player, skill);
        long xp = SkillData.getXP(player, skill);
        long needed = SkillManager.xpToNextLevel(level);
        SkillTier tier = SkillTier.fromLevel(level);
        double progress = SkillManager.getXPProgress(player, skill);

        player.sendSystemMessage(Component.literal("§6=== " + skill.getKoreanName() + " ==="));
        player.sendSystemMessage(Component.literal("§f레벨: §e" + level + " §7/ 100"));
        player.sendSystemMessage(Component.literal("§f티어: §b" + tier.getDisplayName()));
        if (level < SkillData.MAX_LEVEL) {
            player.sendSystemMessage(Component.literal("§fXP: §a" + xp + " §7/ §a" + needed));
            player.sendSystemMessage(Component.literal("§f진행도: " + buildProgressBar(progress)));
        } else {
            player.sendSystemMessage(Component.literal("§6★ 각성 달성! ★"));
        }
        return 1;
    }

    private String formatSkillLine(ServerPlayer player, SkillType skill) {
        int level = SkillData.getLevel(player, skill);
        SkillTier tier = SkillTier.fromLevel(level);
        String bar = buildProgressBar(SkillManager.getXPProgress(player, skill));
        return String.format("§7[%s] §e%-3d §7(%s) %s", skill.getKoreanName(), level, tier.getDisplayName(), bar);
    }

    private String buildProgressBar(double progress) {
        int filled = (int)(progress * 10);
        StringBuilder sb = new StringBuilder("§a");
        for (int i = 0; i < 10; i++) {
            if (i == filled) sb.append("§7");
            sb.append("|");
        }
        return sb.toString();
    }

}
