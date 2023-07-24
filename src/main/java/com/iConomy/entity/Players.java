package com.iConomy.entity;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;
import com.iConomy.util.Messaging;
import com.iConomy.util.Misc;
import com.iConomy.util.StringMgmt;
import com.iConomy.util.Template;
import com.palmergames.bukkit.towny.confirmations.Confirmation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Handles the command usage and account creation upon a
 * player joining the server.
 *
 * @author Nijikokun
 */
public class Players implements Listener {
    private Template Template = null;

    Logger log = iConomy.instance.getLogger();

    /**
     * Initialize the class as well as the template for various
     * messages throughout the commands.
     *
     * @param directory
     */
    public Players(String directory) {
        this.Template = new Template(directory, "Template.yml");
    }

    /**
     * Help documentation for iConomy all in one method.
     *
     * Allows us to easily utilize all throughout the class without having multiple
     * instances of the same help lines.
     */
    private void getMoneyHelp(CommandSender sender) {
        Messaging.send(sender, "`y ");
        Messaging.send(sender, "`w iConomy (`r" + Constants.Codename + "`w)");
        Messaging.send(sender, "`y ");
        Messaging.send(sender, "`w <> Required, [] Optional");
        Messaging.send(sender, " ");
        
        if(sender instanceof Player){
            Messaging.send(sender, "`G  /money `y Check your balance");
        }
        
        Messaging.send(sender, "`G  /money `g? `y For help & Information");

        if (iConomy.hasPermissions(sender, "iConomy.rank", true)) {
            Messaging.send(sender, "`G  /money `grank `G[`wplayer`G] `y Rank on the topcharts.   ");
        }

        if (iConomy.hasPermissions(sender, "iConomy.list", true)) {
            Messaging.send(sender, "`G  /money `gtop `G[`wamount`G] `y Richest players listing.  ");
        }

        if (iConomy.hasPermissions(sender, "iConomy.payment", true)) {
            Messaging.send(sender, "`G  /money `gpay `G<`wplayer`G> <`wamount`G> `y Send money to a player.");
        }

        if (iConomy.hasPermissions(sender, "iConomy.admin.grant", true)) {
            Messaging.send(sender, "`G  /money `ggrant `G<`wplayer`G> <`wamount`G> {`wsilent`G} `y Give money, optionally silent.");
            Messaging.send(sender, "`G  /money `ggrant `G<`wplayer`G> -<`wamount`G> {`wsilent`G} `y Take money, optionally silent.");
        }

        if (iConomy.hasPermissions(sender, "iConomy.admin.set", true)) {
            Messaging.send(sender, "`G  /money `gset `G<`wplayer`G> <`wamount`G> `y Sets a players balance.");
        }

        if (iConomy.hasPermissions(sender, "iConomy.admin.hide", true)) {
            Messaging.send(sender, "`G  /money `ghide `G<`wplayer`G> `wtrue`G/`wfalse `y Hide or show an account.");
        }

        if (iConomy.hasPermissions(sender, "iConomy.admin.account.create", true)) {
            Messaging.send(sender, "`G  /money `gcreate `G<`wplayer`G> `y Create player account.");
        }

        if (iConomy.hasPermissions(sender, "iConomy.admin.account.remove", true)) {
            Messaging.send(sender, "`G  /money `gremove `G<`wplayer`G> `y Remove player account.");
        }

        if (iConomy.hasPermissions(sender, "iConomy.admin.reset", true)) {
            Messaging.send(sender, "`G  /money `greset `G<`wplayer`G> `y Reset player account.");
        }

        if (iConomy.hasPermissions(sender, "iConomy.admin.purge", true)) {
            Messaging.send(sender, "`G  /money `gpurge `y Remove all accounts with inital holdings.");
        }

        if (iConomy.hasPermissions(sender, "iConomy.admin.empty", true)) {
            Messaging.send(sender, "`G  /money `gempty `y Empties database.");
        }

        if (iConomy.hasPermissions(sender, "iConomy.admin.stats", true)) {
            Messaging.send(sender, "`G  /money `gstats `y Check all economic stats.");
        }

        Messaging.send(sender, " ");
    }

