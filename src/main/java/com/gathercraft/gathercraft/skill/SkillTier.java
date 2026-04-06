package com.gathercraft.gathercraft.skill;

public enum SkillTier {
    NOVICE(1, "입문"),
    APPRENTICE(10, "견습"),
    SKILLED(20, "숙련"),
    EXPERT(30, "전문"),
    ARTISAN(40, "장인"),
    MASTER(50, "명인"),
    LEGEND(60, "전설"),
    IMMORTAL(70, "불멸"),
    MYTH(80, "신화"),
    AWAKENED(90, "각성");

    private final int minLevel;
    private final String displayName;

    SkillTier(int minLevel, String displayName) {
        this.minLevel = minLevel;
        this.displayName = displayName;
    }

    public static SkillTier fromLevel(int level) {
        SkillTier result = NOVICE;
        for (SkillTier tier : values()) {
            if (level >= tier.minLevel) result = tier;
        }
        return result;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public String getDisplayName() {
        return displayName;
    }
}
