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
        boolean hasFollow = containsWord(lowerMsg, "follow");
        if (!hasStay && !hasRoam && !hasFollow) return;

        // Group Targeting
        boolean targetAllOwnedSynths = containsWord(lowerMsg, "synths");
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

        Collection<SynthEntity> targets;
        if (!chosenByName.isEmpty()) {
            targets = chosenByName.values();
        } else if (targetAllOwnedSynths) {
            List<SynthEntity> owned = new ArrayList<>();
            for (SynthEntity s : list) {
                if (s.isOwner(player)) owned.add(s);
            }
            if (owned.isEmpty()) return;
            targets = owned;
        } else {
            return;
        }

        // Apply the command to the selected synth(s)
        for (SynthEntity target : targets) {
            if (!target.isActivationComplete()) continue;

            String reply;
            if (hasFollow) {
                target.setStaying(false);
                target.setFollowing(true);
                reply = "All right, I'll follow you.";
            } else if (hasRoam) {
                target.setFollowing(false);
                target.setStaying(false);
                reply = "All right, I'll roam.";
            } else {
                target.setFollowing(false);
                target.setStaying(true);
                reply = "All right, I'll stay here.";
            }

            String displayName = target.getSynthName().isEmpty() ? "Synth" : target.getSynthName();
            String prefix = "§9[§b" + displayName + "§9]§7 ";
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