    /**
     * Listens to the PlayerJoinEvent in order to create new Accounts for players who have not logged in.
     *
     * @param event PlayerJoinEvent we listen to.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (iConomy.getAccount(player.getName()) == null)
            log.warning("Error creating / grabbing account for: " + player.getName());
    }

    /**
     * Account Creation
     */
    private void createAccount(CommandSender sender, String name) {
        iConomy.getAccount(name);
        Messaging.send(sender, this.Template.color("tag.money") + this.Template.parse("accounts.create", new String[] { "+name,+n" }, new String[] { name }));
    }

    /**
     * Account Removal
     */
    private void removeAccount(CommandSender sender, String name) {
        iConomy.Accounts.remove(name);
        Messaging.send(sender, this.Template.color("tag.money") + this.Template.parse("accounts.remove", new String[] { "+name,+n" }, new String[] { name }));
    }

    /**
     * Account Hidden stat, hiding the account from the top list.
     * 
     * @param name Name of the account to hide.
     * @param hidden True to make the accont hidden.
     * @return true if successful.
     */
    private boolean setHidden(String name, boolean hidden) {
        return iConomy.getAccount(name).setHidden(hidden);
    }

    /**
     * Shows the balance to the requesting player.
     *
     * @param name The name of the player we are viewing
     * @param viewing The player who is viewing the account
     * @param mine Is it the player who is trying to view?
     */
    private void showBalance(String name, CommandSender viewing, boolean mine) {
        if (mine)
            Messaging.send(viewing, this.Template.color("tag.money") + this.Template.parse("personal.balance", new String[] { "+balance,+b" }, new String[] { iConomy.format(name) }));
        else
            Messaging.send(viewing, this.Template.color("tag.money") + this.Template.parse("player.balance", new String[] { "+balance,+b", "+name,+n" }, new String[] { iConomy.format(name), name }));
    }

    /**
     * Reset a players account easily.
     *
     * @param resetting The player being reset. Cannot be null.
     * @param by The player resetting the account. Cannot be null.
     * @param notify Do we want to show the updates to each player?
     */
    private void showPayment(String from, String to, double amount) {
        Player paymentFrom = iConomy.getBukkitServer().getPlayer(from);
        Player paymentTo = iConomy.getBukkitServer().getPlayer(to);

        if (paymentFrom != null) {
            from = paymentFrom.getName();
        }

        if (paymentTo != null) {
            to = paymentTo.getName();
        }

        Holdings From = iConomy.getAccount(from).getHoldings();
        Holdings To = iConomy.getAccount(to).getHoldings();

        if (from.equals(to)) {
            if (paymentFrom != null)
                Messaging.send(paymentFrom, this.Template.color("payment.self"));
        } else if (amount < 0.0D || !From.hasEnough(amount)) {
            if (paymentFrom != null)
                Messaging.send(paymentFrom, this.Template.color("error.funds"));
        } else {
            From.subtract(amount);
            To.add(amount);

            Double balanceFrom = Double.valueOf(From.balance());
            Double balanceTo = Double.valueOf(To.balance());

            iConomy.getTransactions().insert(from, to, balanceFrom.doubleValue(), balanceTo.doubleValue(), 0.0D, 0.0D, amount);
            iConomy.getTransactions().insert(to, from, balanceTo.doubleValue(), balanceFrom.doubleValue(), 0.0D, amount, 0.0D);

            if (paymentFrom != null) {
                Messaging.send(paymentFrom, this.Template.color("tag.money") + this.Template.parse("payment.to", new String[] { "+name,+n", "+amount,+a" }, new String[] { to, iConomy.format(amount) }));

                showBalance(from, paymentFrom, true);
            }

            if (paymentTo != null) {
                Messaging.send(paymentTo, this.Template.color("tag.money") + this.Template.parse("payment.from", new String[] { "+name,+n", "+amount,+a" }, new String[] { from, iConomy.format(amount) }));

                showBalance(to, paymentTo, true);
            }
        }
    }

    /**
     * Reset a players account, accessable via Console & In-Game
     *
     * @param account The account we are resetting.
     * @param controller If set to null, won't display messages.
     * @param console Is it sent via console?
     */
    private void showReset(CommandSender sender, String account, Player controller, boolean console) {
        Player player = iConomy.getBukkitServer().getPlayer(account);

        if (player != null) {
            account = player.getName();
        }

        Account Account = iConomy.getAccount(account);

        iConomy.getTransactions().insert(account, "[System]", 0.0D, 0.0D, 0.0D, 0.0D, Account.getHoldings().balance());

        Account.getHoldings().reset();

        if (player != null) {
            Messaging.send(player, this.Template.color("personal.reset"));
        }

        if (controller != null) {
            Messaging.send(sender, this.Template.parse("player.reset", new String[] { "+name,+n" }, new String[] { account }));
        }

        if (console)
            log.info("Player " + account + "'s account has been reset.");
        else
            log.info("Player " + account + "'s account has been reset by " + controller.getName() + ".");
    }

