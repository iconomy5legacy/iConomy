
package com.iConomy;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
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
		boolean hasTowny = false;
		String townPrefix = "";
		String nationPrefix = "";
		String debtPrefix = "";
		String essTownPrefix = "";
		String essNationPrefix = "";
		String essDebtPrefix = "";
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
		 * Read Towny settings.
		 */
		File townySettings = null;
		try {
			townySettings = new File("plugins/Towny/settings/config.yml");

			if (townySettings.isFile()) {

				data.load(townySettings);

				townPrefix = data.getString("economy.town_prefix", "town-");
				nationPrefix = data.getString("economy.nation_prefix", "nation-");
				debtPrefix = data.getString("economy.debt_prefix", "[Debt]-");
				/*
				 * Essentials handles all NPC accounts as lower case.
				 */
				essTownPrefix = townPrefix.replaceAll("-", "_").toLowerCase();
				essNationPrefix = nationPrefix.replaceAll("-", "_").toLowerCase();
				essDebtPrefix = debtPrefix.replaceAll("[\\[\\]-]", "_").toLowerCase();

				hasTowny = true;
			}

		} catch (Exception e) {
			log.warning("Towny data not found or no permission to access.");
		}

		/*
		 * List all account files.
		 */
		File[] accounts;
		try {
			accounts = accountsFolder.listFiles(new FilenameFilter() {
				public boolean accept(File file, String name) {
					return name.toLowerCase().endsWith(".yml");
				}
			});
		} catch (Exception e) {
			log.warning("Error accessing account files.");
			return false;
		}

		log.info("Amount of accounts found:" + accounts.length);
		int i = 0;

		for (File account : accounts) {
			String uuid = null;
			String name = "";
			double money = 0;

			try {
				data = new YamlConfiguration();
				data.load(account);
			} catch (IOException | InvalidConfigurationException e) {
				continue;
			}

			if (account.getName().contains("-")) {
				uuid = account.getName().replace(".yml", "");
			}

			if (uuid != null) {
				name = data.getString("lastAccountName", "");
				try {
					money = Double.parseDouble(data.getString("money", "0"));
				} catch (NumberFormatException e) {
					money = 0;
				}
				String actualName;
				/*
				 * Check for Town/Nation accounts.
				 */
				if (hasTowny) {
					if (name.startsWith(essTownPrefix)) {
						actualName = name.substring(essTownPrefix.length());
						log.info("Import: Town account found: " + actualName);
						name = townPrefix + actualName;

					} else if (name.startsWith(essNationPrefix)) {
						actualName = name.substring(essNationPrefix.length());
						log.info("Import: Nation account found: " + actualName);
						name = nationPrefix + actualName;

					} else if (name.startsWith(essDebtPrefix)) {
						actualName = name.substring(essDebtPrefix.length());
						log.info("Import: Debt account found: " + actualName);
						name = debtPrefix + actualName;
					}
				}
			}

			try {
				if (iConomy.Accounts.exists(name)) {
					if (iConomy.Accounts.get(name).getHoldings().balance() == money) {
						continue;
					} else
						iConomy.Accounts.get(name).getHoldings().set(money);
				} else {
					iConomy.Accounts.create(name);
					iConomy.Accounts.get(name).getHoldings().set(money);
				}

				if ((i > 0) && (i % 10 == 0)) {
					log.info(i + " accounts read...");
				}
				i++;

			} catch (Exception e) {
				log.warning("Importer could not parse account for " + account.getName());
			}
		}

		log.info(i + " accounts loaded.");
		return true;
	}

	private final List<String> SUB_CMDS = Arrays.asList("?", "rank", "top", "pay", "grant", "set", "hide", "create",
			"remove", "preset", "purge", "empty", "stats", "help", "?");
	private final List<String> PLAYER_CMDS = Arrays.asList("rank", "pay", "grant", "set", "hide", "create", "remove",
			"reset");
	private final List<String> AMOUNT_CMDS = Arrays.asList("pay","grant","set") ;

	@Nullable
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
			@NotNull String[] args) {

		String subCmdArg = args[0].toLowerCase(Locale.ROOT);

		if (args.length == 1) {
			return SUB_CMDS.stream().filter(s -> s.startsWith(subCmdArg)).collect(Collectors.toList());
		} else if (args.length == 2) {
			if (PLAYER_CMDS.contains(subCmdArg))	
				return null;
			if (subCmdArg.equals("top"))
				return Arrays.asList("amount");
		} else if (args.length == 3) {
			if (AMOUNT_CMDS.contains(subCmdArg))
				return Arrays.asList("amount");
			if (subCmdArg.equals("hide"))
				return Arrays.asList("true", "false");
		} else if (args.length == 4 && subCmdArg.equals("grant")) {
				return Arrays.asList("true", "false");
		}

		return Arrays.asList("");
	}
}
