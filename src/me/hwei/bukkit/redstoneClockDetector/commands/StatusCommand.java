package me.hwei.bukkit.redstoneClockDetector.commands;

import me.hwei.bukkit.redstoneClockDetector.RCDPlugin;
import me.hwei.bukkit.redstoneClockDetector.util.AbstractCommand;
import me.hwei.bukkit.redstoneClockDetector.util.IOutput;
import me.hwei.bukkit.redstoneClockDetector.util.OutputManager;
import me.hwei.bukkit.redstoneClockDetector.util.UsageException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class StatusCommand
        extends AbstractCommand {
    protected RCDPlugin plugin;
    protected String pluginInfo;

    public StatusCommand(String usage, String perm, AbstractCommand[] children, RCDPlugin plugin)
            throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
        PluginDescriptionFile des = plugin.getDescription();
        this.pluginInfo = String.format("Version: " + ChatColor.YELLOW + "%s" + ChatColor.WHITE + ", Author: " + ChatColor.YELLOW + "%s", new Object[]{des.getVersion(), des.getAuthors().get(0)});
    }

    protected boolean execute(CommandSender sender, AbstractCommand.MatchResult[] data)
            throws UsageException {
        IOutput toSender = OutputManager.GetInstance().toSender(sender);
        OutputManager.GetInstance().prefix(toSender).output(this.pluginInfo);
        CommandSender user = this.plugin.getUser();
        if (user != null) {
            toSender.output(String.format(ChatColor.GREEN.toString() + "%s " + ChatColor.WHITE + "has started a scan, remaining " + ChatColor.YELLOW + "%d " + ChatColor.WHITE + "seconds to finish.", new Object[]{user.getName(), Integer.valueOf(this.plugin.getSecondsRemain())}));
        }
        return true;
    }
}
