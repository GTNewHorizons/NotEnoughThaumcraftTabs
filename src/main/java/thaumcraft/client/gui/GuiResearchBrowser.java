package thaumcraft.client.gui;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fewizz.bt.NETTButton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.tile.TileNodeRenderer;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.Config;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketPlayerCompleteToServer;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.utils.InventoryUtils;

@SideOnly(Side.CLIENT)
public class GuiResearchBrowser extends GuiScreen {
   private static int guiMapTop;
   private static int guiMapLeft;
   private static int guiMapBottom;
   private static int guiMapRight;
   protected int paneWidth = 256;
   protected int paneHeight = 230;
   protected int mouseX = 0;
   protected int mouseY = 0;
   protected double field_74117_m;
   protected double field_74115_n;
   protected double guiMapX;
   protected double guiMapY;
   protected double field_74124_q;
   protected double field_74123_r;
   private int isMouseButtonDown = 0;
   public static int lastX = -5;
   public static int lastY = -6;
   private GuiButton button;
   private LinkedList research = new LinkedList();
   public static HashMap completedResearch = new HashMap();
   public static ArrayList highlightedItem = new ArrayList();
   private static String selectedCategory = null;
   private FontRenderer galFontRenderer;
   private ResearchItem currentHighlight = null;
   private String player = "";
   long popuptime = 0L;
   String popupmessage = "";
   public boolean hasScribestuff = false;
   public GuiButton buttonBack;
   public GuiButton buttonNext;
   public static int page = 0;
   public static int max = 0;

   public GuiResearchBrowser() {
      short var2 = 141;
      short var3 = 141;
      this.field_74117_m = this.guiMapX = this.field_74124_q = (double)(lastX * 24 - var2 / 2 - 12);
      this.field_74115_n = this.guiMapY = this.field_74123_r = (double)(lastY * 24 - var3 / 2);
      this.updateResearch();
      this.galFontRenderer = FMLClientHandler.instance().getClient().standardGalacticFontRenderer;
      this.player = Minecraft.getMinecraft().thePlayer.getCommandSenderName();
   }

   public GuiResearchBrowser(double x, double y) {
      this.field_74117_m = this.guiMapX = this.field_74124_q = x;
      this.field_74115_n = this.guiMapY = this.field_74123_r = y;
      this.updateResearch();
      this.galFontRenderer = FMLClientHandler.instance().getClient().standardGalacticFontRenderer;
      this.player = Minecraft.getMinecraft().thePlayer.getCommandSenderName();
   }

   public void updateResearch() {
      if (this.mc == null) {
         this.mc = Minecraft.getMinecraft();
      }

      this.research.clear();
      this.hasScribestuff = false;
      if (selectedCategory == null) {
         Set col = ResearchCategories.researchCategories.keySet();
         selectedCategory = (String)col.iterator().next();
      }

      Collection col1 = ResearchCategories.getResearchList(selectedCategory).research.values();
      Iterator i$ = col1.iterator();

      while(i$.hasNext()) {
         Object res = i$.next();
         this.research.add((ResearchItem)res);
      }

      if (ResearchManager.consumeInkFromPlayer(this.mc.thePlayer, false) && InventoryUtils.isPlayerCarrying(this.mc.thePlayer, new ItemStack(Items.paper)) >= 0) {
         this.hasScribestuff = true;
      }

      guiMapTop = ResearchCategories.getResearchList(selectedCategory).minDisplayColumn * 24 - 85;
      guiMapLeft = ResearchCategories.getResearchList(selectedCategory).minDisplayRow * 24 - 112;
      guiMapBottom = ResearchCategories.getResearchList(selectedCategory).maxDisplayColumn * 24 - 112;
      guiMapRight = ResearchCategories.getResearchList(selectedCategory).maxDisplayRow * 24 - 61;
   }

   public void onGuiClosed() {
      short var2 = 141;
      short var3 = 141;
      lastX = (int)((this.guiMapX + (double)(var2 / 2) + 12.0D) / 24.0D);
      lastY = (int)((this.guiMapY + (double)(var3 / 2)) / 24.0D);
      super.onGuiClosed();
   }

   public void initGui() {
      this.buttonList.add(this.buttonNext = new NETTButton(1, this.width / 2 + 150, this.height / 2 - 16, "", false));
      this.buttonList.add(this.buttonBack = new NETTButton(2, this.width / 2 - 180, this.height / 2 - 16, "", true));
   }

   protected void actionPerformed(GuiButton button) {
      super.actionPerformed(button);
      if (button.id == 1) {
         if (page <= max) {
            ++page;
            Minecraft.getMinecraft().theWorld.playSound(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ, "thaumcraft:page", 0.66F, 1.0F, false);
         }
      } else if (button.id == 2 && page > 0) {
         --page;
         Minecraft.getMinecraft().theWorld.playSound(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ, "thaumcraft:page", 0.66F, 1.0F, false);
      }

   }

