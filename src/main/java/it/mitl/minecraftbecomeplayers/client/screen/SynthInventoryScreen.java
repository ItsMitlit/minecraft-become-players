package it.mitl.minecraftbecomeplayers.client.screen;

import it.mitl.minecraftbecomeplayers.menu.SynthInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SynthInventoryScreen extends AbstractContainerScreen<SynthInventoryMenu> {
    // Vanilla container texture (generic_54 for chest look)
    private static final ResourceLocation CONTAINER_GENERIC_54 = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    public SynthInventoryScreen(SynthInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        int rows = 3;
        this.imageHeight = 114 + rows * 18;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int rows = 3;
        // Synth inventory
        graphics.blit(CONTAINER_GENERIC_54, x, y, 0, 0, this.imageWidth, rows * 18 + 17);
        // Player inventory
        graphics.blit(CONTAINER_GENERIC_54, x, y + rows * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
