package com.iConomy.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;


public class VaultConnector implements Economy {
	private iConomy plugin;
	private String name;
	
	public VaultConnector(iConomy plugin) {
		this.plugin = plugin;
		//Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(), plugin);
	}
	
	@Override
	public boolean isEnabled() {
		return plugin != null && plugin.isEnabled();
	}

	@Override
	public String getName() {
		name = "iConomy " + plugin.getDescription().getVersion();
		return name;
	}
	
	@Override
	public boolean createPlayerAccount(String playerName) {

		if (hasAccount(playerName)) {
            return false;
        }
        iConomy.getAccount(playerName);
        return true;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer player) {

		if (hasAccount(player.getName())) {
            return false;
        }
        iConomy.getAccount(player.getName());
        return true;
	}

	@Override
	public boolean createPlayerAccount(String playerName, String arg1) {

		if (hasAccount(playerName)) {
            return false;
        }
        iConomy.getAccount(playerName);
        return true;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer player, String arg1) {

		if (hasAccount(player.getName())) {
            return false;
        }
        iConomy.getAccount(player.getName());
        return true;
	}

	@Override
	public String currencyNamePlural() {

		try {
            return Constants.Major.get(1);
        } catch (Exception e) {
            return "";
        }
	}

	@Override
	public String currencyNameSingular() {

		try {
            return Constants.Major.get(0);
        } catch (Exception e) {
            return "";
        }
	}
	
	@Override
	public EconomyResponse depositPlayer(String playerName, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Holdings holdings = iConomy.getAccount(playerName).getHoldings();
        holdings.add(amount);
        balance = getBalance(playerName);
        type = EconomyResponse.ResponseType.SUCCESS;

        iConomy.getTransactions().insert("[Vault]", playerName, 0.0D, balance, 0.0D, amount, 0.0D);

        return new EconomyResponse(amount, balance, type, errorMessage);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Holdings holdings = iConomy.getAccount(player.getName()).getHoldings();
        holdings.add(amount);
        balance = getBalance(player.getName());
        type = EconomyResponse.ResponseType.SUCCESS;

        iConomy.getTransactions().insert("[Vault]", player.getName(), 0.0D, balance, 0.0D, amount, 0.0D);

        return new EconomyResponse(amount, balance, type, errorMessage);
	}

	@Override
	public EconomyResponse depositPlayer(String playerName, String world, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Holdings holdings = iConomy.getAccount(playerName).getHoldings();
        holdings.add(amount);
        balance = getBalance(playerName);
        type = EconomyResponse.ResponseType.SUCCESS;

        iConomy.getTransactions().insert("[Vault]", playerName, 0.0D, balance, 0.0D, amount, 0.0D);

        return new EconomyResponse(amount, balance, type, errorMessage);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer player, String world, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Holdings holdings = iConomy.getAccount(player.getName()).getHoldings();
        holdings.add(amount);
        balance = getBalance(player.getName());
        type = EconomyResponse.ResponseType.SUCCESS;

        iConomy.getTransactions().insert("[Vault]", player.getName(), 0.0D, balance, 0.0D, amount, 0.0D);

        return new EconomyResponse(amount, balance, type, errorMessage);
	}

	@Override
	public String format(double amount) {

		 return iConomy.format(amount);
	}

	@Override
	public int fractionalDigits() {

		return 0;
	}

	@Override
	public double getBalance(String playerName) {

		return iConomy.getAccount(playerName).getHoldings().balance();
	}

	@Override
	public double getBalance(OfflinePlayer player) {		
		return iConomy.getAccount(player.getName()).getHoldings().balance();
	}

	@Override
	public double getBalance(String playerName, String world) {

		return iConomy.getAccount(playerName).getHoldings().balance();
	}

	@Override
	public double getBalance(OfflinePlayer player, String world) {

		return iConomy.getAccount(player.getName()).getHoldings().balance();
	}

	@Override
	public List<String> getBanks() {

		return new ArrayList<String>();
	}

	@Override
	public boolean has(String playerName, double amount) {
		
        return getBalance(playerName) >= amount;
        
    }

	@Override
	public boolean has(OfflinePlayer player, double amount) {
		
        return getBalance(player.getName()) >= amount;
        
    }

	@Override
	public boolean has(String playerName, String world, double amount) {

		return getBalance(playerName) >= amount;
	}

