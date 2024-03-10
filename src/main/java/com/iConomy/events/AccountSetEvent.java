package com.iConomy.events;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;

public class AccountSetEvent extends Event {

	private final Holdings account;
	private double balance;
	private static final HandlerList handlers = new HandlerList();

	Logger log = iConomy.instance.getLogger();

	public AccountSetEvent(Holdings account, double balance) {
		super(!Bukkit.isPrimaryThread());
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
}
