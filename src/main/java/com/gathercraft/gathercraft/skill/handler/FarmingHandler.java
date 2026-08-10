package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.achievement.AchievementManager;
import com.gathercraft.gathercraft.quest.QuestManager;
import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillPointStat;
import com.gathercraft.gathercraft.skill.SkillType;
import com.gathercraft.gathercraft.skill.SkillUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 농사 스킬 핸들러
 * - 완전히 자란 작물 수확 시 XP 적립
 * - 레벨별 보너스: 추가 드롭, 자동 재식(40레벨), 뼛가루 2회 효과(20레벨)/즉시 완숙(40+레벨), 성장 가속, 5x5 수확(100레벨)
 */
public class FarmingHandler {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        BlockState state = event.getState();

        if (!isFullyGrownCrop(state)) return;

        SkillManager.addXP(player, SkillType.FARMING, 6);

        QuestManager.progress(player, "farm", "ANY", 1);
        AchievementManager.incrementAndCheck(player, "harvest", 1, "harvest_1000");

        int level = SkillData.getLevel(player, SkillType.FARMING);
        ServerLevel world = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        double bx = pos.getX() + 0.5;
        double by = pos.getY() + 0.5;
        double bz = pos.getZ() + 0.5;

        world.sendParticles(ParticleTypes.COMPOSTER, bx, by + 0.5, bz, 6, 0.3, 0.2, 0.3, 0.05);

        double extraDropChance = extraDropChance(level);
        if (extraDropChance > 0 && ThreadLocalRandom.current().nextDouble() < extraDropChance) {
            SkillUtil.spawnExtraDrops(state, world, pos, player);
        }

        if (level >= 40) {
            autoReplant(state, world, pos);
            world.sendParticles(ParticleTypes.HAPPY_VILLAGER, bx, by + 0.8, bz, 4, 0.2, 0.2, 0.2, 0.05);
        }

        // [4] 희귀 작물 드롭 (50레벨 3%, 70레벨 8%, 90레벨 15%)
        if (level >= 50) {
            double rareChance;
            if (level >= 90) rareChance = 0.15;
            else if (level >= 70) rareChance = 0.08;
            else rareChance = 0.03;

            if (ThreadLocalRandom.current().nextDouble() < rareChance) {
                Item[] rarePool = {
                    Items.SWEET_BERRIES, Items.GLOW_BERRIES,
                    Items.NETHER_WART, Items.CHORUS_FRUIT
                };
                Item chosen = rarePool[ThreadLocalRandom.current().nextInt(rarePool.length)];
                world.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(
                    world, bx, by, bz, new ItemStack(chosen)));
            }
        }

        if (level >= 100 && ThreadLocalRandom.current().nextDouble() < 0.25) {
            triggerAreaHarvest(player, world, pos);
        }
    }

    // FARMING_BONEMEAL: 20레벨은 뼛가루 1개로 성장 효과 2회, 40/70/90레벨은 확률적 즉시 완숙
    @SubscribeEvent
    public void onBonemeal(BonemealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        int level = SkillData.getLevel(player, SkillType.FARMING);
        if (level < 20) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getBlock();
        if (!(state.getBlock() instanceof CropBlock cropBlock)) return;

        if (level >= 40) {
            double baseChance = level >= 90 ? 0.80 : level >= 70 ? 0.50 : 0.25;
            float statBonus = SkillData.getStatValue(player, SkillPointStat.FARMING_BONEMEAL);
            double chance = Math.min(baseChance + statBonus, 1.0);
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                BlockState fullGrown = cropBlock.getStateForAge(cropBlock.getMaxAge());
                serverLevel.setBlock(pos, fullGrown, Block.UPDATE_ALL);
                event.setResult(Event.Result.ALLOW);
            }
            return;
        }

        // [20레벨] 뼛가루 1개로 performBonemeal을 2회 적용 (원본 성장 효과 2배)
        RandomSource random = serverLevel.getRandom();
        cropBlock.performBonemeal(serverLevel, random, pos, state);
        BlockState afterFirst = serverLevel.getBlockState(pos);
        if (afterFirst.getBlock() instanceof CropBlock cropBlockAfter) {
            cropBlockAfter.performBonemeal(serverLevel, random, pos, afterFirst);
        }
        event.setResult(Event.Result.ALLOW);
    }

    // FARMING_GROWTH: 자연 성장 시 추가 성장
    @SubscribeEvent
    public void onCropGrowPre(BlockEvent.CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        // 주변 16블록 플레이어 중 가장 높은 파밍 레벨
        int level = 0;
        ServerPlayer bestPlayer = null;
        for (ServerPlayer sp : serverLevel.players()) {
            if (sp.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 256.0) {
                int pLevel = SkillData.getLevel(sp, SkillType.FARMING);
                if (pLevel > level) {
                    level = pLevel;
                    bestPlayer = sp;
                }
            }
        }

        double chance;
        if (level >= 80) chance = 0.40;
        else if (level >= 50) chance = 0.20;
        else return;

        if (bestPlayer != null) {
            float statBonus = SkillData.getStatValue(bestPlayer, SkillPointStat.FARMING_GROWTH);
            chance = Math.min(chance + statBonus, 0.95);
        }

        if (!(state.getBlock() instanceof CropBlock cropBlock)) return;
        int age = cropBlock.getAge(state);
        int maxAge = cropBlock.getMaxAge();
        if (age >= maxAge) return;

        if (ThreadLocalRandom.current().nextDouble() < chance) {
            serverLevel.setBlock(pos, cropBlock.getStateForAge(Math.min(age + 1, maxAge)), Block.UPDATE_ALL);
        }
    }

    private boolean isFullyGrownCrop(BlockState state) {
        if (!state.is(BlockTags.CROPS)) return false;
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        return false;
    }

    private double extraDropChance(int level) {
        if (level >= 90) return 0.50;
        if (level >= 60) return 0.30;
        if (level >= 30) return 0.15;
        if (level >= 10) return 0.05;
        return 0;
    }

    private void autoReplant(BlockState state, ServerLevel world, BlockPos pos) {
        if (state.getBlock() instanceof CropBlock crop) {
            world.setBlock(pos, crop.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private void triggerAreaHarvest(ServerPlayer player, ServerLevel world, BlockPos center) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos target = center.offset(dx, 0, dz);
                if (isFullyGrownCrop(world.getBlockState(target))) {
                    world.sendParticles(ParticleTypes.COMPOSTER,
                        target.getX() + 0.5, target.getY() + 0.8, target.getZ() + 0.5,
                        4, 0.2, 0.2, 0.2, 0.05);
                    world.destroyBlock(target, true, player);
                }
            }
        }
    }
}
