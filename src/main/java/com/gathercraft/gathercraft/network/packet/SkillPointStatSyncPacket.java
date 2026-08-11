package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C: MINING_SPEED/LUMBERJACK_SPEED 누적값을 클라이언트 로컬 플레이어 NBT에 동기화한다.
 * PlayerEvent.BreakSpeed는 클라이언트/서버 양쪽에서 각각 발동하는데(클라이언트는 크랙 오버레이 예측용),
 * 이 두 스탯은 서버의 플레이어 NBT에만 있어서 동기화 없이는 클라이언트 예측이 서버 파괴 속도와 어긋난다.
 * 로그인 시 + 두 스탯 중 하나를 선택할 때마다 전송한다.
 */
public class SkillPointStatSyncPacket {

    public final float miningSpeed;
    public final float lumberjackSpeed;

    public SkillPointStatSyncPacket(float miningSpeed, float lumberjackSpeed) {
        this.miningSpeed = miningSpeed;
        this.lumberjackSpeed = lumberjackSpeed;
    }

    public static void encode(SkillPointStatSyncPacket p, FriendlyByteBuf buf) {
        buf.writeFloat(p.miningSpeed);
        buf.writeFloat(p.lumberjackSpeed);
    }

    public static SkillPointStatSyncPacket decode(FriendlyByteBuf buf) {
        return new SkillPointStatSyncPacket(buf.readFloat(), buf.readFloat());
    }

    public static void handle(SkillPointStatSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                if (Minecraft.getInstance().player == null) return;
                CompoundTag tag = SkillData.getRoot(Minecraft.getInstance().player);
                tag.putFloat(SkillPointStat.MINING_SPEED.getNbtKey(), packet.miningSpeed);
                tag.putFloat(SkillPointStat.LUMBERJACK_SPEED.getNbtKey(), packet.lumberjackSpeed);
                SkillData.saveRoot(Minecraft.getInstance().player, tag);
            })
        );
        ctx.get().setPacketHandled(true);
    }
}
