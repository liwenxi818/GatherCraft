package com.gathercraft.gathercraft.waypoint;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class WaypointClientCache {

    private static List<WaypointData> cache = new ArrayList<>();

    public static void set(List<WaypointData> waypoints) {
        cache = new ArrayList<>(waypoints);
    }

    public static List<WaypointData> get() {
        return Collections.unmodifiableList(cache);
    }
}
