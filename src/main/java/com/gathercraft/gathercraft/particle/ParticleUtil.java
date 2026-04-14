package com.gathercraft.gathercraft.particle;

import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3f;

public class ParticleUtil {

    /** 중심 좌표 기준 원형으로 파티클 스폰 */
    public static void spawnCircle(ServerLevel level, double cx, double cy, double cz,
                                    ParticleOptions particle, double radius, int count, double height) {
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = cx + radius * Math.cos(angle);
            double z = cz + radius * Math.sin(angle);
            level.sendParticles(particle, x, cy + height, z, 1, 0, 0, 0, 0);
        }
    }

    /** 지점 중심 폭발형 파티클 스폰 */
    public static void spawnBurst(ServerLevel level, double x, double y, double z,
                                   ParticleOptions particle, int count, double spread) {
        level.sendParticles(particle, x, y + 0.5, z, count, spread, spread, spread, 0.1);
    }

    /** DustParticleOptions 생성 */
    private static DustParticleOptions dust(float r, float g, float b, float size) {
        return new DustParticleOptions(new Vector3f(r, g, b), size);
    }

    /** 스킬별 색상 DustParticleOptions 반환 */
    public static DustParticleOptions getSkillColor(SkillType skill) {
        return switch (skill) {
            case MINING      -> dust(0.7f, 0.7f, 0.7f, 1.2f);
            case LUMBERJACK  -> dust(0.2f, 0.8f, 0.2f, 1.2f);
            case FARMING     -> dust(0.6f, 0.9f, 0.2f, 1.2f);
            case FISHING     -> dust(0.2f, 0.8f, 0.9f, 1.2f);
            case COOKING     -> dust(1.0f, 0.5f, 0.0f, 1.2f);
            case HUNTING     -> dust(0.9f, 0.2f, 0.2f, 1.2f);
            case DEFENSE     -> dust(0.2f, 0.4f, 0.9f, 1.2f);
            case SMITHING    -> dust(1.0f, 0.8f, 0.2f, 1.2f);
            case ENCHANTING  -> dust(0.7f, 0.2f, 0.9f, 1.2f);
        };
    }
}
