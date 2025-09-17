package it.mitl.minecraftbecomeplayers.subroutine;

import it.mitl.minecraftbecomeplayers.entity.custom.SynthEntity;
import it.mitl.minecraftbecomeplayers.event.synthevent.chat.SynthActivationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;

public class SynthUtils {

    public static final int TOTAl_MALE_SKINS = 5;
    public static final int TOTAL_FEMALE_SKINS = 3;

    public static int getTotalSkins(int gender) {
        if (gender == 1) {
            return TOTAl_MALE_SKINS;
        } else if (gender == 2) {
            return TOTAL_FEMALE_SKINS;
        }
        return 0;
    }

    @Nullable
    public static SynthEntity getNearestSynth(Player player) {
        List<SynthEntity> list = player.level().getEntitiesOfClass(
                SynthEntity.class,
                player.getBoundingBox().inflate(SynthActivationEvent.ACTIVATION_RADIUS),
                Entity::isAlive
        );
        if (list.isEmpty()) return null;
        SynthEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (SynthEntity synth : list) {
            double d = synth.distanceToSqr(player);
            if (d < bestDist) {
                bestDist = d;
                best = synth;
            }
        }
        return best;
    }

    public static void sendChatMessages(Player player, String message) {
        new Thread(() -> {
            player.getServer().execute(() -> player.sendSystemMessage(Component.literal(message)));
        }).start();

    }
}
