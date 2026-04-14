package com.gathercraft.gathercraft.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C: 플레이어가 몬스터에게 데미지를 입혔을 때 전송.
 * 클라이언트: 몬스터 머리 위에 부유 텍스트(FloatingCombatText) 추가.
 */
public class DamageTextPacket {

    public final float   damage;
    public final boolean isCrit;
    public final double  x, y, z;

    public DamageTextPacket(float damage, boolean isCrit, double x, double y, double z) {
        this.damage = damage;
        this.isCrit = isCrit;
        this.x      = x;
        this.y      = y;
        this.z      = z;
    }

    public static void encode(DamageTextPacket p, FriendlyByteBuf buf) {
        buf.writeFloat(p.damage);
        buf.writeBoolean(p.isCrit);
        buf.writeDouble(p.x);
        buf.writeDouble(p.y);
        buf.writeDouble(p.z);
    }

    public static DamageTextPacket decode(FriendlyByteBuf buf) {
        float   damage = buf.readFloat();
        boolean isCrit = buf.readBoolean();
        double  x      = buf.readDouble();
        double  y      = buf.readDouble();
        double  z      = buf.readDouble();
        return new DamageTextPacket(damage, isCrit, x, y, z);
    }

    public static void handle(DamageTextPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.gathercraft.gathercraft.client.overlay.FloatingCombatText.addEntry(
                    packet.damage, packet.isCrit,
                    packet.x, packet.y, packet.z
                )
            )
        );
        ctx.get().setPacketHandled(true);
    }
}
