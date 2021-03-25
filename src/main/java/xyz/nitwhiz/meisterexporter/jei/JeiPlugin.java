package xyz.nitwhiz.meisterexporter.jei;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;

@JEIPlugin
public class JeiPlugin implements IModPlugin {

  public static IModRegistry modRegistry = null;

  public static IJeiRuntime runtime = null;

  @Override
  public void register(IModRegistry registry) {
    modRegistry = registry;
  }

  @Override
  public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
    JeiPlugin.runtime = jeiRuntime;

    LogManager.getLogger("meister").info("hello jei! :)");
  }
}
