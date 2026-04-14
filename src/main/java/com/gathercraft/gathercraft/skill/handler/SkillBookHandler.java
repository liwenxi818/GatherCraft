package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.item.SkillBookItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 핫바 9번째 슬롯(index 8)에 스킬 책 아이템을 항상 유지.
 * - 매 100틱마다 확인
 * - 슬롯에 다른 아이템이 있으면 빈 슬롯으로 이동
 * - 스킬 책이 없으면 자동 지급
 */
public class SkillBookHandler {

    private static final int SKILL_BOOK_SLOT = 8; // 핫바 9번째 슬롯 (0-indexed)

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        // 매 100틱(5초)마다 확인 — 성능 최적화
        if (player.tickCount % 100 != player.getId() % 100) return;

        ensureSkillBook(player);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ensureSkillBook(player);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ensureSkillBook(player);
    }

    private void ensureSkillBook(ServerPlayer player) {
        ItemStack slotStack = player.getInventory().getItem(SKILL_BOOK_SLOT);

        // 이미 스킬 책이면 OK
        if (slotStack.getItem() instanceof SkillBookItem) return;

        // 슬롯에 다른 아이템이 있으면 빈 슬롯으로 이동
        if (!slotStack.isEmpty()) {
            moveToEmptySlot(player, slotStack.copy());
        }

        // 스킬 책 지급
        player.getInventory().setItem(SKILL_BOOK_SLOT, new ItemStack(SkillBookItem.INSTANCE));
    }

    /**
     * 아이템을 인벤토리의 빈 슬롯으로 이동.
     * 빈 슬롯이 없으면 드롭.
     */
    private void moveToEmptySlot(ServerPlayer player, ItemStack stack) {
        // 핫바(0-8) 제외한 메인 인벤토리(9-35)에서 빈 슬롯 탐색
        for (int i = 9; i < 36; i++) {
            if (player.getInventory().getItem(i).isEmpty()) {
                player.getInventory().setItem(i, stack);
                return;
            }
        }
        // 핫바에서 빈 슬롯 탐색 (8번 제외)
        for (int i = 0; i < SKILL_BOOK_SLOT; i++) {
            if (player.getInventory().getItem(i).isEmpty()) {
                player.getInventory().setItem(i, stack);
                return;
            }
        }
        // 빈 슬롯이 없으면 드롭
        player.drop(stack, false);
    }
}
