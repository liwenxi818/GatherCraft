package com.gathercraft.gathercraft.title;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

/**
 * 착용 중인 칭호를 채팅 메시지 앞에도 표시한다.
 * 1.20.1의 서명된(secure) 채팅은 ChatType 바인딩으로 발신자 표시를 클라이언트가 직접 그리기 때문에
 * ServerChatEvent#setMessage()로는 표시 이름을 바꿀 수 없다. 따라서 칭호 착용자의 메시지만 이벤트를
 * 취소하고 서명되지 않은 시스템 메시지로 직접 재브로드캐스트한다(다른 프로젝트 채팅 관련 기능과 동일한
 * "안전 취소 + Component.literal 재발송" 패턴).
 */
public class TitleChatHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        String equippedId = TitleManager.getEquipped(player);
        if (equippedId == null || equippedId.isEmpty()) return;

        String titleDisplay = TitleManager.getDisplayName(equippedId);
        if (titleDisplay.isEmpty()) return;

        event.setCanceled(true);

        String plain = titleDisplay + " <" + event.getUsername() + "> " + event.getRawText();
        Component formatted = Component.literal(titleDisplay + " §f<" + event.getUsername() + "> " + event.getRawText());

        player.getServer().getPlayerList().broadcastSystemMessage(formatted, false);
        LOGGER.info("[CHAT] {}", plain);
    }
}
