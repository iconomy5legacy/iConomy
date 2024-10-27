package com.iConomy.system;

import com.iConomy.ConversionAccount;
import com.iConomy.iConomy;
import com.iConomy.util.Constants;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.Resident;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;


/**
 * Manage Accounts.
 * 
 * @author Nijikokun
 */
public class Accounts {

    Logger log = iConomy.instance.getLogger();
	
	/**
	 * Check if an Account exists with this name.
	 * 
	 * @param name the name to check
	 * @return true if an Account exists.
	 */
    public boolean exists(String name) {
    	
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        boolean exists = false;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + Constants.SQLTable + " WHERE username = ? LIMIT 1");
            ps.setString(1, name);
            rs = ps.executeQuery();
            exists = rs.next();
        } catch (Exception ex) {
            exists = false;
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (rs != null)
                try { rs.close(); } catch (SQLException ex) {}
            
            if (conn != null)
                try { conn.close(); } catch (SQLException ex) {}
        }
        return exists;
    }

    /**
     * Create an Account.
     * 
     * @param name the Account name.
     * @return true if successful.
     */
    public boolean create(String name) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("INSERT INTO " + Constants.SQLTable + "(username, balance, hidden) VALUES (?, ?, 0)");
            ps.setString(1, name);
            ps.setDouble(2, Constants.Holdings);
            ps.executeUpdate();
        } catch (Exception e) {
            return false;
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (conn != null)
                try { conn.close(); } catch (SQLException ex) {}
        }
        return true;
    }

    /**
     * Remove the user Account with this name.
     * 
     * @param name the name of the Account (case sensitive)
     * @return true if successful.
     */
    public boolean remove(String name) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("DELETE FROM " + Constants.SQLTable + " WHERE username = ? LIMIT 1");
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (Exception e) {
            return false;
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (conn != null)
                try { conn.close(); } catch (SQLException ex) {}
        }
        return true;
    }

    /**
     * Remove ALL matching Accounts with this name.
     * 
     * @param name the name of the Account (case sensitive)
     * @return true if successful.
     */
    public boolean removeCompletely(String name) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("DELETE FROM " + Constants.SQLTable + " WHERE username = ? LIMIT 1");
            ps.setString(1, name);
            ps.executeUpdate();

            ps.close();

            ps = conn.prepareStatement("DELETE FROM " + Constants.SQLTable + " WHERE account_name = ?");
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (Exception e) {
            return false;
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (conn != null)
                try { conn.close(); } catch (SQLException ex) {}
        }
        return true;
    }

    /**
     * Delete all accounts with default holdings
     * 
     * @return true if successful.
     */
    public boolean purge() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("DELETE FROM " + Constants.SQLTable + " WHERE balance = ?");
            ps.setDouble(1, Constants.Holdings);
            ps.executeUpdate();
        } catch (Exception e) {
            return false;
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (conn != null)
                try { conn.close(); } catch (SQLException ex) {}
        }
        return true;
    }

    /**
     * Removes all accounts from the database.
     * ## Do not use this ##
     * 
     * @return true if successful.
     */
    public boolean emptyDatabase() {
        Connection conn = null;
        Statement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.createStatement();
            ps.execute("TRUNCATE TABLE " + Constants.SQLTable);
        } catch (Exception e) {
            return false;
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (conn != null)
                try { conn.close(); } catch (SQLException ex) {}
        }
        return true;
    }

    /**
     * Fetch a list of all Account balances.
     * 
     * @return a list of balances.
     */
    public List<Double> values() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<Double> Values = new ArrayList<Double>();
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT balance FROM " + Constants.SQLTable);
            rs = ps.executeQuery();

            while (rs.next())
                Values.add(Double.valueOf(rs.getDouble("balance")));
            
        } catch (Exception e) {
            return null;
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (conn != null)
                try { conn.close(); } catch (SQLException ex) {}
        }
        return Values;
    }

    /**
     * Fetch X top non-hidden account names with balances.
     * 
     * @param amount the number of accounts to return.
     * @return a map of top accounts.
     */
    public LinkedHashMap<String, Double> ranking(int amount) {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        LinkedHashMap<String, Double> Ranking = new LinkedHashMap<String, Double>();
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT username,balance FROM " + Constants.SQLTable + " WHERE hidden = 0 ORDER BY balance DESC LIMIT ?");
            ps.setInt(1, amount);
            rs = ps.executeQuery();

            while (rs.next())
                Ranking.put(rs.getString("username"), Double.valueOf(rs.getDouble("balance")));
        } catch (Exception e) {
            log.warning(e.getMessage());
            return null;
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (conn != null)
                try { conn.close(); } catch (SQLException ex) {}
        }
        return Ranking;
    }

    /**
     * Get an Account by name.
     * Creates one if it doesn't exist.
     * 
     * @param name the name of the Account.
     * @return an Account or null if unable.
     */
    public Account get(String name) {
        if (exists(name)) {
            return new Account(name);
        }
        if (!create(name)) {
            return null;
        }

        return new Account(name);
    }

    public Set<ConversionAccount> getAllAccountsForConversion() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Set<ConversionAccount> accounts = new HashSet<>();
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT username,balance,hidden FROM " + Constants.SQLTable);
            rs = ps.executeQuery();

            while (rs.next()) {
            	String name = rs.getString("username");
            	double balance = rs.getDouble("balance");
            	boolean hidden = rs.getBoolean("hidden");
            	UUID uuid = TownyEconomyHandler.getTownyObjectUUID(name);
            	if (uuid == null) {
            		// It could be a player.
            		Resident res = TownyAPI.getInstance().getResident(name);
            		if (res == null)
            			continue;
            		uuid = res.getUUID();
            		if (uuid == null)
            			continue;
            	}
            	accounts.add(new ConversionAccount(name, uuid.toString(), balance, hidden));
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
            return null;
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (conn != null)
                try { conn.close(); } catch (SQLException ex) {}
        }
        return accounts;
    }
}
