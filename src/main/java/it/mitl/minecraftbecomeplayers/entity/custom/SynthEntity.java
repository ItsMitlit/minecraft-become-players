package it.mitl.minecraftbecomeplayers.entity.custom;

import it.mitl.minecraftbecomeplayers.entity.ai.goal.FollowOwnerGoal;
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

    private static final EntityDataAccessor<String> SYNTH_SKIN = SynchedEntityData.defineId(SynthEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> SYNTH_SKIN_ENABLED = SynchedEntityData.defineId(SynthEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> SYNTH_NAME = SynchedEntityData.defineId(SynthEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ACTIVATION_STAGE = SynchedEntityData.defineId(SynthEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GENDER = SynchedEntityData.defineId(SynthEntity.class, EntityDataSerializers.INT);

    @Nullable
    private UUID ownerUUID;

    // Goals for when the synth is fully activated (>= 3)
    private WaterAvoidingRandomStrollGoal strollGoal;
    private RandomLookAroundGoal randomLookGoal;
    private boolean activationGoalsAdded = false;

    // Goal for when activation stage >= 1 (just the look at player one atm)
    private LookAtPlayerGoal lookAtPlayerGoal;
    private boolean lookGoalAdded = false;

    // Whether the synth was told to stay (stand still)
    private boolean staying = false;

    // Whether the synth should follow the owner
    private boolean following = false;
    private FollowOwnerGoal followOwnerGoal;

    public SynthEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SYNTH_SKIN, "default.png"); // Default skin
        this.entityData.define(SYNTH_SKIN_ENABLED, true); // Whether the custom skin should be used (used for turning off your synth's skin and making sure it saves)
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

        // Activation-dependent goals (once synth is activated and/or activating)
        this.strollGoal = new WaterAvoidingRandomStrollGoal(this, 1.0D);
        this.randomLookGoal = new RandomLookAroundGoal(this);
        this.lookAtPlayerGoal = new LookAtPlayerGoal(this, Player.class, 8.0F);
        this.followOwnerGoal = new FollowOwnerGoal(this, 1.15D, 4.0F, 2.0F);

        this.updateActivationDependentGoals();
    }

    private void updateActivationDependentGoals() {
        if (this.level().isClientSide) return;

        int stage = this.getActivationStage();

        // Enable player look goal when stage >= 1
        boolean shouldLook = stage >= 1;
        if (shouldLook) {
            if (!lookGoalAdded) {
                this.goalSelector.addGoal(2, this.lookAtPlayerGoal);
                lookGoalAdded = true;
            }
        } else if (lookGoalAdded) {
            this.goalSelector.removeGoal(this.lookAtPlayerGoal);
            lookGoalAdded = false;
        }

        boolean shouldHaveActivationGoals = isActivationComplete() && !staying && !following;
        if (shouldHaveActivationGoals) {
            if (!activationGoalsAdded) {
                this.goalSelector.addGoal(1, this.strollGoal);
                this.goalSelector.addGoal(3, this.randomLookGoal);
                activationGoalsAdded = true;
            }
        } else if (activationGoalsAdded) {
            this.goalSelector.removeGoal(this.strollGoal);
            this.goalSelector.removeGoal(this.randomLookGoal);
            activationGoalsAdded = false;
        }

        // Follow goal handling
        boolean shouldFollow = isActivationComplete() && following && !staying;
        if (shouldFollow) {
            // Ensure follow goal is present
            if (!this.goalSelector.getAvailableGoals().stream().anyMatch(w -> w.getGoal() == this.followOwnerGoal)) {
                this.goalSelector.addGoal(1, this.followOwnerGoal);
            }
        } else {
            this.goalSelector.removeGoal(this.followOwnerGoal);
        }

        // Don't move if staying
        if (staying) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand pHand) {
        if (pHand == InteractionHand.MAIN_HAND && player.getItemInHand(pHand).isEmpty()) {
            if (!player.level().isClientSide && !isActivationComplete()) {
                player.sendSystemMessage(Component.literal("§9[§bCrafter§3Life§9]§7 Greetings, " + player.getName().getString() + "!"));
                player.sendSystemMessage(Component.literal("§9[§bCrafter§3Life§9]§7 Congratulations on the purchase of your new synthetic appliance! To begin attunement, please say 'Activate Synth'."));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    public String getSynthSkin() {
        return this.entityData.get(SYNTH_SKIN);
    }
    public void setSynthSkin(String skin) {
        this.entityData.set(SYNTH_SKIN, skin);
    }

    public Boolean getSynthSkinEnabled() {
        return this.entityData.get(SYNTH_SKIN_ENABLED);
    }

    public void setSynthSkinEnabled(boolean enabled) {
        this.entityData.set(SYNTH_SKIN_ENABLED, enabled);
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

    public boolean isActivationComplete() {
        int lastActivationStage = 3;
        return this.getActivationStage() >= lastActivationStage;
    }

    public boolean isStaying() {
        return this.staying;
    }

    public void setStaying(boolean staying) {
        this.staying = staying;
        if (!this.level().isClientSide) {
            if (staying) {
                this.getNavigation().stop();
                this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            }
            this.updateActivationDependentGoals();
        }
    }

    public boolean isFollowing() {
        return this.following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
        if (!this.level().isClientSide) {
            if (!following) {
                this.getNavigation().stop();
            }
            this.updateActivationDependentGoals();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("SynthSkin", this.getSynthSkin());
        tag.putBoolean("SynthSkinEnabled", this.getSynthSkinEnabled());
        tag.putString("SynthName", this.getSynthName());
        tag.putInt("ActivationStage", this.getActivationStage());
        tag.putInt("Gender", this.getGender());
        tag.putBoolean("Staying", this.staying);
        tag.putBoolean("Following", this.following);
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUuid", this.ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SynthSkin")) {
            this.setSynthSkin(tag.getString("SynthSkin"));
        }
        if (tag.contains("SynthSkinEnabled")) {
            this.setSynthSkinEnabled(tag.getBoolean("SynthSkinEnabled"));
        }
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
        if (tag.contains("Staying")) {
            this.setStaying(tag.getBoolean("Staying"));
        }
        if (tag.contains("Following")) {
            this.setFollowing(tag.getBoolean("Following"));
        }
    }
}
