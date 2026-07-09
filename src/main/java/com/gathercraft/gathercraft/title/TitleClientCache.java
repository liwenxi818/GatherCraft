package com.gathercraft.gathercraft.title;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TitleClientCache {

    private static List<String> unlocked = new ArrayList<>();
    private static String equipped = "";

    public static void set(List<String> newUnlocked, String newEquipped) {
        unlocked = new ArrayList<>(newUnlocked);
        equipped = newEquipped;
    }

    public static List<String> getUnlocked() {
        return Collections.unmodifiableList(unlocked);
    }

    public static String getEquipped() {
        return equipped;
    }
}
