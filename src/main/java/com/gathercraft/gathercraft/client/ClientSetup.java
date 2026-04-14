package com.gathercraft.gathercraft.client;

import com.gathercraft.gathercraft.client.keybinding.KeyBindings;
import com.gathercraft.gathercraft.client.overlay.DamageFlashOverlay;
import com.gathercraft.gathercraft.client.overlay.SkillBarOverlay;
import com.gathercraft.gathercraft.client.overlay.SkillXpBarOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("damage_flash", DamageFlashOverlay::render);
        event.registerAboveAll("skill_bar", SkillBarOverlay::render);
        event.registerAboveAll("skill_xp_bar", SkillXpBarOverlay::render);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.DASH);
    }
}
