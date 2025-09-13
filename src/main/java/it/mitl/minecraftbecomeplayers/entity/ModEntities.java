package it.mitl.minecraftbecomeplayers.entity;

import it.mitl.minecraftbecomeplayers.Main;
import it.mitl.minecraftbecomeplayers.entity.custom.SynthEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Main.MOD_ID);

    public static final RegistryObject<EntityType<SynthEntity>> SYNTH =
            ENTITY_TYPES.register("synth", () -> EntityType.Builder.of(SynthEntity::new, MobCategory.MISC)
                    .build("synth"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
