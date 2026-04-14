package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S: 플레이어가 스탯 선택지 1개를 골랐을 때 서버로 전달.
 * 서버: 스탯 적용 → pending 감소 → 남은 pending 있으면 다음 offer 전송.
 */
public class SkillPointChoicePacket {

    public final SkillType skill;
    public final int chosenStatOrdinal;

    public SkillPointChoicePacket(SkillType skill, int chosenStatOrdinal) {
        this.skill = skill;
        this.chosenStatOrdinal = chosenStatOrdinal;
    }

    public static void encode(SkillPointChoicePacket p, FriendlyByteBuf buf) {
        buf.writeEnum(p.skill);
        buf.writeInt(p.chosenStatOrdinal);
    }

    public static SkillPointChoicePacket decode(FriendlyByteBuf buf) {
        SkillType skill = buf.readEnum(SkillType.class);
        int ord = buf.readInt();
        return new SkillPointChoicePacket(skill, ord);
    }

    public static void handle(SkillPointChoicePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sp = ctx.get().getSender();
            if (sp == null) return;

            SkillPointStat[] all = SkillPointStat.values();
            if (packet.chosenStatOrdinal < 0 || packet.chosenStatOrdinal >= all.length) return;

            SkillPointStat chosen = all[packet.chosenStatOrdinal];
            // 선택한 스탯이 해당 스킬 것인지 검증
            if (chosen.skill != packet.skill) return;

            // 스탯 누적 적용
            SkillData.addStatValue(sp, chosen, chosen.increment);

            // pending 감소
            int remaining = SkillData.getPendingCount(sp, packet.skill) - 1;
            SkillData.setPendingCount(sp, packet.skill, remaining);

            // 남은 offer가 있으면 즉시 다음 선택지 전송
            if (remaining > 0) {
                SkillManager.sendSkillPointOffer(sp, packet.skill);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
