package com.gathercraft.gathercraft.waypoint;

import net.minecraft.nbt.CompoundTag;

/** 웨이포인트 1개의 데이터. NBT 직렬화 담당. */
public class WaypointData {

    public String name;
    public String icon;       // "home" | "village" | "end" | "nether" | "custom"
    public String dimension;  // 예: "minecraft:overworld"
    public double x, y, z;
    public float yaw;

    public WaypointData(String name, String icon, String dimension,
                         double x, double y, double z, float yaw) {
        this.name = name;
        this.icon = icon;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
    }

    public static WaypointData fromNBT(CompoundTag tag) {
        return new WaypointData(
            tag.getString("name"),
            tag.getString("icon"),
            tag.getString("dim"),
            tag.getDouble("x"),
            tag.getDouble("y"),
            tag.getDouble("z"),
            tag.getFloat("yaw")
        );
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putString("icon", icon);
        tag.putString("dim", dimension);
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putFloat("yaw", yaw);
        return tag;
    }
}
