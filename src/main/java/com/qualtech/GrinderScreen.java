package com.qualtech;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

// No custom art assets exist yet, so the panel/slots/bars are drawn procedurally with GuiGraphics.fill
// instead of blitting a background texture. Swap in a real texture later by overriding renderBg to blit it.
public class GrinderScreen extends AbstractContainerScreen<GrinderMenu> {
    private static final int PROGRESS_X = 79;
    private static final int PROGRESS_Y = 39;
    private static final int PROGRESS_WIDTH = 34;
    private static final int PROGRESS_HEIGHT = 8;

    private static final int ENERGY_BAR_X = 152;
    private static final int ENERGY_BAR_Y = 17;
    private static final int ENERGY_BAR_WIDTH = 8;
    private static final int ENERGY_BAR_HEIGHT = 54;

    public GrinderScreen(GrinderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF8B8B8B);
        guiGraphics.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, 0xFFC6C6C6);

        drawSlotBackground(guiGraphics, x + 56, y + 35);
        drawSlotBackground(guiGraphics, x + 116, y + 35);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBackground(guiGraphics, x + 8 + col * 18, y + 84 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotBackground(guiGraphics, x + 8 + col * 18, y + 142);
        }

        guiGraphics.fill(x + PROGRESS_X, y + PROGRESS_Y, x + PROGRESS_X + PROGRESS_WIDTH, y + PROGRESS_Y + PROGRESS_HEIGHT, 0xFF373737);
        int progressWidth = getProgressWidth();
        if (progressWidth > 0) {
            guiGraphics.fill(x + PROGRESS_X, y + PROGRESS_Y, x + PROGRESS_X + progressWidth, y + PROGRESS_Y + PROGRESS_HEIGHT, 0xFFDC6B0F);
        }

        guiGraphics.fill(x + ENERGY_BAR_X, y + ENERGY_BAR_Y, x + ENERGY_BAR_X + ENERGY_BAR_WIDTH, y + ENERGY_BAR_Y + ENERGY_BAR_HEIGHT, 0xFF373737);
        int energyHeight = getEnergyBarHeight();
        if (energyHeight > 0) {
            guiGraphics.fill(x + ENERGY_BAR_X, y + ENERGY_BAR_Y + ENERGY_BAR_HEIGHT - energyHeight,
                    x + ENERGY_BAR_X + ENERGY_BAR_WIDTH, y + ENERGY_BAR_Y + ENERGY_BAR_HEIGHT, 0xFFDD0000);
        }
    }

    private void drawSlotBackground(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
    }

    private int getProgressWidth() {
        int maxProgress = menu.getMaxProgress();
        return maxProgress == 0 ? 0 : menu.getProgress() * PROGRESS_WIDTH / maxProgress;
    }

    private int getEnergyBarHeight() {
        int maxEnergy = menu.getMaxEnergyStored();
        return maxEnergy == 0 ? 0 : menu.getEnergyStored() * ENERGY_BAR_HEIGHT / maxEnergy;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
