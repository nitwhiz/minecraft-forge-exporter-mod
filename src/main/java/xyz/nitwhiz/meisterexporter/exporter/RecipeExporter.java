package xyz.nitwhiz.meisterexporter.exporter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import xyz.nitwhiz.meisterexporter.jei.Ingredients;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeExporter extends Exporter {

  public RecipeExporter(IJeiRuntime jeiRuntime, IModRegistry jeiModRegistry) {
    super(jeiRuntime, jeiModRegistry);
  }

  protected static String getRecipeWrapperKey(IRecipeWrapper recipeWrapper) {
    try {
      return normalize(recipeWrapper.getClass().getCanonicalName().replaceAll("^mezz\\.jei\\.plugins\\.", ""));
    } catch (Exception e) {
      return "nullwrapper";
    }
  }

  protected static JsonArray handleIngredients(Map<IIngredientType, List<List>> dataMap) {
    JsonArray ingredients = new JsonArray();

    for (Map.Entry<?, List<List>> entry : dataMap.entrySet()) {
      List<List> list = entry.getValue();

      int position = 0;

      for (List slot : list) {
        if (slot.size() != 0) {
          for (Object possibleItem : slot) {
            if (possibleItem instanceof ItemStack) {
              ItemStack itemStack = (ItemStack) possibleItem;
              ResourceLocation res = Item.REGISTRY.getNameForObject(itemStack.getItem());

              if (res != null) {
                String resName = normalize(res.toString());
                int meta = itemStack.getMetadata();
                String id = resName + "_" + meta;

                JsonObject ing = new JsonObject();

                ing.addProperty("id", id);
                ing.addProperty("count", itemStack.getCount());
                ing.addProperty("name", resName);
                ing.addProperty("meta", meta);
                ing.addProperty("position", position);

                ingredients.add(ing);
              }
            }
          }
        }

        ++position;
      }
    }

    return ingredients;
  }

  protected JsonObject handleRecipe(IRecipeCategory recipeCategory, IRecipeWrapper recipeWrapper) {
    JsonObject recipe = new JsonObject();

    String categoryTitle = recipeCategory.getTitle();
    String categoryModName = recipeCategory.getModName();

    JsonObject category = new JsonObject();

    category.addProperty("modName", categoryModName);
    category.addProperty("title", categoryTitle);
    category.addProperty("wrapper", getRecipeWrapperKey(recipeWrapper));

    recipe.add("category", category);

    JsonObject dimensions = new JsonObject();

    dimensions.add("width", JsonNull.INSTANCE);
    dimensions.add("height", JsonNull.INSTANCE);

    if (recipeWrapper instanceof IShapedCraftingRecipeWrapper) {
      int w = ((IShapedCraftingRecipeWrapper) recipeWrapper).getWidth();
      int h = ((IShapedCraftingRecipeWrapper) recipeWrapper).getHeight();

      dimensions.addProperty("width", w);
      dimensions.addProperty("height", h);
    }

    recipe.add("dimensions", dimensions);

    Ingredients i = new Ingredients(jeiModRegistry.getIngredientRegistry());

    recipeWrapper.getIngredients(i);

    Map<IIngredientType, List<List>> rawInputs = i.getRawInputs();

    JsonArray jsonInputs = handleIngredients(rawInputs);

    if (jsonInputs.size() == 0) {
      return null;
    }

    recipe.add("input", jsonInputs);

    Map<IIngredientType, List<List>> rawOutputs = i.getRawOutputs();

    JsonArray jsonOutputs = handleIngredients(rawOutputs);

    if (jsonOutputs.size() == 0) {
      return null;
    }

    recipe.add("output", jsonOutputs);

    return recipe;
  }

  protected HashMap<String, JsonArray> collectAllRecipes(IJeiRuntime jeiRuntime) {
    HashMap<String, JsonArray> recipeMap = new HashMap<>();

    for (IRecipeCategory recipeCategory : jeiRuntime.getRecipeRegistry().getRecipeCategories()) {
      for (Object recipeWrapper : jeiRuntime.getRecipeRegistry().getRecipeWrappers(recipeCategory)) {
        String key = DigestUtils.sha256Hex(
          recipeCategory.getModName() + recipeCategory.getTitle() + getRecipeWrapperKey((IRecipeWrapper) recipeWrapper)
        );
        JsonArray wrapperRecipes = recipeMap.get(key);

        if (wrapperRecipes == null) {
          wrapperRecipes = new JsonArray();
        }

        JsonObject processedRecipe = handleRecipe(recipeCategory, (IRecipeWrapper) recipeWrapper);

        if (processedRecipe != null) {
          wrapperRecipes.add(processedRecipe);
          recipeMap.putIfAbsent(key, wrapperRecipes);
        }
      }
    }

    return recipeMap;
  }

  @Override
  protected void run() throws IOException {
    JsonArray recipeFiles = new JsonArray();

    collectAllRecipes(jeiRuntime).forEach((String key, JsonArray recipes) -> {
      String fileName = "./export/recipes_" + key + ".json";

      recipeFiles.add(key);

      LogManager.getLogger("meister").info("exporting " + recipes.size() + " recipes for " + key + " to " + fileName);

      try {
        FileOutputStream outputStream = new FileOutputStream(fileName);

        outputStream.write(recipes.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    FileOutputStream outputStream = new FileOutputStream("./export/recipe_files.json");

    outputStream.write(recipeFiles.toString().getBytes(StandardCharsets.UTF_8));
    outputStream.close();
  }
}
