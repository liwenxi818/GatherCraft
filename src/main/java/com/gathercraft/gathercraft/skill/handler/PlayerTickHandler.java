package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.achievement.AchievementManager;
import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.QuestSyncPacket;
import com.gathercraft.gathercraft.network.packet.SkillXpUpdatePacket;
import com.gathercraft.gathercraft.network.packet.TitleBroadcastPacket;
import com.gathercraft.gathercraft.network.packet.TitleSyncPacket;
import com.gathercraft.gathercraft.network.packet.WaypointSyncPacket;
import com.gathercraft.gathercraft.quest.QuestManager;
import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import com.gathercraft.gathercraft.skill.SkillType;
import com.gathercraft.gathercraft.skill.dash.DashManager;
import com.gathercraft.gathercraft.title.TitleManager;
import com.gathercraft.gathercraft.tpa.TpaManager;
import com.gathercraft.gathercraft.waypoint.WaypointManager;
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
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
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

        // 대시 잔상/도착 파티클 처리 — Set 체크로 비대시 틱의 NBT 읽기 방지
        if (DashManager.dashingPlayers.contains(player.getUUID())) {
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
                    DashManager.dashingPlayers.remove(player.getUUID());
                }
                SkillData.saveRoot(player, gcData);
            }
        }

        // 80틱(4초)마다 채광 Haste 효과 갱신
        if (tick % 80 == player.getId() % 80) {
            applyMiningHaste(player);
        }

        // 20틱(1초)마다 벌목 Haste + 각성 아이템 효과 갱신
        if (tick % 20 == player.getId() % 20) {
            applyLumberjackHaste(player);
            applyAwakenedItemEffect(player);
        }

        // 100틱(5초)마다 방어 속성 갱신
        if (tick % 100 == player.getId() % 100) {
            applyDefenseAttributes(player);
        }

        // 30틱(1.5초)마다 all_100 칭호 보유 시 이동속도 지속 적용
        if (tick % 30 == player.getId() % 30) {
            if (TitleManager.hasTitle(player, "all_100")) {
                player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED, 40, 0, false, false));
            }
        }
    }

    /**
     * 로그인 시 처리:
     * 1) NBT 로드 확인 (loadFromNBT)
     * 2) 각 스킬 XP 바를 클라이언트에 동기화 (SkillXpUpdatePacket)
     * 3) 대기 중인 스킬 포인트 offer 재전송
     */
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        SkillData.loadFromNBT(sp);

        // 서버 재시작 시 IsDashing 상태가 NBT에 남아있을 수 있으므로 초기화
        CompoundTag gcLogin = SkillData.getRoot(sp);
        gcLogin.putBoolean("IsDashing", false);
        gcLogin.putInt("DashTicksLeft", 0);
        SkillData.saveRoot(sp, gcLogin);
        DashManager.dashingPlayers.remove(sp.getUUID());

        // 클라이언트 HUD 오버레이 전체 동기화
        for (SkillType skill : SkillType.values()) {
            int level = SkillData.getLevel(sp, skill);
            float progress = (float) SkillManager.getXPProgress(sp, skill);
            PacketHandler.sendToPlayer(sp, new SkillXpUpdatePacket(skill, level, progress, false, false));
        }

        // 미선택 스킬 포인트 offer 재전송
        for (SkillType skill : SkillType.values()) {
            if (SkillData.getPendingCount(sp, skill) > 0) {
                SkillManager.sendSkillPointOffer(sp, skill);
            }
        }

        // MINING_SPEED/LUMBERJACK_SPEED 클라이언트 동기화 (BreakSpeed 예측용)
        SkillManager.sendSpeedStatSync(sp);

        // 웨이포인트 목록 클라이언트 동기화
        PacketHandler.sendToPlayer(sp, new WaypointSyncPacket(WaypointManager.getAll(sp)));

        // 퀘스트 갱신 체크 + 동기화
        PacketHandler.sendToPlayer(sp, new QuestSyncPacket(QuestManager.getQuests(sp)));

        // 업적 동기화
        PacketHandler.sendToPlayer(sp, AchievementManager.buildSyncPacket(sp));

        // 칭호 체크 + 클라이언트 동기화
        TitleManager.checkAndUnlock(sp);
        PacketHandler.sendToPlayer(sp, new TitleSyncPacket(TitleManager.getUnlocked(sp), TitleManager.getEquipped(sp)));

        // 칭호 이름표: 본인 칭호를 주변에 브로드캐스트 + 주변 플레이어 칭호를 본인에게 전송
        if (sp.level() instanceof ServerLevel loginLevel) {
            String myTitle = TitleManager.getEquipped(sp);
            for (ServerPlayer nearby : loginLevel.getEntitiesOfClass(ServerPlayer.class, sp.getBoundingBox().inflate(64))) {
                if (nearby == sp) continue;
                PacketHandler.sendToPlayer(nearby, new TitleBroadcastPacket(sp.getUUID(), myTitle));
                PacketHandler.sendToPlayer(sp, new TitleBroadcastPacket(nearby.getUUID(), TitleManager.getEquipped(nearby)));
            }
        }
    }

    /**
     * 로그아웃 시 NBT를 명시 저장한다.
     * updateSkill()이 이미 실시간 반영하지만, 이중 안전장치.
     */
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SkillData.saveToNBT(player);
        DashManager.dashingPlayers.remove(player.getUUID());
        TpaManager.clearPlayer(player.getUUID());
    }

    /**
     * 사망 리스폰 후 XP 바 재동기화.
     * (엔더 클리어 리스폰은 onPlayerClone에서 데이터를 복사하므로 스킵)
     */
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (event.isEndConquered()) return;

        SkillData.loadFromNBT(sp);

        for (SkillType skill : SkillType.values()) {
            int level = SkillData.getLevel(sp, skill);
            float progress = (float) SkillManager.getXPProgress(sp, skill);
            PacketHandler.sendToPlayer(sp, new SkillXpUpdatePacket(skill, level, progress, false, false));
        }
    }

    /**
     * 사망 리스폰 AND 엔더 클리어 후 데이터 복사.
     *
     * 버그 수정: 기존 코드는 isWasDeath() = true(사망)만 처리했으나,
     * 엔더 드래곤 클리어 시 isWasDeath() = false로 Clone 이벤트가 발생해
     * 데이터가 복사되지 않고 소실됐음. 조건 제거로 두 케이스 모두 처리.
     */
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag originalData = event.getOriginal().getPersistentData();
        if (originalData.contains(SkillData.ROOT_KEY)) {
            event.getEntity().getPersistentData()
                .put(SkillData.ROOT_KEY, originalData.getCompound(SkillData.ROOT_KEY).copy());
        }
    }

    // 벌목 80레벨: 도끼 들고 있을 때 Haste III 지속
    // LUMBERJACK_SPEED 스탯 보너스는 Haste가 아니라 LumberjackHandler.onBreakSpeed()에서 직접 배속으로 적용
    private void applyLumberjackHaste(ServerPlayer player) {
        int level = SkillData.getLevel(player, SkillType.LUMBERJACK);
        if (level < 80) return;
        if (!(player.getMainHandItem().getItem() instanceof AxeItem)) return;

        player.addEffect(new MobEffectInstance(
            MobEffects.DIG_SPEED,
            30,
            2,
            true,
            false
        ));
    }

    // [7b] 각성 아이템: gathercraft_awakened NBT가 있는 아이템 들면 Haste II 지속
    private void applyAwakenedItemEffect(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;
        CompoundTag tag = mainHand.getTag();
        if (tag == null || !tag.getBoolean("gathercraft_awakened")) return;

        player.addEffect(new MobEffectInstance(
            MobEffects.DIG_SPEED,
            30,
            1,
            true,
            false
        ));
    }

    // 채광 20/40/80레벨: Haste I/II/III 상시 적용
    // MINING_SPEED 스탯 보너스는 Haste가 아니라 MiningHandler.onBreakSpeed()에서 직접 배속으로 적용
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
        applyDefenseAttributes(player);
    }

    private static void applyDefenseAttributes(ServerPlayer player) {
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
