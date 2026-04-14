package com.gathercraft.gathercraft.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SkillBarOverlay {

    // ---- 대시 쿨타임 상태 ----
    private static long dashCooldownEndMs = 0L;
    private static long dashCooldownTotalMs = 10000L;
    private static float readyPulse = 0f;
    private static boolean wasOnCooldown = false;
    private static boolean readySoundPlayed = false;

    // ---- 슬롯 목록 (향후 확장용) ----
    public static final List<SkillSlotEntry> SLOTS = new ArrayList<>();
    static {
        SLOTS.add(new SkillSlotEntry("R", 0xFF4488FF, 0));
    }

    private static final int RADIUS = 13;
    private static final int SLOT_SIZE = 30;  // 슬롯 프레임 크기
    private static final int SLOT_SPACING = 35;

    /** DashSyncPacket에서 호출 — 쿨타임 시작 */
    public static void onDashActivated(long cooldownTicks) {
        dashCooldownEndMs = System.currentTimeMillis() + cooldownTicks * 50L;
        dashCooldownTotalMs = cooldownTicks * 50L;
        wasOnCooldown = true;
        readySoundPlayed = false;
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick,
                               int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        long now = System.currentTimeMillis();
        long remaining = Math.max(0L, dashCooldownEndMs - now);
        float progress = (dashCooldownTotalMs > 0)
            ? 1f - (float) remaining / dashCooldownTotalMs
            : 1f;
        progress = Mth.clamp(progress, 0f, 1f);
        boolean isReady = (remaining <= 0);

        // 준비완료 엣지 감지
        if (isReady && wasOnCooldown) {
            readyPulse = 1.0f;
            wasOnCooldown = false;
            if (!readySoundPlayed) {
                mc.level.playLocalSound(
                    mc.player.blockPosition(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.6f, 1.2f, false
                );
                readySoundPlayed = true;
            }
        }

        // 슬롯 렌더링
        for (SkillSlotEntry slot : SLOTS) {
            float cx = slot.getRenderX(screenWidth);
            float cy = screenHeight - 30;

            renderSlot(graphics, cx, cy, progress, isReady, slot);
        }

        // 펄스 페이드아웃
        if (readyPulse > 0) {
            readyPulse = Math.max(0f, readyPulse - 0.05f);
        }
    }

    private static void renderSlot(GuiGraphics graphics, float cx, float cy,
                                    float progress, boolean isReady, SkillSlotEntry slot) {
        // 슬롯 외곽 프레임 (테두리만)
        int fx = (int)(cx - SLOT_SIZE / 2f);
        int fy = (int)(cy - SLOT_SIZE / 2f);
        int fw = SLOT_SIZE;
        int fh = SLOT_SIZE;
        int frameColor = isReady ? 0xFF4488FF : 0xFF555555;

        // 상하좌우 테두리 (2px)
        graphics.fill(fx, fy, fx + fw, fy + 2, frameColor);
        graphics.fill(fx, fy + fh - 2, fx + fw, fy + fh, frameColor);
        graphics.fill(fx, fy + 2, fx + 2, fy + fh - 2, frameColor);
        graphics.fill(fx + fw - 2, fy + 2, fx + fw, fy + fh - 2, frameColor);

        // 배경 원 색상 (준비: 파랑+펄스, 쿨타임: 회색)
        int bgAlpha = 180;
        int bgR, bgG, bgB;
        if (isReady) {
            float pulse = readyPulse;
            bgR = (int)(0x44 + pulse * 0x40);
            bgG = (int)(0x88 + pulse * 0x40);
            bgB = 0xFF;
        } else {
            bgR = 0x44; bgG = 0x44; bgB = 0x44;
        }

        // 배경 원 그리기
        drawFilledCircle(graphics, cx, cy, RADIUS, 32, bgR, bgG, bgB, bgAlpha);

        // 쿨타임 덮개 (미완료 부분)
        if (!isReady) {
            drawCooldownArc(graphics, cx, cy, RADIUS, progress);
        }

        // "R" 텍스트
        Minecraft mc = Minecraft.getInstance();
        int textColor = isReady ? 0xFFFFFF : 0xAAAAAA;
        int textX = (int)(cx - mc.font.width(slot.label) / 2f);
        int textY = (int)(cy - mc.font.lineHeight / 2f);
        graphics.drawString(mc.font, slot.label, textX, textY, textColor, true);

        // 쿨타임 남은 시간 (초) - 쿨타임 중일 때만 표시
        if (!isReady) {
            long remaining = Math.max(0L, dashCooldownEndMs - System.currentTimeMillis());
            int secs = (int)(remaining / 1000) + 1;
            String secStr = String.valueOf(secs);
            int secX = (int)(cx - mc.font.width(secStr) / 2f);
            graphics.drawString(mc.font, secStr, secX, (int)(cy + RADIUS + 2), 0xAAAAAA, false);
        }
    }

    private static void drawFilledCircle(GuiGraphics graphics, float cx, float cy,
                                          float radius, int segments, int r, int g, int b, int a) {
        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        builder.vertex(matrix, cx, cy, 0).color(r, g, b, a).endVertex();

        for (int i = 0; i <= segments; i++) {
            float angle = (float)(-Math.PI / 2 + 2 * Math.PI * i / segments);
            float px = cx + radius * Mth.cos(angle);
            float py = cy + radius * Mth.sin(angle);
            builder.vertex(matrix, px, py, 0).color(r, g, b, a).endVertex();
        }

        BufferUploader.drawWithShader(builder.end());
        RenderSystem.disableBlend();
    }

    private static void drawCooldownArc(GuiGraphics graphics, float cx, float cy,
                                         float radius, float progress) {
        if (progress >= 1.0f) return;

        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        builder.vertex(matrix, cx, cy, 0).color(0, 0, 0, 160).endVertex();

        float startAngle = (float)(-Math.PI / 2 + 2 * Math.PI * progress);
        float endAngle   = (float)( Math.PI * 3 / 2);  // = -π/2 + 2π
        int segments = Math.max(1, (int)((1f - progress) * 32));

        for (int i = 0; i <= segments; i++) {
            float angle = startAngle + (endAngle - startAngle) * i / segments;
            float px = cx + radius * Mth.cos(angle);
            float py = cy + radius * Mth.sin(angle);
            builder.vertex(matrix, px, py, 0).color(0, 0, 0, 160).endVertex();
        }

        BufferUploader.drawWithShader(builder.end());
        RenderSystem.disableBlend();
    }

    // ---- 확장 가능한 슬롯 구조 ----
    public static class SkillSlotEntry {
        public final String label;
        public final int readyColor;
        public final int slotIndex;

        public SkillSlotEntry(String label, int readyColor, int slotIndex) {
            this.label = label;
            this.readyColor = readyColor;
            this.slotIndex = slotIndex;
        }

        public float getRenderX(int screenWidth) {
            return screenWidth - 30 - slotIndex * SLOT_SPACING;
        }
    }
}
