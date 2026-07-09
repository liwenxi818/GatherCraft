package com.gathercraft.gathercraft.command;

import com.gathercraft.gathercraft.tpa.TpaManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * /tpaccept, /tpdeny - TPA 채팅 클릭 메시지의 RUN_COMMAND 대상.
 */
public class TpaCommand {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("tpaccept").executes(ctx -> respond(ctx.getSource(), true))
        );
        dispatcher.register(
            Commands.literal("tpdeny").executes(ctx -> respond(ctx.getSource(), false))
        );
    }

    private int respond(CommandSourceStack source, boolean accept) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("플레이어만 사용 가능합니다."));
            return 0;
        }
        TpaManager.respond(player, accept);
        return 1;
    }
}
