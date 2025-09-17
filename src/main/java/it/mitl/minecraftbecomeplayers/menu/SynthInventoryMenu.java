package it.mitl.minecraftbecomeplayers.menu;

import it.mitl.minecraftbecomeplayers.entity.custom.SynthEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SynthInventoryMenu extends AbstractContainerMenu {
    private final SynthEntity synth;
    private final IItemHandler itemHandler;

    public SynthInventoryMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(windowId, playerInventory, (SynthEntity) playerInventory.player.level().getEntity(buf.readVarInt()));
    }

    public SynthInventoryMenu(int windowId, Inventory playerInventory, SynthEntity synth) {
        super(ModMenus.SYNTH_INVENTORY.get(), windowId);
        this.synth = synth;
        this.itemHandler = synth.getItemHandler();

    // Synth inventory (3x9)
    int index = 0;
    int startX = 8;
    int startY = 18;
    int columns = 9;
    int rows = 3;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                this.addSlot(new SlotItemHandler(itemHandler, index++, startX + c * 18, startY + r * 18));
            }
        }

        // Player inventory (3x9)
        int playerInvY = startY + rows * 18 + 14;
        for (int r = 0; r < 3; ++r) {
            for (int c = 0; c < 9; ++c) {
                this.addSlot(new Slot(playerInventory, c + r * 9 + 9, 8 + c * 18, playerInvY + r * 18));
            }
        }
        // Hotbar (1x9)
        int hotbarY = playerInvY + 58;
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, hotbarY));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(synth) < 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            int synthSlots = 27;
            if (index < synthSlots) {
                // Move from synth to player
                if (!this.moveItemStackTo(stackInSlot, synthSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from player to synth
                if (!this.moveItemStackTo(stackInSlot, 0, synthSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }
}
