package com.iConomy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Timer;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.iConomy.entity.Players;
import com.iConomy.net.Database;
import com.iConomy.settings.Settings;
import com.iConomy.system.Account;
import com.iConomy.system.Accounts;
import com.iConomy.system.Interest;
import com.iConomy.system.Transactions;
import com.iConomy.util.Constants;
import com.iConomy.util.FileManager;
import com.iConomy.util.Messaging;
import com.iConomy.util.Misc;
import com.iConomy.util.VaultConnector;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.initialization.TownyInitException;
import com.palmergames.bukkit.towny.scheduling.TaskScheduler;
import com.palmergames.bukkit.towny.scheduling.impl.BukkitTaskScheduler;
import com.palmergames.bukkit.towny.scheduling.impl.FoliaTaskScheduler;

import net.milkbowl.vault.economy.Economy;

/**
 * iConomy by Team iCo
 *
 * @copyright     Copyright AniGaiku LLC (C) 2010-2011
 * @author          Nijikokun <nijikokun@gmail.com>
 * @author          Coelho <robertcoelho@live.com>
 * @author          ShadowDrakken <shadowdrakken@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class iConomy extends JavaPlugin {
	
    public static Accounts Accounts = null;

    private static Server Server = null;
    private static Database Database = null;
    private static Transactions Transactions = null;
    
    private static Players playerListener = null;
    private static Timer Interest_Timer = null;

    public static iConomy instance = null;
    public static Economy economy = null;

    Logger log = this.getLogger();
    private final Object scheduler;
    private static String requiredTownyVersion = "0.99.2.0";

    public iConomy() {
        instance = this;
        this.scheduler = townyVersionCheck() ? isFoliaClassPresent() ? new FoliaTaskScheduler(this) : new BukkitTaskScheduler(this) : null;
    }

    public static Plugin getPlugin() {
        return instance;
    }
    
    public static Players getPlayerListener() {
        return playerListener;
    }

    @Override
    public void onEnable() {

        if(!townyVersionCheck()) {
            disableWithMessage("Towny version does not meet required minimum version: " + requiredTownyVersion.toString());
            return;
        }

        Locale.setDefault(Locale.US);

        registerCommands();

        // Get the server
        Server = getServer();

        // Plugin Directory
        getDataFolder().mkdir();
        getDataFolder().setWritable(true);
        getDataFolder().setExecutable(true);

        // Setup the path.
        Constants.Plugin_Directory = getDataFolder().getPath();

        // Grab plugin details
        PluginDescriptionFile pdfFile = getDescription();

        // Versioning File
        FileManager file = new FileManager(getDataFolder().getPath(), "VERSION", false);

        // Default Files
        extract("Template.yml");

        if (!loadConfig()) {
            onDisable();
            return;
        }

        try {
            Constants.load(new File(getDataFolder(), "Config.yml"));
        } catch (Exception e) {
            Server.getPluginManager().disablePlugin(this);
            log.warning("Failed to retrieve configuration from directory.");
            log.info("Please back up your current settings and let iConomy recreate it.");
            return;
        }

        // Setup database and connections.
        try {
            Database = new Database();
            Database.setupAccountTable();

        } catch (Exception e) {
            log.severe("Database initialization failed: " + e);
            Server.getPluginManager().disablePlugin(this);
            return;
        }

        // Transaction logger.
        try {
            Transactions = new Transactions();
            Database.setupTransactionTable();
        } catch (Exception e) {
            log.info("Could not load transaction logger: " + e);
        }

        // Check version details before the system loads
        update(file, Double.valueOf(pdfFile.getVersion().split("-")[0]).doubleValue());

        // Initialize default systems
        Accounts = new Accounts();

        try {
            if (Constants.Interest) {
                long time = Constants.InterestSeconds * 1000L;

                Interest_Timer = new Timer();
                Interest_Timer.scheduleAtFixedRate(new Interest(getDataFolder().getPath()), time, time);
            }
        } catch (Exception e) {
            log.severe("Failed to start interest system: " + e);
            Server.getPluginManager().disablePlugin(this);
            return;
        }

        // Initializing Listeners
        playerListener = new Players(getDataFolder().getPath());

        // Event Registration
        getServer().getPluginManager().registerEvents(playerListener, this);

        // Console details.
        log.info("v" + pdfFile.getVersion() + " (" + Constants.Codename + ") loaded.");
        log.info("Developed by: " + pdfFile.getAuthors());
    }

	private void registerCommands() {
		MoneyCommand commandExec = new MoneyCommand();
		PluginCommand command = getCommand("money");
		command.setExecutor(commandExec);
		command.setTabCompleter(commandExec);
		command = getCommand("icoimport");
		command.setExecutor(commandExec);
		command.setTabCompleter(commandExec);
	}

	private boolean loadConfig() {
		try {
			Settings.loadConfig();
		} catch (TownyInitException e) {
			e.printStackTrace();
			log.severe("Config.yml failed to load! Disabling!");
			return false;
		}
		log.info("Config.yml loaded successfully.");
		return true;
	}

	@Override
    public void onLoad() {
        // Register as a ServiceProvider and with Vault.
        if (!registerEconomy()) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    /**
     * Register as a ServiceProvider, and with Vault.
     * 
     * @return true if successful.
     */
    private boolean registerEconomy() {
    	
        if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
            final ServicesManager sm = this.getServer().getServicesManager();
            sm.register(Economy.class, new VaultConnector(this), this, ServicePriority.Highest);
            log.info("Registered Vault interface.");

            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            
            if (rsp != null) {
                economy = rsp.getProvider();
            }
            return true;
        } else {
            log.severe("Vault not found. Please download Vault to use iConomy " + getDescription().getVersion().toString() + ".");
            return false;
        }
    }

    private void disableWithMessage(String message) {
        getLogger().severe(message);
        getLogger().severe("Disabling iConomy...");
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        try {
            if (Constants.isDatabaseTypeH2()) {
                Database.connectionPool().dispose();
            }

            log.info("Plugin disabled.");
        } catch (Exception e) {
            log.severe("Plugin disabled.");
        } finally {
            if (Interest_Timer != null) {
                Interest_Timer.cancel();
            }

            Server = null;
            Accounts = null;
            Database = null;
            Transactions = null;
            playerListener = null;
            Interest_Timer = null;
        }
    }

    /**
     * Update old databases to current.
     * 
     * @param fileManager
     * @param version
     */
    private void update(FileManager fileManager, double version) {
    	
    	/*
    	 * Does a VERSION file exist?
    	 */
        if (fileManager.exists()) {
            fileManager.read();
            try {
                double current = Double.parseDouble(fileManager.getSource());
                LinkedList<String> MySQL = new LinkedList<String>();
                LinkedList<String> GENERIC = new LinkedList<String>();
                LinkedList<String> SQL = new LinkedList<String>();

                /*
                 * If current database version doesn't match plugin version
                 */
                if (current != version) {

                	/*
                	 * Add updates oldest to newest so
                	 * the database is updated in order.
                	 */
                	if (current < 4.62D) {
                        MySQL.add("ALTER IGNORE TABLE " + Constants.SQLTable + " ADD UNIQUE INDEX(username(32));");
                        GENERIC.add("ALTER TABLE " + Constants.SQLTable + " ADD UNIQUE(username);");
                    }
                	
                    if (current < 4.64D) {
                        MySQL.add("ALTER TABLE " + Constants.SQLTable + " ADD hidden boolean DEFAULT '0';");
                        GENERIC.add("ALTER TABLE " + Constants.SQLTable + " ADD HIDDEN BOOLEAN DEFAULT '0';");
                    }

                    if (!MySQL.isEmpty() && !GENERIC.isEmpty()) {
                        Connection conn = null;
                        ResultSet rs = null;
                        Statement stmt = null;
                        try {
                            conn = getiCoDatabase().getConnection();
                            stmt = null;

                            log.info(" - Updating " + Constants.DatabaseType + " Database for latest iConomy");

                            int i = 1;
                            SQL = Constants.DatabaseType.equalsIgnoreCase("mysql") ? MySQL : GENERIC;

                            for (String Query : SQL) {
                                stmt = conn.createStatement();
                                stmt.execute(Query);

                                log.info("   Executing SQL Query #" + i + " of " + SQL.size());
                                i++;
                            }

                            fileManager.write(Double.valueOf(version));

                            log.info(" + Database Update Complete.");
                        } catch (SQLException ex) {
                            log.warning("Error updating database: " + ex.getMessage());
                        } finally {
                            if (stmt != null)
                                try {
                                    stmt.close();
                                } catch (SQLException ex) {}
                            if (rs != null)
                                try {
                                    rs.close();
                                } catch (SQLException ex) {}
                            getiCoDatabase().close(conn);
                        }
                    }
                } else {
                	// This should not be needed.
                    fileManager.write(Double.valueOf(version));
                }
            } catch (Exception e) {
                log.warning("Error on version check: ");
                e.printStackTrace();
                fileManager.delete();
            }
        } else {
        	/*
        	 * No VERSION file.
        	 */
            if (!Constants.DatabaseType.equalsIgnoreCase("flatfile")) {
                String[] SQL = new String[0];

                String[] MySQL = { "DROP TABLE " + Constants.SQLTable + ";", "RENAME TABLE ibalances TO " + Constants.SQLTable + ";", "ALTER TABLE " + Constants.SQLTable + " CHANGE  player  username TEXT NOT NULL, CHANGE balance balance DECIMAL(64, 2) NOT NULL;" };

                String[] SQLite = { "DROP TABLE " + Constants.SQLTable + ";", "CREATE TABLE '" + Constants.SQLTable + "' ('id' INT ( 10 ) PRIMARY KEY , 'username' TEXT , 'balance' DECIMAL ( 64 , 2 ));", "INSERT INTO " + Constants.SQLTable + "(id, username, balance) SELECT id, player, balance FROM ibalances;", "DROP TABLE ibalances;" };

                Connection conn = null;
                ResultSet rs = null;
                PreparedStatement ps = null;
                try {
                    conn = getiCoDatabase().getConnection();
                    DatabaseMetaData dbm = conn.getMetaData();
                    rs = dbm.getTables(null, null, "ibalances", null);
                    ps = null;

                    if (rs.next()) {
                        log.info(" - Updating " + Constants.DatabaseType + " Database for latest iConomy");

                        int i = 1;
                        SQL = Constants.DatabaseType.equalsIgnoreCase("mysql") ? MySQL : SQLite;

                        for (String Query : SQL) {
                            ps = conn.prepareStatement(Query);
                            ps.executeQuery(Query);

                            log.info("   Executing SQL Query #" + i + " of " + SQL.length);
                            i++;
                        }

                        log.info(" + Database Update Complete.");
                    }

                    fileManager.write(Double.valueOf(version));
                } catch (SQLException ex) {
                    log.warning("Error updating database: " + ex.getMessage());
                    
                } finally {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (SQLException ex) {}
                    if (rs != null)
                        try {
                            rs.close();
                        } catch (SQLException ex) {}
                    if (conn != null) {
                        getiCoDatabase().close(conn);
                    }
                }
            }
            fileManager.create();
            fileManager.write(Double.valueOf(version));
        }
    }

    private void extract(String name) {
        File actual = new File(getDataFolder(), name);
        if (!actual.exists()) {
            InputStream input = getClass().getResourceAsStream("/" + name);
            if (input != null) {
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length = 0;

                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }

                    log.info("Default setup file written: " + name);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (input != null)
                            input.close();
                    } catch (Exception e) {}
                    try {
                        if (output != null)
                            output.close();
                    } catch (Exception e) {}
                }
            }
        }
    }

    /**
     * Formats the holding balance in a human readable form with the currency attached:<br /><br />
     * 20000.53 = 20,000.53 Coin<br />
     * 20000.00 = 20,000 Coin
     *
     * @param account The name of the account you wish to be formatted
     * @return String
     */
    public static String format(String account) {
        return getAccount(account).getHoldings().toString();
    }

    /**
     * Formats the money in a human readable form with the currency attached:<br /><br />
     * 20000.53 = 20,000.53 Coin<br />
     * 20000.00 = 20,000 Coin
     *
     * @param amount double
     * @return String
     */
    public static String format(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        String formatted = formatter.format(amount);

        if (formatted.endsWith(".")) {
            formatted = formatted.substring(0, formatted.length() - 1);
        }

        return Misc.formatted(formatted, Constants.Major, Constants.Minor);
    }

    /**
     * Grab an account, if it doesn't exist, create it.
     *
     * @param name
     * @return Account or null
     */
    public static Account getAccount(String name) {
        return Accounts.get(name);
    }

    public static boolean hasAccount(String name) {
        return Accounts.exists(name);
    }

    /**
     * Grabs Database controller.
     * @return iDatabase
     */
    public static Database getiCoDatabase() {
        return Database;
    }

    /**
     * Grabs Transaction Log Controller.
     *
     * Used to log transactions between a player and anything. Such as the
     * system or another player or just environment.
     *
     * @return T
     */
    public static Transactions getTransactions() {
        return Transactions;
    }

    /**
     * Check and see if the sender has the permission as designated by node.
     *
     * @param sender
     * @param node
     * @return boolean
     */
    public static boolean hasPermissions(CommandSender sender, String node) {
        return hasPermissions(sender, node, false);
    }

    /**
     * Check and see if the sender has the permission as designated by node.
     *
     * @param sender
     * @param node
     * @param silent
     * @return boolean
     */
    public static boolean hasPermissions(CommandSender sender, String node, boolean silent) {
        if (sender instanceof Player player) {
            boolean hasPermission = player.hasPermission(node);
            if (!hasPermission && !silent)
                Messaging.send(player, "`RYou do not have the permission to use that command.");
            return hasPermission;
        }
        return true;
    }

    /**
     * Grab the server so we can do various activities if needed.
     * @return Server
     */
    public static Server getBukkitServer() {
        return Server;
    }

    private boolean townyVersionCheck() {
		try {
			return Towny.isTownyVersionSupported(requiredTownyVersion);
		} catch (NoSuchMethodError e) {
			return false;
		}
	}

	public TaskScheduler getScheduler() {
		return (TaskScheduler) this.scheduler;
	}

	private static boolean isFoliaClassPresent() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}