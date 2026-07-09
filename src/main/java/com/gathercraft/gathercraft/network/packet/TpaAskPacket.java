package com.gathercraft.gathercraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TpaAskPacket {

    public final String requesterName;

    public TpaAskPacket(String requesterName) {
        this.requesterName = requesterName;
    }

    public static void encode(TpaAskPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.requesterName, 32);
    }

    public static TpaAskPacket decode(FriendlyByteBuf buf) {
        return new TpaAskPacket(buf.readUtf(32));
    }

    public static void handle(TpaAskPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                com.gathercraft.gathercraft.client.gui.TpaRequestScreen.scheduleShow(packet.requesterName);

                MutableComponent accept = Component.literal("§a§l[수락]")
                    .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.literal("§a클릭하여 텔레포트 요청을 수락합니다"))));

                MutableComponent deny = Component.literal("§c§l[거절]")
                    .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            Component.literal("§c클릭하여 요청을 거절합니다"))));

                MutableComponent message = Component.literal("§e" + packet.requesterName + "§f님이 순간이동을 요청했습니다. ")
                    .append(accept).append(Component.literal(" ")).append(deny);

                net.minecraft.client.Minecraft.getInstance().gui.getChat().addMessage(message);
            })
        );
        ctx.get().setPacketHandled(true);
    }
}
