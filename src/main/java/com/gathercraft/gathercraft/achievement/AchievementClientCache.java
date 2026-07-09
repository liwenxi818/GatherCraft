package com.gathercraft.gathercraft.achievement;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class AchievementClientCache {

    private static List<String> unlocked = new ArrayList<>();
    private static List<String> claimed = new ArrayList<>();
    private static Map<String, Integer> counters = new HashMap<>();

    public static void set(List<String> newUnlocked, List<String> newClaimed, Map<String, Integer> newCounters) {
        unlocked = new ArrayList<>(newUnlocked);
        claimed = new ArrayList<>(newClaimed);
        counters = new HashMap<>(newCounters);
    }

    public static List<String> getUnlocked() {
        return Collections.unmodifiableList(unlocked);
    }

    public static List<String> getClaimed() {
        return Collections.unmodifiableList(claimed);
    }

    public static boolean isClaimed(String id) {
        return claimed.contains(id);
    }

    public static int getCounter(String key) {
        return counters.getOrDefault(key, 0);
    }
}
