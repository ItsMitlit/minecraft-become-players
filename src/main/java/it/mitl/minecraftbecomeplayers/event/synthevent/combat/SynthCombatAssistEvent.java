package it.mitl.minecraftbecomeplayers.event.synthevent.combat;

import it.mitl.minecraftbecomeplayers.entity.custom.SynthEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class SynthCombatAssistEvent {

    private static final double RADIUS = 25.0D;

    @SubscribeEvent
    public static void onOwnerAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        commandNearbyAvailableSynths(player, target);
    }

    @SubscribeEvent
    public static void onOwnerHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim instanceof Player player)) return;
        DamageSource source = event.getSource();
        LivingEntity attacker = source.getEntity() instanceof LivingEntity entity ? entity : null;
        if (attacker == null || attacker == player) return;
        commandNearbyAvailableSynths(player, attacker);
    }

    @SubscribeEvent
    public static void onSynthHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof SynthEntity synth)) return;
        // Attack only if activated and not staying
        if (!synth.isActivationComplete()) return;
        if (synth.isStaying()) return;

        DamageSource source = event.getSource();
        LivingEntity attacker = null;
        if (source.getEntity() instanceof LivingEntity le) attacker = le;
        else if (source.getDirectEntity() instanceof LivingEntity le2) attacker = le2;

        if (attacker == null) return;
        // Don't attack owner
        if (attacker instanceof Player player && synth.isOwner(player)) return;

        synth.setTarget(attacker);
    }

    private static void commandNearbyAvailableSynths(Player owner, LivingEntity target) {
        List<SynthEntity> list = owner.level().getEntitiesOfClass(
                SynthEntity.class,
                owner.getBoundingBox().inflate(RADIUS),
                LivingEntity::isAlive
        );
        for (SynthEntity synth : list) {
            if (!synth.isActivationComplete()) continue;
            if (!synth.isOwner(owner)) continue;
            if (synth.isStaying()) continue;
            synth.setTarget(target);
        }
    }
}
