package it.mitl.minecraftbecomeplayers.datagen;

import it.mitl.minecraftbecomeplayers.Main;
import it.mitl.minecraftbecomeplayers.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Main.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent(ModItems.SYNTH_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
    }
}