   protected void keyTyped(char par1, int key) {
      super.keyTyped(par1, key);
      if (key == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
         highlightedItem.clear();
         this.mc.displayGuiScreen((GuiScreen)null);
         this.mc.setIngameFocus();
      } else if (key == 1) {
         highlightedItem.clear();
      } else if (key != 200 && key != 203) {
         if ((key == 208 || key == 205) && max != page) {
            ++page;
            Minecraft.getMinecraft().theWorld.playSound(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ, "thaumcraft:page", 0.66F, 1.0F, false);
         }
      } else if (page != 0) {
         --page;
         Minecraft.getMinecraft().theWorld.playSound(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ, "thaumcraft:page", 0.66F, 1.0F, false);
      }

   }

   public void drawScreen(int mx, int my, float par3) {
      this.buttonBack.enabled = page != 0;
      this.buttonNext.enabled = max != page;
      int var4 = (this.width - this.paneWidth) / 2;
      int var5 = (this.height - this.paneHeight) / 2;
      int cats;
      int count;
      if (Mouse.isButtonDown(0)) {
         cats = var4 + 8;
         count = var5 + 17;
         if ((this.isMouseButtonDown == 0 || this.isMouseButtonDown == 1) && mx >= cats && mx < cats + 224 && my >= count && my < count + 196) {
            if (this.isMouseButtonDown == 0) {
               this.isMouseButtonDown = 1;
            } else {
               this.guiMapX -= (double)(mx - this.mouseX);
               this.guiMapY -= (double)(my - this.mouseY);
               this.field_74124_q = this.field_74117_m = this.guiMapX;
               this.field_74123_r = this.field_74115_n = this.guiMapY;
            }

            this.mouseX = mx;
            this.mouseY = my;
         }

         if (this.field_74124_q < (double)guiMapTop) {
            this.field_74124_q = (double)guiMapTop;
         }

         if (this.field_74123_r < (double)guiMapLeft) {
            this.field_74123_r = (double)guiMapLeft;
         }

         if (this.field_74124_q >= (double)guiMapBottom) {
            this.field_74124_q = (double)(guiMapBottom - 1);
         }

         if (this.field_74123_r >= (double)guiMapRight) {
            this.field_74123_r = (double)(guiMapRight - 1);
         }
      } else {
         this.isMouseButtonDown = 0;
      }

      this.drawDefaultBackground();
      this.genResearchBackground(mx, my, par3);
      if (this.popuptime > System.currentTimeMillis()) {
         cats = var4 + 128;
         count = var5 + 128;
         int swop = this.fontRendererObj.splitStringWidth(this.popupmessage, 150) / 2;
         this.drawGradientRect(cats - 78, count - swop - 3, cats + 78, count + swop + 3, -1073741824, -1073741824);
         this.fontRendererObj.drawSplitString(this.popupmessage, cats - 75, count - swop, 150, -7302913);
      }

      Set var14 = ResearchCategories.researchCategories.keySet();
      count = 0;
      int oldCount = 0;
      boolean var15 = false;

      for(Iterator i$ = var14.iterator(); i$.hasNext(); count = oldCount) {
         Object obj = i$.next();
         if (count - count / 18 * 18 >= 9) {
            oldCount = count;
            count -= 9;
            count -= page * 18;
            var15 = true;
         } else {
            oldCount = count;
            count -= page * 18;
            var15 = false;
         }

         ResearchCategoryList rcl = ResearchCategories.getResearchList((String)obj);
         if (!((String)obj).equals("ELDRITCH") || ResearchManager.isResearchComplete(this.player, "ELDRITCHMINOR")) {
            int mposx = mx - (var4 - 24 + (var15 ? 280 : 0));
            int mposy = my - (var5 + count * 24);
            if (mposx >= 0 && mposx < 24 && mposy >= 0 && mposy < 24) {
               this.fontRendererObj.drawStringWithShadow(ResearchCategories.getCategoryName((String)obj), mx, my - 8, 16777215);
            }

            ++oldCount;
         }
      }

   }

   public void updateScreen() {
      this.field_74117_m = this.guiMapX;
      this.field_74115_n = this.guiMapY;
      double var1 = this.field_74124_q - this.guiMapX;
      double var3 = this.field_74123_r - this.guiMapY;
      if (var1 * var1 + var3 * var3 < 4.0D) {
         this.guiMapX += var1;
         this.guiMapY += var3;
      } else {
         this.guiMapX += var1 * 0.85D;
         this.guiMapY += var3 * 0.85D;
      }

   }