    /**
    *
    * @param account
    * @param controller If set to null, won't display messages.
    * @param amount
    * @param console Is it sent via console?
    */
    private void showGrant(CommandSender sender, String name, Player controller, double amount, boolean console, boolean silent) {
        Player online = iConomy.getBukkitServer().getPlayer(name);

        if (online != null) {
            name = online.getName();
        }

        Account account = iConomy.getAccount(name);

        if (account != null) {
            Holdings holdings = account.getHoldings();
            holdings.add(amount);

            Double balance = Double.valueOf(holdings.balance());

            if (amount < 0.0D)
                iConomy.getTransactions().insert("[System]", name, 0.0D, balance.doubleValue(), 0.0D, 0.0D, amount);
            else {
                iConomy.getTransactions().insert("[System]", name, 0.0D, balance.doubleValue(), 0.0D, amount, 0.0D);
            }

            if (online != null && !silent) {
                Messaging.send(online, this.Template.color("tag.money") + this.Template.parse(amount < 0.0D ? "personal.debit" : "personal.credit", new String[] { "+by", "+amount,+a" }, new String[] { console ? "console" : controller.getName(), iConomy.format(amount < 0.0D ? amount * -1.0D : amount) }));

                showBalance(name, online, true);
            }

            if (controller != null) {
                Messaging.send(sender, this.Template.color("tag.money") + this.Template.parse(amount < 0.0D ? "player.debit" : "player.credit", new String[] { "+name,+n", "+amount,+a" }, new String[] { name, iConomy.format(amount < 0.0D ? amount * -1.0D : amount) }));
            }

            if (console)
                log.info("Player " + account.getName() + "'s account had " + (amount < 0.0D ? "negative " : "") + iConomy.format(amount < 0.0D ? amount * -1.0D : amount) + " grant to it.");
            else
                log.info("Player " + account.getName() + "'s account had " + (amount < 0.0D ? "negative " : "") + iConomy.format(amount < 0.0D ? amount * -1.0D : amount) + " grant to it by " + controller.getName() + ".");
        }
    }

    /**
     * Show the actual setting of the new balance of an account.
     *
     * @param account
     * @param controller If set to null, won't display messages.
     * @param amount
     * @param console Is it sent via console?
     */
    private void showSet(CommandSender sender, String name, Player controller, double amount, boolean console) {
        Player online = iConomy.getBukkitServer().getPlayer(name);

        if (online != null) {
            name = online.getName();
        }

        Account account = iConomy.getAccount(name);

        if (account != null) {
            Holdings holdings = account.getHoldings();
            holdings.set(amount);

            Double balance = Double.valueOf(holdings.balance());

            iConomy.getTransactions().insert("[System]", name, 0.0D, balance.doubleValue(), amount, 0.0D, 0.0D);

            if (online != null) {
                Messaging.send(online, this.Template.color("tag.money") + this.Template.parse("personal.set", new String[] { "+by", "+amount,+a" }, new String[] { console ? "Console" : controller.getName(), iConomy.format(amount) }));

                showBalance(name, online, true);
            }

            if (controller != null) {
                Messaging.send(sender, this.Template.color("tag.money") + this.Template.parse("player.set", new String[] { "+name,+n", "+amount,+a" }, new String[] { name, iConomy.format(amount) }));
            }

            if (console)
                log.info("Player " + account + "'s account had " + iConomy.format(amount) + " set to it.");
            else
                log.info("Player " + account + "'s account had " + iConomy.format(amount) + " set to it by " + controller.getName() + ".");
        }
    }

