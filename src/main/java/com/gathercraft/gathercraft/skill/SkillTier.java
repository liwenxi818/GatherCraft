package com.gathercraft.gathercraft.skill;

public enum SkillTier {
    NOVICE    ( 1,  "입문", 0x808080, 0x444444),
    APPRENTICE(10,  "견습", 0xFFFFFF, 0x888888),
    SKILLED   (20,  "숙련", 0x55FF55, 0x2A7F2A),
    EXPERT    (30,  "전문", 0x5555FF, 0x2A2A7F),
    ARTISAN   (40,  "장인", 0xAA00AA, 0x550055),
    MASTER    (50,  "명인", 0xFFAA00, 0x7F5500),
    LEGEND    (60,  "전설", 0xFF5555, 0x7F2A2A),
    IMMORTAL  (70,  "불멸", 0xFF55FF, 0x7F2A7F),
    MYTH      (80,  "신화", 0x55FFFF, 0x2A7F7F),
    AWAKENED  (90,  "각성", 0xFFD700, 0x806B00);

    private final int minLevel;
    private final String displayName;
    /** 슬롯 배경/테두리용 RGB 색상 */
    public final int color;
    /** 티어 텍스트용 더 어두운 RGB 색상 */
    public final int textColor;

    SkillTier(int minLevel, String displayName, int color, int textColor) {
        this.minLevel    = minLevel;
        this.displayName = displayName;
        this.color       = color;
        this.textColor   = textColor;
    }

    public int    getMinLevel()    { return minLevel; }
    public String getDisplayName() { return displayName; }

    public static SkillTier fromLevel(int level) {
        SkillTier result = NOVICE;
        for (SkillTier tier : values()) {
            if (level >= tier.minLevel) result = tier;
        }
        return result;
    }
}
