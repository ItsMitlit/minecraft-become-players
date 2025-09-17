package it.mitl.minecraftbecomeplayers.item;

import it.mitl.minecraftbecomeplayers.Main;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Main.MOD_ID);

    public static final RegistryObject<Item> BLUE_BLOOD = ITEMS.register("blue_blood", () ->
            new Item(new Item.Properties().stacksTo(16))
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
