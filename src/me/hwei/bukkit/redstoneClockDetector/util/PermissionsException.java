package me.hwei.bukkit.redstoneClockDetector.util;

public class PermissionsException
        extends Exception {
    private String perms;
    private static final long serialVersionUID = 1L;

    public PermissionsException(String perms) {
        this.perms = perms;
    }

    public String getPerms() {
        return this.perms;
    }
}
