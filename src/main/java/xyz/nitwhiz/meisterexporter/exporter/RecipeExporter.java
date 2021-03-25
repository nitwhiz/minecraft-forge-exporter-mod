package xyz.nitwhiz.meisterexporter.exporter;

import com.google.gson.JsonArray;
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
import org.apache.logging.log4j.LogManager;
import xyz.nitwhiz.meisterexporter.jei.Ingredients;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeExporter extends Exporter {

  public RecipeExporter(IJeiRuntime jeiRuntime, IModRegistry jeiModRegistry) {
    super(jeiRuntime, jeiModRegistry);
  }

  protected static String getRecipeWrapperKey(IRecipeWrapper recipeWrapper) {
    return normalize(recipeWrapper.getClass().getCanonicalName().replaceAll("^mezz\\.jei\\.plugins\\.", ""));
  }

  protected static JsonArray handleIngredients(Map<IIngredientType, List<List>> dataMap) {
    JsonArray ingredients = new JsonArray();

    for (Map.Entry<?, List<List>> entry : dataMap.entrySet()) {
      List<List> list = entry.getValue();

      for (List i : list) {
        if (i.size() == 0) {
          ingredients.add(JsonNull.INSTANCE);
          continue;
        }

        JsonArray fittingIngredients = new JsonArray();

        for (Object j : i) {
          if (j instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) j;
            ResourceLocation res = Item.REGISTRY.getNameForObject(itemStack.getItem());

            if (res != null) {
              String name = normalize(res.toString() + ":" + itemStack.getItemDamage());

              JsonObject ing = new JsonObject();

              ing.addProperty("name", name);
              ing.addProperty("count", itemStack.getCount());

              fittingIngredients.add(ing);
            }
          }
        }

        ingredients.add(fittingIngredients);
      }
    }

    return ingredients;
  }

  protected JsonObject handleRecipe(IRecipeWrapper recipeWrapper) {
    JsonObject recipe = new JsonObject();

    recipe.add("width", JsonNull.INSTANCE);
    recipe.add("height", JsonNull.INSTANCE);

    if (recipeWrapper instanceof IShapedCraftingRecipeWrapper) {
      int w = ((IShapedCraftingRecipeWrapper) recipeWrapper).getWidth();
      int h = ((IShapedCraftingRecipeWrapper) recipeWrapper).getHeight();

      recipe.addProperty("width", w);
      recipe.addProperty("height", h);
    }

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

  // returns recipes as map: wrapper category -> recipes
  protected HashMap<String, JsonArray> collectAllRecipes(IJeiRuntime jeiRuntime) {
    HashMap<String, JsonArray> recipeMap = new HashMap<>();

    for (IRecipeCategory recipeCategory : jeiRuntime.getRecipeRegistry().getRecipeCategories()) {
      for (Object recipeWrapper : jeiRuntime.getRecipeRegistry().getRecipeWrappers(recipeCategory)) {
        String recipeWrapperKey = getRecipeWrapperKey((IRecipeWrapper) recipeWrapper);
        JsonArray wrapperRecipes = recipeMap.get(recipeWrapperKey);

        if (wrapperRecipes == null) {
          wrapperRecipes = new JsonArray();
        }

        JsonObject processedRecipe = handleRecipe((IRecipeWrapper) recipeWrapper);

        if (processedRecipe != null) {
          wrapperRecipes.add(processedRecipe);
          recipeMap.putIfAbsent(recipeWrapperKey, wrapperRecipes);
        }
      }
    }

    return recipeMap;
  }

  @Override
  protected void run() throws Exception {
    JsonArray recipeCategories = new JsonArray();

    collectAllRecipes(jeiRuntime).forEach((String key, JsonArray recipes) -> {
      String fileName = "./export/recipes_" + key + ".json";

      recipeCategories.add(key);

      LogManager.getLogger("meister").info("exporting " + recipes.size() + " recipes for " + key + " to " + fileName);

      try {
        FileOutputStream outputStream = new FileOutputStream(fileName);

        outputStream.write(recipes.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    FileOutputStream outputStream = new FileOutputStream("./export/recipe_categories.json");

    outputStream.write(recipeCategories.toString().getBytes(StandardCharsets.UTF_8));
    outputStream.close();
  }

}
