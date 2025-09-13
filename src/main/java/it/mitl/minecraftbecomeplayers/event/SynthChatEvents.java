package it.mitl.minecraftbecomeplayers.event;

import it.mitl.minecraftbecomeplayers.entity.custom.SynthEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SynthChatEvents {

    @SubscribeEvent
    public static void onChatMessage(ServerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getRawText();
        System.out.println(message);
        if (message.equalsIgnoreCase("Activate Synth") && hasSynthNearby(player)) {
            event.setCanceled(true);
            event.getPlayer().sendSystemMessage(Component.literal("§9[§bCrafter§3Life§9]§7 Initializing..."));
            event.getPlayer().sendSystemMessage(Component.literal("§9[§bCrafter§3Life§9]§7 Activation complete. Welcome!"));
        }
    }

    public static boolean hasSynthNearby(Player player) {
        double radius = 10.0D; // 10 block radius

        return !player.level().getEntitiesOfClass(
                SynthEntity.class,
                player.getBoundingBox().inflate(radius),
                e -> e.isAlive()
        ).isEmpty();
    }
}