    /**
     * Parses and outputs personal rank.
     *
     * Grabs rankings via the bank system and outputs the data,
     * using the template variables, to the given player stated
     * in the method.
     *
     * @param viewing
     * @param player
     */
    private void showRank(CommandSender viewing, String player) {
        Account account = iConomy.getAccount(player);

        if (account != null) {
            int rank = account.getRank();
            boolean isSelf = ((Player) viewing).getName().equalsIgnoreCase(player);

            Messaging.send(viewing, this.Template.color("tag.money") + this.Template.parse(isSelf ? "personal.rank" : "player.rank", new Object[] { "+name,+n", "+rank,+r" }, new Object[] { player, Integer.valueOf(rank) }));
        } else {
            Messaging.send(viewing, this.Template.parse("error.account", new Object[] { "+name,+n" }, new Object[] { player }));
        }
    }

    /**
     * Top ranking users by cash flow.
     *
     * Grabs the top amount of players and outputs the data, using the template
     * system, to the given viewing player.
     *
     * @param viewing
     * @param amount
     */
    private void showTop(CommandSender viewing, int amount) {
        LinkedHashMap<String, Double> Ranking = iConomy.Accounts.ranking(amount);
        int count = 1;

        Messaging.send(viewing, this.Template.parse("top.opening", new Object[] { "+amount,+a" }, new Object[] { Integer.valueOf(amount) }));

        if (Ranking == null || Ranking.isEmpty()) {
            Messaging.send(viewing, this.Template.color("top.empty"));

            return;
        }

        for (String account : Ranking.keySet()) {
            Double balance = Ranking.get(account);

            Messaging.send(viewing, this.Template.parse("top.line", new String[] { "+i,+number", "+player,+name,+n", "+balance,+b" }, new Object[] { Integer.valueOf(count), account, iConomy.format(balance.doubleValue()) }));

            count++;
        }
    }

    /**
     * Commands sent from in-game are parsed and evaluated here.
     *
     * @param sender
     * @param split
     */
	public void parseMoneyCommand(CommandSender sender, String[] split) {
		boolean isPlayer = sender instanceof Player;
		Player player = isPlayer ? (Player) sender : null;

		if (split.length == 0) {
			if (isPlayer)
				showBalance(player.getName(), player, true);
			else
				Messaging.send(sender, "`RSpecify a player to view their balance.");

			return;
		}

		String name = split[0];
		String command = split[0].toLowerCase(Locale.ROOT);
		split = StringMgmt.remFirstArg(split);
		switch (command) {
		case "create", "-c" -> parseMoneyCreateCommand(sender, split);
		case "empty", "-e" -> parseMoneyEmptyCommand(sender);
		case "grant", "-g" -> parseMoneyGrantCommand(player, sender, isPlayer, split);
		case "help", "?" -> getMoneyHelp(sender);
		case "hide", "-h" -> parseMoneyHideCommand(sender, split);
		case "pay", "-p" -> parseMoneyPayCommand(player, sender, isPlayer, split);
		case "purge", "-pf" -> parseMoneyPurgeCommand(sender);
		case "rank", "-r" -> parseMoneyRankCommand(player, sender, isPlayer, split);
		case "remove", "-v" -> parseMoneyRemoveCommand(sender, split);
		case "reset", "-x" -> parseMoneyResetCommand(player, sender, isPlayer, split);
		case "set" -> parseMoneySetCommand(player, sender, isPlayer, split);
		case "stats", "-s" -> parseMoneyStatsCommand(sender);
		case "top", "-t" -> parseMoneyTopCommand(player, sender, split);
		default -> parseMoneyPlayerName(sender, name);
		}
	}

	private void parseMoneySetCommand(Player player, CommandSender sender, boolean isPlayer, String[] args) {
		if (!iConomy.hasPermissions(player, "iConomy.admin.set"))
			return;

		if (args.length == 0) {
			getMoneyHelp(sender);
			return;
		}

		String name = "";
		double amount = 0.0D;

		Player check = Misc.playerMatch(args[0]);

		if (check != null)
			name = check.getName();
		else
			name = args[0];

		if (!iConomy.hasAccount(name)) {
			Messaging.send(sender, this.Template.parse("error.account", new String[] { "+name,+n" }, new String[] { args[0] }));
			return;
		}

		try {
			amount = Double.parseDouble(args[1]);
		} catch (NumberFormatException e) {
			Messaging.send(sender, "`rInvalid amount: `w" + args[1]);
			Messaging.send(sender, "`rUsage: `w/money `r[`w-g`r|`wgrant`r] <`wplayer`r> (`w-`r)`r<`wamount`r>");
			return;
		}

		showSet(sender, name, player, amount, isPlayer);
	}

