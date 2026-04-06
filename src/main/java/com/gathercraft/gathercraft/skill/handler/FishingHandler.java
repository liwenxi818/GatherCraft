package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Random;

/**
 * 낚시 스킬 핸들러
 * - 낚시로 아이템 획득 시 XP 적립
 * - 레벨별 보너스: 쓰레기 드롭 감소, 낚시 속도(TODO), 희귀 아이템 확률, 추가 물고기, 보물 확률
 */
public class FishingHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public void onItemFished(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        List<ItemStack> drops = event.getDrops();
        if (drops.isEmpty()) return;

        SkillManager.addXP(player, SkillType.FISHING, 15);

        int level = SkillData.getLevel(player, SkillType.FISHING);

        // 10레벨: 쓰레기 드롭 감소 (쓰레기 아이템 제거 확률)
        if (level >= 10) {
            drops.removeIf(stack -> isJunk(stack) && RANDOM.nextDouble() < junkRemoveChance(level));
        }

        // 60레벨: 추가 물고기 드롭
        if (level >= 60 && RANDOM.nextDouble() < 0.25) {
            drops.add(new ItemStack(Items.COD));
        }

        // 80레벨: 보물 드롭 확률 대폭 증가 (enchanted book 추가)
        if (level >= 80 && RANDOM.nextDouble() < 0.15) {
            drops.add(new ItemStack(Items.BOOK)); // TODO: 랜덤 인챈트 책으로 교체
        }

        // 90레벨: 쓰레기 완전 제거
        if (level >= 90) {
            drops.removeIf(this::isJunk);
        }

        // 100레벨 각성: 희귀 커스텀 아이템 (TODO: 커스텀 아이템 등록 후 구현)
        if (level >= 100 && RANDOM.nextDouble() < 0.05) {
            drops.add(new ItemStack(Items.NAUTILUS_SHELL));
        }
    }

    private boolean isJunk(ItemStack stack) {
        return stack.is(Items.LILY_PAD)
            || stack.is(Items.BOWL)
            || stack.is(Items.STRING)
            || stack.is(Items.BONE)
            || stack.is(Items.INK_SAC)
            || stack.is(Items.TRIPWIRE_HOOK);
    }

    private double junkRemoveChance(int level) {
        if (level >= 80) return 0.80;
        if (level >= 50) return 0.50;
        if (level >= 30) return 0.30;
        return 0.15;
    }
}
