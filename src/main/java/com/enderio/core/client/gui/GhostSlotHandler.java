package com.enderio.core.client.gui;

import static com.enderio.core.client.render.EnderWidget.NEUTRAL_SLOT_BACKGROUND;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.client.render.RenderUtil;
import com.enderio.core.common.util.ItemUtil;
import com.google.common.collect.Lists;

public class GhostSlotHandler {

    protected List<GhostSlot> ghostSlots = Lists.newArrayList();
    protected GhostSlot hoverGhostSlot;

    public GhostSlotHandler() {}

    // GhostSlot managing

    protected List<GhostSlot> getGhostSlots() {
        return ghostSlots;
    }

    protected GhostSlot getGhostSlot(GuiContainerBase guiContainerBase, int mouseX, int mouseY) {
        mouseX -= guiContainerBase.getGuiLeft();
        mouseY -= guiContainerBase.getGuiTop();
        for (GhostSlot slot : ghostSlots) {
            if (slot.isVisible() && slot.isMouseOver(mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    // Slot interaction

    /**
     * Called when a ghost slot is clicked or mouse wheeled.
     * 
     * @param gui    The GUI the GhostSlot is in
     * @param slot   The GhostSlot
     * @param x      Mouse position x
     * @param y      Mouse position y
     * @param button The button used (0=left, 1=right). The mouse wheel is mapped to -1=down and -2=up.
     */
    protected void ghostSlotClicked(GuiContainerBase gui, GhostSlot slot, int x, int y, int button) {
        ItemStack handStack = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
        ItemStack existingStack = slot.getStack();
        if (button == 0) { // left
            ghostSlotClickedPrimaryMouseButton(slot, handStack, existingStack);
        } else if (button == 1) { // right
            ghostSlotClickedSecondaryMouseButton(slot, handStack, existingStack);
        } else if (button == -2) { // wheel up
            ghostSlotClickedMouseWheelUp(slot, handStack, existingStack);
        } else if (button == -1) { // wheel down
            ghostSlotClickedMouseWheelDown(slot, handStack, existingStack);
        }
    }

    protected void ghostSlotClickedPrimaryMouseButton(GhostSlot slot, ItemStack handStack, ItemStack existingStack) {
        if (handStack == null || handStack.getItem() == null || handStack.stackSize == 0) { // empty hand
            slot.putStack(null);
        } else { // item in hand
            if (existingStack == null || existingStack.getItem() == null || existingStack.stackSize == 0) { // empty
                                                                                                            // slot
                replaceSlot(slot, handStack);
            } else { // filled slot
                if (ItemUtil.areStackMergable(existingStack, handStack)) { // same item
                    if (existingStack.stackSize < existingStack.getMaxStackSize()
                            && existingStack.stackSize < slot.getStackSizeLimit()) {
                        increaseSlot(slot, existingStack);
                    } else {
                        // NOP
                    }
                } else { // different item
                    replaceSlot(slot, handStack);
                }
            }
        }
    }

    protected void ghostSlotClickedSecondaryMouseButton(GhostSlot slot, ItemStack handStack, ItemStack existingStack) {
        if (handStack == null || handStack.getItem() == null || handStack.stackSize == 0) { // empty hand
            slot.putStack(null);
        } else { // item in hand
            if (existingStack == null || existingStack.getItem() == null || existingStack.stackSize == 0) { // empty
                                                                                                            // slot
                replaceSlot1Item(slot, handStack);
            } else { // filled slot
                if (ItemUtil.areStackMergable(existingStack, handStack)) { // same item
                    descreaseSlot(slot, existingStack);
                } else { // different item
                    replaceSlot1Item(slot, handStack);
                }
            }
        }
    }

    protected void ghostSlotClickedMouseWheelUp(GhostSlot slot, ItemStack handStack, ItemStack existingStack) {
        if (existingStack != null && existingStack.getItem() != null
                && existingStack.stackSize > 0
                && existingStack.stackSize < existingStack.getMaxStackSize()
                && existingStack.stackSize < slot.getStackSizeLimit()) {
            increaseSlot(slot, existingStack);
        }
    }

    protected void ghostSlotClickedMouseWheelDown(GhostSlot slot, ItemStack handStack, ItemStack existingStack) {
        if (existingStack != null && existingStack.getItem() != null) {
            descreaseSlot(slot, existingStack);
        }
    }

    // Slot interaction tools

    protected void descreaseSlot(GhostSlot slot, ItemStack existingStack) {
        if (existingStack.stackSize > 1) {
            existingStack.stackSize--;
            slot.putStack(existingStack);
        } else {
            slot.putStack(null);
        }
    }

    protected void increaseSlot(GhostSlot slot, ItemStack existingStack) {
        existingStack.stackSize++;
        slot.putStack(existingStack);
    }

    protected void replaceSlot1Item(GhostSlot slot, ItemStack handStack) {
        ItemStack oneItem = handStack.copy();
        oneItem.stackSize = 1;
        slot.putStack(oneItem);
    }

    protected void replaceSlot(GhostSlot slot, ItemStack handStack) {
        if (handStack.stackSize <= slot.getStackSizeLimit()) {
            slot.putStack(handStack);
        } else {
            ItemStack tmp = handStack.copy();
            tmp.stackSize = slot.getStackSizeLimit();
            slot.putStack(tmp);
        }
    }

    // Rendering

    protected void startDrawing(GuiContainerBase gui) {
        gui.hoverGhostSlot = hoverGhostSlot = null;
    }

    protected void drawGhostSlots(GuiContainerBase gui, int mouseX, int mouseY) {
        int sx = gui.getGuiLeft();
        int sy = gui.getGuiTop();
        gui.drawFakeItemsStart();
        try {
            gui.hoverGhostSlot = hoverGhostSlot = null;
            for (GhostSlot slot : ghostSlots) {
                ItemStack stack = slot.getStack();
                if (slot.isVisible()) {
                    if (stack != null) {
                        gui.drawFakeItemStack(slot.x + sx, slot.y + sy, stack);
                        if (slot.shouldDisplayStdOverlay()) {
                            gui.drawFakeItemStackStdOverlay(slot.x + sx, slot.y + sy, stack);
                        }
                        if (slot.shouldGrayOut()) {
                            gui.ghostSlotHandler.drawGhostSlotGrayout(gui, slot);
                        }
                    }
                    if (slot.isMouseOver(mouseX - sx, mouseY - sy)) {
                        gui.hoverGhostSlot = hoverGhostSlot = slot;
                    }
                }
            }
            if (hoverGhostSlot != null) {
                // draw hover last to prevent it from affecting rendering of other slots ...
                gui.drawFakeItemHover(hoverGhostSlot.x + sx, hoverGhostSlot.y + sy);
            }
        } finally {
            gui.drawFakeItemsEnd();
        }
    }

    /**
     * Gray out the item that was just painted into a GhostSlot by overpainting it with 50% transparent background. This
     * gives the illusion that the item was painted with 50% transparency. (100%*a ° 100%*b ° 50%*a == 100%*a ° 50%*b)
     */
    protected void drawGhostSlotGrayout(GuiContainerBase gui, GhostSlot slot) {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, slot.getGrayOutLevel());
        String guiTexture = gui.getGuiTexture();
        if (guiTexture == null) {
            NEUTRAL_SLOT_BACKGROUND.getMap().render(
                    NEUTRAL_SLOT_BACKGROUND,
                    gui.getGuiLeft() + slot.x,
                    gui.getGuiTop() + slot.y,
                    gui.getZlevel(),
                    true);
        } else {
            RenderUtil.bindTexture(guiTexture);
            gui.drawTexturedModalRect(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, slot.x, slot.y, 16, 16);
        }
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    protected void drawGhostSlotTooltip(GuiContainerBase guiContainerBase, GhostSlot slot, int mouseX, int mouseY) {
        ItemStack stack = slot.getStack();
        if (stack != null) {
            guiContainerBase.renderToolTip(stack, mouseX, mouseY);
        }
    }

    protected void drawGhostSlotToolTip(GuiContainerBase gui, int mouseX, int mouseY) {
        if (hoverGhostSlot != null && gui.mc.thePlayer.inventory.getItemStack() == null) {
            gui.drawGhostSlotTooltip(hoverGhostSlot, mouseX, mouseY);
        }

    }

}
