package com.gathercraft.gathercraft.client.keybinding;

import com.gathercraft.gathercraft.client.gui.SkillPointScreen;
import com.gathercraft.gathercraft.client.overlay.SkillXpBarOverlay;
import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.DashRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientKeyHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // 스킬 포인트 팝업 딜레이 처리 (화면 열려 있어도 카운트다운 유지)
        SkillPointScreen.clientTick();

        if (mc.screen != null) return;  // GUI 열려있으면 키 입력 무시

        while (KeyBindings.DASH.consumeClick()) {
            PacketHandler.sendToServer(new DashRequestPacket());
        }
    }

    /** 스킬 XP 바 표시 중에는 바닐라 경험치 바를 숨긴다 */
    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() == VanillaGuiOverlay.EXPERIENCE_BAR.type()
                && SkillXpBarOverlay.isActive()) {
            event.setCanceled(true);
        }
    }
}
