package com.iConomy.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;

import java.util.logging.Logger;

public class AccountResetEvent extends Event {

	private final Holdings account;
	private boolean cancelled = false;
	private static final HandlerList handlers = new HandlerList();

	Logger log = iConomy.instance.getLogger();

	public AccountResetEvent(Holdings account) {
		super();
		this.account = account;
	}

	public String getAccountName() {
		return this.account.getName();
	}

	public Holdings getAccount() {
		return account;
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

	public void schedule(AccountResetEvent event) {

		if (Bukkit.isPrimaryThread()) {
			reset(event);

		} else {
			iConomy.instance.getScheduler().runLater(() -> reset(event), 1);
		}
	}

	private void reset(AccountResetEvent event) {

		iConomy.instance.getServer().getPluginManager().callEvent(event);

		if (!event.isCancelled())
			account.set(Constants.Holdings);
	}
}
