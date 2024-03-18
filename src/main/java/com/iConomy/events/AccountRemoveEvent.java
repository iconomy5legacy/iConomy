package com.iConomy.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.iConomy.iConomy;
import com.iConomy.util.Constants;

public class AccountRemoveEvent extends Event {

	private final String account;
	private boolean cancelled = false;
	private static final HandlerList handlers = new HandlerList();

	Logger log = iConomy.instance.getLogger();

	public AccountRemoveEvent(String account) {
		super(!Bukkit.isPrimaryThread());
		this.account = account;
	}

	public String getAccountName() {
		return this.account;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public void schedule(AccountRemoveEvent event) {

		if (Bukkit.isPrimaryThread()) {
			remove(event);

		} else {
			iConomy.instance.getScheduler().runLater(() -> remove(event), 1);
		}

	}

	private void remove(AccountRemoveEvent event) {

		iConomy.instance.getServer().getPluginManager().callEvent(event);

		if (!event.isCancelled()) {
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				conn = iConomy.getiCoDatabase().getConnection();
				ps = conn.prepareStatement("DELETE FROM " + Constants.SQLTable + " WHERE username = ?");
				ps.setString(1, event.getAccountName());
				ps.executeUpdate();
			} catch (Exception ex) {
				log.warning("Failed to remove account: " + ex);
			} finally {
				if (ps != null)
					try {
						ps.close();
					} catch (SQLException ex) {}
				if (conn != null)
					iConomy.getiCoDatabase().close(conn);
			}
		}
	}
}
