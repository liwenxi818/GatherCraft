package com.gathercraft.gathercraft.client.gui;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.SkillPointChoicePacket;
import com.gathercraft.gathercraft.network.packet.SkillPointOfferPacket;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import com.gathercraft.gathercraft.skill.SkillTier;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * 레벨업 스탯 포인트 선택 GUI.
 * 서버가 SkillPointOfferPacket을 전송하면 0.5초(10틱) 딜레이 후 팝업된다.
 * 플레이어가 3개 선택지 중 하나를 클릭하면 C2S 패킷으로 서버에 전달 후 닫힌다.
 * GUI를 그냥 닫으면(ESC) 서버 pending count는 유지 → 다음 로그인 시 재팝업.
 */
@OnlyIn(Dist.CLIENT)
public class SkillPointScreen extends Screen {

    private static final int POPUP_W = 300;
    private static final int POPUP_H = 230;
    private static final int BTN_W   = 260;
    private static final int BTN_H   = 38;
    private static final int BTN_GAP = 8;

    // --- 딜레이 큐 (static) ---
    private static int                pendingDelayTicks = -1;
    private static SkillPointOfferPacket pendingPacket  = null;

    /** SkillPointOfferPacket 수신 시 ClientKeyHandler.tick()에서 10틱 대기 후 화면 오픈 */
    public static void scheduleShow(SkillPointOfferPacket packet) {
        pendingPacket      = packet;
        pendingDelayTicks  = 10;
    }

    /** ClientKeyHandler.onClientTick()에서 매 틱 호출 */
    public static void clientTick() {
        if (pendingDelayTicks <= 0) return;
        pendingDelayTicks--;
        if (pendingDelayTicks == 0 && pendingPacket != null) {
            Minecraft mc = Minecraft.getInstance();
            // 다른 화면이 열려 있으면 잠깐 더 대기
            if (mc.screen != null) {
                pendingDelayTicks = 5;
                return;
            }
            mc.setScreen(new SkillPointScreen(pendingPacket));
            pendingPacket = null;
        }
    }

    // --- 인스턴스 ---
    private final SkillPointOfferPacket offer;
    private final SkillPointStat[] stats;   // 선택지 3개
    private final float[]  currentValues;   // 현재 누적값

    private int popupX, popupY;
    private int hoveredBtn = -1;

    public SkillPointScreen(SkillPointOfferPacket offer) {
        super(Component.literal("스탯 포인트 선택"));
        this.offer = offer;
        this.stats = new SkillPointStat[3];
        SkillPointStat[] all = SkillPointStat.values();
        for (int i = 0; i < 3; i++) {
            this.stats[i] = all[offer.statOrdinals[i]];
        }
        this.currentValues = offer.currentValues;
    }

