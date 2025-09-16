package it.mitl.minecraftbecomeplayers.event.synthchat;

import it.mitl.minecraftbecomeplayers.entity.custom.SynthEntity;
import it.mitl.minecraftbecomeplayers.subroutine.SynthUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class SynthInstructionEvent {

    public static final double INSTRUCTION_RADIUS = 25.0D;

    @SubscribeEvent
    public static void onChatMessage(ServerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getRawText();
        if (message == null) return;
        message = message.trim();
        if (message.isEmpty()) return;

        String lowerMsg = message.toLowerCase();

        // Detect commands
        boolean hasStay = containsWord(lowerMsg, "stay");
        boolean hasRoam = containsWord(lowerMsg, "roam");
        if (!hasStay && !hasRoam) return;

        boolean setStaying = !hasRoam;

        // Get nearby synths
        List<SynthEntity> list = player.level().getEntitiesOfClass(
                SynthEntity.class,
                player.getBoundingBox().inflate(INSTRUCTION_RADIUS),
                Entity::isAlive
        );
        if (list.isEmpty()) return;

        // Nearest owned synth whose name is in the message
        Map<String, SynthEntity> chosenByName = new HashMap<>();
        Map<String, Double> bestDistByName = new HashMap<>();

        for (SynthEntity synth : list) {
            if (!synth.isOwner(player)) continue;
            String name = synth.getSynthName();
            if (name == null || name.isEmpty()) continue;

            String nameLower = name.toLowerCase();
            if (!containsWord(lowerMsg, nameLower)) continue;

            double dist = synth.distanceToSqr(player);
            Double best = bestDistByName.get(nameLower);
            if (best == null || dist < best) {
                bestDistByName.put(nameLower, dist);
                chosenByName.put(nameLower, synth);
            }
        }

        if (chosenByName.isEmpty()) return;

        // Apply the stay/roam command to the selected synth
        for (SynthEntity target : chosenByName.values()) {
            // Return (continue) if the synth isn't enabled
            if (!target.isActivationComplete()) continue;

            target.setStaying(setStaying);

            String prefix = "§9[§b" + target.getSynthName() + "§9]§7 ";
            String reply = setStaying
                    ? "All right, I'll stay here."
                    : "All right, I'll roam.";

            if (player.getServer() != null) {
                SynthUtils.sendChatMessages(player, prefix + reply);
            } else {
                // Fallback bc just in case
                player.sendSystemMessage(Component.literal(prefix + reply));
            }
        }

    }

    private static boolean containsWord(String word, String wholeString) {
        int indexPos = 0;
        while ((indexPos = word.indexOf(wholeString, indexPos)) != -1) {
            boolean startOk = indexPos == 0 || !Character.isLetterOrDigit(word.charAt(indexPos - 1));
            int endIndexPos = indexPos + wholeString.length();
            boolean endOk = endIndexPos >= word.length() || !Character.isLetterOrDigit(word.charAt(endIndexPos));
            if (startOk && endOk) return true;
            indexPos = indexPos + 1;
        }
        return false;
    }
}