package me.hwei.bukkit.redstoneClockDetector.commands;

import me.hwei.bukkit.redstoneClockDetector.RCDPlugin;
import me.hwei.bukkit.redstoneClockDetector.util.AbstractCommand;
import me.hwei.bukkit.redstoneClockDetector.util.IOutput;
import me.hwei.bukkit.redstoneClockDetector.util.OutputManager;
import me.hwei.bukkit.redstoneClockDetector.util.UsageException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class BreakCommand
        extends AbstractCommand {
    protected RCDPlugin plugin;

    public BreakCommand(String usage, String perm, AbstractCommand[] children, RCDPlugin plugin)
            throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
    }

    protected boolean execute(CommandSender sender, AbstractCommand.MatchResult[] data)
            throws UsageException {
        int tpNum = 0;
        if (data.length != 1) {
            throw new UsageException(this.coloredUsage, "Must specify location num.");
        }
        Integer numData = data[0].getInteger();
        if ((numData == null) || (numData.intValue() <= 0)) {
            throw new UsageException(this.coloredUsage, "Location num must be a positive integer.");
        }
        OutputManager outputManager = OutputManager.GetInstance();
        IOutput toSender = outputManager.toSender(sender);

        List<Map.Entry<Location, Integer>> actList = this.plugin.getRedstoneActivityList();
        if (tpNum >= actList.size()) {
            toSender.output(String.format("Location num " + ChatColor.YELLOW + "%d " + ChatColor.WHITE + "dose not exist.", new Object[]{Integer.valueOf(tpNum + 1)}));
        } else {
            Location loc = (Location) ((Map.Entry) actList.get(tpNum)).getKey();
            Block block = loc.getBlock();
            String blockName = block.getType().toString();
            if (!block.breakNaturally()) {
                toSender.output(String.format("Can not break %s block at " + ChatColor.GREEN + "(%d, %d, %d) %s " + ChatColor.WHITE + ".", new Object[]{blockName, Integer.valueOf(loc.getBlockX()), Integer.valueOf(loc.getBlockY()), Integer.valueOf(loc.getBlockZ()), loc.getWorld().getName()}));


                return true;
            }
            block.setType(Material.SIGN_POST);
            Sign s = (Sign) block.getState();
            s.setLine(0, sender.getName());
            s.setLine(1, ChatColor.DARK_RED + "broke a");
            s.setLine(2, blockName);
            s.setLine(3, ChatColor.DARK_RED + "here.");
            s.update();
            toSender.output(String.format("Has Broken %s block at " + ChatColor.GREEN + "(%d, %d, %d) %s " + ChatColor.WHITE + ".", new Object[]{blockName, Integer.valueOf(loc.getBlockX()), Integer.valueOf(loc.getBlockY()), Integer.valueOf(loc.getBlockZ()), loc.getWorld().getName()}));
        }
        return true;
    }
}