    @Override
    protected void init() {
        popupX = (width  - POPUP_W) / 2;
        popupY = (height - POPUP_H) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // 전체 반투명 어두운 배경
        g.fill(0, 0, width, height, 0x88000000);

        SkillType skill = offer.skill;
        int skillColor  = 0xFF000000 | skill.color;
        SkillTier tier  = SkillTier.fromLevel(offer.level);

        // 팝업 배경
        g.fill(popupX, popupY, popupX + POPUP_W, popupY + POPUP_H, 0xEE1A1A2E);

        // 팝업 외곽선 (스킬 색상)
        g.fill(popupX,              popupY,              popupX + POPUP_W, popupY + 2,          skillColor);
        g.fill(popupX,              popupY + POPUP_H - 2,popupX + POPUP_W, popupY + POPUP_H,    skillColor);
        g.fill(popupX,              popupY + 2,           popupX + 2,       popupY + POPUP_H - 2,skillColor);
        g.fill(popupX + POPUP_W - 2,popupY + 2,           popupX + POPUP_W, popupY + POPUP_H - 2,skillColor);

        // 제목
        String title = "§e[" + skill.getKoreanName() + "] §fLv." + offer.level + " §a달성!";
        int titleX = popupX + (POPUP_W - font.width(title)) / 2;
        g.drawString(font, title, titleX, popupY + 12, 0xFFFFFF, true);

        // 티어 배지
        String tierBadge = tier.getDisplayName() + " 티어";
        int tierColor = 0xFF000000 | tier.textColor;
        int badgeX = popupX + (POPUP_W - font.width(tierBadge)) / 2;
        g.drawString(font, tierBadge, badgeX, popupY + 24, tierColor, false);

        // 부제목
        String sub = "§7스탯 포인트를 선택하세요";
        int subX = popupX + (POPUP_W - font.width(sub)) / 2;
        g.drawString(font, sub, subX, popupY + 38, 0xAAAAAA, false);

        // 구분선
        g.fill(popupX + 10, popupY + 50, popupX + POPUP_W - 10, popupY + 51, 0x55FFFFFF);

        // 버튼 3개
        hoveredBtn = -1;
        int btnStartY = popupY + 58;
        for (int i = 0; i < 3; i++) {
            int bx = popupX + (POPUP_W - BTN_W) / 2;
            int by = btnStartY + i * (BTN_H + BTN_GAP);
            boolean hovered = mouseX >= bx && mouseX < bx + BTN_W
                           && mouseY >= by && mouseY < by + BTN_H;
            if (hovered) hoveredBtn = i;
            renderButton(g, bx, by, i, hovered, skillColor);
        }

        // 툴팁
        if (hoveredBtn >= 0) {
            renderStatTooltip(g, hoveredBtn, mouseX, mouseY);
        }

        // ESC 힌트
        String hint = "§8ESC: 닫기 (다음 로그인 시 다시 표시)";
        int hintX = popupX + (POPUP_W - font.width(hint)) / 2;
        g.drawString(font, hint, hintX, popupY + POPUP_H - 14, 0x555555, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderButton(GuiGraphics g, int bx, int by, int idx, boolean hovered, int skillColor) {
        SkillPointStat stat = stats[idx];

        // 버튼 배경
        int bgColor = hovered ? 0xCC2A3A5A : 0x991A2A4A;
        g.fill(bx, by, bx + BTN_W, by + BTN_H, bgColor);

        // 버튼 테두리
        int borderColor = hovered ? skillColor : (0x88000000 | (stat.skill.color & 0xFFFFFF));
        g.fill(bx,            by,            bx + BTN_W, by + 1,          borderColor);
        g.fill(bx,            by + BTN_H - 1,bx + BTN_W, by + BTN_H,      borderColor);
        g.fill(bx,            by + 1,        bx + 1,     by + BTN_H - 1,  borderColor);
        g.fill(bx + BTN_W - 1,by + 1,        bx + BTN_W, by + BTN_H - 1,  borderColor);

        // 스탯 이름
        String nameText = "§f" + stat.displayName;
        int nameY = by + (BTN_H - font.lineHeight * 2 - 4) / 2;
        int nameX = bx + (BTN_W - font.width(nameText)) / 2;
        g.drawString(font, nameText, nameX, nameY, 0xFFFFFF, true);

        // 증가량
        String incText = "§a" + stat.incrementText;
        int incX = bx + (BTN_W - font.width(incText)) / 2;
        g.drawString(font, incText, incX, nameY + font.lineHeight + 3, 0xFFFFFF, false);
    }

    private void renderStatTooltip(GuiGraphics g, int idx, int mouseX, int mouseY) {
        SkillPointStat stat = stats[idx];
        float current = currentValues[idx];

        List<FormattedCharSequence> lines = new ArrayList<>();
        lines.add(Component.literal("§6" + stat.displayName).getVisualOrderText());

        String valStr = formatStatValue(stat, current);
        lines.add(Component.literal("§7현재 누적: §a" + valStr).getVisualOrderText());

        String afterStr = formatStatValue(stat, current + stat.increment);
        lines.add(Component.literal("§7선택 후: §b" + afterStr).getVisualOrderText());

        g.renderTooltip(font, lines, mouseX, mouseY);
    }

    private String formatStatValue(SkillPointStat stat, float value) {
        // HP 단위 스탯 (DEFENSE_MAX_HEALTH, HUNTING_HEAL, COOKING_BUFF_AMPLIFIER, ENCHANTING_LEVEL_BONUS)
        if (stat == SkillPointStat.DEFENSE_MAX_HEALTH) {
            return String.format("+%.0f HP", value);
        }
        if (stat == SkillPointStat.HUNTING_HEAL) {
            return String.format("+%.1f HP", value);
        }
        if (stat == SkillPointStat.COOKING_BUFF_AMPLIFIER || stat == SkillPointStat.ENCHANTING_LEVEL_BONUS) {
            return String.format("+%.1f", value);
        }
        // 기본: 백분율
        return String.format("+%.1f%%", value * 100f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int btnStartY = popupY + 58;
        for (int i = 0; i < 3; i++) {
            int bx = popupX + (POPUP_W - BTN_W) / 2;
            int by = btnStartY + i * (BTN_H + BTN_GAP);
            if (mouseX >= bx && mouseX < bx + BTN_W
             && mouseY >= by && mouseY < by + BTN_H) {
                selectStat(i);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void selectStat(int idx) {
        // 효과음
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(
            SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.5f)
        );

        // 서버에 선택 전달
        PacketHandler.sendToServer(new SkillPointChoicePacket(offer.skill, offer.statOrdinals[idx]));

        // 화면 닫기
        mc.setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
