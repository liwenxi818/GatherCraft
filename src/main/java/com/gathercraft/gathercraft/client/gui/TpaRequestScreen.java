package com.gathercraft.gathercraft.client.gui;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.TpaResponsePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * TPA 요청 수신 팝업. 채팅 클릭이 안 되는 상황(채팅창 미개방)에서도
 * 항상 수락/거절이 가능하도록 TpaAskPacket 수신 시 자동으로 뜬다.
 */
@OnlyIn(Dist.CLIENT)
public class TpaRequestScreen extends Screen {

    private static final int POPUP_W = 260;
    private static final int POPUP_H = 90;
    private static final int BTN_W = 110;
    private static final int BTN_H = 24;

    private static int pendingDelayTicks = -1;
    private static String pendingRequesterName = null;

    public static void scheduleShow(String requesterName) {
        pendingRequesterName = requesterName;
        pendingDelayTicks = 10;
    }

    public static void clientTick() {
        if (pendingDelayTicks <= 0) return;
        pendingDelayTicks--;
        if (pendingDelayTicks == 0 && pendingRequesterName != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen != null) {
                pendingDelayTicks = 5;
                return;
            }
            mc.setScreen(new TpaRequestScreen(pendingRequesterName));
            pendingRequesterName = null;
        }
    }

    private final String requesterName;
    private int popupX, popupY;

    public TpaRequestScreen(String requesterName) {
        super(Component.literal("텔포 요청"));
        this.requesterName = requesterName;
    }

    @Override
    protected void init() {
        popupX = (width - POPUP_W) / 2;
        popupY = (height - POPUP_H) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x88000000);
        g.fill(popupX, popupY, popupX + POPUP_W, popupY + POPUP_H, 0xEE1A1A2E);
        g.fill(popupX, popupY, popupX + POPUP_W, popupY + 2, 0xFFFFD700);
        g.fill(popupX, popupY + POPUP_H - 2, popupX + POPUP_W, popupY + POPUP_H, 0xFFFFD700);

        String title = "§e" + requesterName + "§f님의 순간이동 요청";
        int titleX = popupX + (POPUP_W - font.width(title)) / 2;
        g.drawString(font, title, titleX, popupY + 14, 0xFFFFFF, true);

        int btnY = popupY + POPUP_H - BTN_H - 12;
        int acceptX = popupX + 16;
        int denyX = popupX + POPUP_W - BTN_W - 16;

        boolean acceptHovered = mouseX >= acceptX && mouseX < acceptX + BTN_W && mouseY >= btnY && mouseY < btnY + BTN_H;
        g.fill(acceptX, btnY, acceptX + BTN_W, btnY + BTN_H, acceptHovered ? 0xCC2A5A2A : 0x991A3A1A);
        String acceptText = "§a수락";
        g.drawString(font, acceptText, acceptX + (BTN_W - font.width(acceptText)) / 2, btnY + (BTN_H - font.lineHeight) / 2, 0xFFFFFF, true);

        boolean denyHovered = mouseX >= denyX && mouseX < denyX + BTN_W && mouseY >= btnY && mouseY < btnY + BTN_H;
        g.fill(denyX, btnY, denyX + BTN_W, btnY + BTN_H, denyHovered ? 0xCC5A2A2A : 0x993A1A1A);
        String denyText = "§c거절";
        g.drawString(font, denyText, denyX + (BTN_W - font.width(denyText)) / 2, btnY + (BTN_H - font.lineHeight) / 2, 0xFFFFFF, true);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int btnY = popupY + POPUP_H - BTN_H - 12;
            int acceptX = popupX + 16;
            int denyX = popupX + POPUP_W - BTN_W - 16;

            if (mouseX >= acceptX && mouseX < acceptX + BTN_W && mouseY >= btnY && mouseY < btnY + BTN_H) {
                respond(true);
                return true;
            }
            if (mouseX >= denyX && mouseX < denyX + BTN_W && mouseY >= btnY && mouseY < btnY + BTN_H) {
                respond(false);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void respond(boolean accept) {
        PacketHandler.sendToServer(new TpaResponsePacket(accept));
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
