package com.gathercraft.gathercraft.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * 스킬 책 아이템 — 핫바 9번째 슬롯에 상시 표시.
 * 우클릭 시 스킬 현황 GUI를 오픈한다.
 * 클라이언트 사이드에서 직접 Screen을 열기 때문에 서버 GUI 없이 동작.
 */
public class SkillBookItem extends Item {

    public static final SkillBookItem INSTANCE = new SkillBookItem();

    private SkillBookItem() {
        super(new Item.Properties()
            .stacksTo(1)
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.gathercraft.gathercraft.client.gui.SkillBookScreen.open()
            );
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** 아이템 이름 (한국어) */
    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("§6스킬 책");
    }

    /** 인첸트 글로우 효과 표시 */
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
