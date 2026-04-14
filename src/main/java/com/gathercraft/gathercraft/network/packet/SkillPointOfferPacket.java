package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C: 레벨업 스탯 선택지 3개를 클라이언트에 전달.
 * 클라이언트는 SkillPointScreen을 0.5초 후 표시한다.
 */
public class SkillPointOfferPacket {

    public final SkillType skill;
    public final int level;
    /** 선택지 3개의 SkillPointStat ordinal */
    public final int[] statOrdinals;   // length 3
    /** 각 선택지의 현재 누적 값 (툴팁 표시용) */
    public final float[] currentValues; // length 3

    public SkillPointOfferPacket(SkillType skill, int level, int[] statOrdinals, float[] currentValues) {
        this.skill = skill;
        this.level = level;
        this.statOrdinals = statOrdinals;
        this.currentValues = currentValues;
    }

    public static void encode(SkillPointOfferPacket p, FriendlyByteBuf buf) {
        buf.writeEnum(p.skill);
        buf.writeInt(p.level);
        for (int ord : p.statOrdinals)   buf.writeInt(ord);
        for (float v  : p.currentValues) buf.writeFloat(v);
    }

    public static SkillPointOfferPacket decode(FriendlyByteBuf buf) {
        SkillType skill = buf.readEnum(SkillType.class);
        int level = buf.readInt();
        int[]   ords = new int[3];
        float[] vals = new float[3];
        for (int i = 0; i < 3; i++) ords[i] = buf.readInt();
        for (int i = 0; i < 3; i++) vals[i] = buf.readFloat();
        return new SkillPointOfferPacket(skill, level, ords, vals);
    }

    public static void handle(SkillPointOfferPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.gathercraft.gathercraft.client.gui.SkillPointScreen.scheduleShow(packet)
            )
        );
        ctx.get().setPacketHandled(true);
    }
}
