
package com.iConomy;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.iConomy.util.StringMgmt;
import com.palmergames.bukkit.towny.TownySettings;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.iConomy.util.Messaging;

public class MoneyCommand implements TabExecutor {

	DecimalFormat dFormat = new DecimalFormat("#.##"); 

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		switch (commandLabel.toLowerCase(Locale.ROOT)) {
		case "money" -> iConomy.getPlayerListener().parseMoneyCommand(sender, args);
		case "icoimport" -> parseImport(sender);
		}
		return true;
	}

	private void parseImport(CommandSender sender) {
		if (sender instanceof Player) {
			Messaging.send(sender, "`rThis command is available from the console only.");
			return;
		}
		if (!importEssEco())
			Messaging.send(sender, "`rImport failed.");
	}

	/**
	 * Import any EssentialsEco data.
	 * 
	 * @return
	 */
	private boolean importEssEco() {

		YamlConfiguration data = new YamlConfiguration();
		File accountsFolder = null;
		String townPrefix = TownySettings.getTownAccountPrefix();
		String nationPrefix = TownySettings.getNationAccountPrefix();
		Logger log = iConomy.instance.getLogger();
		/*
		 * Try to access essentials data.
		 */
		try {
			accountsFolder = new File("plugins/Essentials/userdata/");
			if (!accountsFolder.isDirectory())
				throw new Exception();

		} catch (Exception e) {
			log.warning("Essentials data not found or no permission to access.");
			return false;
		}

		/*
		 * List all account files.
		 */
		File[] userDataFiles;
		try {
			userDataFiles = accountsFolder.listFiles(new FilenameFilter() {
				public boolean accept(File file, String name) {
					return name.toLowerCase().endsWith(".yml");
				}
			});
		} catch (Exception e) {
			log.warning("Error accessing account files.");
			return false;
		}

		log.info("Import: Amount of accounts found:" + userDataFiles.length);
		int i = 0;

		for (File userDataFile : userDataFiles) {
			String uuid = null;
			String name = "";
			double money = 0;

			try {
				data = new YamlConfiguration();
				data.load(userDataFile);
			} catch (IOException | InvalidConfigurationException e) {
				continue;
			}

			if (userDataFile.getName().contains("-")) {
				uuid = userDataFile.getName().replace(".yml", "");
			}

			if (uuid == null || uuid.isEmpty())
				continue;

			log.info("Import: Attempting import from " + userDataFile.getName() + " file.");
			name = data.getString("last-account-name", "");
			try {
				money = Double.valueOf(dFormat.format(data.getDouble("money", 0.0)));
				log.info("    Parsing balance " + data.getString("money") + " into double " + money);
			} catch (NumberFormatException e) {
				money = 0;
				log.info("    Error parsing balance " + data.getString("money") + " into double.");
			}

			/*
			 * Check for Town/Nation accounts.
			 */
			String npcName = data.getString("npc-name", "");
			if (!npcName.isEmpty()) {
				if (npcName.startsWith(townPrefix)) {
					log.info("    Town account found: " + name.substring(townPrefix.length()));
				} else if (npcName.startsWith(nationPrefix)) {
					log.info("    Nation account found: " + name.substring(nationPrefix.length()));
				}
				name = npcName;
			}

			try {
				if (iConomy.Accounts.exists(name) && iConomy.Accounts.get(name).getHoldings().balance() != money) {
					iConomy.Accounts.get(name).getHoldings().set(money);
					log.info("    Existing account in ico5 named " + name + " being set with money " + money);
				} else {
					iConomy.Accounts.create(name);
					iConomy.Accounts.get(name).getHoldings().set(money);
					log.info("    New account named " + name + " being created with money " + money);
				}
			} catch (Exception e) {
				log.warning("    Importer could not parse account for " + userDataFile.getName());
			}

			if ((i > 0) && (i % 10 == 0)) {
				log.info("Import: " + i + " accounts read...");
			}
			i++;
		}

		log.info("Import: " + i + " accounts loaded.");
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
				return Arrays.asList("<amount>");
		} else if (args.length == 3) {
			if (AMOUNT_CMDS.contains(subCmdArg))
				return Arrays.asList("<amount>");
			if (subCmdArg.equals("hide"))
				return Arrays.asList("true", "false");
		} else if (args.length == 4 && subCmdArg.equals("grant")) {
				return Arrays.asList("silent");
		}

		return Arrays.asList("");
	}
}
