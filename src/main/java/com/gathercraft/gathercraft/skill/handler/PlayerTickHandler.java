package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillHUD;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

/**
 * 플레이어 틱마다 지속 효과/속성 적용
 * - 채광: Haste I/II/III (20/40/80레벨)
 * - 방어: 체력 최대치 +2/+4 (40/70레벨), 넉백 저항 (20레벨)
 * - 성능 최적화: 100틱마다 속성 갱신, 80틱마다 효과 갱신
 */
public class PlayerTickHandler {

    private static final UUID HEALTH_BONUS_UUID = UUID.fromString("a3f1c2d4-e5b6-4890-abcd-ef1234567890");
    private static final UUID KNOCKBACK_UUID    = UUID.fromString("b4e2d3c5-f6a7-5901-bcde-fe2345678901");

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        long tick = player.tickCount;

        // 매 틱: 스킬 HUD ActionBar 업데이트
        SkillHUD.tick(player);

        // 80틱(4초)마다 채광 Haste 효과 갱신
        if (tick % 80 == player.getId() % 80) {
            applyMiningHaste(player);
        }

        // 100틱(5초)마다 방어 속성 갱신
        if (tick % 100 == player.getId() % 100) {
            applyDefenseAttributes(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        SkillHUD.remove(event.getEntity().getUUID());
    }

    private void applyMiningHaste(ServerPlayer player) {
        int level = SkillData.getLevel(player, SkillType.MINING);
        if (level < 20) return;

        int hasteAmplifier;
        if (level >= 80) {
            hasteAmplifier = 2; // Haste III
        } else if (level >= 40) {
            hasteAmplifier = 1; // Haste II
        } else {
            hasteAmplifier = 0; // Haste I
        }

        // 90틱 지속 (80틱 주기보다 길어야 끊기지 않음)
        player.addEffect(new MobEffectInstance(
            MobEffects.DIG_SPEED,
            90,
            hasteAmplifier,
            true,   // ambient (틱 파티클 없음)
            false   // showIcon은 표시
        ));
    }

    private void applyDefenseAttributes(ServerPlayer player) {
        int level = SkillData.getLevel(player, SkillType.DEFENSE);

        // 체력 최대치 보너스 (40레벨: +4HP / 70레벨: +8HP)
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_BONUS_UUID);
            if (level >= 70) {
                healthAttr.addPermanentModifier(new AttributeModifier(
                    HEALTH_BONUS_UUID, "gathercraft_defense_health", 8.0, Operation.ADDITION
                ));
            } else if (level >= 40) {
                healthAttr.addPermanentModifier(new AttributeModifier(
                    HEALTH_BONUS_UUID, "gathercraft_defense_health", 4.0, Operation.ADDITION
                ));
            }
        }

        // 넉백 저항 (20레벨: 20% 저항)
        AttributeInstance knockbackAttr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackAttr != null) {
            knockbackAttr.removeModifier(KNOCKBACK_UUID);
            if (level >= 20) {
                knockbackAttr.addPermanentModifier(new AttributeModifier(
                    KNOCKBACK_UUID, "gathercraft_defense_knockback", 0.2, Operation.ADDITION
                ));
            }
        }
    }
}
