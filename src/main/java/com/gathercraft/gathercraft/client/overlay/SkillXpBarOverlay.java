package com.gathercraft.gathercraft.client.overlay;

import com.gathercraft.gathercraft.network.packet.SkillXpUpdatePacket;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/**
 * 스킬 XP 획득/레벨업 시 화면 맨 위 중앙에 표시되는 경험치 바 오버레이.
 * - 바닐라 XP 바 위에 렌더링
 * - 레벨업 시 금색으로 꽉 차는 애니메이션
 * - 티어업 시 흰색 플래시 효과
 */
@OnlyIn(Dist.CLIENT)
public class SkillXpBarOverlay {

    // 표시 지속 시간 (ms)
    private static final long SHOW_DURATION_MS = 2500L;
    private static final long LEVELUP_DURATION_MS = 3500L;

    // 레벨업 애니메이션 단계
    private static final long LEVELUP_FILL_MS = 800L;   // 금색으로 꽉 차는 시간
    private static final long LEVELUP_FADE_MS = 500L;   // 새 레벨로 페이드

    // 티어업 플래시 지속
    private static final long TIERUP_FLASH_MS = 600L;

    // 현재 표시 상태
    private static SkillType currentSkill = null;
    private static int currentLevel = 1;
    private static float currentProgress = 0f;
    private static boolean isLevelUp = false;
    private static boolean isTierUp = false;

    private static long showUntilMs = 0L;
    private static long levelUpStartMs = 0L;
    private static long tierUpStartMs = 0L;

    // 바 치수 (바닐라 XP 바와 동일)
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;

    // 스킬별 색상
    public static int getSkillColor(SkillType skill) {
        return skill.color;
    }

    /** 스킬 XP 바가 현재 표시 중인지 여부 */
    public static boolean isActive() {
        return currentSkill != null && System.currentTimeMillis() <= showUntilMs;
    }

    /** S2C 패킷에서 호출 */
    public static void onXpUpdate(SkillXpUpdatePacket packet) {
        currentSkill = packet.skill;
        currentLevel = packet.level;
        currentProgress = packet.progress;
        isLevelUp = packet.leveledUp;
        isTierUp = packet.tierUp;

        long now = System.currentTimeMillis();

        if (isLevelUp) {
            showUntilMs = now + LEVELUP_DURATION_MS;
            levelUpStartMs = now;
        } else {
            showUntilMs = now + SHOW_DURATION_MS;
        }

        if (isTierUp) {
            tierUpStartMs = now;
        }
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick,
                               int screenWidth, int screenHeight) {
        if (currentSkill == null) return;

        long now = System.currentTimeMillis();
        if (now > showUntilMs) {
            currentSkill = null;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        // 바 위치: 화면 맨 위 중앙
        int barX = (screenWidth - BAR_WIDTH) / 2;
        int barY = 2;

        // 페이드 알파 계산
        float alpha = 1.0f;
        long remaining = showUntilMs - now;
        if (remaining < 500L) {
            alpha = remaining / 500f;
        }
        int alphaInt = (int)(Mth.clamp(alpha, 0f, 1f) * 255);

        // 배경 (어두운 반투명)
        int bgColor = (alphaInt / 2) << 24;
        graphics.fill(barX - 1, barY - 2, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 12, bgColor | 0x000000);

        // 스킬 이름 + 레벨 텍스트 (바 아래에 표시)
        int skillColor = getSkillColor(currentSkill);
        int nameColor = (alphaInt << 24) | (skillColor & 0xFFFFFF);

        String nameText = currentSkill.getKoreanName() + "  Lv." + currentLevel;
        int textX = (screenWidth - mc.font.width(nameText)) / 2;
        graphics.drawString(mc.font, nameText, textX, barY + BAR_HEIGHT + 3, nameColor, true);

        // 바 렌더링
        renderBar(graphics, barX, barY, now, alphaInt, skillColor);
    }

    private static void renderBar(GuiGraphics graphics, int barX, int barY,
                                   long now, int alphaInt, int skillColor) {
        // 배경 바 (어두운 회색)
        int bgBarColor = (alphaInt << 24) | 0x222222;
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, bgBarColor);

        float renderProgress;
        int barColor;

        if (isLevelUp) {
            long elapsed = now - levelUpStartMs;

            if (elapsed < LEVELUP_FILL_MS) {
                // 금색으로 꽉 차는 단계
                float t = (float) elapsed / LEVELUP_FILL_MS;
                renderProgress = Mth.lerp(Mth.clamp(t, 0f, 1f), currentProgress, 1.0f);
                barColor = lerpColor(skillColor, 0xFFD700, Mth.clamp(t, 0f, 1f));
            } else {
                // 새 레벨로 초기화되는 단계
                long fadeElapsed = elapsed - LEVELUP_FILL_MS;
                float t = (float) fadeElapsed / LEVELUP_FADE_MS;
                renderProgress = Mth.lerp(Mth.clamp(t, 0f, 1f), 0.0f, currentProgress);
                barColor = lerpColor(0xFFD700, skillColor, Mth.clamp(t, 0f, 1f));
            }
        } else {
            renderProgress = currentProgress;
            barColor = skillColor;
        }

        // 티어업 플래시 효과 (흰색 오버레이)
        if (isTierUp) {
            long tierElapsed = now - tierUpStartMs;
            if (tierElapsed < TIERUP_FLASH_MS) {
                float t = (float) tierElapsed / TIERUP_FLASH_MS;
                float flashIntensity = (float)(Math.sin(Math.PI * t));
                barColor = lerpColor(barColor, 0xFFFFFF, flashIntensity * 0.8f);
            }
        }

        // 바 채우기
        int fillWidth = (int)(BAR_WIDTH * Mth.clamp(renderProgress, 0f, 1f));
        if (fillWidth > 0) {
            int fillColor = (alphaInt << 24) | (barColor & 0xFFFFFF);
            graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, fillColor);

            // 바 끝부분 밝은 하이라이트
            int highlightColor = (alphaInt << 24) | blendColor(barColor, 0xFFFFFF, 0.4f);
            graphics.fill(barX, barY, barX + fillWidth, barY + 1, highlightColor);
        }
    }

    /** 두 RGB 색상을 선형 보간 */
    private static int lerpColor(int colorA, int colorB, float t) {
        int rA = (colorA >> 16) & 0xFF;
        int gA = (colorA >> 8) & 0xFF;
        int bA = colorA & 0xFF;
        int rB = (colorB >> 16) & 0xFF;
        int gB = (colorB >> 8) & 0xFF;
        int bB = colorB & 0xFF;
        int r = (int)(rA + (rB - rA) * t);
        int g = (int)(gA + (gB - gA) * t);
        int b = (int)(bA + (bB - bA) * t);
        return (r << 16) | (g << 8) | b;
    }

    /** 두 RGB 색상 블렌드 */
    private static int blendColor(int base, int overlay, float amount) {
        return lerpColor(base, overlay, amount);
    }
}