	private void parseMoneyHideCommand(CommandSender sender, String[] args) {
		if (!iConomy.hasPermissions(sender, "iConomy.admin.hide"))
			return;

		if (args.length != 2) {
			getMoneyHelp(sender);
			return;
		}

		String name = "";
		Player check = Misc.playerMatch(args[0]);

		if (check != null)
			name = check.getName();
		else
			name = args[0];

		if (!iConomy.hasAccount(name)) {
			Messaging.send(sender, this.Template.parse("error.account", new String[] { "+name,+n" }, new String[] { args[0] }));
			return;
		}

		boolean hidden = Misc.is(args[1], new String[] { "true", "t", "-t", "yes", "da", "-d" });

		if (!setHidden(name, hidden))
			Messaging.send(sender, this.Template.parse("error.account", new String[] { "+name,+n" }, new String[] { name }));
		else
			Messaging.send(sender, this.Template.parse("accounts.status", new String[] { "+status,+s" },
					new String[] { hidden ? "hidden" : "visible" }));

	}

	private void parseMoneyGrantCommand(Player player, CommandSender sender, boolean isPlayer, String[] args) {
		if (!iConomy.hasPermissions(sender, "iConomy.admin.grant"))
			return;

		if (args.length == 0) {
			getMoneyHelp(sender);
			return;
		}

		ArrayList<String> accounts = new ArrayList<String>();
		boolean console = !isPlayer;
		double amount = 0.0D;

		Player check = Misc.playerMatch(args[0]);
		String name = "";

		if (check != null)
			name = check.getName();
		else
			name = args[0];

		if (iConomy.hasAccount(name)) {
			accounts.add(name);
		} else {
			Messaging.send(sender, this.Template.parse("error.account", new String[] { "+name,+n" }, new String[] { name }));
			return;
		}

		if (accounts.size() < 1 || accounts.isEmpty()) {
			Messaging.send(sender, this.Template.color("<rose>Grant Query returned 0 accounts to alter."));
			return;
		}

		try {
			amount = Double.parseDouble(args[1]);
		} catch (NumberFormatException e) {
			Messaging.send(sender, "`rInvalid amount: `w" + args[1]);
			Messaging.send(sender, "`rUsage: `w/money `r[`w-g`r|`wgrant`r] <`wplayer`r> (`w-`r)`r<`wamount`r>");
			return;
		}

		boolean silent = args.length == 3 && Misc.is(args[2], new String[] { "silent", "-s" });

		for (String accountName : accounts)
			showGrant(sender, accountName, player, amount, console, silent);

	}

	private void parseMoneyPayCommand(Player player, CommandSender sender, boolean isPlayer, String[] args) {
		if (!iConomy.hasPermissions(sender, "iConomy.payment"))
			return;

		if (!isPlayer) {
			Messaging.send(sender, "`rCommand unavailable from console. Try money grant {name} {amount}.");
			return;
		}

		if (args.length == 0) {
			getMoneyHelp(sender);
			return;
		}

		String name = "";
		double amount = 0.0D;

		if (iConomy.hasAccount(args[0])) {
			name = args[0];
		} else {
			Messaging.send(sender, this.Template.parse("error.account", new String[] { "+name,+n" }, new String[] { args[0] }));
			return;
		}

		try {
			amount = Double.parseDouble(args[1]);

			if (amount < 0.01D)
				throw new NumberFormatException();
		} catch (NumberFormatException ex) {
			Messaging.send(sender, "`rInvalid amount: `w" + amount);
			Messaging.send(sender, "`rUsage: `w/money `r[`w-p`r|`wpay`r] <`wplayer`r> `r<`wamount`r>");
			return;
		}

		showPayment(player.getName(), name, amount);
	}

	private void parseMoneyResetCommand(Player player, CommandSender sender, boolean isPlayer, String[] args) {
		if (!iConomy.hasPermissions(sender, "iConomy.admin.reset"))
			return;

		if (args.length == 0) {
			getMoneyHelp(sender);
			return;
		}

		if (iConomy.hasAccount(args[0]))
			showReset(sender, args[0], player, isPlayer);
		else
			Messaging.send(sender, this.Template.parse("error.account", new String[] { "+name,+n" }, new String[] { args[0] }));
	}

