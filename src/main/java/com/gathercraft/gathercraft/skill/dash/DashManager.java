package com.gathercraft.gathercraft.skill.dash;

import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.DashSyncPacket;
import com.gathercraft.gathercraft.skill.SkillData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class DashManager {

    public static final long COOLDOWN_TICKS = 200L;  // 10초
    public static final int INVUL_TICKS = 15;         // 0.75초 무적
    public static final int DASH_TRAIL_TICKS = 10;    // 잔상 지속 틱

    private static final double DASH_SPEED_GROUND = 1.2;
    private static final double DASH_SPEED_AIR = 1.1;
    private static final double AIR_Y_FACTOR = 0.4;

    public static void tryDash(ServerPlayer player) {
        CompoundTag gc = SkillData.getRoot(player);

        // 쿨타임 체크
        long gameTime = player.level().getGameTime();
        if (gameTime < gc.getLong("dash_cooldown_end")) {
            return;
        }

        // 대시 중복 방지
        if (gc.getBoolean("IsDashing")) {
            return;
        }

        // look 방향 계산 (수평 정규화)
        Vec3 look = player.getLookAngle();
        Vec3 horiz = new Vec3(look.x, 0, look.z);
        if (horiz.lengthSqr() < 1e-6) return;  // 수직으로만 바라보는 경우 방지
        Vec3 norm = horiz.normalize();

        double vx, vy, vz;
        if (player.onGround()) {
            vx = norm.x * DASH_SPEED_GROUND;
            vy = 0.2;
            vz = norm.z * DASH_SPEED_GROUND;
        } else {
            vx = norm.x * DASH_SPEED_AIR;
            vy = look.y * AIR_Y_FACTOR;
            vz = norm.z * DASH_SPEED_AIR;
        }

        // velocity 적용 (hurtMarked = true 로 클라이언트 강제 동기화)
        player.setDeltaMovement(vx, vy, vz);
        player.hurtMarked = true;

        // 무적 적용
        player.invulnerableTime = INVUL_TICKS;

        // NBT 상태 저장
        gc.putLong("dash_cooldown_end", gameTime + COOLDOWN_TICKS);
        gc.putBoolean("IsDashing", true);
        gc.putInt("DashTicksLeft", DASH_TRAIL_TICKS);
        SkillData.saveRoot(player, gc);

        // 시작 파티클 + 효과음
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                player.getX(), player.getY() + 1.0, player.getZ(),
                5, 0.4, 0.4, 0.4, 0.0);
            serverLevel.playSound(null, player.blockPosition(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.3f);
        }

        // 클라이언트 UI 쿨타임 동기화
        PacketHandler.sendToPlayer(player, new DashSyncPacket(COOLDOWN_TICKS));
    }
}
