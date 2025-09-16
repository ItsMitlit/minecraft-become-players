package it.mitl.minecraftbecomeplayers.entity.ai.goal;

import it.mitl.minecraftbecomeplayers.entity.custom.SynthEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class FollowOwnerGoal extends Goal {
    private final SynthEntity mob;
    private Player owner;
    private final double speedModifier;
    private final float stopDistance;
    private final float startDistance;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public FollowOwnerGoal(SynthEntity mob, double speedModifier, float startDistance, float stopDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!mob.isFollowing() || mob.isStaying() || !mob.isActivationComplete()) return false;
        Player player = getOwner();
        if (player == null) return false;
        if (player.isSpectator()) return false;
        if (mob.distanceTo(player) < (double) this.startDistance) return false;
        this.owner = player;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!mob.isFollowing() || mob.isStaying()) return false;
        if (owner == null || owner.isSpectator()) return false;
        return mob.distanceTo(owner) > (double) this.stopDistance;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mob.getPathfindingMalus(BlockPathTypes.WATER);
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.mob.getNavigation().stop();
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        if (owner == null) return;
        this.mob.getLookControl().setLookAt(owner, 10.0F, this.mob.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            this.mob.getNavigation().moveTo(owner, this.speedModifier);
        }
    }

    @Nullable
    private Player getOwner() {
        if (mob.level().isClientSide) return null;
        return mob.level().getPlayerByUUID(mob.getOwnerUUID());
    }
}
