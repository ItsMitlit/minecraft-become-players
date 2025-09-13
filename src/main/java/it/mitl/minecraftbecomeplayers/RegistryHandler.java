package it.mitl.minecraftbecomeplayers;

import it.mitl.minecraftbecomeplayers.entity.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class RegistryHandler {

    public static void register() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientEvents::register);

        ModEntities.register(modEventBus);
    }

}
