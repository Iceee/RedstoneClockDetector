package me.hwei.bukkit.redstoneClockDetector.commands;

import me.hwei.bukkit.redstoneClockDetector.RCDPlugin;
import me.hwei.bukkit.redstoneClockDetector.util.AbstractCommand;
import me.hwei.bukkit.redstoneClockDetector.util.IOutput;
import me.hwei.bukkit.redstoneClockDetector.util.OutputManager;
import me.hwei.bukkit.redstoneClockDetector.util.UsageException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class ListCommand
        extends AbstractCommand {
    protected RCDPlugin plugin;

    public ListCommand(String usage, String perm, AbstractCommand[] children, RCDPlugin plugin)
            throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
    }

    protected final int pageSize = 10;

    protected boolean execute(CommandSender sender, AbstractCommand.MatchResult[] data)
            throws UsageException {
        int pageNum = 1;
        if (data.length > 0) {
            Integer pageData = data[0].getInteger();
            if ((pageData == null) || (pageData.intValue() <= 0)) {
                throw new UsageException(this.coloredUsage, "page number should be a positive integer.");
            }
            pageNum = pageData.intValue();
        }
        IOutput toSender = OutputManager.GetInstance().toSender(sender);
        getClass();
        int startIndex = (pageNum - 1) * 10;
        List<Map.Entry<Location, Integer>> actList = this.plugin.getRedstoneActivityList();
        getClass();
        int totalPage = actList.size() == 0 ? 0 : (actList.size() - 1) / 10 + 1;
        toSender.output(String.format("Page: " + ChatColor.YELLOW + "%d" + ChatColor.WHITE + "/" + ChatColor.GOLD + "%d", new Object[]{Integer.valueOf(pageNum), Integer.valueOf(totalPage)}));
        if (startIndex >= actList.size()) {
            toSender.output(ChatColor.GRAY.toString() + "No data.");
        } else {
            int i = startIndex;
            getClass();
            for (int e = Math.min(startIndex + 10, actList.size()); i < e; i++) {
                Map.Entry<Location, Integer> entry = (Map.Entry) actList.get(i);
                Location l = (Location) entry.getKey();
                toSender.output(String.format(ChatColor.YELLOW.toString() + "%d" + ChatColor.WHITE + ". " + ChatColor.GREEN + "(%d, %d, %d) %s " + ChatColor.DARK_GREEN + "%d", new Object[]{Integer.valueOf(i + 1), Integer.valueOf(l.getBlockX()), Integer.valueOf(l.getBlockY()), Integer.valueOf(l.getBlockZ()), l.getWorld().getName(), entry.getValue()}));
            }
        }
        return true;
    }
}
