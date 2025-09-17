package it.mitl.minecraftbecomeplayers.item;

import it.mitl.minecraftbecomeplayers.Main;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MINECRAFTBECOMEPLAYERS_TAB = CREATIVE_MODE_TABS.register("minecraftbecomeplayers_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BLUE_BLOOD.get()))
                    .title(Component.translatable("creativetab.minecraftbecomeplayers_tab"))
                    .displayItems((displayParameters, output) -> {
                        output.accept(ModItems.BLUE_BLOOD.get());
                    }).build());
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}