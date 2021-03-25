package xyz.nitwhiz.meisterexporter;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import xyz.nitwhiz.meisterexporter.command.MeisterCommand;

@Mod(
  modid = MeisterExporter.MOD_ID,
  name = MeisterExporter.MOD_NAME,
  version = MeisterExporter.VERSION
)
public class MeisterExporter {

  public static final String MOD_ID = "meisterexporter";
  public static final String MOD_NAME = "MeisterExporter";
  public static final String VERSION = "1.0-SNAPSHOT";

  @Mod.EventHandler
  public void init(FMLServerStartingEvent event) {
    event.registerServerCommand(new MeisterCommand());
  }
}
