package com.gathercraft.gathercraft;

import com.gathercraft.gathercraft.command.GatherCraftCommand;
import com.gathercraft.gathercraft.command.SkillCommand;
import com.gathercraft.gathercraft.skill.handler.*;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(GatherCraft.MOD_ID)
public class GatherCraft {

    public static final String MOD_ID = "gathercraft";
    private static final Logger LOGGER = LogUtils.getLogger();

    public GatherCraft(FMLJavaModLoadingContext context) {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

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
        forgeBus.register(new SkillCommand());
        forgeBus.register(new GatherCraftCommand());

        LOGGER.info("GatherCraft initialized - 9 skill RPG mod loaded!");
    }
}