	private void parseMoneyRemoveCommand(CommandSender sender, String[] args) {
		if (!iConomy.hasPermissions(sender, "iConomy.admin.account.remove"))
			return;

		if (args.length == 0) {
			getMoneyHelp(sender);
			return;
		}

		if (iConomy.hasAccount(args[0]))
			removeAccount(sender, args[0]);
		else
			Messaging.send(sender, this.Template.parse("error.account", new String[] { "+name,+n" }, new String[] { args[0] }));
	}

	private void parseMoneyCreateCommand(CommandSender sender, String[] args) {
		if (!iConomy.hasPermissions(sender, "iConomy.admin.account.create"))
			return;

		if (args.length == 0) {
			getMoneyHelp(sender);
			return;
		}

		if (!iConomy.hasAccount(args[0]))
			createAccount(sender, args[0]);
		else
			Messaging.send(sender, this.Template.parse("error.exists", new String[] { "+name,+n" }, new String[] { args[0] }));
	}

	private void parseMoneyPlayerName(CommandSender sender, String name) {
		if (!iConomy.hasPermissions(sender, "iConomy.access"))
			return;

		Player online = iConomy.getBukkitServer().getPlayer(name);

		if (online != null)
			name = online.getName();

		if (iConomy.hasAccount(name))
			showBalance(name, sender, false);
		else
			Messaging.send(sender, this.Template.parse("error.account", new String[] { "+name,+n" }, new String[] { name }));
	}

	private void parseMoneyStatsCommand(CommandSender sender) {
		if (!iConomy.hasPermissions(sender, "iConomy.admin.stats"))
			return;

		Collection<Double> accountHoldings = iConomy.Accounts.values();
		Collection<Double> totalHoldings = accountHoldings;

		double TCOH = 0.0D;
		int accounts = accountHoldings.size();
		int totalAccounts = accounts;

		for (Object o : totalHoldings.toArray())
			TCOH += ((Double) o).doubleValue();

		Messaging.send(sender, this.Template.color("statistics.opening"));

		Messaging.send(sender, this.Template.parse("statistics.total", new String[] { "+currency,+c", "+amount,+money,+a,+m" },
						new Object[] { Constants.Major.get(1), iConomy.format(TCOH) }));

		Messaging.send(sender, this.Template.parse("statistics.average", new String[] { "+currency,+c", "+amount,+money,+a,+m" },
						new Object[] { Constants.Major.get(1), iConomy.format(TCOH / totalAccounts) }));

		Messaging.send(sender, this.Template.parse("statistics.accounts", new String[] { "+currency,+c", "+amount,+accounts,+a" },
						new Object[] { Constants.Major.get(1), Integer.valueOf(accounts) }));
	}

	private void parseMoneyPurgeCommand(CommandSender sender) {
		if (!iConomy.hasPermissions(sender, "iConomy.admin.purge"))
			return;

		Confirmation.runOnAccept(()-> {
			iConomy.Accounts.purge();
			Messaging.send(sender, this.Template.color("accounts.purge"));
		}).sendTo(sender);
	}

	private void parseMoneyEmptyCommand(CommandSender sender) {
		if (!iConomy.hasPermissions(sender, "iConomy.admin.empty"))
			return;

		Confirmation.runOnAccept(()-> {
			iConomy.Accounts.emptyDatabase();
			Messaging.send(sender, this.Template.color("accounts.empty"));
		}).sendTo(sender);
	}

	private void parseMoneyTopCommand(Player player, CommandSender sender, String[] args) {
		if (!iConomy.hasPermissions(player, "iConomy.list"))
			return;

		if (args.length == 0) {
			showTop(sender, 5);	
			return;
		}

		try {
			int top = Integer.parseInt(args[0]);
			showTop(sender, top > 100 ? 100 : top < 0 ? 5 : top);
		} catch (Exception e) {
			showTop(sender, 5);
		}
	}

	private void parseMoneyRankCommand(Player player, CommandSender sender, boolean isPlayer, String[] args) {
		if (!iConomy.hasPermissions(sender, "iConomy.rank"))
			return;

		if (args.length == 0 && !isPlayer) {
			Messaging.send(sender, "`rTo use this command from the console you must specify a player name.");
			return;
		}

		if (args.length == 0 && isPlayer) {
			showRank(player, player.getName());
			return;
		}

		if (iConomy.hasAccount(args[0]))
			showRank(sender, args[0]);
		else
			Messaging.send(sender, this.Template.parse("error.account", new String[] { "+name,+n" }, new String[] { args[0] }));
	}
}
