package xyz.nitwhiz.meisterexporter.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import xyz.nitwhiz.meisterexporter.exporter.ItemExporter;
import xyz.nitwhiz.meisterexporter.exporter.ItemIconExporter;
import xyz.nitwhiz.meisterexporter.exporter.RecipeExporter;
import xyz.nitwhiz.meisterexporter.jei.JeiPlugin;

import java.io.File;

public class MeisterCommand extends CommandBase {

  protected static String TYPE_ICONS = "icons";
  protected static String TYPE_RECIPES = "recipes";
  protected static String TYPE_ITEMS = "items";

  protected static String[] TYPES = new String[]{
    TYPE_ICONS, TYPE_RECIPES, TYPE_ITEMS
  };

  @Override
  public String getName() {
    return "meister";
  }

  @Override
  public String getUsage(ICommandSender sender) {
    StringBuilder availableTypes = new StringBuilder(" available export types: \n");

    for (String s : TYPES) {
      availableTypes.append("  ").append(s).append("\n");
    }

    return "/" + this.getName() + " <type>" + "\n" + availableTypes;
  }

  protected void tellSender(ICommandSender sender, String msg) {
    sender.sendMessage(new TextComponentString(msg));
  }

  @Override
  public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
    if (args.length < 1) {
      tellSender(sender, this.getUsage(sender));
    } else {
      File exportDir = new File("./export/");

      exportDir.mkdirs();

      String cmd = args[0].toLowerCase();
      boolean handled = false;

      if (cmd.equals(TYPE_ICONS)) {
        tellSender(sender, "exporting icons");
        new ItemIconExporter(JeiPlugin.runtime, JeiPlugin.modRegistry).exec();
        tellSender(sender, "done");

        handled = true;
      }

      if (cmd.equals(TYPE_RECIPES)) {
        tellSender(sender, "exporting recipes");
        new RecipeExporter(JeiPlugin.runtime, JeiPlugin.modRegistry).exec();
        tellSender(sender, "done");

        handled = true;
      }

      if (cmd.equals(TYPE_ITEMS)) {
        tellSender(sender, "exporting items");
        new ItemExporter(JeiPlugin.runtime, JeiPlugin.modRegistry).exec();
        tellSender(sender, "done");

        handled = true;
      }

      if (!handled) {
        tellSender(sender, getUsage(sender));
      }
    }
  }
}
