package fewizz.bt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class NETTButton extends GuiButton {
    boolean left = false;

    public NETTButton(int id, int x, int y, String text, boolean left) {
        super(id, x, y, text);
        this.width = 30;
        this.height = 20;
        this.left = left;
    }

    public NETTButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
    }

    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(3042);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.left) {
            if (this.enabled) {
                minecraft.getTextureManager().bindTexture(new ResourceLocation("bt:TC_button_left.png"));
            } else {
                minecraft.getTextureManager().bindTexture(new ResourceLocation("bt:TC_button_left_off.png"));
            }
        } else if (this.enabled) {
            minecraft.getTextureManager().bindTexture(new ResourceLocation("bt:TC_button_right.png"));
        } else {
            minecraft.getTextureManager().bindTexture(new ResourceLocation("bt:TC_button_right_off.png"));
        }

        GL11.glPushMatrix();
        GL11.glScalef(0.125F, 0.125F, 1.0F);
        this.drawTexturedModalRect(this.xPosition * 8, this.yPosition * 8, 0, 0, 256, 256);
        GL11.glPopMatrix();
        GL11.glDisable(3042);
        RenderHelper.disableStandardItemLighting();
    }
}
