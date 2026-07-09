package com.gathercraft.gathercraft.quest;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class QuestClientCache {

    private static List<QuestData> cache = new ArrayList<>();

    public static void set(List<QuestData> quests) {
        cache = new ArrayList<>(quests);
    }

    public static List<QuestData> get() {
        return Collections.unmodifiableList(cache);
    }
}
