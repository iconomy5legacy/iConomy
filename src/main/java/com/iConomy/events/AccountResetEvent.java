package com.iConomy.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;

public class AccountResetEvent extends Event {

	private final Holdings account;
	private boolean cancelled = false;
	private static final HandlerList handlers = new HandlerList();

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

		} else if (iConomy.instance.getServer().getScheduler().scheduleSyncDelayedTask(iConomy.instance, new Runnable() {

			@Override
			public void run() {
				reset(event);
			}
		}, 1) == -1)
			System.out.println("[iConomy] Could not schedule Account Reset Event.");
	}

	private void reset(AccountResetEvent event) {

		iConomy.instance.getServer().getPluginManager().callEvent(event);

		if (!event.isCancelled())
			account.set(Constants.Holdings);
	}
}
