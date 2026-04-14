package com.gathercraft.gathercraft;

import com.gathercraft.gathercraft.client.ClientSetup;
import com.gathercraft.gathercraft.command.GatherCraftCommand;
import com.gathercraft.gathercraft.command.SkillCommand;
import com.gathercraft.gathercraft.item.SkillBookItem;
import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.skill.handler.*;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(GatherCraft.MOD_ID)
public class GatherCraft {

    public static final String MOD_ID = "gathercraft";
    private static final Logger LOGGER = LogUtils.getLogger();

    // 아이템 DeferredRegister
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    static {
        ITEMS.register("skill_book", () -> SkillBookItem.INSTANCE);
    }

    public GatherCraft(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        // 아이템 등록
        ITEMS.register(modBus);

        PacketHandler.register();

        // 클라이언트 전용: GUI 오버레이 + 키 입력 핸들러 + 부유 텍스트 등록
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modBus.register(ClientSetup.class);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
                com.gathercraft.gathercraft.client.keybinding.ClientKeyHandler.class
            );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
                com.gathercraft.gathercraft.client.overlay.FloatingCombatText.class
            );
        });

        forgeBus.register(new MiningHandler());
        forgeBus.register(new LumberjackHandler());
        forgeBus.register(new FarmingHandler());
        forgeBus.register(new FishingHandler());
        forgeBus.register(new CookingHandler());
        forgeBus.register(new HuntingHandler());
        forgeBus.register(new DefenseHandler());
        forgeBus.register(new SmithingHandler());
        forgeBus.register(new EnchantingHandler());
        forgeBus.register(new PlayerTickHandler());
        forgeBus.register(new SkillBookHandler());
        forgeBus.register(new SkillCommand());
        forgeBus.register(new GatherCraftCommand());

        LOGGER.info("GatherCraft initialized - 9 skill RPG mod loaded!");
    }
}
