package it.mitl.minecraftbecomeplayers.item;

import it.mitl.minecraftbecomeplayers.Main;
import it.mitl.minecraftbecomeplayers.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
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

    public static final RegistryObject<Item> SYNTH_SPAWN_EGG = ITEMS.register("synth_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SYNTH, 0x6497d0, 0x95e2ff,
                    new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
