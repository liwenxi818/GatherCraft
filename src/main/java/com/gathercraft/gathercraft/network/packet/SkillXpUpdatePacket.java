package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C: XP 획득/레벨업 정보를 클라이언트에 전달 (스킬 HUD 오버레이 갱신용)
 */
public class SkillXpUpdatePacket {

    public final SkillType skill;
    public final int level;
    public final float progress;  // 0.0~1.0
    public final boolean leveledUp;
    public final boolean tierUp;  // 10레벨 배수 티어업 여부

    public SkillXpUpdatePacket(SkillType skill, int level, float progress,
                                boolean leveledUp, boolean tierUp) {
        this.skill = skill;
        this.level = level;
        this.progress = progress;
        this.leveledUp = leveledUp;
        this.tierUp = tierUp;
    }

    public static void encode(SkillXpUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.skill);
        buf.writeInt(packet.level);
        buf.writeFloat(packet.progress);
        buf.writeBoolean(packet.leveledUp);
        buf.writeBoolean(packet.tierUp);
    }

    public static SkillXpUpdatePacket decode(FriendlyByteBuf buf) {
        SkillType skill = buf.readEnum(SkillType.class);
        int level = buf.readInt();
        float progress = buf.readFloat();
        boolean leveledUp = buf.readBoolean();
        boolean tierUp = buf.readBoolean();
        return new SkillXpUpdatePacket(skill, level, progress, leveledUp, tierUp);
    }

    public static void handle(SkillXpUpdatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                com.gathercraft.gathercraft.client.overlay.SkillXpBarOverlay.onXpUpdate(packet);
                com.gathercraft.gathercraft.client.gui.SkillBookScreen.onXpUpdate(packet.skill, packet.level, packet.progress);
            })
        );
        ctx.get().setPacketHandled(true);
    }
}
