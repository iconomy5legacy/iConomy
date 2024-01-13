
package com.iConomy;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.iConomy.util.StringMgmt;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoneyCommand implements TabExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		switch (commandLabel.toLowerCase(Locale.ROOT)) {
		case "money" -> iConomy.getPlayerListener().parseMoneyCommand(sender, args);
		}
		return true;
	}

	private final List<String> SUB_CMDS = Arrays.asList("?", "rank", "top", "pay", "grant", "set", "hide", "create",
			"remove", "preset", "purge", "empty", "stats");
	private final List<String> PLAYER_CMDS = Arrays.asList("rank", "pay", "grant", "set", "hide", "create", "remove",
			"reset");
	private final List<String> AMOUNT_CMDS = Arrays.asList("pay","grant","set") ;

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {

		String subCmdArg = args[0].toLowerCase(Locale.ROOT);

		if (args.length == 1) {
			if (StringMgmt.filterByStart(SUB_CMDS, subCmdArg).size() > 0) {
				return SUB_CMDS.stream().filter(s -> s.startsWith(subCmdArg)).collect(Collectors.toList());
			} else {
				return null;
			}
		} else if (args.length == 2) {
			if (PLAYER_CMDS.contains(subCmdArg))	
				return null;
			if (subCmdArg.equals("top"))
				return List.of("<amount>");
		} else if (args.length == 3) {
			if (AMOUNT_CMDS.contains(subCmdArg))
				return List.of("<amount>");
			if (subCmdArg.equals("hide"))
				return Arrays.asList("true", "false");
		} else if (args.length == 4 && subCmdArg.equals("grant")) {
				return List.of("silent");
		}

		return Arrays.asList("");
	}
}
