package com.iConomy.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;

public class AccountSetEvent extends Event {

	private final Holdings account;
	private double balance;
	private static final HandlerList handlers = new HandlerList();

	Logger log = iConomy.instance.getLogger();

	public AccountSetEvent(Holdings account, double balance) {
		super();
		this.account = account;
		this.balance = balance;
	}

	public String getAccountName() {
		return this.account.getName();
	}

	public Holdings getAccount() {
		return account;
	}

	public double getBalance() {
		return this.balance;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public void schedule(AccountSetEvent event) {

		if (Bukkit.isPrimaryThread()) {
			setBalance(event);

		} else if (iConomy.instance.getServer().getScheduler().scheduleSyncDelayedTask(iConomy.instance, new Runnable() {

			@Override
			public void run() {
				setBalance(event);
			}
		}, 1) == -1)
			iConomy.instance.getServer().getLogger().warning("Could not schedule Account Set Event.");
	}

	private void setBalance(AccountSetEvent event) {

		iConomy.instance.getServer().getPluginManager().callEvent(event);

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = iConomy.getiCoDatabase().getConnection();

			if (event.getAccount().getBankId() == 0) {
				ps = conn.prepareStatement("UPDATE " + Constants.SQLTable + " SET balance = ? WHERE username = ?");
				ps.setDouble(1, balance);
				ps.setString(2, event.getAccountName());
			} else {
				ps = conn.prepareStatement("UPDATE " + Constants.SQLTable + "_BankRelations SET holdings = ? WHERE account_name = ? AND bank_id = ?");
				ps.setDouble(1, balance);
				ps.setString(2, event.getAccountName());
				ps.setInt(3, event.getAccount().getBankId());
			}

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
}
