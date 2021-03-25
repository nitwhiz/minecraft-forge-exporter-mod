/**
 * This code is originally from https://github.com/TheCBProject/NotEnoughItems
 * and was slightly modified. The following license takes effect.
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 ChickenBones
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package xyz.nitwhiz.meisterexporter.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class ExportItemImagesGui extends GuiScreen {
  private ArrayList<ItemStack> items;

  private int drawIndex;
  private int parseIndex;
  private File dir;
  private int iconSize;
  private int borderSize;
  private int boxSize;
  private IntBuffer pixelBuffer;
  private int[] pixelValues;

  public ExportItemImagesGui(ArrayList<ItemStack> items, File targetDir) {
    this.items = items;

    this.iconSize = 128;
    borderSize = iconSize / 16;
    boxSize = iconSize + borderSize * 2;

    dir = targetDir;

    if (dir.exists()) {
      for (File f : dir.listFiles()) {
        if (f.isFile()) {
          f.delete();
        }
      }
    } else {
      dir.mkdirs();
    }

  }

  private void returnScreen() {
    Minecraft.getMinecraft().displayGuiScreen(null);
  }

  @Override
  public void drawScreen(int mousex, int mousey, float frame) {
    try {
      drawItems();
      exportItems();
    } catch (Exception e) {
    }
  }

  private void drawItems() {
    Minecraft mc = Minecraft.getMinecraft();
    Dimension d = new Dimension(mc.displayWidth, mc.displayHeight);

    GlStateManager.matrixMode(GL11.GL_PROJECTION);
    GlStateManager.loadIdentity();
    GlStateManager.ortho(0, d.width * 16D / iconSize, d.height * 16D / iconSize, 0, 1000, 3000);
    GlStateManager.matrixMode(GL11.GL_MODELVIEW);
    GlStateManager.clearColor(0, 0, 0, 0);
    GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

    int rows = d.height / boxSize;
    int cols = d.width / boxSize;
    int fit = rows * cols;

    RenderHelper.enableGUIStandardItemLighting();
    GlStateManager.enableRescaleNormal();
    GlStateManager.color(1, 1, 1, 1);

    for (int i = 0; drawIndex < items.size() && i < fit; drawIndex++, i++) {
      int x = i % cols * 18;
      int y = i / cols * 18;
      drawItem(x + 1, y + 1, items.get(drawIndex));
    }

    GL11.glFlush();
  }

  private void drawItem(int i, int j, ItemStack itemstack) {
    GlStateManager.enableLighting();
    GlStateManager.enableDepth();

    RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

    renderItem.renderItemAndEffectIntoGUI(itemstack, i, j);

    GlStateManager.disableLighting();
    GlStateManager.disableDepth();
  }

  private void exportItems() throws IOException {
    BufferedImage img = screenshot();
    int rows = img.getHeight() / boxSize;
    int cols = img.getWidth() / boxSize;
    int fit = rows * cols;
    for (int i = 0; parseIndex < items.size() && i < fit; parseIndex++, i++) {
      int x = i % cols * boxSize;
      int y = i / cols * boxSize;
      exportImage(dir, img.getSubimage(x + borderSize, y + borderSize, iconSize, iconSize), items.get(parseIndex));
    }

    if (parseIndex >= items.size()) {
      returnScreen();
    }
  }

  private void exportImage(File dir, BufferedImage img, ItemStack stack) throws IOException {
    ResourceLocation res = Item.REGISTRY.getNameForObject(stack.getItem());
    String name = "_";

    if (res != null) {
      name = (res.toString() + ":" + stack.getItemDamage()).replaceAll("[^a-zA-Z0-9]", "_");
    }

    File file = new File(dir, name + ".png");

    if (!file.exists()) {
      ImageIO.write(img, "png", file);
    }
  }

  private BufferedImage screenshot() {
    Framebuffer fb = Minecraft.getMinecraft().getFramebuffer();
    Minecraft mc = Minecraft.getMinecraft();
    Dimension mcSize = new Dimension(mc.displayWidth, mc.displayHeight);
    Dimension texSize = mcSize;

    if (OpenGlHelper.isFramebufferEnabled()) {
      texSize = new Dimension(fb.framebufferTextureWidth, fb.framebufferTextureHeight);
    }

    int k = texSize.width * texSize.height;
    if (pixelBuffer == null || pixelBuffer.capacity() < k) {
      pixelBuffer = BufferUtils.createIntBuffer(k);
      pixelValues = new int[k];
    }

    GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
    GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
    pixelBuffer.clear();

    if (OpenGlHelper.isFramebufferEnabled()) {
      GlStateManager.bindTexture(fb.framebufferTexture);
      GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
    } else {
      GL11.glReadPixels(0, 0, texSize.width, texSize.height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
    }

    pixelBuffer.get(pixelValues);
    TextureUtil.processPixelValues(pixelValues, texSize.width, texSize.height);

    BufferedImage img = new BufferedImage(mcSize.width, mcSize.height, BufferedImage.TYPE_INT_ARGB);
    if (OpenGlHelper.isFramebufferEnabled()) {
      int yOff = texSize.height - mcSize.height;
      for (int y = 0; y < mcSize.height; ++y) {
        for (int x = 0; x < mcSize.width; ++x) {
          img.setRGB(x, y, pixelValues[(y + yOff) * texSize.width + x]);
        }
      }
    } else {
      img.setRGB(0, 0, texSize.width, height, pixelValues, 0, texSize.width);
    }

    return img;
  }
}
