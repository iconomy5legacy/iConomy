package com.iConomy.net;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.iConomy.iConomy;
import org.h2.jdbcx.JdbcConnectionPool;

import com.iConomy.util.Constants;
import com.iConomy.util.Downloader;

public class Database {
    private JdbcConnectionPool h2pool;
    private String driver;
    private String dsn;
    private String username;
    private String password;

    Logger log = iConomy.instance.getLogger();

    public Database() throws Exception {
    	
        if (Constants.isDatabaseTypeH2()) {
            this.driver = "org.h2.Driver";
            this.dsn = "jdbc:h2:" + Constants.Plugin_Directory + File.separator + Constants.SQLDatabase + ";AUTO_RECONNECT=TRUE";
            this.username = "sa";
            this.password = "sa";
            
        } else if (Constants.DatabaseType.equalsIgnoreCase("mysql")) {
            this.driver = "com.mysql.jdbc.Driver";
            this.dsn = "jdbc:mysql://" + Constants.SQLHostname + ":" + Constants.SQLPort + "/" + Constants.SQLDatabase + Constants.SQLFlags;
            this.username = Constants.SQLUsername;
            this.password = Constants.SQLPassword;
            
        } else {
            throw new Exception("Invalid database type!");
        }

        // Check we have a driver.
        try {
            Class.forName(this.driver);
            
        } catch (Exception e) {
            log.warning("Driver error: " + e);
            
            // Create Lib Directory
            new File("lib" + File.separator).mkdir();
            new File("lib" + File.separator).setWritable(true);
            new File("lib" + File.separator).setExecutable(true);
            
            // Download dependencies as we are running an old version.
            Downloader down = new Downloader();
            if (Constants.isDatabaseTypeH2()) {
                if (!new File("lib" + File.separator, "h2.jar").exists()) {
                    down.install(Constants.H2_Jar_Location, "h2.jar");
                }
            } else if (!new File("lib" + File.separator, "mysql-connector-java-bin.jar").exists()) {
                down.install(Constants.MySQL_Jar_Location, "mysql-connector-java-bin.jar");
            }
        }

        if (Constants.isDatabaseTypeH2() && this.h2pool == null)
            this.h2pool = JdbcConnectionPool.create(this.dsn, this.username, this.password);
    }

    public Connection getConnection() {
    	
        try {
            if (this.username.equalsIgnoreCase("") && this.password.equalsIgnoreCase("")) {
                return DriverManager.getConnection(this.dsn);
            }
            if (Constants.isDatabaseTypeH2()) {
                return this.h2pool.getConnection();
            }
            return DriverManager.getConnection(this.dsn, this.username, this.password);
        } catch (SQLException e) {
            log.severe("Could not create connection: " + e);
        }
        return null;
    }

    public void close(Connection connection) {
    	
        if (connection != null)
            try { connection.close(); } catch (SQLException ex) {}
    }

    /**
     * Create the accounts table if it doesn't exist already.
     * 
     * @throws Exception
     */
    public void setupAccountTable() throws Exception {
    	
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        if (Constants.isDatabaseTypeH2()) {
            try {
            	ps = conn.prepareStatement(
            			"CREATE TABLE " + Constants.SQLTable + "(" 
            					+ "id INT auto_increment PRIMARY KEY," 
            					+ "username VARCHAR(32) UNIQUE," 
            					+ "balance DECIMAL (64, 2)," 
            					+ "hidden BOOLEAN DEFAULT '0'" 
            					+ ");"
            			);

                ps.executeUpdate();
            } catch (SQLException E) {}
        } else {
            DatabaseMetaData dbm = conn.getMetaData();
            rs = dbm.getTables(null, null, Constants.SQLTable, null);

            if (!rs.next()) {
                log.info("Creating table: " + Constants.SQLTable);

                ps = conn.prepareStatement(
                		"CREATE TABLE " + Constants.SQLTable + " (" 
                				+ "`id` INT(10) NOT NULL AUTO_INCREMENT," 
                				+ "`username` VARCHAR(32) NOT NULL," 
                				+ "`balance` DECIMAL(64, 2) NOT NULL," 
                				+ "`hidden` BOOLEAN NOT NULL DEFAULT '0'," 
                				+ "PRIMARY KEY (`id`)," 
                				+ "UNIQUE(`username`)" 
                				+ ")"
                		);

                if (ps != null) {
                    ps.executeUpdate();
                }

                log.info("Table Created.");
            }
        }

        if (ps != null)
            try { ps.close(); } catch (SQLException ex) {}
        
        if (rs != null)
            try { rs.close(); } catch (SQLException ex) {}
        
        if (conn != null)
            try { conn.close(); } catch (SQLException ex) {}
    }

    public void setupTransactionTable() throws Exception {
    	
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        if (Constants.Logging) {
            if (Constants.isDatabaseTypeH2()) {
                try {
                	ps = conn.prepareStatement(
                			"CREATE TABLE " + Constants.SQLTable + "_Transactions(" 
                					+ "id INT AUTO_INCREMENT PRIMARY KEY, " 
                					+ "account_from TEXT, " 
                					+ "account_to TEXT, " 
                					+ "account_from_balance DECIMAL(64, 2), " 
                					+ "account_to_balance DECIMAL(64, 2), " 
                					+ "timestamp TEXT, " 
                					+ "set DECIMAL(64, 2), " 
                					+ "gain DECIMAL(64, 2), " 
                					+ "loss DECIMAL(64, 2)" 
                					+ ");"
                			);

                    ps.executeUpdate();
                } catch (SQLException E) {}
            } else {
                DatabaseMetaData dbm = conn.getMetaData();
                rs = dbm.getTables(null, null, Constants.SQLTable + "_Transactions", null);

                if (!rs.next()) {
                    log.info("Creating logging database.. [" + Constants.SQLTable + "_Transactions]");
                    ps = conn.prepareStatement(
                    		"CREATE TABLE " + Constants.SQLTable + "_Transactions (" 
                    				+ "`id` INT(255) NOT NULL AUTO_INCREMENT, " 
                    				+ "`account_from` TEXT NOT NULL, " 
                    				+ "`account_to` TEXT NOT NULL, " 
                    				+ "`account_from_balance` DECIMAL(65, 2) NOT NULL, " 
                    				+ "`account_to_balance` DECIMAL(65, 2) NOT NULL, " 
                    				+ "`timestamp` TEXT NOT NULL, " 
                    				+ "`set` DECIMAL(65, 2) NOT NULL, " 
                    				+ "`gain` DECIMAL(65, 2) NOT NULL, " 
                    				+ "`loss` DECIMAL(65, 2) NOT NULL, " 
                    				+ "PRIMARY KEY (`id`)" 
                    				+ ");"
                    		);

                    if (ps != null) {
                        ps.executeUpdate();
                        log.info("Database Created.");
                    }
                }
                log.info("Logging enabled.");
            }
        } else
            log.info("Logging is currently disabled.");

        if (ps != null)
            try { ps.close(); } catch (SQLException ex) {}
        
        if (rs != null)
            try { rs.close(); } catch (SQLException ex) {}
        
        if (conn != null)
            try { conn.close(); } catch (SQLException ex) {}
    }

    public JdbcConnectionPool connectionPool() {
        return this.h2pool;
    }
}
