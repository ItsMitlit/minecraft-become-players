package it.mitl.minecraftbecomeplayers;

import it.mitl.minecraftbecomeplayers.entity.ModEntities;
import it.mitl.minecraftbecomeplayers.item.ModCreativeModeTabs;
import it.mitl.minecraftbecomeplayers.item.ModItems;
import it.mitl.minecraftbecomeplayers.menu.ModMenus;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class RegistryHandler {

    public static void register() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientEvents::register);

        ModEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
    }

}
