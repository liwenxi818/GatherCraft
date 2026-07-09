package com.gathercraft.gathercraft.quest;

import net.minecraft.nbt.CompoundTag;

/** 퀘스트 1개의 데이터. NBT 직렬화 담당. */
public class QuestData {

    public final String id;
    public final String description;
    public final String skillType;   // SkillType.name()
    public final String actionType;  // "mine"|"hunt"|"farm"|"fish"|"cook"|"lumberjack"
    public final String targetBlock; // "DIAMOND_ORE"|"ANY"|"BOSS" 등
    public final int goal;
    public int progress;
    public boolean completed;
    public boolean claimed;
    public final long rewardXP;
    public final int rewardExpBottles;

    public QuestData(String id, String description, String skillType, String actionType, String targetBlock,
                      int goal, int progress, boolean completed, boolean claimed,
                      long rewardXP, int rewardExpBottles) {
        this.id = id;
        this.description = description;
        this.skillType = skillType;
        this.actionType = actionType;
        this.targetBlock = targetBlock;
        this.goal = goal;
        this.progress = progress;
        this.completed = completed;
        this.claimed = claimed;
        this.rewardXP = rewardXP;
        this.rewardExpBottles = rewardExpBottles;
    }

    public static QuestData fromNBT(CompoundTag tag) {
        return new QuestData(
            tag.getString("id"),
            tag.getString("description"),
            tag.getString("skillType"),
            tag.getString("actionType"),
            tag.getString("targetBlock"),
            tag.getInt("goal"),
            tag.getInt("progress"),
            tag.getBoolean("completed"),
            tag.getBoolean("claimed"),
            tag.getLong("rewardXP"),
            tag.getInt("rewardExpBottles")
        );
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("description", description);
        tag.putString("skillType", skillType);
        tag.putString("actionType", actionType);
        tag.putString("targetBlock", targetBlock);
        tag.putInt("goal", goal);
        tag.putInt("progress", progress);
        tag.putBoolean("completed", completed);
        tag.putBoolean("claimed", claimed);
        tag.putLong("rewardXP", rewardXP);
        tag.putInt("rewardExpBottles", rewardExpBottles);
        return tag;
    }
}
