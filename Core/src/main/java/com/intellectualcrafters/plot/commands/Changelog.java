package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.Updater;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

@CommandDeclaration(
        command = "changelog",
        permission = "plots.admin.command.changelog",
        description = "View the changelog",
        usage = "/plot changelog",
        requiredType = RequiredType.NONE,
        aliases = {"cl"},
        category = CommandCategory.ADMINISTRATION)
public class Changelog extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        try {
            Updater updater = PS.get().getUpdater();
            String changes = updater != null ? updater.getChanges() : null;
            if (changes == null) {
                try (Scanner scanner = new Scanner(new URL("http://empcraft.com/plots/cl?" + Integer.toHexString(PS.get().getVersion().hash)).openStream(), "UTF-8")) {
                    changes = scanner.useDelimiter("\\A").next();
                }
            }
            changes = changes.replaceAll("#([0-9]+)", "github.com/IntellectualSites/PlotSquared/pulls/$1");
            MainUtil.sendMessage(player, changes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

}
