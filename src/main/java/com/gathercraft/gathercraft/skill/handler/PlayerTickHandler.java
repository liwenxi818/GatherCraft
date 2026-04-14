package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import javax.annotation.Nullable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

/**
 * 플레이어 틱마다 지속 효과/속성 적용
 * - 채광: Haste I/II/III (20/40/80레벨)
 * - 방어: 체력 최대치 +2/+4 (40/70레벨), 넉백 저항 (20레벨)
 * - 성능 최적화: 100틱마다 속성 갱신, 80틱마다 효과 갱신
 * - 모션: 무적 시간 중 ENCHANT 파티클 잔상
 */
public class PlayerTickHandler {

    private static final UUID HEALTH_BONUS_UUID = UUID.fromString("a3f1c2d4-e5b6-4890-abcd-ef1234567890");
    private static final UUID KNOCKBACK_UUID    = UUID.fromString("b4e2d3c5-f6a7-5901-bcde-fe2345678901");

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        long tick = player.tickCount;

        // 5틱마다 무적 시간 확인 → ENCHANT 파티클 잔상
        if (tick % 5 == 0 && player.invulnerableTime > 0 && player.invulnerableTime < 15) {
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    4, 0.4, 0.4, 0.4, 0.3);
            }
        }

        // 대시 잔상/도착 파티클 처리
        CompoundTag gcData = SkillData.getRoot(player);
        if (gcData.getBoolean("IsDashing") && player.level() instanceof ServerLevel serverLevel) {
            int trailLeft = gcData.getInt("DashTicksLeft");
            if (trailLeft > 0) {
                if (tick % 2 == 0) {
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                        player.getX(), player.getY() + 0.5, player.getZ(),
                        3, 0.2, 0.2, 0.2, 0.02);
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                        player.getX(), player.getY() + 0.5, player.getZ(),
                        5, 0.3, 0.3, 0.3, 0.1);
                }
                gcData.putInt("DashTicksLeft", trailLeft - 1);
            } else {
                // 대시 종료 - 도착 파티클 + 효과음
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    1, 0, 0, 0, 0);
                serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.GENERIC_EXPLODE,
                    SoundSource.PLAYERS, 0.6f, 1.8f);
                gcData.putBoolean("IsDashing", false);
            }
            // getRoot()는 실제 참조를 반환하므로 수정 내용이 자동 반영됨
            // saveRoot()는 데이터 안전을 위해 명시적으로 호출
            SkillData.saveRoot(player, gcData);
        }

        // 80틱(4초)마다 채광 Haste 효과 갱신
        if (tick % 80 == player.getId() % 80) {
            applyMiningHaste(player);
        }

        // 100틱(5초)마다 방어 속성 갱신
        if (tick % 100 == player.getId() % 100) {
            applyDefenseAttributes(player);
        }
    }

    /**
     * 로그인 시 대기 중인 스킬 포인트 offer를 재전송한다.
     * 오프라인 레벨업 또는 GUI를 닫고 선택을 미룬 경우 모두 처리.
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        for (SkillType skill : SkillType.values()) {
            if (SkillData.getPendingCount(sp, skill) > 0) {
                SkillManager.sendSkillPointOffer(sp, skill);
            }
        }
    }

    /**
     * 사망 후 리스폰 시 GatherCraft NBT 데이터 복사.
     * Forge는 사망 시 새 플레이어 엔티티를 생성하므로 수동 복사가 필요.
     */
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        CompoundTag originalData = event.getOriginal().getPersistentData();
        if (originalData.contains(SkillData.ROOT_KEY)) {
            event.getEntity().getPersistentData()
                .put(SkillData.ROOT_KEY, originalData.getCompound(SkillData.ROOT_KEY).copy());
        }
    }

    private void applyMiningHaste(ServerPlayer player) {
        int level = SkillData.getLevel(player, SkillType.MINING);
        if (level < 20) return;

        int hasteAmplifier = level >= 80 ? 2 : level >= 40 ? 1 : 0;

        // 90틱 지속 (80틱 주기보다 길어야 끊기지 않음)
        player.addEffect(new MobEffectInstance(
            MobEffects.DIG_SPEED,
            90,
            hasteAmplifier,
            true,   // ambient (틱 파티클 없음)
            false
        ));
    }

    /** 외부(레벨업 등)에서 즉시 방어 속성을 갱신할 때 사용 */
    public static void applyDefenseAttributesNow(ServerPlayer player) {
        new PlayerTickHandler().applyDefenseAttributes(player);
    }

    private void applyDefenseAttributes(ServerPlayer player) {
        int level = SkillData.getLevel(player, SkillType.DEFENSE);

        // 체력 최대치 보너스: 레벨 기반 + 스탯 포인트 누적
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            double levelBonus = level >= 70 ? 8.0 : level >= 40 ? 4.0 : 0.0;
            double statBonus  = SkillData.getStatValue(player, SkillPointStat.DEFENSE_MAX_HEALTH);
            double desired    = levelBonus + statBonus;
            @Nullable AttributeModifier existing = healthAttr.getModifier(HEALTH_BONUS_UUID);
            double current = existing != null ? existing.getAmount() : 0.0;
            if (Math.abs(desired - current) > 0.01) {
                healthAttr.removeModifier(HEALTH_BONUS_UUID);
                if (desired > 0) {
                    healthAttr.addPermanentModifier(new AttributeModifier(
                        HEALTH_BONUS_UUID, "gathercraft_defense_health", desired, Operation.ADDITION
                    ));
                }
            }
        }

        // 넉백 저항: 20레벨부터 기본 0.2 + 스탯 누적
        AttributeInstance knockbackAttr = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackAttr != null) {
            float statKnockback = SkillData.getStatValue(player, SkillPointStat.DEFENSE_KNOCKBACK);
            double desired = (level >= 20 ? 0.2 : 0.0) + statKnockback;
            desired = Math.min(desired, 0.8); // 최대 80%로 캡
            @Nullable AttributeModifier existing = knockbackAttr.getModifier(KNOCKBACK_UUID);
            double current = existing != null ? existing.getAmount() : 0.0;
            boolean shouldHave = desired > 0;
            boolean hasModifier = existing != null;
            if (shouldHave != hasModifier || Math.abs(desired - current) > 0.001) {
                knockbackAttr.removeModifier(KNOCKBACK_UUID);
                if (shouldHave) {
                    knockbackAttr.addPermanentModifier(new AttributeModifier(
                        KNOCKBACK_UUID, "gathercraft_defense_knockback", desired, Operation.ADDITION
                    ));
                }
            }
        }
    }
}
