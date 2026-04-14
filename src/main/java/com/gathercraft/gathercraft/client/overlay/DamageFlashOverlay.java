package com.gathercraft.gathercraft.client.overlay;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DamageFlashOverlay {

    private static volatile float flashAlpha = 0.0f;

    public static void triggerFlash(float intensity) {
        flashAlpha = Math.min(1.0f, intensity);
    }

    /** RegisterGuiLayersEvent 에서 등록되는 렌더 콜백 */
    public static void render(net.minecraftforge.client.gui.overlay.ForgeGui gui,
                               GuiGraphics guiGraphics, float partialTick,
                               int screenWidth, int screenHeight) {
        float alpha = flashAlpha;
        if (alpha <= 0.0f) return;

        int a = (int)(alpha * 180);
        int color = (a << 24) | (0xC8 << 16); // ARGB: red=200, green=0, blue=0
        int border = 32;

        guiGraphics.fill(0, 0, screenWidth, border, color);
        guiGraphics.fill(0, screenHeight - border, screenWidth, screenHeight, color);
        guiGraphics.fill(0, border, border, screenHeight - border, color);
        guiGraphics.fill(screenWidth - border, border, screenWidth, screenHeight - border, color);

        // 페이드아웃 (렌더 프레임마다 감소)
        flashAlpha = Math.max(0.0f, alpha - 0.04f);
    }
}
