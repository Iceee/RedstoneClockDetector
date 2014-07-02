package me.hwei.bukkit.redstoneClockDetector.commands;

import me.hwei.bukkit.redstoneClockDetector.RCDPlugin;
import me.hwei.bukkit.redstoneClockDetector.util.AbstractCommand;
import me.hwei.bukkit.redstoneClockDetector.util.IOutput;
import me.hwei.bukkit.redstoneClockDetector.util.OutputManager;
import me.hwei.bukkit.redstoneClockDetector.util.UsageException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class StartCommand
        extends AbstractCommand {
    protected RCDPlugin plugin;
    protected AbstractCommand listCommand;

    public StartCommand(String usage, String perm, AbstractCommand[] children, RCDPlugin plugin, AbstractCommand listCommand)
            throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
        this.listCommand = listCommand;
    }

    protected boolean execute(CommandSender sender, AbstractCommand.MatchResult[] data)
            throws UsageException {
        Integer seconds = data[0].getInteger();
        if (seconds == null) {
            return false;
        }
        if (seconds.intValue() <= 0) {
            throw new UsageException(this.coloredUsage, "seconds number should be a positive integer.");
        }
        CommandSender user = this.plugin.getUser();
        OutputManager outputManager = OutputManager.GetInstance();
        IOutput toSender = outputManager.toSender(sender);
        if (user != null) {
            toSender.output(String.format(ChatColor.GREEN.toString() + "%s " + ChatColor.WHITE + "has already started a scan.", new Object[]{user.getName()}));


            return true;
        }
        IOutput toSenderPrefix = outputManager.prefix(outputManager.toSender(sender));
        this.plugin.start(sender, seconds.intValue(), new ProgressReporter(toSenderPrefix, new FinishCallback(this.listCommand, sender)));
        toSender.output(String.format("Start a scan of %d seconds.", new Object[]{seconds}));
        return true;
    }

    protected class ProgressReporter
            implements RCDPlugin.IProgressReporter {
        protected IOutput toSender;
        protected StartCommand.FinishCallback finishCallback;

        public ProgressReporter(IOutput toSender, StartCommand.FinishCallback finishCallback) {
            this.toSender = toSender;
            this.finishCallback = finishCallback;
        }

        public void onProgress(int secondsRemain) {
            if (secondsRemain <= 0) {
                this.finishCallback.onFinish();
            } else if (secondsRemain <= 5) {
                this.toSender.output(String.format("Remain %d seconds.", new Object[]{Integer.valueOf(secondsRemain)}));
            } else if ((secondsRemain <= 60) && (secondsRemain % 10 == 0)) {
                this.toSender.output(String.format("Remain %d seconds.", new Object[]{Integer.valueOf(secondsRemain)}));
            } else if (secondsRemain % 60 == 0) {
                this.toSender.output(String.format("Remain %d minutes.", new Object[]{Integer.valueOf(secondsRemain / 60)}));
            }
        }
    }

    protected class FinishCallback {
        protected AbstractCommand listCommand;
        protected CommandSender sender;

        public FinishCallback(AbstractCommand listCommand, CommandSender sender) {
            this.listCommand = listCommand;
            this.sender = sender;
        }

        public void onFinish() {
            try {
                this.listCommand.execute(this.sender, new String[]{"list"});
            } catch (Exception e) {
            }
        }
    }
}
