package it.mitl.minecraftbecomeplayers.entity.client;

import it.mitl.minecraftbecomeplayers.Main;
import it.mitl.minecraftbecomeplayers.entity.custom.SynthEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SynthRenderer extends HumanoidMobRenderer<SynthEntity, HumanoidModel<SynthEntity>> {
    public SynthRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(SynthEntity entity) {
        return new ResourceLocation(Main.MOD_ID, "textures/entity/synth.png");
    }
}
