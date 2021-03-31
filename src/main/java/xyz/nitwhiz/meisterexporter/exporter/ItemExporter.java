package xyz.nitwhiz.meisterexporter.exporter;

import com.google.gson.JsonArray;
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
            String id = normalize(res.toString() + "_" + itemStack.getMetadata());

            items.put(id, itemStack);
          }
        }
      }
    }

    return items;
  }

  protected JsonArray getItemsAsJsonArray() {
    HashMap<String, ItemStack> itemMap = this.getAllItems();
    JsonArray allItems = new JsonArray();

    itemMap.forEach((String id, ItemStack itemStack) -> {
      JsonObject item = new JsonObject();

      item.addProperty("id", id);
      item.addProperty("displayName", itemStack.getDisplayName());

      allItems.add(item);
    });

    return allItems;
  }

  @Override
  protected void run() throws Exception {
    String fileName = "./export/items.json";
    JsonArray allItems = getItemsAsJsonArray();

    LogManager.getLogger("meister").info("exporting " + allItems.size() + " items to " + fileName);

    FileOutputStream outputStream = new FileOutputStream(fileName);

    outputStream.write(allItems.toString().getBytes(StandardCharsets.UTF_8));
    outputStream.close();
  }

}
