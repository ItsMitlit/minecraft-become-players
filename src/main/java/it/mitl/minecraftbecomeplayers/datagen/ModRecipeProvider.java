package it.mitl.minecraftbecomeplayers.datagen;

import it.mitl.minecraftbecomeplayers.item.ModItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> pWriter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BLUE_BLOOD.get(), 2)
                .pattern("GLG")
                .pattern("GLG")
                .pattern("GLG")
                .define('G', Items.GLASS_PANE)
                .define('L', Items.LAPIS_LAZULI)
                .unlockedBy("has_lapis_lazuli", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(Items.LAPIS_LAZULI).build()))
                .save(pWriter);


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SYNTH_SPAWN_EGG.get())
                .pattern("ISI")
                .pattern("DND")
                .pattern("IBI")
                .define('I', Items.IRON_INGOT)
                .define('S', Tags.Items.HEADS)
                .define('D', Items.DIAMOND)
                .define('N', Items.NETHERITE_INGOT)
                .define('B', ModItems.BLUE_BLOOD.get())
                .unlockedBy("has_blue_blood", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(ModItems.BLUE_BLOOD.get()).build()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TRACKER.get())
                .pattern("ILI")
                .pattern("IGI")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('L', Items.LIGHTNING_ROD)
                .define('G', Tags.Items.GLASS_PANES)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_lightning_rod", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(Items.LIGHTNING_ROD).build()))
                .save(pWriter);

    }
}
