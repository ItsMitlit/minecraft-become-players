package it.mitl.minecraftbecomeplayers.event;

import it.mitl.minecraftbecomeplayers.Main;
import it.mitl.minecraftbecomeplayers.entity.ModEntities;
import it.mitl.minecraftbecomeplayers.entity.client.SynthRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SYNTH.get(), SynthRenderer::new);
    }
}
