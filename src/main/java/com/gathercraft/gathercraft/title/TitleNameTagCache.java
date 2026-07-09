package com.gathercraft.gathercraft.title;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 주변 플레이어의 착용 칭호 캐시 (이름표 렌더링용). */
@OnlyIn(Dist.CLIENT)
public class TitleNameTagCache {

    private static final Map<UUID, String> cache = new HashMap<>();

    public static void set(UUID uuid, String titleId) {
        cache.put(uuid, titleId);
    }

    public static String get(UUID uuid) {
        return cache.get(uuid);
    }

    public static void remove(UUID uuid) {
        cache.remove(uuid);
    }
}
