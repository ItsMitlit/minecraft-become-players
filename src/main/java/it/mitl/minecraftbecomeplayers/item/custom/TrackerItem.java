package it.mitl.minecraftbecomeplayers.item.custom;

import it.mitl.minecraftbecomeplayers.entity.custom.SynthEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;

import java.util.List;

import java.util.Optional;
import java.util.UUID;

public class TrackerItem extends Item {
    private static final String TAG_TRACKED = "TrackedSynths";
    private static final String TAG_SELECTED = "Selected";
    private static final String KEY_ID = "Id";
    private static final String KEY_NAME = "Name";
    private static final int MAX_TRACKED = 5;

    public TrackerItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        ListTag list = trackedList(stack);
        if (list.isEmpty()) {
            tooltip.add(Component.literal("Linked Synths: ").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("  • None").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.add(Component.literal("Linked Synths:").withStyle(ChatFormatting.GRAY));
        int selected = getSelectedIndex(stack);
        for (int i = 0; i < list.size(); i++) {
            String name = getNameAt(list, i).orElse("Synth");
            MutableComponent line = Component.literal("  • " + name);
            if (i == selected) {
                line = line.withStyle(s -> s.withColor(ChatFormatting.AQUA).withBold(true));
            } else {
                line = line.withStyle(s -> s.withColor(ChatFormatting.DARK_GRAY));
            }
            tooltip.add(line);
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof SynthEntity synth)) return InteractionResult.PASS;
        if (player.level().isClientSide) return InteractionResult.SUCCESS;

        if (!synth.isOwner(player)) {
            player.displayClientMessage(Component.literal("You do not own this synth."), true);
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            // Remove from tracker
            boolean removed = removeTracked(stack, synth.getUUID());
            if (removed) {
                player.displayClientMessage(Component.literal("Removed " + displayName(synth) + " from tracker."), true);
            } else {
                player.displayClientMessage(Component.literal(displayName(synth) + " is not linked to this tracker."), true);
            }
            return InteractionResult.CONSUME;
        } else {
            // Add to tracker
            if (isTracked(stack, synth.getUUID())) {
                // Select if already added
                selectByUUID(stack, synth.getUUID());
                player.displayClientMessage(Component.literal("Selected " + displayName(synth) + "."), true);
                return InteractionResult.CONSUME;
            }
            int size = trackedList(stack).size();
            if (size >= MAX_TRACKED) {
                player.displayClientMessage(Component.literal("Tracker is full (" + MAX_TRACKED + ")."), true);
                return InteractionResult.CONSUME;
            }
            addTracked(stack, synth.getUUID(), synth.getSynthName());
            // select newly added
            setSelectedIndex(stack, trackedList(stack).size() - 1);
            player.displayClientMessage(Component.literal("Linked tracker to " + displayName(synth) + "."), true);
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        ListTag list = trackedList(stack);
        if (list.isEmpty()) {
            player.displayClientMessage(Component.literal("No synths linked to this tracker."), true);
            return InteractionResultHolder.consume(stack);
        }

        if (player.isShiftKeyDown()) {
            // Cycle selection
            int idx = getSelectedIndex(stack);
            idx = (idx + 1) % list.size();
            setSelectedIndex(stack, idx);
            String name = getNameAt(list, idx).orElse("Synth");
            player.displayClientMessage(Component.literal("Selected: " + name), true);
            return InteractionResultHolder.consume(stack);
        } else {
            // Show coordinates for selected synth if in same dimension and present
            Optional<UUID> id = getSelectedUUID(stack);
            if (id.isEmpty()) {
                player.displayClientMessage(Component.literal("No synth selected."), true);
                return InteractionResultHolder.consume(stack);
            }
            UUID uuid = id.get();
            if (!(level instanceof ServerLevel serverLevel)) {
                return InteractionResultHolder.pass(stack);
            }
            SynthEntity synth = findSynth(serverLevel, uuid);
            if (synth == null) {
                player.displayClientMessage(Component.literal("Unable to find selected synth."), true);
                return InteractionResultHolder.consume(stack);
            }
            if (!synth.level().dimension().equals(player.level().dimension())) {
                player.displayClientMessage(Component.literal("Selected synth is in another dimension."), true);
                return InteractionResultHolder.consume(stack);
            }
            // Coordinates
            int x = (int) Math.floor(synth.getX());
            int y = (int) Math.floor(synth.getY());
            int z = (int) Math.floor(synth.getZ());
            String name = synth.getSynthName().isEmpty() ? "Synth" : synth.getSynthName();
            player.displayClientMessage(Component.literal(name + " @ (" + x + ", " + y + ", " + z + ")"), true);
            return InteractionResultHolder.consume(stack);
        }
    }

    private static String displayName(SynthEntity synth) {
        String n = synth.getSynthName();
        return n == null || n.isEmpty() ? "Synth" : n;
    }

    private static SynthEntity findSynth(ServerLevel level, UUID uuid) {
        var ent = level.getEntity(uuid);
        if (ent instanceof SynthEntity s) return s;
        AABB box = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        for (var s : level.getEntitiesOfClass(SynthEntity.class, box)) {
            if (uuid.equals(s.getUUID())) return s;
        }
        return null;
    }

    // NBT helpers
    private static ListTag trackedList(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_TRACKED, Tag.TAG_LIST)) {
            ListTag list = new ListTag();
            tag.put(TAG_TRACKED, list);
            if (!tag.contains(TAG_SELECTED)) tag.putInt(TAG_SELECTED, 0);
            return list;
        }
        return tag.getList(TAG_TRACKED, Tag.TAG_COMPOUND);
    }

    private static boolean isTracked(ItemStack stack, UUID uuid) {
        ListTag list = trackedList(stack);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            if (uuid.toString().equalsIgnoreCase(e.getString(KEY_ID))) return true;
        }
        return false;
    }

    private static void addTracked(ItemStack stack, UUID uuid, String name) {
        ListTag list = trackedList(stack);
        CompoundTag e = new CompoundTag();
        e.putString(KEY_ID, uuid.toString());
        if (name != null && !name.isEmpty()) e.putString(KEY_NAME, name);
        list.add(e);
    }

    private static boolean removeTracked(ItemStack stack, UUID uuid) {
        ListTag list = trackedList(stack);
        int idxToRemove = -1;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            if (uuid.toString().equalsIgnoreCase(e.getString(KEY_ID))) {
                idxToRemove = i;
                break;
            }
        }
        if (idxToRemove >= 0) {
            list.remove(idxToRemove);
            int sel = getSelectedIndex(stack);
            if (list.isEmpty()) {
                setSelectedIndex(stack, 0);
            } else if (idxToRemove < sel) {
                setSelectedIndex(stack, Math.max(0, sel - 1));
            } else if (idxToRemove == sel) {
                setSelectedIndex(stack, sel % list.size());
            }
            return true;
        }
        return false;
    }

    private static void selectByUUID(ItemStack stack, UUID uuid) {
        ListTag list = trackedList(stack);
        for (int i = 0; i < list.size(); i++) {
            if (uuid.toString().equalsIgnoreCase(list.getCompound(i).getString(KEY_ID))) {
                setSelectedIndex(stack, i);
                return;
            }
        }
    }

    private static int getSelectedIndex(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int size = trackedList(stack).size();
        int sel = tag.getInt(TAG_SELECTED);
        if (size == 0) return 0;
        if (sel < 0 || sel >= size) sel = 0;
        return sel;
    }

    private static void setSelectedIndex(ItemStack stack, int idx) {
        stack.getOrCreateTag().putInt(TAG_SELECTED, Math.max(0, idx));
    }

    private static Optional<UUID> getSelectedUUID(ItemStack stack) {
        ListTag list = trackedList(stack);
        if (list.isEmpty()) return Optional.empty();
        int sel = getSelectedIndex(stack);
        CompoundTag e = list.getCompound(sel);
        try {
            return Optional.of(UUID.fromString(e.getString(KEY_ID)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private static Optional<String> getNameAt(ListTag list, int idx) {
        if (idx < 0 || idx >= list.size()) return Optional.empty();
        CompoundTag e = list.getCompound(idx);
        if (e.contains(KEY_NAME, Tag.TAG_STRING)) return Optional.of(e.getString(KEY_NAME));
        return Optional.of("Synth");
    }
}