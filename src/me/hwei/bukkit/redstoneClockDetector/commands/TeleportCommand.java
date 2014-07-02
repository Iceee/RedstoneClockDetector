package me.hwei.bukkit.redstoneClockDetector.commands;

import me.hwei.bukkit.redstoneClockDetector.RCDPlugin;
import me.hwei.bukkit.redstoneClockDetector.util.AbstractCommand;
import me.hwei.bukkit.redstoneClockDetector.util.IOutput;
import me.hwei.bukkit.redstoneClockDetector.util.OutputManager;
import me.hwei.bukkit.redstoneClockDetector.util.UsageException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class TeleportCommand
        extends AbstractCommand {
    protected RCDPlugin plugin;

    public TeleportCommand(String usage, String perm, AbstractCommand[] children, RCDPlugin plugin)
            throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
    }

    protected boolean execute(CommandSender sender, AbstractCommand.MatchResult[] data)
            throws UsageException {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        int tpNum = 0;
        OutputManager outputManager = OutputManager.GetInstance();
        IOutput toSender = outputManager.toSender(sender);
        if (data.length == 0) {
            if (player == null) {
                throw new UsageException(this.coloredUsage, "Must specify which player to teleport.");
            }
        } else if (data.length == 1) {
            if (player == null) {
                String playerName = data[0].getString();
                player = this.plugin.getServer().getPlayer(playerName);
                if (player == null) {
                    toSender.output(String.format("Can not find player " + ChatColor.GREEN + "%d" + ChatColor.WHITE + ".", new Object[]{playerName}));


                    return true;
                }
            } else {
                Integer numData = data[0].getInteger();
                if ((numData == null) || (numData.intValue() <= 0)) {
                    throw new UsageException(this.coloredUsage, "Location num must be a positive integer.");
                }
                tpNum = numData.intValue() - 1;
            }
        } else if (data.length == 2) {
            String playerName = data[0].getString();
            player = this.plugin.getServer().getPlayer(playerName);
            if (player == null) {
                toSender.output(String.format("Can not find player " + ChatColor.GREEN + "%d" + ChatColor.WHITE + ".", new Object[]{playerName}));


                return true;
            }
            Integer numData = data[1].getInteger();
            if ((numData == null) || (numData.intValue() <= 0)) {
                throw new UsageException(this.coloredUsage, "Location num must be a positive integer.");
            }
            tpNum = numData.intValue() - 1;
        }
        List<Map.Entry<Location, Integer>> actList = this.plugin.getRedstoneActivityList();
        if (tpNum >= actList.size()) {
            toSender.output(String.format("Location num " + ChatColor.YELLOW + "%d " + ChatColor.WHITE + "dose not exist.", new Object[]{Integer.valueOf(tpNum + 1)}));
        } else {
            player.teleport((Location) ((Map.Entry) actList.get(tpNum)).getKey());
            IOutput toPlayer = outputManager.prefix(outputManager.toSender(player));
            if (player == sender) {
                toPlayer.output("Teleporting...");
            } else {
                toPlayer.output(String.format(ChatColor.GREEN.toString() + "%s " + ChatColor.WHITE + "is teleporting you...", new Object[]{sender.getName()}));
            }
        }
        return true;
    }
}
