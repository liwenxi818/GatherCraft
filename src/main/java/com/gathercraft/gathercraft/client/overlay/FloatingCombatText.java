package com.gathercraft.gathercraft.client.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 부유 전투 텍스트 (Floating Combat Text).
 * - 서버에서 DamageTextPacket을 수신하면 addEntry()로 항목 추가.
 * - ClientTickEvent: ticksAlive 증가, 만료 항목 제거.
 * - RenderLevelStageEvent.AFTER_ENTITIES: 월드 좌표에서 빌보딩 렌더링.
 *
 * 표시 형식:
 *   크리티컬: §c§l✦ 23.5  (굵은 빨강, 크게)
 *   일반:     §f12.5       (흰색)
 *   HP 줄:    §c❤ 17 / 40
 */
@OnlyIn(Dist.CLIENT)
public class FloatingCombatText {

    private static final int LIFETIME = 50; // 2.5초 (20틱 = 1초)

    private static final List<TextEntry> ENTRIES = new ArrayList<>();

    // ---- 공개 API ----

    public static void addEntry(float damage, boolean isCrit, double x, double y, double z) {
        TextEntry e = new TextEntry();
        e.damage = damage;
        e.isCrit = isCrit;
        e.worldX = x;
        e.worldY = y;
        e.worldZ = z;
        // 텍스트 겹침 완화용 랜덤 수평 오프셋
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        e.offsetX = (float)(rng.nextDouble() - 0.5) * 0.4f;
        e.offsetZ = (float)(rng.nextDouble() - 0.5) * 0.4f;
        ENTRIES.add(e);
        // 최대 50개 유지 (과부하 방지)
        if (ENTRIES.size() > 50) ENTRIES.remove(0);
    }

    // ---- 이벤트 핸들러 ----

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().isPaused()) return;
        ENTRIES.removeIf(e -> ++e.ticksAlive >= LIFETIME);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        if (ENTRIES.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        Camera camera = event.getCamera();
        PoseStack poseStack = event.getPoseStack();
        Font font = mc.font;

        Tesselator tesselator = Tesselator.getInstance();
        MultiBufferSource.BufferSource bufferSource =
            MultiBufferSource.immediate(tesselator.getBuilder());

        for (TextEntry e : ENTRIES) {
            float progress = (float) e.ticksAlive / LIFETIME;
            // 1.5블록 상승
            float rise = progress * 1.5f;
            // 후반 40%에서 페이드 아웃
            int alpha = progress < 0.6f
                ? 255
                : (int)((1.0f - (progress - 0.6f) / 0.4f) * 255);
            if (alpha <= 0) continue;

            double dx = e.worldX + e.offsetX - camera.getPosition().x;
            double dy = e.worldY + rise      - camera.getPosition().y;
            double dz = e.worldZ + e.offsetZ - camera.getPosition().z;

            poseStack.pushPose();
            poseStack.translate(dx, dy, dz);
            // 빌보딩: 카메라 방향을 바라보도록 회전
            poseStack.mulPose(camera.rotation());
            // 크리티컬은 조금 더 크게
            float scale = e.isCrit ? 0.035f : 0.025f;
            poseStack.scale(-scale, -scale, scale);

            Matrix4f matrix = poseStack.last().pose();

            // 데미지 줄
            String dmgText = e.isCrit
                ? "\u2736 " + formatDamage(e.damage)   // ✶ 기호
                : formatDamage(e.damage);
            int dmgColor = buildColor(alpha, e.isCrit ? 0xFF4444 : 0xFFFFFF);
            float dmgWidth = font.width(dmgText);
            font.drawInBatch(dmgText, -dmgWidth / 2f, 0,
                dmgColor, false, matrix, bufferSource,
                Font.DisplayMode.NORMAL, 0, 15728880);

            poseStack.popPose();
        }

        bufferSource.endBatch();
    }

    // ---- 내부 유틸 ----

    /** alpha(0~255) + RGB(0xRRGGBB) → ARGB int */
    private static int buildColor(int alpha, int rgb) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0x00FFFFFF);
    }

    /** 데미지: 소수점 1자리, 정수면 ".0" 생략 */
    private static String formatDamage(float v) {
        if (v == (int) v) return String.valueOf((int) v);
        return String.format("%.1f", v);
    }

    // ---- 내부 클래스 ----

    private static class TextEntry {
        double worldX, worldY, worldZ;
        float  damage;
        boolean isCrit;
        int    ticksAlive = 0;
        float  offsetX, offsetZ;
    }
}
