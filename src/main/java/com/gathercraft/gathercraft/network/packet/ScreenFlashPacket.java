package com.gathercraft.gathercraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ScreenFlashPacket {

    private final float intensity;

    public ScreenFlashPacket(float intensity) {
        this.intensity = intensity;
    }

    public static void encode(ScreenFlashPacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.intensity);
    }

    public static ScreenFlashPacket decode(FriendlyByteBuf buf) {
        return new ScreenFlashPacket(buf.readFloat());
    }

    public static void handle(ScreenFlashPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.gathercraft.gathercraft.client.overlay.DamageFlashOverlay.triggerFlash(packet.intensity)
            )
        );
        ctx.get().setPacketHandled(true);
    }
}
