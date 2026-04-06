package com.gathercraft.gathercraft.skill;

public enum SkillType {
    MINING("채광", "Mining"),
    LUMBERJACK("벌목", "Lumberjack"),
    FARMING("농사", "Farming"),
    FISHING("낚시", "Fishing"),
    COOKING("요리", "Cooking"),
    HUNTING("사냥", "Hunting"),
    DEFENSE("방어", "Defense"),
    SMITHING("대장장이", "Smithing"),
    ENCHANTING("마법부여", "Enchanting");

    private final String koreanName;
    private final String englishName;

    SkillType(String koreanName, String englishName) {
        this.koreanName = koreanName;
        this.englishName = englishName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public String getEnglishName() {
        return englishName;
    }
}
