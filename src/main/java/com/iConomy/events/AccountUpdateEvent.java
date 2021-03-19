package com.iConomy.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;

public class AccountUpdateEvent extends Event {

	private final Holdings account;
	private double balance;
	private double previous;
	private double amount;
	private boolean cancelled = false;
	private static final HandlerList handlers = new HandlerList();

	public AccountUpdateEvent(Holdings account, double previous, double balance, double amount) {
		super(!Bukkit.isPrimaryThread());
		this.account = account;
		this.previous = previous;
		this.balance = balance;
		this.amount = amount;
	}

	public String getAccountName() {
		return this.account.getName();
	}

	public Holdings getAccount() {
		return account;
	}

	public double getAmount() {
		return this.amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
		this.balance = this.previous + amount;
	}

	public double getPrevious() {
		return this.previous;
	}

	public double getBalance() {
		return this.balance;
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

	public void schedule(AccountUpdateEvent event) {
		
		update(event);
	}

	private void update(AccountUpdateEvent event) {

		iConomy.instance.getServer().getPluginManager().callEvent(event);

		if (!event.isCancelled())
			account.set(event.getBalance());
	}
}
