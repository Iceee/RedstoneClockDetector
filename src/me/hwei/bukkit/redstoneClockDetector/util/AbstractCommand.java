package me.hwei.bukkit.redstoneClockDetector.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCommand {
    protected String coloredUsage;
    protected String perm;
    protected AbstractCommand[] children;
    protected Token[] tokens;

    public AbstractCommand(String usage, String perm, AbstractCommand[] children)
            throws Exception {
        this.perm = perm;
        this.children = (children == null ? new AbstractCommand[0] : children);
        buildTokens(usage);
    }

    public void showUsage(CommandSender sender, String rootCommand) {
        if (sender.hasPermission(this.perm)) {
            IOutput toSender = OutputManager.GetInstance().toSender(sender);
            toSender.output(ChatColor.YELLOW.toString() + rootCommand + ChatColor.WHITE + " " + this.coloredUsage);
        }
        for (AbstractCommand child : this.children) {
            child.showUsage(sender, rootCommand);
        }
    }

    public boolean execute(CommandSender sender, String[] args)
            throws PermissionsException, UsageException {
        return execute(sender, args, new MatchResult[0]);
    }

    protected boolean execute(CommandSender sender, String[] args, MatchResult[] matchResults)
            throws PermissionsException, UsageException {
        List<String> argList = new ArrayList(Arrays.asList(args));
        for (int i = 0; i < this.tokens.length - args.length; i++) {
            argList.add(null);
        }
        List<MatchResult> matchResultList = new ArrayList(Arrays.asList(matchResults));
        for (int i = matchResultList.size(); i < this.tokens.length; i++) {
            MatchResult matchResult = this.tokens[i].match((String) argList.get(i));
            if (matchResult == null) {
                return false;
            }
            matchResultList.add(matchResult);
        }
        if (args.length > this.tokens.length) {
            boolean matched = false;
            for (AbstractCommand child : this.children) {
                matched = (matched) || (child.execute(sender, args, (MatchResult[]) matchResultList.toArray(new MatchResult[0])));
            }
            return matched;
        }
        if ((this.perm != null) && (!sender.hasPermission(this.perm))) {
            throw new PermissionsException(this.perm);
        }
        List<MatchResult> dataList = new ArrayList(args.length);
        for (MatchResult matchResult : matchResultList) {
            if (matchResult.hasData()) {
                dataList.add(matchResult);
            }
        }
        return execute(sender, (MatchResult[]) dataList.toArray(new MatchResult[0]));
    }

    protected abstract boolean execute(CommandSender paramCommandSender, MatchResult[] paramArrayOfMatchResult)
            throws UsageException;

    private void buildTokens(String usage)
            throws Exception {
        int splitPos = usage.indexOf("  ");
        String tokenDefine = null;
        if (splitPos == -1) {
            tokenDefine = usage;
            this.coloredUsage = tokenDefine;
        } else {
            tokenDefine = usage.substring(0, splitPos);
            this.coloredUsage = (tokenDefine + ChatColor.GRAY + usage.substring(splitPos));
        }
        String[] tokenStrings = tokenDefine.split(" ");
        List<Token> tokenList = new ArrayList(tokenStrings.length);
        for (String tokenString : tokenStrings) {
            if (!tokenString.isEmpty()) {
                tokenList.add(new Token(tokenString));
            }
        }
        this.tokens = tokenList.toArray(new Token[0]);
        for (AbstractCommand child : this.children) {
            if (child.tokens.length < this.tokens.length) {
                throw new Exception("Child command is shorter than parent command.");
            }
            for (int i = 0; i < this.tokens.length; i++) {
                if (!this.tokens[i].equals(child.tokens[i])) {
                    throw new Exception("Child command does not have a prefix of parent command.");
                }
            }
        }
    }

    protected static class Token {
        private boolean optional;
        private String template;

        public Token(String template) {
            if (template.length() >= 2) {
                if ((template.charAt(0) == '[') && (template.charAt(template.length() - 1) == ']')) {
                    this.template = null;
                    this.optional = true;
                    return;
                }
                if ((template.charAt(0) == '<') && (template.charAt(template.length() - 1) == '>')) {
                    this.template = null;
                    this.optional = false;
                    return;
                }
            }
            this.template = template;
            this.optional = false;
        }

        public AbstractCommand.MatchResult match(String part) {
            if (part == null) {
                return this.optional ? new AbstractCommand.MatchResult() : null;
            }
            if (this.template == null) {
                return new AbstractCommand.MatchResult(part);
            }
            return this.template.equalsIgnoreCase(part) ? new AbstractCommand.MatchResult() : null;
        }

        public boolean equals(Token token) {
            if (this.template == null) {
                return token.template == null;
            }
            if (token.template == null) {
                return false;
            }
            return this.template.equalsIgnoreCase(token.template);
        }
    }

    protected static class MatchResult {
        private String data;

        public MatchResult(String data) {
            this.data = data;
        }

        public MatchResult() {
            this.data = null;
        }

        public boolean hasData() {
            return this.data != null;
        }

        public Integer getInteger() {
            if (this.data == null) {
                return null;
            }
            Integer result = null;
            try {
                result = Integer.valueOf(Integer.parseInt(this.data));
            } catch (Exception e) {
            }
            return result;
        }

        public Double getDouble() {
            if (this.data == null) {
                return null;
            }
            Double result = null;
            try {
                result = Double.valueOf(Double.parseDouble(this.data));
            } catch (Exception e) {
            }
            return result;
        }

        public String getString() {
            return this.data;
        }
    }
}
