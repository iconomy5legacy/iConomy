package com.iConomy.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;

import java.util.logging.Logger;

public class AccountUpdateEvent extends Event {

	private final Holdings account;
	private double balance;
	private double previous;
	private double amount;
	private static final HandlerList handlers = new HandlerList();

	Logger log = iConomy.instance.getLogger();

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

	public double getPrevious() {
		return this.previous;
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

	/**
	 * @deprecated with no replacement.
	 */
	@Deprecated
	public void setAmount(double amount) {}

	/**
	 * @deprecated with no replacement.
	 * @return false;
	 */
	@Deprecated
	public boolean isCancelled() {return false;}

	/**
	 * @deprecated with no replacement.
	 */
	@Deprecated
	public void setCancelled(boolean cancelled) {}
}
