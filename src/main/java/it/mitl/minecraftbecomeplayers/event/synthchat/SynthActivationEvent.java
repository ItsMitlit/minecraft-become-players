package it.mitl.minecraftbecomeplayers.event.synthchat;

import it.mitl.minecraftbecomeplayers.entity.custom.SynthEntity;
import it.mitl.minecraftbecomeplayers.subroutine.SynthUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber
public class SynthActivationEvent {

    private static final String DEFAULT_PREFIX = "§9[§bCrafter§3Life§9]§7 ";
    public static final double ACTIVATION_RADIUS = 10.0D;

    @SubscribeEvent
    public static void onChatMessage(ServerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getRawText();
        if (message == null) return;
        message = message.trim();

        SynthEntity synth = getNearestSynth(player);
        if (synth == null) {
            return; // No synth nearby
        }

        String prefix = getSynthPrefix(synth);

        // Handle activation phrase
        if (message.equalsIgnoreCase("Activate Synth")) {
            event.setCanceled(true);

            // Synth already fully activated
            if (synth.isActivationComplete()) {
                player.sendSystemMessage(Component.literal(prefix + "This synth has already been activated."));
                return;
            }

            // If already in activation by another player
            if (synth.getActivationStage() > 0 && !synth.isOwner(player)) {
                player.sendSystemMessage(Component.literal(prefix + "This synth is currently being activated by another user."));
                return;
            }

            // Begin or resume activation
            synth.setOwnerUUID(player.getUUID());
            if (synth.getActivationStage() <= 0) {
                synth.setActivationStage(1); // naming stage
                player.sendSystemMessage(Component.literal(DEFAULT_PREFIX + "Initializing..."));
                player.sendSystemMessage(Component.literal(DEFAULT_PREFIX + "Activation complete. Welcome!"));
                player.sendSystemMessage(Component.literal(DEFAULT_PREFIX + "Please state the name you would like me to go by."));
            } else {
                // Already in a stage for this player
                promptForCurrentStage(player, synth);
            }
            return;
        }

        // Only handle if player is the owner and synth is in an activation stage
        if (synth.getActivationStage() > 0 && synth.isOwner(player)) {
            int stage = synth.getActivationStage();
            switch (stage) {
                case 1: { // name
                    event.setCanceled(true);
                    String name = message;
                    if (name.isEmpty()) {
                        player.sendSystemMessage(Component.literal(DEFAULT_PREFIX + "Please provide a non-empty name."));
                        return;
                    }
                    if (name.length() > 16) {
                        player.sendSystemMessage(Component.literal(DEFAULT_PREFIX + "That name is a bit long. Please use 16 characters or fewer."));
                        return;
                    }
                    synth.setSynthName(name);
                    String newPrefix = getSynthPrefix(synth);
                    player.sendSystemMessage(Component.literal(newPrefix + "Thank you. My name is now " + name + "."));
                    synth.setActivationStage(2);
                    player.sendSystemMessage(Component.literal(newPrefix + "Please specify my gender (Male/Female/Non-binary/Other)."));
                    return;
                }
                case 2: { // gender
                    event.setCanceled(true);
                    int gender = handleGenders(message);
                    if (gender == -1) {
                        player.sendSystemMessage(Component.literal(getSynthPrefix(synth) + "I didn't catch that. Please reply with Male, Female, Non-binary, or Other."));
                        return;
                    }

                    synth.setSynthSkin(pickRandomSkin(gender));
                    synth.setGender(gender);
                    synth.setActivationStage(3); // activation completed
                    player.sendSystemMessage(Component.literal(getSynthPrefix(synth) + "Activation complete. Ready for service."));
                    return;
                }
            }
        }
    }

    private static void promptForCurrentStage(Player player, SynthEntity synth) {
        String prefix = getSynthPrefix(synth);
        switch (synth.getActivationStage()) {
            case 1 -> player.sendSystemMessage(Component.literal(prefix + "Please state the name you would like me to go by."));
            case 2 -> player.sendSystemMessage(Component.literal(prefix + "Please specify my gender (Male/Female/Non-binary/Other)."));
            default -> player.sendSystemMessage(Component.literal(prefix + "This synth is already activated."));
        }
    }

    public static boolean isSynthNearby(Player player) {
        return getNearestSynth(player) != null;
    }

    @Nullable
    private static SynthEntity getNearestSynth(Player player) {
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

    // Returns -1 for unrecognized, 1 male, 2 female, 3 non-binary, 4 other
    private static int handleGenders(String input) {
        String genderInput = input.trim().toLowerCase();
        return switch (genderInput) {
            case "male", "m" -> 1;
            case "female", "f" -> 2;
            case "non-binary", "nonbinary", "nb" -> 3;
            case "other", "o" -> 4;
            default -> -1;
        };
    }

    private static String getSynthPrefix(SynthEntity synth) {
        String name = synth.getSynthName();
        if (name == null || name.isEmpty()) {
            return DEFAULT_PREFIX;
        }
        return "§9[§b" + name + "§9]§7 ";
    }

    private static String pickRandomSkin(int gender) {
        int totalSkins = 0;
        String path = "";
        if (gender == 1) {
            totalSkins = SynthUtils.getTotalSkins(gender);
            path = "male/";
        } else if (gender == 2) {
            totalSkins = SynthUtils.getTotalSkins(gender);
            path = "female/";
        } else if (gender == 3 || gender == 4) {
            int newGender = (Math.random() < 0.5) ? 1 : 2;
            totalSkins = SynthUtils.getTotalSkins(newGender);
            if (newGender == 1) {
                path = "male/";
            } else {
                path = "female/";
            }
        }

        int skinNumber = 1 + (int)(Math.random() * totalSkins);
        return path + skinNumber + ".png";
    }

    public static void sendChat(Player player, Component message) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                player.sendSystemMessage(message);
            } catch (InterruptedException ignored) { // set this up later
            }
        });
    }

//    public static void sendChatMessages(CommandSourceStack source, String message) {
//        List<String> messages = splitMessage(message);
//
//
//        new Thread(() -> {
//            for (String msg : messages) {
//                try {
//                    String formatter = chooseFormatterRandom();
//                    source.getServer().getCommands().performPrefixedCommand(source, "/say " + formatter + msg);
//                    Thread.sleep(2500); // Introduce a slight delay between messages
//                } catch (InterruptedException e) {
//                    LOGGER.error("{}", e.getMessage());
//                }
//            }
//
//        }).start();
//
//    }
}