	@Override
	public boolean has(OfflinePlayer player, String world, double amount) {

		return getBalance(player.getName()) >= amount;
	}

	@Override
	public boolean hasAccount(String playerName) {

		return iConomy.hasAccount(playerName);
	}

	@Override
	public boolean hasAccount(OfflinePlayer player) {

		return iConomy.hasAccount(player.getName());
	}

	@Override
	public boolean hasAccount(String playerName, String worldName) {

		return iConomy.hasAccount(playerName);
	}

	@Override
	public boolean hasAccount(OfflinePlayer player, String worldName) {
		return iConomy.hasAccount(player.getName());
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Holdings holdings = iConomy.getAccount(playerName).getHoldings();
        if (holdings.hasEnough(amount)) {
            holdings.subtract(amount);
            balance = getBalance(playerName);
            type = EconomyResponse.ResponseType.SUCCESS;

            iConomy.getTransactions().insert("[Vault]", playerName, 0.0D, balance, 0.0D, 0.0D, amount);

            return new EconomyResponse(amount, balance, type, errorMessage);
        } else {
            amount = 0;
            balance = getBalance(playerName);
            type = EconomyResponse.ResponseType.FAILURE;
            errorMessage = "Insufficient funds";
            return new EconomyResponse(amount, balance, type, errorMessage);
        }
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Holdings holdings = iConomy.getAccount(player.getName()).getHoldings();
        if (holdings.hasEnough(amount)) {
            holdings.subtract(amount);
            balance = getBalance(player.getName());
            type = EconomyResponse.ResponseType.SUCCESS;

            iConomy.getTransactions().insert("[Vault]", player.getName(), 0.0D, balance, 0.0D, 0.0D, amount);

            return new EconomyResponse(amount, balance, type, errorMessage);
        } else {
            amount = 0;
            balance = getBalance(player.getName());
            type = EconomyResponse.ResponseType.FAILURE;
            errorMessage = "Insufficient funds";
            return new EconomyResponse(amount, balance, type, errorMessage);
        }
	}

	@Override
	public EconomyResponse withdrawPlayer(String playerName, String world, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Holdings holdings = iConomy.getAccount(playerName).getHoldings();
        if (holdings.hasEnough(amount)) {
            holdings.subtract(amount);
            balance = getBalance(playerName);
            type = EconomyResponse.ResponseType.SUCCESS;

            iConomy.getTransactions().insert("[Vault]", playerName, 0.0D, balance, 0.0D, 0.0D, amount);

            return new EconomyResponse(amount, balance, type, errorMessage);
        } else {
            amount = 0;
            balance = getBalance(playerName);
            type = EconomyResponse.ResponseType.FAILURE;
            errorMessage = "Insufficient funds";
            return new EconomyResponse(amount, balance, type, errorMessage);
        }
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer player, String world, double amount) {

		double balance;
        EconomyResponse.ResponseType type;
        String errorMessage = null;

        Holdings holdings = iConomy.getAccount(player.getName()).getHoldings();
        if (holdings.hasEnough(amount)) {
            holdings.subtract(amount);
            balance = getBalance(player.getName());
            type = EconomyResponse.ResponseType.SUCCESS;

            iConomy.getTransactions().insert("[Vault]", player.getName(), 0.0D, balance, 0.0D, 0.0D, amount);

            return new EconomyResponse(amount, balance, type, errorMessage);
        } else {
            amount = 0;
            balance = getBalance(player.getName());
            type = EconomyResponse.ResponseType.FAILURE;
            errorMessage = "Insufficient funds";
            return new EconomyResponse(amount, balance, type, errorMessage);
        }
	}

	@Override
    public EconomyResponse bankBalance(String arg0) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");		
	}

	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}

	@Override
	public EconomyResponse bankHas(String arg0, double arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}

	@Override
	public EconomyResponse bankWithdraw(String arg0, double arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}

	@Override
	public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single account banks!");
    }

	@Override
	public EconomyResponse createBank(String arg0, OfflinePlayer arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single account banks!");
	}
	
	@Override
	public EconomyResponse isBankMember(String arg0, String arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}

	@Override
	public EconomyResponse isBankMember(String arg0, OfflinePlayer arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, String arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, OfflinePlayer arg1) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
	}
	
	@Override
	public EconomyResponse deleteBank(String arg0) {

		return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support bank accounts!");
	}
}
