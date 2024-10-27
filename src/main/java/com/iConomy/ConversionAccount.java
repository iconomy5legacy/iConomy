package com.iConomy;

public class ConversionAccount {
	final String name;
	final String uuid;
	final double balance;
	final boolean hidden;

	public ConversionAccount(String name, String uuid, double balance, boolean hidden) {
		this.name = name;
		this.uuid = uuid;
		this.balance = balance;
		this.hidden = hidden;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @return the balance
	 */
	public double getBalance() {
		return balance;
	}

	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}
}
