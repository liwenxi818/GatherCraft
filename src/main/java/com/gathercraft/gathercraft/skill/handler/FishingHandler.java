package com.gathercraft.gathercraft.skill.handler;

import com.gathercraft.gathercraft.achievement.AchievementManager;
import com.gathercraft.gathercraft.particle.ParticleUtil;
import com.gathercraft.gathercraft.quest.QuestManager;
import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillManager;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 낚시 스킬 핸들러
 * - 낚시로 아이템 획득 시 XP 적립
 * - 레벨별 보너스: 쓰레기 드롭 감소, 희귀 아이템 확률, 추가 물고기, 보물 확률, 낚시 속도
 * - 모션: SPLASH + UNDERWATER (항상), TOTEM (희귀 아이템)
 */
public class FishingHandler {

    @SubscribeEvent
    public void onItemFished(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        List<ItemStack> drops = event.getDrops();
        if (drops.isEmpty()) return;

        SkillManager.addXP(player, SkillType.FISHING, 15);

        QuestManager.progress(player, "fish", "ANY", drops.size());
        AchievementManager.incrementAndCheck(player, "fish", drops.size(), "fish_100");

        if (player.level() instanceof ServerLevel serverLevel) {
            double px = player.getX();
            double py = player.getY();
            double pz = player.getZ();
            serverLevel.sendParticles(ParticleTypes.SPLASH, px, py + 0.3, pz, 15, 0.5, 0.1, 0.5, 0.2);
            serverLevel.sendParticles(ParticleTypes.UNDERWATER, px, py + 0.5, pz, 8, 0.4, 0.3, 0.4, 0.05);
        }

        int level = SkillData.getLevel(player, SkillType.FISHING);

        // 10레벨: 쓰레기 드롭 감소
        if (level >= 10) {
            final double removeChance = junkRemoveChance(level);
            drops.removeIf(stack -> isJunk(stack) && ThreadLocalRandom.current().nextDouble() < removeChance);
        }

        // 60레벨: 추가 물고기 드롭
        if (level >= 60 && ThreadLocalRandom.current().nextDouble() < 0.25) {
            drops.add(new ItemStack(Items.COD));
        }

        // 80레벨: 보물 드롭 확률 대폭 증가
        if (level >= 80 && ThreadLocalRandom.current().nextDouble() < 0.15) {
            drops.add(new ItemStack(Items.BOOK));
        }

        // 90레벨: 쓰레기 완전 제거
        if (level >= 90) {
            drops.removeIf(this::isJunk);
        }

        // 100레벨 각성: 각성의 낚시대 (5% 확률)
        if (level >= 100 && ThreadLocalRandom.current().nextDouble() < 0.05) {
            drops.add(createAwakenedFishingRod());
        }

        boolean hasRare = drops.stream().anyMatch(this::isRare);
        if (hasRare && player.level() instanceof ServerLevel serverLevel) {
            ParticleUtil.spawnBurst(serverLevel,
                player.getX(), player.getY() + 1, player.getZ(),
                ParticleTypes.TOTEM_OF_UNDYING, 30, 0.5);
        }
    }

    // FISHING_SPEED: 낚시 부표 생성 시 대기시간 단축
    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof FishingHook hook)) return;
        if (!(hook.getOwner() instanceof ServerPlayer player)) return;

        int level = SkillData.getLevel(player, SkillType.FISHING);
        float reduction;
        if (level >= 90) reduction = 0.65f;
        else if (level >= 60) reduction = 0.40f;
        else if (level >= 30) reduction = 0.20f;
        else return;

        try {
            Field f;
            try {
                f = FishingHook.class.getDeclaredField("timeUntilHooked");
            } catch (NoSuchFieldException e) {
                f = FishingHook.class.getDeclaredField("f_36102_");
            }
            f.setAccessible(true);
            int current = f.getInt(hook);
            f.setInt(hook, Math.max(20, (int)(current * (1f - reduction))));
        } catch (Exception ignored) {}

        // [5] 50레벨: 인챈트된 낚싯대 효과 강화 (인챈트 1레벨당 10% 추가 감소)
        if (level >= 50) {
            ItemStack rod = player.getMainHandItem();
            int lureLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FISHING_SPEED, rod);
            int luckLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FISHING_LUCK, rod);
            int totalLevels = lureLevel + luckLevel;
            if (totalLevels > 0) {
                float enchantReduction = Math.min(totalLevels * 0.10f, 0.50f);
                try {
                    Field ef;
                    try {
                        ef = FishingHook.class.getDeclaredField("timeUntilHooked");
                    } catch (NoSuchFieldException ex) {
                        ef = FishingHook.class.getDeclaredField("f_36102_");
                    }
                    ef.setAccessible(true);
                    int cur = ef.getInt(hook);
                    ef.setInt(hook, Math.max(20, (int)(cur * (1f - enchantReduction))));
                } catch (Exception ignored) {}
            }
        }
    }

    private boolean isJunk(ItemStack stack) {
        return stack.is(Items.LILY_PAD)
            || stack.is(Items.BOWL)
            || stack.is(Items.STRING)
            || stack.is(Items.BONE)
            || stack.is(Items.INK_SAC)
            || stack.is(Items.TRIPWIRE_HOOK);
    }

    private boolean isRare(ItemStack stack) {
        return !isJunk(stack)
            && !stack.is(Items.COD)
            && !stack.is(Items.SALMON)
            && !stack.is(Items.TROPICAL_FISH)
            && !stack.is(Items.PUFFERFISH);
    }

    private ItemStack createAwakenedFishingRod() {
        ItemStack rod = new ItemStack(Items.FISHING_ROD);

        rod.enchant(Enchantments.FISHING_LUCK, 3);
        rod.enchant(Enchantments.FISHING_SPEED, 3);
        rod.enchant(Enchantments.UNBREAKING, 3);
        rod.enchant(Enchantments.MENDING, 1);

        rod.setHoverName(Component.literal("§b§l각성의 낚시대"));

        ListTag loreTag = new ListTag();
        loreTag.add(StringTag.valueOf(Component.Serializer.toJson(
            Component.literal("§7낚시 100레벨 각성 보상"))));
        loreTag.add(StringTag.valueOf(Component.Serializer.toJson(
            Component.literal("§d신화의 경지에서 낚아 올린 전설"))));
        rod.getOrCreateTagElement("display").put("Lore", loreTag);

        return rod;
    }

    private double junkRemoveChance(int level) {
        if (level >= 80) return 0.80;
        if (level >= 50) return 0.50;
        if (level >= 30) return 0.30;
        return 0.15;
    }
}
