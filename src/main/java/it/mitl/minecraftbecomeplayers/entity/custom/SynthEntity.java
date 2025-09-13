package it.mitl.minecraftbecomeplayers.entity.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SynthEntity extends PathfinderMob {

    public SynthEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand pHand) {
        if (pHand == InteractionHand.MAIN_HAND && player.getItemInHand(pHand).isEmpty()) {
            if (!player.level().isClientSide) {
                player.sendSystemMessage(Component.literal("§9[§bCrafter§3Life§9]§7 Greetings, " + player.getName().getString() + "!"));
                player.sendSystemMessage(Component.literal("§9[§bCrafter§3Life§9]§7 Congratulations on the purchase of your new synthetic appliance! To begin attunement, please say the word 'Activate Synth'."));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }
}
