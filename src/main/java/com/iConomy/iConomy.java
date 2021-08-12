package com.iConomy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
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

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.iConomy.entity.Players;
import com.iConomy.net.Database;
import com.iConomy.system.Account;
import com.iConomy.system.Accounts;
import com.iConomy.system.Bank;
import com.iConomy.system.Banks;
import com.iConomy.system.Interest;
import com.iConomy.system.Transactions;
import com.iConomy.util.Constants;
import com.iConomy.util.FileManager;
import com.iConomy.util.Messaging;
import com.iConomy.util.Misc;
import com.iConomy.util.VaultConnector;

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
	
    public static Banks Banks = null;
    public static Accounts Accounts = null;

    private static Server Server = null;
    private static Database Database = null;
    private static Transactions Transactions = null;
    
    private static Players playerListener = null;
    private static Timer Interest_Timer = null;

    public static iConomy instance = null;
    public static Economy economy = null;

    Logger log = getServer().getLogger();

    @Override
    public void onEnable() {
    	
        instance = this;
        Locale.setDefault(Locale.US);
        
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
        extract("Config.yml");
        extract("Template.yml");
        
        try {
            Constants.load(new File(getDataFolder(), "Config.yml"));
        } catch (Exception e) {
            Server.getPluginManager().disablePlugin(this);
            log.info("[iConomy] Failed to retrieve configuration from directory.");
            log.info("[iConomy] Please back up your current settings and let iConomy recreate it.");
            return;
        }

        // Register as a ServiceProvider and with Vault.
        if (!registerEconomy()) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Setup database and connections.
        try {
            Database = new Database();
            Database.setupAccountTable();

            if (Constants.Banking) {
                Database.setupBankTable();
                Database.setupBankRelationTable();
            }
        } catch (Exception e) {
            log.severe("[iConomy] Database initialization failed: " + e);
            Server.getPluginManager().disablePlugin(this);
            return;
        }

        // Transaction logger.
        try {
            Transactions = new Transactions();
            Database.setupTransactionTable();
        } catch (Exception e) {
            log.info("[iConomy] Could not load transaction logger: " + e);
        }

        // Check version details before the system loads
        update(file, Double.valueOf(pdfFile.getVersion().split("-")[0]).doubleValue());

        // Initialize default systems
        Accounts = new Accounts();

        // Initialize the banks
        if (Constants.Banking)
            Banks = new Banks();

        try {
            if (Constants.Interest) {
                long time = Constants.InterestSeconds * 1000L;

                Interest_Timer = new Timer();
                Interest_Timer.scheduleAtFixedRate(new Interest(getDataFolder().getPath()), time, time);
            }
        } catch (Exception e) {
            log.severe("[iConomy] Failed to start interest system: " + e);
            Server.getPluginManager().disablePlugin(this);
            return;
        }

        // Initializing Listeners
        playerListener = new Players(getDataFolder().getPath());

        // Event Registration
        getServer().getPluginManager().registerEvents(playerListener, this);

        // Console details.
        log.info("[iConomy] v" + pdfFile.getVersion() + " (" + Constants.Codename + ") loaded.");
        log.info("[iConomy] Developed by: " + pdfFile.getAuthors());
    }

    /**
     * Register as a ServiceProvider, and with Vault.
     * 
     * @return true if successful.
     */
    private boolean registerEconomy() {
    	
        if (Server.getPluginManager().isPluginEnabled("Vault")) {
            final ServicesManager sm = Server.getServicesManager();
            sm.register(Economy.class, new VaultConnector(this), this, ServicePriority.Highest);
            log.info("[iConomy] Registered Vault interface.");

            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            
            if (rsp != null) {
                economy = rsp.getProvider();
            }
            //log.info("[Vault] [Economy] " + economy.getName() + " found: Waiting.");
            return true;
        } else {
            PluginDescriptionFile pdfFile = getDescription();
            log.severe("[iConomy] Vault not found. Please download Vault to use iConomy " + pdfFile.getVersion().toString());
            return false;
        }
    }

    @Override
    public void onDisable() {
        try {
            if (Constants.isDatabaseTypeH2()) {
                Database.connectionPool().dispose();
            }

            log.info("[iConomy] Plugin disabled.");
        } catch (Exception e) {
            log.severe("[iConomy] Plugin disabled.");
        } finally {
            if (Interest_Timer != null) {
                Interest_Timer.cancel();
            }

            Server = null;
            Banks = null;
            Accounts = null;
            Database = null;
            Transactions = null;
            playerListener = null;
            Interest_Timer = null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String[] split = new String[args.length + 1];
        split[0] = cmd.getName().toLowerCase();
        System.arraycopy(args, 0, split, 1, args.length);
        boolean isPlayer = sender instanceof Player;
        
        switch (commandLabel.toLowerCase()) {
        
        case "bank":
        	if (!Constants.Banking) {
        		Messaging.send(sender, "`rBanking is disabled.");
        		return true;
        	}

        case "money":	// Allow bank to fall through to this case.
        	return playerListener.onPlayerCommand(sender, split);
        	
        case "icoimport":
        	if (!isPlayer && !importEssEco()) {
        		Messaging.send(sender, "`rImport failed.");
        	}
        	return true;
        }
        
        return false;
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
                        log.info("[iConomy] Import: Town account found: " + actualName);
                        name = townPrefix + actualName;
                        
                    } else if (name.startsWith(essNationPrefix)) {
                        actualName = name.substring(essNationPrefix.length());
                        log.info("[iConomy] Import: Nation account found: " + actualName);
                        name = nationPrefix + actualName;
                        
                    } else if (name.startsWith(essDebtPrefix)) {
                        actualName = name.substring(essDebtPrefix.length());
                        log.info("[iConomy] Import: Debt account found: " + actualName);
                        name = debtPrefix + actualName;
                    }
                }
            }
            
            try {
                if (money > 0) {
                    if (Accounts.exists(name)) {
                        if (Accounts.get(name).getHoldings().balance() == money) {
                            continue;
                        } else
                            Accounts.get(name).getHoldings().set(money);
                    } else {
                        Accounts.create(name);
                        Accounts.get(name).getHoldings().set(money);
                    }
                }
                
                if ((i > 0) && (i % 10 == 0)) {
                    log.info(i + " accounts read...");
                }
                i++;
                
            } catch (Exception e) {
                log.warning("[iConomy] Importer could not parse account for " + account.getName());
            }
        }

        log.info(i + " accounts loaded.");
        return true;
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
                            log.warning("[iConomy] Error updating database: " + ex.getMessage());
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
                log.warning("[iConomy] Error on version check: ");
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
                    log.warning("[iConomy] Error updating database: " + ex.getMessage());
                    
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

                    log.info("[iConomy] Default setup file written: " + name);
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
     * Formats the balance in a human readable form with the currency attached:<br /><br />
     * 20000.53 = 20,000.53 Coin<br />
     * 20000.00 = 20,000 Coin
     *
     * @param account The name of the account you wish to be formatted
     * @return String
     */
    public static String format(String bank, String account) {
        return new Bank(bank).getAccount(account).getHoldings().toString();
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
     * Grab the bank to modify and access bank accounts.
     *
     * @return Bank
     */
    public static Bank getBank(String name) {
        return Banks.get(name);
    }

    /**
     * Grab the bank to modify and access bank accounts.
     *
     * @return Bank
     */
    public static Bank getBank(int id) {
        return Banks.get(id);
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
        if (sender instanceof Player) {
            return ((Player) sender).hasPermission(node);
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
}