   protected void genResearchBackground(int par1, int par2, float par3) {
      long t = System.nanoTime() / 50000000L;
      int var4 = MathHelper.floor_double(this.field_74117_m + (this.guiMapX - this.field_74117_m) * (double)par3);
      int var5 = MathHelper.floor_double(this.field_74115_n + (this.guiMapY - this.field_74115_n) * (double)par3);
      if (var4 < guiMapTop) {
         var4 = guiMapTop;
      }

      if (var5 < guiMapLeft) {
         var5 = guiMapLeft;
      }

      if (var4 >= guiMapBottom) {
         var4 = guiMapBottom - 1;
      }

      if (var5 >= guiMapRight) {
         var5 = guiMapRight - 1;
      }

      int xR = (this.width - this.paneWidth) / 2;
      int yR = (this.height - this.paneHeight) / 2;
      int xR2 = xR + 16;
      int yR2 = yR + 17;
      this.zLevel = 0.0F;
      GL11.glDepthFunc(518);
      GL11.glPushMatrix();
      GL11.glTranslatef(0.0F, 0.0F, -200.0F);
      GL11.glEnable(3553);
      RenderHelper.enableGUIStandardItemLighting();
      GL11.glDisable(2896);
      GL11.glEnable(32826);
      GL11.glEnable(2903);
      GL11.glPushMatrix();
      GL11.glScalef(2.0F, 2.0F, 1.0F);
      int vx = (int)((float)(var4 - guiMapTop) / (float)Math.abs(guiMapTop - guiMapBottom) * 288.0F);
      int vy = (int)((float)(var5 - guiMapLeft) / (float)Math.abs(guiMapLeft - guiMapRight) * 316.0F);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      Minecraft.getMinecraft().renderEngine.bindTexture(ResearchCategories.getResearchList(selectedCategory).background);
      this.drawTexturedModalRect(xR2 / 2, yR2 / 2, vx / 2, vy / 2, 112, 98);
      GL11.glScalef(0.5F, 0.5F, 1.0F);
      GL11.glPopMatrix();
      GL11.glEnable(2929);
      GL11.glDepthFunc(515);
      int var24;
      int var26;
      int var27;
      int var42;
      boolean var30;
      if (completedResearch.get(this.player) != null) {
         for(int var22 = 0; var22 < this.research.size(); ++var22) {
            ResearchItem itemRenderer = (ResearchItem)this.research.get(var22);
            int var25;
            boolean cats;
            ResearchItem var41;
            boolean count;
            if (itemRenderer.parents != null && itemRenderer.parents.length > 0) {
               for(var42 = 0; var42 < itemRenderer.parents.length; ++var42) {
                  if (itemRenderer.parents[var42] != null && ResearchCategories.getResearch(itemRenderer.parents[var42]).category.equals(selectedCategory)) {
                     var41 = ResearchCategories.getResearch(itemRenderer.parents[var42]);
                     if (!var41.isVirtual()) {
                        var24 = itemRenderer.displayColumn * 24 - var4 + 11 + xR2;
                        var25 = itemRenderer.displayRow * 24 - var5 + 11 + yR2;
                        var26 = var41.displayColumn * 24 - var4 + 11 + xR2;
                        var27 = var41.displayRow * 24 - var5 + 11 + yR2;
                        cats = ((ArrayList)completedResearch.get(this.player)).contains(itemRenderer.key);
                        count = ((ArrayList)completedResearch.get(this.player)).contains(var41.key);
                        var30 = Math.sin((double)(Minecraft.getSystemTime() % 600L) / 600.0D * 3.141592653589793D * 2.0D) > 0.6D ? true : true;
                        if (cats) {
                           this.drawLine(var24, var25, var26, var27, 0.1F, 0.1F, 0.1F, par3, false);
                        } else if (!itemRenderer.isLost() && (!itemRenderer.isHidden() && !itemRenderer.isLost() || ((ArrayList)completedResearch.get(this.player)).contains("@" + itemRenderer.key)) && (!itemRenderer.isConcealed() || this.canUnlockResearch(itemRenderer))) {
                           if (count) {
                              this.drawLine(var24, var25, var26, var27, 0.0F, 1.0F, 0.0F, par3, true);
                           } else if ((!var41.isHidden() && !itemRenderer.isLost() || ((ArrayList)completedResearch.get(this.player)).contains("@" + var41.key)) && (!var41.isConcealed() || this.canUnlockResearch(var41))) {
                              this.drawLine(var24, var25, var26, var27, 0.0F, 0.0F, 1.0F, par3, true);
                           }
                        }
                     }
                  }
               }
            }

            if (itemRenderer.siblings != null && itemRenderer.siblings.length > 0) {
               for(var42 = 0; var42 < itemRenderer.siblings.length; ++var42) {
                  if (itemRenderer.siblings[var42] != null && ResearchCategories.getResearch(itemRenderer.siblings[var42]).category.equals(selectedCategory)) {
                     var41 = ResearchCategories.getResearch(itemRenderer.siblings[var42]);
                     if (!var41.isVirtual() && (var41.parents == null || var41.parents != null && !Arrays.asList(var41.parents).contains(itemRenderer.key))) {
                        var24 = itemRenderer.displayColumn * 24 - var4 + 11 + xR2;
                        var25 = itemRenderer.displayRow * 24 - var5 + 11 + yR2;
                        var26 = var41.displayColumn * 24 - var4 + 11 + xR2;
                        var27 = var41.displayRow * 24 - var5 + 11 + yR2;
                        cats = ((ArrayList)completedResearch.get(this.player)).contains(itemRenderer.key);
                        count = ((ArrayList)completedResearch.get(this.player)).contains(var41.key);
                        if (cats) {
                           this.drawLine(var24, var25, var26, var27, 0.1F, 0.1F, 0.2F, par3, false);
                        } else if (!itemRenderer.isLost() && (!itemRenderer.isHidden() || ((ArrayList)completedResearch.get(this.player)).contains("@" + itemRenderer.key)) && (!itemRenderer.isConcealed() || this.canUnlockResearch(itemRenderer))) {
                           if (count) {
                              this.drawLine(var24, var25, var26, var27, 0.0F, 1.0F, 0.0F, par3, true);
                           } else if ((!var41.isHidden() || ((ArrayList)completedResearch.get(this.player)).contains("@" + var41.key)) && (!var41.isConcealed() || this.canUnlockResearch(var41))) {
                              this.drawLine(var24, var25, var26, var27, 0.0F, 0.0F, 1.0F, par3, true);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      this.currentHighlight = null;
      RenderItem var43 = new RenderItem();
      GL11.glEnable(32826);
      GL11.glEnable(2903);
      int var44;
      if (completedResearch.get(this.player) != null) {
         for(var24 = 0; var24 < this.research.size(); ++var24) {
            ResearchItem var48 = (ResearchItem)this.research.get(var24);
            var26 = var48.displayColumn * 24 - var4;
            var27 = var48.displayRow * 24 - var5;
            if (!var48.isVirtual() && var26 >= -24 && var27 >= -24 && var26 <= 224 && var27 <= 196) {
               var42 = xR2 + var26;
               var44 = yR2 + var27;
               float var50;
               if (((ArrayList)completedResearch.get(this.player)).contains(var48.key)) {
                  if (ThaumcraftApi.getWarp(var48.key) > 0) {
                     this.drawForbidden((double)(var42 + 11), (double)(var44 + 11));
                  }

                  var50 = 1.0F;
                  GL11.glColor4f(var50, var50, var50, 1.0F);
               } else {
                  if (!((ArrayList)completedResearch.get(this.player)).contains("@" + var48.key) && (var48.isLost() || var48.isHidden() && !((ArrayList)completedResearch.get(this.player)).contains("@" + var48.key) || var48.isConcealed() && !this.canUnlockResearch(var48))) {
                     continue;
                  }

                  if (ThaumcraftApi.getWarp(var48.key) > 0) {
                     this.drawForbidden((double)(var42 + 11), (double)(var44 + 11));
                  }

                  if (this.canUnlockResearch(var48)) {
                     var50 = (float)Math.sin((double)(Minecraft.getSystemTime() % 600L) / 600.0D * 3.141592653589793D * 2.0D) * 0.25F + 0.75F;
                     GL11.glColor4f(var50, var50, var50, 1.0F);
                  } else {
                     var50 = 0.3F;
                     GL11.glColor4f(var50, var50, var50, 1.0F);
                  }
               }

               UtilsFX.bindTexture("textures/gui/gui_research.png");
               GL11.glEnable(2884);
               GL11.glEnable(3042);
               GL11.glBlendFunc(770, 771);
               if (var48.isRound()) {
                  this.drawTexturedModalRect(var42 - 2, var44 - 2, 54, 230, 26, 26);
               } else if (var48.isHidden()) {
                  if (Config.researchDifficulty != -1 && (Config.researchDifficulty != 0 || !var48.isSecondary())) {
                     this.drawTexturedModalRect(var42 - 2, var44 - 2, 86, 230, 26, 26);
                  } else {
                     this.drawTexturedModalRect(var42 - 2, var44 - 2, 230, 230, 26, 26);
                  }
               } else if (Config.researchDifficulty == -1 || Config.researchDifficulty == 0 && var48.isSecondary()) {
                  this.drawTexturedModalRect(var42 - 2, var44 - 2, 110, 230, 26, 26);
               } else {
                  this.drawTexturedModalRect(var42 - 2, var44 - 2, 0, 230, 26, 26);
               }

               if (var48.isSpecial()) {
                  this.drawTexturedModalRect(var42 - 2, var44 - 2, 26, 230, 26, 26);
               }

               if (!this.canUnlockResearch(var48)) {
                  float swop = 0.1F;
                  GL11.glColor4f(swop, swop, swop, 1.0F);
                  var43.renderWithColor = false;
               }

               GL11.glDisable(3042);
               if (highlightedItem.contains(var48.key)) {
                  GL11.glPushMatrix();
                  GL11.glEnable(3042);
                  GL11.glBlendFunc(770, 771);
                  GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                  this.mc.renderEngine.bindTexture(ParticleEngine.particleTexture);
                  int var45 = (int)(t % 16L) * 16;
                  GL11.glTranslatef((float)(var42 - 5), (float)(var44 - 5), 0.0F);
                  UtilsFX.drawTexturedQuad(0, 0, var45, 80, 16, 16, 0.0D);
                  GL11.glDisable(3042);
                  GL11.glPopMatrix();
               }

               if (var48.icon_item != null) {
                  GL11.glPushMatrix();
                  GL11.glEnable(3042);
                  GL11.glBlendFunc(770, 771);
                  RenderHelper.enableGUIStandardItemLighting();
                  GL11.glDisable(2896);
                  GL11.glEnable(32826);
                  GL11.glEnable(2903);
                  GL11.glEnable(2896);
                  var43.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.renderEngine, InventoryUtils.cycleItemStack(var48.icon_item), var42 + 3, var44 + 3);
                  GL11.glDisable(2896);
                  GL11.glDepthMask(true);
                  GL11.glEnable(2929);
                  GL11.glDisable(3042);
                  GL11.glPopMatrix();
               } else if (var48.icon_resource != null) {
                  GL11.glPushMatrix();
                  GL11.glEnable(3042);
                  GL11.glBlendFunc(770, 771);
                  this.mc.renderEngine.bindTexture(var48.icon_resource);
                  if (!var43.renderWithColor) {
                     GL11.glColor4f(0.2F, 0.2F, 0.2F, 1.0F);
                  }

                  UtilsFX.drawTexturedQuadFull(var42 + 3, var44 + 3, (double)this.zLevel);
                  GL11.glPopMatrix();
               }

               if (!this.canUnlockResearch(var48)) {
                  var43.renderWithColor = true;
               }

               if (par1 >= xR2 && par2 >= yR2 && par1 < xR2 + 224 && par2 < yR2 + 196 && par1 >= var42 && par1 <= var42 + 22 && par2 >= var44 && par2 <= var44 + 22) {
                  this.currentHighlight = var48;
               }

               GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      }

      GL11.glDisable(2929);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      Set researchCategories = ResearchCategories.researchCategories.keySet();
      int numberOfPage = 0;
      boolean lefted = false;
      Iterator var34 = researchCategories.iterator();
      max = (ResearchManager.isResearchComplete(this.player, "ELDRITCHMINOR") ? researchCategories.size() - 1 : researchCategories.size() - 2) / 18;
      System.out.println(researchCategories.size() + " " + max);
      var30 = false;

      while(true) {
         int warp;
         Object research;
         ResearchCategoryList researchCategoryList;
         int ws;
         do {
            if (!var34.hasNext()) {
               UtilsFX.bindTexture("textures/gui/gui_research.png");
               GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
               this.drawTexturedModalRect(xR, yR, 0, 0, this.paneWidth, this.paneHeight);
               GL11.glPopMatrix();
               this.zLevel = 0.0F;
               GL11.glDepthFunc(515);
               GL11.glDisable(2929);
               GL11.glEnable(3553);
               super.drawScreen(par1, par2, par3);
               if (completedResearch.get(this.player) != null && this.currentHighlight != null) {
                  String string = this.currentHighlight.getName();
                  var26 = par1 + 6;
                  var27 = par2 - 4;
                  int var53 = 0;
                  FontRenderer var51 = this.fontRendererObj;
                  if (!((ArrayList)completedResearch.get(this.player)).contains(this.currentHighlight.key) && !this.canUnlockResearch(this.currentHighlight)) {
                     var51 = this.galFontRenderer;
                  }

                  if (!this.canUnlockResearch(this.currentHighlight)) {
                     GL11.glPushMatrix();
                     var42 = (int)Math.max((float)var51.getStringWidth(string), (float)var51.getStringWidth(StatCollector.translateToLocal("tc.researchmissing")) / 1.5F);
                     String var56 = StatCollector.translateToLocal("tc.researchmissing");
                     ws = var51.splitStringWidth(var56, var42 * 2);
                     this.drawGradientRect(var26 - 3, var27 - 3, var26 + var42 + 3, var27 + ws + 10, -1073741824, -1073741824);
                     GL11.glTranslatef((float)var26, (float)(var27 + 12), 0.0F);
                     GL11.glScalef(0.5F, 0.5F, 0.5F);
                     this.fontRendererObj.drawSplitString(var56, 0, 0, var42 * 2, -9416624);
                     GL11.glPopMatrix();
                  } else {
                     boolean var55 = !((ArrayList)completedResearch.get(this.player)).contains(this.currentHighlight.key) && this.currentHighlight.tags != null && this.currentHighlight.tags.size() > 0 && (Config.researchDifficulty == -1 || Config.researchDifficulty == 0 && this.currentHighlight.isSecondary());
                     boolean var54 = !var55 && !((ArrayList)completedResearch.get(this.player)).contains(this.currentHighlight.key);
                     var42 = (int)Math.max((float)var51.getStringWidth(string), (float)var51.getStringWidth(this.currentHighlight.getText()) / 1.9F);
                     var44 = var51.splitStringWidth(string, var42) + 5;
                     if (var54) {
                        var53 += 9;
                        var42 = (int)Math.max((float)var42, (float)var51.getStringWidth(StatCollector.translateToLocal("tc.research.shortprim")) / 1.9F);
                     }

                     if (var55) {
                        var53 += 29;
                        var42 = (int)Math.max((float)var42, (float)var51.getStringWidth(StatCollector.translateToLocal("tc.research.short")) / 1.9F);
                     }

                     warp = ThaumcraftApi.getWarp(this.currentHighlight.key);
                     if (warp > 5) {
                        warp = 5;
                     }

                     String var57 = StatCollector.translateToLocal("tc.forbidden");
                     String wr = StatCollector.translateToLocal("tc.forbidden.level." + warp);
                     String wte = var57.replaceAll("%n", wr);
                     if (ThaumcraftApi.getWarp(this.currentHighlight.key) > 0) {
                        var53 += 9;
                        var42 = (int)Math.max((float)var42, (float)var51.getStringWidth(wte) / 1.9F);
                     }

                     this.drawGradientRect(var26 - 3, var27 - 3, var26 + var42 + 3, var27 + var44 + 6 + var53, -1073741824, -1073741824);
                     GL11.glPushMatrix();
                     GL11.glTranslatef((float)var26, (float)(var27 + var44 - 1), 0.0F);
                     GL11.glScalef(0.5F, 0.5F, 0.5F);
                     this.fontRendererObj.drawStringWithShadow(this.currentHighlight.getText(), 0, 0, -7302913);
                     GL11.glPopMatrix();
                     if (warp > 0) {
                        GL11.glPushMatrix();
                        GL11.glTranslatef((float)var26, (float)(var27 + var44 + 8), 0.0F);
                        GL11.glScalef(0.5F, 0.5F, 0.5F);
                        this.fontRendererObj.drawStringWithShadow(wte, 0, 0, 16777215);
                        GL11.glPopMatrix();
                        var44 += 9;
                     }

                     GL11.glPushMatrix();
                     if (var54) {
                        GL11.glPushMatrix();
                        GL11.glTranslatef((float)var26, (float)(var27 + var44 + 8), 0.0F);
                        GL11.glScalef(0.5F, 0.5F, 0.5F);
                        if (ResearchManager.getResearchSlot(this.mc.thePlayer, this.currentHighlight.key) >= 0) {
                           this.fontRendererObj.drawStringWithShadow(StatCollector.translateToLocal("tc.research.hasnote"), 0, 0, 16753920);
                        } else if (this.hasScribestuff) {
                           this.fontRendererObj.drawStringWithShadow(StatCollector.translateToLocal("tc.research.getprim"), 0, 0, 8900331);
                        } else {
                           this.fontRendererObj.drawStringWithShadow(StatCollector.translateToLocal("tc.research.shortprim"), 0, 0, 14423100);
                        }

                        GL11.glPopMatrix();
                     } else if (var55) {
                        boolean enough = true;
                        int cc = 0;
                        Aspect[] arr$ = this.currentHighlight.tags.getAspectsSortedAmount();
                        int len$ = arr$.length;

                        for(int i$ = 0; i$ < len$; ++i$) {
                           Aspect a = arr$[i$];
                           if (Thaumcraft.proxy.playerKnowledge.hasDiscoveredAspect(this.player, a)) {
                              float alpha = 1.0F;
                              if (Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(this.player, a) < this.currentHighlight.tags.getAmount(a)) {
                                 alpha = (float)Math.sin((double)(Minecraft.getSystemTime() % 600L) / 600.0D * 3.141592653589793D * 2.0D) * 0.25F + 0.75F;
                                 enough = false;
                              }

                              GL11.glPushMatrix();
                              GL11.glPushAttrib(1048575);
                              UtilsFX.drawTag(var26 + cc * 16, var27 + var44 + 8, a, (float)this.currentHighlight.tags.getAmount(a), 0, 0.0D, 771, alpha, false);
                              GL11.glPopAttrib();
                              GL11.glPopMatrix();
                           } else {
                              enough = false;
                              GL11.glPushMatrix();
                              UtilsFX.bindTexture("textures/aspects/_unknown.png");
                              GL11.glColor4f(0.5F, 0.5F, 0.5F, 0.5F);
                              GL11.glTranslated((double)(var26 + cc * 16), (double)(var27 + var44 + 8), 0.0D);
                              UtilsFX.drawTexturedQuadFull(0, 0, 0.0D);
                              GL11.glPopMatrix();
                           }

                           ++cc;
                        }

                        GL11.glPushMatrix();
                        GL11.glTranslatef((float)var26, (float)(var27 + var44 + 27), 0.0F);
                        GL11.glScalef(0.5F, 0.5F, 0.5F);
                        if (enough) {
                           this.fontRendererObj.drawStringWithShadow(StatCollector.translateToLocal("tc.research.purchase"), 0, 0, 8900331);
                        } else {
                           this.fontRendererObj.drawStringWithShadow(StatCollector.translateToLocal("tc.research.short"), 0, 0, 14423100);
                        }

                        GL11.glPopMatrix();
                     }

                     GL11.glPopMatrix();
                  }

                  var51.drawStringWithShadow(string, var26, var27, this.canUnlockResearch(this.currentHighlight) ? (this.currentHighlight.isSpecial() ? -128 : -1) : (this.currentHighlight.isSpecial() ? -8355776 : -8355712));
               }

               GL11.glEnable(2929);
               GL11.glEnable(2896);
               RenderHelper.disableStandardItemLighting();
               return;
            }

            research = var34.next();
            researchCategoryList = ResearchCategories.getResearchList((String)research);
         } while(((String)research).equals("ELDRITCH") && !ResearchManager.isResearchComplete(this.player, "ELDRITCHMINOR"));

         GL11.glPushMatrix();
         int oldNumber;
         if (numberOfPage - numberOfPage / 18 * 18 >= 9) {
            lefted = true;
            oldNumber = numberOfPage;
            numberOfPage -= 9;
            numberOfPage -= page * 18;
         } else {
            lefted = false;
            oldNumber = numberOfPage;
            numberOfPage -= page * 18;
         }

         int pixelsToLeft = !lefted ? 0 : 264;
         byte primary = 0;
         warp = lefted ? 14 : 0;
         if (!selectedCategory.equals((String)research)) {
            primary = 24;
            warp = lefted ? 6 : 8;
         }

         UtilsFX.bindTexture("textures/gui/gui_research.png");
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         if (lefted) {
            this.drawTexturedModalRectReversed(xR + pixelsToLeft - 8, yR + numberOfPage * 24, 176 + primary, 232, 24, 24);
         } else {
            this.drawTexturedModalRect(xR - 24 + pixelsToLeft, yR + numberOfPage * 24, 152 + primary, 232, 24, 24);
         }

         if (highlightedItem.contains((String)research)) {
            GL11.glPushMatrix();
            this.mc.renderEngine.bindTexture(ParticleEngine.particleTexture);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            ws = (int)(16L * (t % 16L));
            UtilsFX.drawTexturedQuad(xR - 27 + warp + pixelsToLeft, yR - 4 + numberOfPage * 24, ws, 80, 16, 16, -90.0D);
            GL11.glPopMatrix();
         }

         GL11.glPushMatrix();
         this.mc.renderEngine.bindTexture(researchCategoryList.icon);
         GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         UtilsFX.drawTexturedQuadFull(xR - 19 + warp + pixelsToLeft, yR + 4 + numberOfPage * 24, -80.0D);
         GL11.glPopMatrix();
         if (!selectedCategory.equals((String)research)) {
            UtilsFX.bindTexture("textures/gui/gui_research.png");
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (lefted) {
               this.drawTexturedModalRectReversed(xR + pixelsToLeft - 8, yR + numberOfPage * 24, 224, 232, 24, 24);
            } else {
               this.drawTexturedModalRect(xR - 24 + pixelsToLeft, yR + numberOfPage * 24, 200, 232, 24, 24);
            }
         }

         GL11.glPopMatrix();
         numberOfPage = oldNumber + 1;
      }
   }

   protected void mouseClicked(int par1, int par2, int par3) {
      this.popuptime = System.currentTimeMillis() - 1L;
      int count;
      if (this.currentHighlight != null && !((ArrayList)completedResearch.get(this.player)).contains(this.currentHighlight.key) && this.canUnlockResearch(this.currentHighlight)) {
         this.updateResearch();
         boolean var14 = this.currentHighlight.tags != null && this.currentHighlight.tags.size() > 0 && (Config.researchDifficulty == -1 || Config.researchDifficulty == 0 && this.currentHighlight.isSecondary());
         if (var14) {
            boolean var15 = true;
            Aspect[] var16 = this.currentHighlight.tags.getAspects();
            count = var16.length;

            for(int var18 = 0; var18 < count; ++var18) {
               Aspect var17 = var16[var18];
               if (Thaumcraft.proxy.playerKnowledge.getAspectPoolFor(this.player, var17) < this.currentHighlight.tags.getAmount(var17)) {
                  var15 = false;
                  break;
               }
            }

            if (var15) {
               PacketHandler.INSTANCE.sendToServer(new PacketPlayerCompleteToServer(this.currentHighlight.key, this.mc.thePlayer.getCommandSenderName(), this.mc.thePlayer.worldObj.provider.dimensionId, (byte)0));
            }
         } else if (this.hasScribestuff && ResearchManager.getResearchSlot(this.mc.thePlayer, this.currentHighlight.key) == -1) {
            PacketHandler.INSTANCE.sendToServer(new PacketPlayerCompleteToServer(this.currentHighlight.key, this.mc.thePlayer.getCommandSenderName(), this.mc.thePlayer.worldObj.provider.dimensionId, (byte)1));
            this.popuptime = System.currentTimeMillis() + 3000L;
            this.popupmessage = (new ChatComponentTranslation(StatCollector.translateToLocal("tc.research.popup"), new Object[]{"" + this.currentHighlight.getName()})).getUnformattedText();
         }
      } else if (this.currentHighlight != null && ((ArrayList)completedResearch.get(this.player)).contains(this.currentHighlight.key)) {
         this.mc.displayGuiScreen(new GuiResearchRecipe(this.currentHighlight, 0, this.guiMapX, this.guiMapY));
      } else {
         int var4 = (this.width - this.paneWidth) / 2;
         int var5 = (this.height - this.paneHeight) / 2;
         Set cats = ResearchCategories.researchCategories.keySet();
         count = 0;
         boolean swop = false;
         Iterator i$ = cats.iterator();
         boolean var10 = false;

         label89:
         while(true) {
            Object obj;
            do {
               if (!i$.hasNext()) {
                  break label89;
               }

               obj = i$.next();
               ResearchCategoryList rcl = ResearchCategories.getResearchList((String)obj);
            } while(((String)obj).equals("ELDRITCH") && !ResearchManager.isResearchComplete(this.player, "ELDRITCHMINOR"));

            int oldCount;
            if (count - count / 18 * 18 >= 9) {
               oldCount = count;
               count -= 9;
               count -= page * 18;
               swop = true;
            } else {
               oldCount = count;
               count -= page * 18;
               swop = false;
            }

            int mposx = par1 - (var4 - 24 + (swop ? 280 : 0));
            int mposy = par2 - (var5 + count * 24);
            if (mposx >= 0 && mposx < 24 && mposy >= 0 && mposy < 24) {
               selectedCategory = (String)obj;
               this.updateResearch();
               this.playButtonClick();
               break;
            }

            count = oldCount + 1;
         }
      }

      super.mouseClicked(par1, par2, par3);
   }

   public void drawTexturedModalRectReversed(int par1, int par2, int par3, int par4, int par5, int par6) {
      float f = 0.00390625F;
      float f1 = 0.00390625F;
      Tessellator tessellator = Tessellator.instance;
      tessellator.startDrawingQuads();
      tessellator.addVertexWithUV((double)(par1 + 0), (double)(par2 + par6), (double)this.zLevel, (double)((float)(par3 + 0) * f), (double)((float)(par4 + par6) * f1));
      tessellator.addVertexWithUV((double)(par1 + par5), (double)(par2 + par6), (double)this.zLevel, (double)((float)(par3 - par5) * f), (double)((float)(par4 + par6) * f1));
      tessellator.addVertexWithUV((double)(par1 + par5), (double)(par2 + 0), (double)this.zLevel, (double)((float)(par3 - par5) * f), (double)((float)(par4 + 0) * f1));
      tessellator.addVertexWithUV((double)(par1 + 0), (double)(par2 + 0), (double)this.zLevel, (double)((float)(par3 + 0) * f), (double)((float)(par4 + 0) * f1));
      tessellator.draw();
   }

   private void playButtonClick() {
      this.mc.renderViewEntity.worldObj.playSound(this.mc.renderViewEntity.posX, this.mc.renderViewEntity.posY, this.mc.renderViewEntity.posZ, "thaumcraft:cameraclack", 0.4F, 1.0F, false);
   }

   private boolean canUnlockResearch(ResearchItem res) {
      String[] arr$;
      int len$;
      int i$;
      String pt;
      ResearchItem parent;
      if (res.parents != null && res.parents.length > 0) {
         arr$ = res.parents;
         len$ = arr$.length;

         for(i$ = 0; i$ < len$; ++i$) {
            pt = arr$[i$];
            parent = ResearchCategories.getResearch(pt);
            if (parent != null && !((ArrayList)completedResearch.get(this.player)).contains(parent.key)) {
               return false;
            }
         }
      }

      if (res.parentsHidden != null && res.parentsHidden.length > 0) {
         arr$ = res.parentsHidden;
         len$ = arr$.length;

         for(i$ = 0; i$ < len$; ++i$) {
            pt = arr$[i$];
            parent = ResearchCategories.getResearch(pt);
            if (parent != null && !((ArrayList)completedResearch.get(this.player)).contains(parent.key)) {
               return false;
            }
         }
      }

      return true;
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   private void drawLine(int x, int y, int x2, int y2, float r, float g, float b, float te, boolean wiggle) {
      float count = (float)FMLClientHandler.instance().getClient().thePlayer.ticksExisted + te;
      Tessellator var12 = Tessellator.instance;
      GL11.glPushMatrix();
      GL11.glAlphaFunc(516, 0.003921569F);
      GL11.glDisable(3553);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      double d3 = (double)(x - x2);
      double d4 = (double)(y - y2);
      float dist = MathHelper.sqrt_double(d3 * d3 + d4 * d4);
      int inc = (int)(dist / 2.0F);
      float dx = (float)(d3 / (double)inc);
      float dy = (float)(d4 / (double)inc);
      if (Math.abs(d3) > Math.abs(d4)) {
         dx *= 2.0F;
      } else {
         dy *= 2.0F;
      }

      GL11.glLineWidth(3.0F);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      var12.startDrawing(3);

      for(int a = 0; a <= inc; ++a) {
         float r2 = r;
         float g2 = g;
         float b2 = b;
         float mx = 0.0F;
         float my = 0.0F;
         float op = 0.6F;
         if (wiggle) {
            float phase = (float)a / (float)inc;
            mx = MathHelper.sin((count + (float)a) / 7.0F) * 5.0F * (1.0F - phase);
            my = MathHelper.sin((count + (float)a) / 5.0F) * 5.0F * (1.0F - phase);
            r2 = r * (1.0F - phase);
            g2 = g * (1.0F - phase);
            b2 = b * (1.0F - phase);
            op *= phase;
         }

         var12.setColorRGBA_F(r2, g2, b2, op);
         var12.addVertex((double)((float)x - dx * (float)a + mx), (double)((float)y - dy * (float)a + my), 0.0D);
         if (Math.abs(d3) > Math.abs(d4)) {
            dx *= 1.0F - 1.0F / ((float)inc * 3.0F / 2.0F);
         } else {
            dy *= 1.0F - 1.0F / ((float)inc * 3.0F / 2.0F);
         }
      }

      var12.draw();
      GL11.glBlendFunc(770, 771);
      GL11.glDisable(2848);
      GL11.glDisable(3042);
      GL11.glDisable(32826);
      GL11.glEnable(3553);
      GL11.glAlphaFunc(516, 0.1F);
      GL11.glPopMatrix();
   }

   private void drawForbidden(double x, double y) {
      int count = FMLClientHandler.instance().getClient().thePlayer.ticksExisted;
      GL11.glPushMatrix();
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      UtilsFX.bindTexture(TileNodeRenderer.nodetex);
      byte frames = 32;
      int part = count % frames;
      GL11.glTranslated(x, y, 0.0D);
      UtilsFX.renderAnimatedQuadStrip(80.0F, 0.66F, frames, 5, frames - 1 - part, 0.0F, 4456533);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glDisable(3042);
      GL11.glPopMatrix();
   }
}
