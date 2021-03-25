package xyz.nitwhiz.meisterexporter.exporter;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import xyz.nitwhiz.meisterexporter.gui.ExportItemImagesGui;

import java.io.File;
import java.util.ArrayList;

public class ItemIconExporter extends Exporter {

  protected ItemExporter itemExporter;

  public ItemIconExporter(IJeiRuntime jeiRuntime, IModRegistry jeiModRegistry) {
    super(jeiRuntime, jeiModRegistry);

    itemExporter = new ItemExporter(jeiRuntime, jeiModRegistry);
  }

  @Override
  protected void run() {
    ArrayList<ItemStack> items = new ArrayList<>(itemExporter.getAllItems().values());

    Minecraft.getMinecraft().displayGuiScreen(new ExportItemImagesGui(items, new File("./export/item_icons/")));
  }
}
