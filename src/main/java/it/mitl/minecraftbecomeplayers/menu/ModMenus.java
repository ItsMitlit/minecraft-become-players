package it.mitl.minecraftbecomeplayers.menu;

import it.mitl.minecraftbecomeplayers.Main;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Main.MOD_ID);

    public static final RegistryObject<MenuType<SynthInventoryMenu>> SYNTH_INVENTORY =
        MENUS.register("synth_inventory", () -> IForgeMenuType.create(SynthInventoryMenu::new));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
