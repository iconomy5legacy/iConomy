package com.iConomy.system;

import com.iConomy.events.AccountResetEvent;
import com.iConomy.events.AccountSetEvent;
import com.iConomy.events.AccountUpdateEvent;
import com.iConomy.iConomy;
import com.iConomy.util.Constants;
import com.iConomy.util.Misc;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

/**
 * Controls player Holdings, and Bank Account holdings.
 * 
 * @author Nijikokun
 */
public class Holdings {
	
    private String name = "";
    private Double balance = null;
    Logger log = iConomy.instance.getLogger();

    public Holdings(String name) {
        this.name = name;
    }
    
    /**
     * Holdings name.
     * 
     * @return name of this Holding
     */
    public String getName() {
    	return this.name;
    }

    /**
     * Get the balance for this Holding.
     * 
     * @return the balance.
     */
    public double balance() {
        if (balance == null)
            balance = get();
        return balance.doubleValue();
    }

    private synchronized double get() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Double balance = Double.valueOf(Constants.Holdings);
        try {
            conn = iConomy.getiCoDatabase().getConnection();

            ps = conn.prepareStatement("SELECT * FROM " + Constants.SQLTable + " WHERE username = ? LIMIT 1");
            ps.setString(1, this.name);

            rs = ps.executeQuery();

            if (rs.next())
                balance = Double.valueOf(rs.getDouble("balance"));
        } catch (Exception ex) {
            log.warning("Failed to grab holdings: " + ex);
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (rs != null)
                try { rs.close(); } catch (SQLException ex) {}
            
            if (conn != null)
                try { conn.close(); } catch (SQLException ex) {}
        }
        return balance.doubleValue();
    }

    public synchronized void set(double balance) {

		this.balance = balance;
		Bukkit.getPluginManager().callEvent(new AccountSetEvent(this, balance));

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = iConomy.getiCoDatabase().getConnection();

			ps = conn.prepareStatement("UPDATE " + Constants.SQLTable + " SET balance = ? WHERE username = ?");
			ps.setDouble(1, balance);
			ps.setString(2, this.name);

			ps.executeUpdate();

		} catch (Exception ex) {
			log.warning("Failed to set holdings: " + ex);
		} finally {
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException ex) {}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ex) {}
		}
    }

    public synchronized void add(double amount) {
        double balance = balance();
        double ending = balance + amount;

        callEventAndSetHoldings(amount, balance, ending);
    }

    public synchronized void subtract(double amount) {
        double balance = balance();
        double ending = balance - amount;

        callEventAndSetHoldings(amount, balance, ending);
    }

    public synchronized void divide(double amount) {
        double balance = balance();
        double ending = balance / amount;

        callEventAndSetHoldings(amount, balance, ending);
    }

    public synchronized void multiply(double amount) {
        double balance = balance();
        double ending = balance * amount;

        callEventAndSetHoldings(amount, balance, ending);
    }

	/**
	 * Reset Holdings to default, if the Event is not cancelled.
	 */
	public void reset() {
		Bukkit.getPluginManager().callEvent(new AccountResetEvent(this));
		set(Constants.Holdings);
	}

	private void callEventAndSetHoldings(double amount, double previousBalance, double newBalance) {
		Bukkit.getPluginManager().callEvent(new AccountUpdateEvent(this, previousBalance, newBalance, amount));
		set(newBalance);
	}

    /**
     * Is this balance negative?
     * 
     * @return true if negative.
     */
    public boolean isNegative() {
        return get() < 0.0D;
    }

    /**
     * Does this Holding have this amount or more?
     * 
     * @param amount the amount to test for.
     * @return true if the balance is sufficient.
     */
    public boolean hasEnough(double amount) {
        return amount <= get();
    }

    /**
     * Is the balance over the amount?
     * 
     * @param amount the amount to test for.
     * @return true if balance is higher.
     */
    public boolean hasOver(double amount) {
        return amount < get();
    }

    /**
     * Is the balance under the amount?
     * 
     * @param amount the amount to test for.
     * @return true if balance is lower.
     */
    public boolean hasUnder(double amount) {
        return amount > get();
    }

    public String toString() {
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        Double balance = Double.valueOf(get());
        String formatted = formatter.format(balance);

        if (formatted.endsWith(".")) {
            formatted = formatted.substring(0, formatted.length() - 1);
        }

        return Misc.formatted(formatted, Constants.Major, Constants.Minor);
    }
}
