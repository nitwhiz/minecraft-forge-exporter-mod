package xyz.nitwhiz.meisterexporter.exporter;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModRegistry;

import java.util.Locale;

public abstract class Exporter {

  protected IJeiRuntime jeiRuntime;

  protected IModRegistry jeiModRegistry;

  public Exporter(IJeiRuntime jeiRuntime, IModRegistry jeiModRegistry) {
    if (jeiRuntime == null) {
      throw new RuntimeException("jei runtime is not ready, try again later");
    }

    if (jeiModRegistry == null) {
      throw new RuntimeException("jei mod registry is not ready, try again later");
    }

    this.jeiRuntime = jeiRuntime;
    this.jeiModRegistry = jeiModRegistry;
  }

  protected static String normalize(String str) {
    return str.toLowerCase(Locale.ROOT).replaceAll("[^a-zA-Z0-9]", "_");
  }

  protected abstract void run() throws Exception;

  public void exec() {
    try {
      this.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
