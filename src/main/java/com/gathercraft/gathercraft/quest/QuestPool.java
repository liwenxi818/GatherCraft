package com.gathercraft.gathercraft.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** 일일 퀘스트 풀 (쉬움/보통/어려움 각 8개) 및 시드 기반 선택 로직. */
public class QuestPool {

    private static List<QuestData> easyPool() {
        List<QuestData> list = new ArrayList<>();
        list.add(new QuestData("mine_coal_30", "석탄 30개 채굴", "MINING", "mine", "COAL_ORE", 30, 0, false, false, 300, 1));
        list.add(new QuestData("mine_iron_20", "철 20개 채굴", "MINING", "mine", "IRON_ORE", 20, 0, false, false, 400, 1));
        list.add(new QuestData("hunt_zombie_20", "좀비 20마리 처치", "HUNTING", "hunt", "ZOMBIE", 20, 0, false, false, 400, 1));
        list.add(new QuestData("hunt_skeleton_15", "스켈레톤 15마리 처치", "HUNTING", "hunt", "SKELETON", 15, 0, false, false, 350, 1));
        list.add(new QuestData("farm_wheat_30", "작물 30개 수확", "FARMING", "farm", "ANY", 30, 0, false, false, 300, 1));
        list.add(new QuestData("fish_any_10", "물고기 10마리 낚기", "FISHING", "fish", "ANY", 10, 0, false, false, 300, 1));
        list.add(new QuestData("cook_any_20", "음식 20개 요리", "COOKING", "cook", "ANY", 20, 0, false, false, 320, 1));
        list.add(new QuestData("lumberjack_log_30", "원목 30개 채굴", "LUMBERJACK", "lumberjack", "ANY", 30, 0, false, false, 280, 1));
        return list;
    }

    private static List<QuestData> mediumPool() {
        List<QuestData> list = new ArrayList<>();
        list.add(new QuestData("mine_gold_15", "금 15개 채굴", "MINING", "mine", "GOLD_ORE", 15, 0, false, false, 800, 2));
        list.add(new QuestData("mine_diamond_5", "다이아 5개 채굴", "MINING", "mine", "DIAMOND_ORE", 5, 0, false, false, 1200, 3));
        list.add(new QuestData("hunt_creeper_20", "크리퍼 20마리 처치", "HUNTING", "hunt", "CREEPER", 20, 0, false, false, 800, 2));
        list.add(new QuestData("hunt_blaze_10", "블레이즈 10마리 처치", "HUNTING", "hunt", "BLAZE", 10, 0, false, false, 1000, 2));
        list.add(new QuestData("farm_harvest_80", "작물 80개 수확", "FARMING", "farm", "ANY", 80, 0, false, false, 800, 2));
        list.add(new QuestData("fish_rare_5", "아이템 5개 낚기", "FISHING", "fish", "ANY", 5, 0, false, false, 900, 2));
        list.add(new QuestData("cook_meat_30", "음식 30개 요리", "COOKING", "cook", "ANY", 30, 0, false, false, 750, 2));
        list.add(new QuestData("lumberjack_log_80", "원목 80개 채굴", "LUMBERJACK", "lumberjack", "ANY", 80, 0, false, false, 700, 2));
        return list;
    }

    private static List<QuestData> hardPool() {
        List<QuestData> list = new ArrayList<>();
        list.add(new QuestData("mine_diamond_15", "다이아 15개 채굴", "MINING", "mine", "DIAMOND_ORE", 15, 0, false, false, 3000, 4));
        list.add(new QuestData("mine_ancient_3", "고대잔해 3개 채굴", "MINING", "mine", "ANCIENT_DEBRIS", 3, 0, false, false, 4000, 5));
        list.add(new QuestData("hunt_boss_1", "보스 1마리 처치", "HUNTING", "hunt", "BOSS", 1, 0, false, false, 5000, 5));
        list.add(new QuestData("hunt_enderman_30", "엔더맨 30마리 처치", "HUNTING", "hunt", "ENDERMAN", 30, 0, false, false, 2500, 4));
        list.add(new QuestData("farm_harvest_200", "작물 200개 수확", "FARMING", "farm", "ANY", 200, 0, false, false, 2000, 3));
        list.add(new QuestData("fish_any_50", "물고기 50마리 낚기", "FISHING", "fish", "ANY", 50, 0, false, false, 2500, 4));
        list.add(new QuestData("cook_any_100", "음식 100개 요리", "COOKING", "cook", "ANY", 100, 0, false, false, 2000, 3));
        list.add(new QuestData("hunt_warden_1", "워든 1마리 처치", "HUNTING", "hunt", "WARDEN", 1, 0, false, false, 4000, 5));
        return list;
    }

    /** seed 기반으로 쉬움 1개 + 보통 1개 + 어려움 1개를 선택해 반환한다. */
    public static List<QuestData> getDailyQuests(long seed) {
        Random random = new Random(seed);
        List<QuestData> easy = easyPool();
        List<QuestData> medium = mediumPool();
        List<QuestData> hard = hardPool();

        List<QuestData> result = new ArrayList<>();
        result.add(easy.get(random.nextInt(easy.size())));
        result.add(medium.get(random.nextInt(medium.size())));
        result.add(hard.get(random.nextInt(hard.size())));
        return result;
    }
}
