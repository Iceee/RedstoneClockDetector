package me.hwei.bukkit.redstoneClockDetector;

import me.hwei.bukkit.redstoneClockDetector.commands.*;
import me.hwei.bukkit.redstoneClockDetector.util.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class RCDPlugin
        extends JavaPlugin
        implements CommandExecutor, Listener {
    protected HashMap<Location, Integer> redstoneActivityTable;
    protected List<Map.Entry<Location, Integer>> redstoneActivityList;
    protected Worker worker;
    protected CommandSender sender;
    protected int taskId;
    protected String prefex;
    protected IOutput toConsole;
    protected AbstractCommand topCommand;

    public RCDPlugin() {
        this.redstoneActivityTable = null;
        this.redstoneActivityList = null;
        this.worker = null;
        this.sender = null;
        this.taskId = -2147483648;
        this.prefex = "";
        this.toConsole = null;
        this.topCommand = null;
    }

    public void onDisable() {
        stop();
        this.redstoneActivityTable = null;
        this.redstoneActivityList = null;
        this.toConsole.output("Disabled.");
    }

    public void onEnable() {
        IOutput toConsole = new IOutput() {
            public void output(String message) {
                RCDPlugin.this.getServer().getConsoleSender().sendMessage(message);
            }
        };
        IOutput toAll = new IOutput() {
            public void output(String message) {
                RCDPlugin.this.getServer().broadcastMessage(message);
            }
        };
        OutputManager.IPlayerGetter playerGetter = new OutputManager.IPlayerGetter() {
            public Player get(String name) {
                return RCDPlugin.this.getServer().getPlayer(name);
            }
        };
        String pluginName = getDescription().getName();
        OutputManager.Setup("[" + ChatColor.YELLOW + pluginName + ChatColor.WHITE + "] ", toConsole, toAll, playerGetter);


        this.toConsole = OutputManager.GetInstance().prefix(toConsole);
        this.toConsole.output("Enabled.");


        this.redstoneActivityTable = new HashMap();
        this.redstoneActivityList = new ArrayList();
        stop();

        setupCommands();

        getServer().getPluginManager().registerEvents(this, this);
    }

    protected boolean setupCommands() {
        try {
            ListCommand listCommand = new ListCommand("list [page]  List locations of redstone activities.", "redstoneclockdetector.list", null, this);


            AbstractCommand[] childCommands = {new StartCommand("<sec>  Start scan for <sec> seconds.", "redstoneclockdetector.start", null, this, listCommand), new StopCommand("stop  Stop scan.", "redstoneclockdetector.stop", null, this), listCommand, new TeleportCommand("tp [player] [num]  Teleport player [player] to place of number [num] in list.", "redstoneclockdetector.tp", null, this), new BreakCommand("break <num>  Break the block at place of number <num> in list.", "redstoneclockdetector.break", null, this)};


            this.topCommand = new StatusCommand("  Status of plugin.", "redstoneclockdetector", childCommands, this);
        } catch (Exception e) {
            this.toConsole.output("Can not setup commands!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<Map.Entry<Location, Integer>> getRedstoneActivityList() {
        return this.redstoneActivityList;
    }

    public CommandSender getUser() {
        return this.sender;
    }

    public int getSecondsRemain() {
        if (this.taskId == -2147483648) {
            return -1;
        }
        return this.worker.getSecondsRemain();
    }

    protected class Worker
            implements Runnable {
        protected RCDPlugin.IProgressReporter progressReporter;
        protected int secondsRemain;

        public Worker(int seconds, RCDPlugin.IProgressReporter progressReporter) {
            this.progressReporter = progressReporter;
            this.secondsRemain = seconds;
        }

        public void run() {
            if (this.secondsRemain <= 0) {
                if ((RCDPlugin.this.stop()) && (this.progressReporter != null)) {
                    this.progressReporter.onProgress(this.secondsRemain);
                }
            } else {
                if (this.progressReporter != null) {
                    this.progressReporter.onProgress(this.secondsRemain);
                }
                this.secondsRemain -= 1;
            }
        }

        public int getSecondsRemain() {
            return this.secondsRemain;
        }
    }

    public boolean start(CommandSender sender, int seconds, IProgressReporter progressReporter) {
        if (this.taskId != -2147483648) {
            return false;
        }
        this.sender = sender;
        this.worker = new Worker(seconds, progressReporter);
        this.taskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, this.worker, 0L, 20L);
        return true;
    }

    public boolean stop() {
        if (this.taskId != -2147483648) {
            getServer().getScheduler().cancelTask(this.taskId);
            this.taskId = -2147483648;
            this.sender = null;
            this.worker = null;
            sortList();
            this.redstoneActivityTable.clear();
            return true;
        }
        return false;
    }

    protected void sortList() {
        class ValueComparator
                implements Comparator<Location> {
            Map<Location, Integer> base;

            public ValueComparator(Map<Location, Integer> base) {
                this.base = base;
            }

            @Override
            public int compare(Location a, Location b) {
                if (this.base.get(a) < this.base.get(b)) {
                    return 1;
                }
                if (this.base.get((Object)a) != this.base.get((Object)b)) return -1;
                return 0;
            }
        }
        ValueComparator bvc = new ValueComparator(this.redstoneActivityTable);
        TreeMap<Location, Integer> sortedMap = new TreeMap<>(bvc);
        sortedMap.putAll(this.redstoneActivityTable);
        this.redstoneActivityList.clear();
        this.redstoneActivityList.addAll(sortedMap.entrySet());
    }

    @EventHandler
    public void onBlockRedstoneChange(BlockPhysicsEvent event) {
        if (this.taskId == -2147483648) {
            return;
        }
        Block block = event.getBlock();
        if (block.getBlockPower() == 0) {
            return;
        }
        Location loc = event.getBlock().getLocation();
        int count = 1;
        if (this.redstoneActivityTable.containsKey(loc)) {
            count += ((Integer) this.redstoneActivityTable.get(loc)).intValue();
        }
        this.redstoneActivityTable.put(loc, Integer.valueOf(count));
    }

    @EventHandler
    public void onHopperUpdate(InventoryPickupItemEvent event) {
        if (this.taskId == -2147483648) {
            return;
        }
        if ((event.getInventory().getType() != InventoryType.BREWING) && (event.getInventory().getType() != InventoryType.HOPPER) && (event.getInventory().getType() != InventoryType.CHEST)) {
            return;
        }
        Location loc = event.getItem().getLocation();
        loc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (this.redstoneActivityTable.containsKey(loc)) {
            this.redstoneActivityTable.put(loc, Integer.valueOf(((Integer) this.redstoneActivityTable.get(loc)).intValue() + 1));
        } else {
            this.redstoneActivityTable.put(loc, Integer.valueOf(1));
        }
    }

    @EventHandler
    public void onHopperUpdate(InventoryMoveItemEvent event) {
        if (this.taskId == -2147483648) {
            return;
        }
        if (event.getSource().getType() != InventoryType.HOPPER) {
            return;
        }
        Hopper hopper = (Hopper) event.getSource().getHolder();
        Location loc = hopper.getLocation();
        loc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (this.redstoneActivityTable.containsKey(loc)) {
            this.redstoneActivityTable.put(loc, Integer.valueOf(((Integer) this.redstoneActivityTable.get(loc)).intValue() + 1));
        } else {
            this.redstoneActivityTable.put(loc, Integer.valueOf(1));
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!this.topCommand.execute(sender, args)) {
                this.topCommand.showUsage(sender, command.getName());
            }
        } catch (PermissionsException e) {
            sender.sendMessage(String.format(ChatColor.RED.toString() + "You do not have permission of %s", new Object[]{e.getPerms()}));
        } catch (UsageException e) {
            sender.sendMessage("Usage: " + ChatColor.YELLOW + command.getName() + " " + e.getUsage());
            sender.sendMessage(String.format(ChatColor.RED.toString() + e.getMessage(), new Object[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static abstract interface IProgressReporter {
        public abstract void onProgress(int paramInt);
    }
}
