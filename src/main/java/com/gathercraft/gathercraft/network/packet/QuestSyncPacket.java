package com.gathercraft.gathercraft.network.packet;

import com.gathercraft.gathercraft.quest.QuestClientCache;
import com.gathercraft.gathercraft.quest.QuestData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class QuestSyncPacket {

    public final List<QuestData> quests;

    public QuestSyncPacket(List<QuestData> quests) {
        this.quests = quests;
    }

    public static void encode(QuestSyncPacket p, FriendlyByteBuf buf) {
        ListTag list = new ListTag();
        for (QuestData quest : p.quests) list.add(quest.toNBT());
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("list", list);
        buf.writeNbt(wrapper);
    }

    public static QuestSyncPacket decode(FriendlyByteBuf buf) {
        List<QuestData> quests = new ArrayList<>();
        CompoundTag wrapper = buf.readNbt();
        if (wrapper != null) {
            ListTag list = wrapper.getList("list", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                quests.add(QuestData.fromNBT(list.getCompound(i)));
            }
        }
        return new QuestSyncPacket(quests);
    }

    public static void handle(QuestSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                QuestClientCache.set(packet.quests)
            )
        );
        ctx.get().setPacketHandled(true);
    }
}
