package xyz.nitwhiz.meisterexporter.exporter;

import com.google.gson.JsonObject;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;

public class ItemExporter extends Exporter {

  public ItemExporter(IJeiRuntime jeiRuntime, IModRegistry jeiModRegistry) {
    super(jeiRuntime, jeiModRegistry);
  }

  // retruns all items as map: normalized item name -> itemstack
  protected HashMap<String, ItemStack> getAllItems() {
    HashMap<String, ItemStack> items = new HashMap<>();

    for (IIngredientType registeredIngredientType : jeiModRegistry.getIngredientRegistry().getRegisteredIngredientTypes()) {
      Collection<?> ingredientTypes = jeiModRegistry.getIngredientRegistry().getAllIngredients(registeredIngredientType);

      for (Object ingredientType : ingredientTypes) {
        if (ingredientType instanceof ItemStack) {
          ItemStack itemStack = (ItemStack) ingredientType;

          ResourceLocation res = Item.REGISTRY.getNameForObject(itemStack.getItem());

          if (res != null) {
            String name = normalize(res.toString() + ":" + itemStack.getItemDamage());

            items.put(name, itemStack);
          }
        }
      }
    }

    return items;
  }

  protected JsonObject getItemsAsJsonObject() {
    HashMap<String, ItemStack> itemMap = this.getAllItems();
    JsonObject allItems = new JsonObject();

    itemMap.forEach((String name, ItemStack itemStack) -> {
      JsonObject item = new JsonObject();
      allItems.add(name, item);
    });

    return allItems;
  }

  @Override
  protected void run() throws Exception {
    String fileName = "./export/items.json";
    JsonObject allItems = getItemsAsJsonObject();

    LogManager.getLogger("meister").info("exporting " + allItems.size() + " items to " + fileName);

    FileOutputStream outputStream = new FileOutputStream(fileName);

    outputStream.write(allItems.toString().getBytes(StandardCharsets.UTF_8));
    outputStream.close();
  }

}
