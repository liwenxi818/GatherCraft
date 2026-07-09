package com.gathercraft.gathercraft.client.overlay;

import com.gathercraft.gathercraft.title.TitleManager;
import com.gathercraft.gathercraft.title.TitleNameTagCache;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

/**
 * 착용 중인 칭호를 대상 플레이어 이름표 바로 위에 렌더링한다.
 * RenderNameTagEvent는 단일 Component만 교체 가능하고 "추가 줄" 훅이 없으므로,
 * vanilla 이름표는 그대로 두고 같은 PoseStack/버퍼로 별도 텍스트를 덧그린다.
 */
@OnlyIn(Dist.CLIENT)
public class TitleNameTagRenderer {

    @SubscribeEvent
    public void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player targetPlayer)) return;

        String titleId = TitleNameTagCache.get(targetPlayer.getUUID());
        if (titleId == null || titleId.isEmpty()) return;

        String displayName = TitleManager.getDisplayName(titleId);
        if (displayName.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (targetPlayer.isInvisibleTo(mc.player)) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        Font font = mc.font;
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        // vanilla 이름표 오프셋(bbHeight + 0.5) + 칭호용 추가 오프셋 0.3
        float height = targetPlayer.getBbHeight() + 0.5f + 0.3f;

        poseStack.pushPose();
        poseStack.translate(0.0, height, 0.0);
        poseStack.mulPose(dispatcher.cameraOrientation());
        poseStack.scale(0.025f, -0.025f, 0.025f);

        Matrix4f matrix = poseStack.last().pose();
        Component titleComponent = Component.literal(displayName);
        float width = font.width(titleComponent);
        int bgColor = (int) (mc.options.getBackgroundOpacity(0.25f) * 255.0f) << 24;

        font.drawInBatch(titleComponent, -width / 2f, 0, 553648127, false, matrix, buffer,
            Font.DisplayMode.SEE_THROUGH, bgColor, packedLight);
        font.drawInBatch(titleComponent, -width / 2f, 0, -1, false, matrix, buffer,
            Font.DisplayMode.NORMAL, 0, packedLight);

        poseStack.popPose();
    }
}
