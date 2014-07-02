package me.hwei.bukkit.redstoneClockDetector.commands;

import me.hwei.bukkit.redstoneClockDetector.RCDPlugin;
import me.hwei.bukkit.redstoneClockDetector.util.AbstractCommand;
import me.hwei.bukkit.redstoneClockDetector.util.IOutput;
import me.hwei.bukkit.redstoneClockDetector.util.OutputManager;
import me.hwei.bukkit.redstoneClockDetector.util.UsageException;
import org.bukkit.command.CommandSender;

public class StopCommand
        extends AbstractCommand {
    protected RCDPlugin plugin;

    public StopCommand(String usage, String perm, AbstractCommand[] children, RCDPlugin plugin)
            throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
    }

    protected boolean execute(CommandSender sender, AbstractCommand.MatchResult[] data)
            throws UsageException {
        IOutput toSender = OutputManager.GetInstance().toSender(sender);
        if (this.plugin.stop()) {
            toSender.output("Successfully stopped.");
        } else {
            toSender.output("Already stopped.");
        }
        return true;
    }
}
