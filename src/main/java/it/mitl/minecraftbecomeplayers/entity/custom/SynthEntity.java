package it.mitl.minecraftbecomeplayers.entity.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

import javax.annotation.Nullable;
import java.util.UUID;

public class SynthEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> SYNTH_NAME = SynchedEntityData.defineId(SynthEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ACTIVATION_STAGE = SynchedEntityData.defineId(SynthEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GENDER = SynchedEntityData.defineId(SynthEntity.class, EntityDataSerializers.INT);

    @Nullable
    private UUID ownerUUID;

    // Goals for when the synth is fully activated (>= 3)
    private WaterAvoidingRandomStrollGoal strollGoal;
    private RandomLookAroundGoal randomLookGoal;
    private boolean activationGoalsAdded = false;

    public SynthEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SYNTH_NAME, "");
        this.entityData.define(ACTIVATION_STAGE, 0); // 0 = inactive, 1 = naming, 2 = gender, 3+ = done
        this.entityData.define(GENDER, 0); // -1 = unrecognised, 1 = male, 2 = female, 3 = non-binary, 4 = other
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected void registerGoals() {
        // Permanent goals
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));

        // Activation-dependent goals (once synth is activated)
        this.strollGoal = new WaterAvoidingRandomStrollGoal(this, 1.0D);
        this.randomLookGoal = new RandomLookAroundGoal(this);

        this.updateActivationDependentGoals();
    }

    private void updateActivationDependentGoals() {
        if (this.level().isClientSide) return;

        boolean shouldEnable = this.getActivationStage() >= 3;
        if (shouldEnable) {
            if (!activationGoalsAdded) {
                this.goalSelector.addGoal(1, this.strollGoal);
                this.goalSelector.addGoal(3, this.randomLookGoal);
                activationGoalsAdded = true;
            }
        } else {
            if (activationGoalsAdded) {
                this.goalSelector.removeGoal(this.strollGoal);
                this.goalSelector.removeGoal(this.randomLookGoal);
                activationGoalsAdded = false;
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand pHand) {
        if (pHand == InteractionHand.MAIN_HAND && player.getItemInHand(pHand).isEmpty()) {
            if (!player.level().isClientSide && this.getActivationStage() < 3) {
                player.sendSystemMessage(Component.literal("§9[§bCrafter§3Life§9]§7 Greetings, " + player.getName().getString() + "!"));
                player.sendSystemMessage(Component.literal("§9[§bCrafter§3Life§9]§7 Congratulations on the purchase of your new synthetic appliance! To begin attunement, please say 'Activate Synth'."));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    public String getSynthName() {
        return this.entityData.get(SYNTH_NAME);
    }

    public void setSynthName(String name) {
        this.entityData.set(SYNTH_NAME, name);
        if (!name.isEmpty()) {
            this.setCustomName(Component.literal(name));
            this.setCustomNameVisible(true);
        }
    }

    public int getActivationStage() {
        return this.entityData.get(ACTIVATION_STAGE);
    }

    public void setActivationStage(int stage) {
        this.entityData.set(ACTIVATION_STAGE, stage);
        this.updateActivationDependentGoals();
    }

    public int getGender() {
        return this.entityData.get(GENDER);
    }

    public void setGender(int gender) {
        this.entityData.set(GENDER, gender);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public boolean isOwner(Player player) {
        return player.getUUID().equals(ownerUUID);
    }


    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("SynthName", this.getSynthName());
        tag.putInt("ActivationStage", this.getActivationStage());
        tag.putInt("Gender", this.getGender());
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUuid", this.ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SynthName")) {
            this.setSynthName(tag.getString("SynthName"));
        }
        if (tag.contains("ActivationStage")) {
            this.setActivationStage(tag.getInt("ActivationStage"));
        }
        if (tag.contains("Gender")) {
            this.setGender(tag.getInt("Gender"));
        }
        if (tag.hasUUID("OwnerUuid")) {
            this.ownerUUID = tag.getUUID("OwnerUuid");
        } else {
            this.ownerUUID = null;
        }
    }
}
