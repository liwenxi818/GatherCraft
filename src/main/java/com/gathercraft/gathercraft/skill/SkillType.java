package com.gathercraft.gathercraft.skill;

public enum SkillType {
    MINING    ("채광",   "Mining",     0x808080),
    LUMBERJACK("벌목",   "Lumberjack", 0x55FF55),
    FARMING   ("농사",   "Farming",    0xFFFF55),
    FISHING   ("낚시",   "Fishing",    0x55FFFF),
    COOKING   ("요리",   "Cooking",    0xFFAA00),
    HUNTING   ("사냥",   "Hunting",    0xFF5555),
    DEFENSE   ("방어",   "Defense",    0x5555FF),
    SMITHING  ("대장장이","Smithing",   0xAA6600),
    ENCHANTING("마법부여","Enchanting", 0xAA00AA);

    private final String koreanName;
    private final String englishName;
    /** UI 표시용 RGB 색상 (0xRRGGBB) */
    public final int color;

    SkillType(String koreanName, String englishName, int color) {
        this.koreanName = koreanName;
        this.englishName = englishName;
        this.color = color;
    }

    public String getKoreanName()  { return koreanName; }
    public String getEnglishName() { return englishName; }

    /**
     * 이름으로 스킬을 검색한다. enum명(대소문자 무시), 한국어명, 영어명 모두 허용.
     * 일치하는 스킬이 없으면 null 반환.
     */
    public static SkillType findByName(String name) {
        String lower = name.toLowerCase();
        for (SkillType skill : values()) {
            if (skill.name().equalsIgnoreCase(lower)
                    || skill.koreanName.equals(name)
                    || skill.englishName.equalsIgnoreCase(lower)) {
                return skill;
            }
        }
        return null;
    }
}
