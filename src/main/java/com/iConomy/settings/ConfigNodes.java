package com.iConomy.settings;

public enum ConfigNodes {
	VERSION_HEADER("version", "", ""),
	VERSION(
			"version.version",
			"",
			"# This is the current version.  Please do not edit."),
	SYSTEM_ROOT(
			"system", "", ""),
	DEFAULT_ROOT(
			"system.default", "", ""),
	DEFAULT_CURRENCY(
			"system.default.currency", "", ""),
	DEFAULT_CURRENCY_MAJOR(
			"system.default.currency.major",
			"[ 'Dollar', 'Dollars' ]",
			"",
			"# Major ([Major].Minor) 1.00 Dollar (With Seperate 1 Dollar)"),
	DEFAULT_CURRENCY_MINOR(
			"system.default.currency.minor",
			"[ 'Coin', 'Coins' ]",
			"",
			"# Minor (Major.[Minor]) 0.23 Coins  (With Seperate 0 Dollars, 23 Coins)"),
	DEFAULT_ACCOUNT_ROOT(
			"system.default.account", "", ""),
	DEFAULT_ACCOUNT_BALANCE(
			"system.default.account.holdings",
			"30",
			"",
			"# Default holdings on hand upon join / creation."),
	FORMATTING_ROOT(
			"system.formatting", "", ""),
	FORMATTING_MINOR(
			"system.formatting.minor",
			"false",
			"",
			"# Example (true) 0.23 Coins and 1.23 Dollars (false) 0.23 Dollars and 1.23 Dollars"),
	FORMATTING_SEPARATE(
			"system.formatting.Seperate",
			"false",
			"",
			"# Example (true) 1 Dollar, 23 Coins (false) 1.23 Dollars (Only if Minor is true)"),
	LOGGING_ROOT(
			"system.logging", "", ""),
	LOGGING_ENABLED(
			"system.logging.enabled",
			"false",
			"",
			"# Logs transactions done inside iConomy only, other plugins must utilize the api. (Logs to SQL)"),
	INTEREST_ROOT(
			"system.interest", "", ""),
	INTEREST_ENABLED(
			"system.interest.enabled",
			"false", ""),
	INTEREST_ONLINE(
			"system.interest.Online",
			"true",
			"",
			"# Only give to players who are online?"),
	INTEREST_ANNOUNCE_ROOT(
			"system.interest.announce", "", ""),
	INTEREST_ANNOUNCE_ENABLED(
			"system.interest.announce.enabled",
			"false",
			"",
			"# Sends message each time a player receives interest."),
	INTEREST_INTERVAL_ROOT(
			"system.interest.interval", "", ""),
	INTEREST_INTERVAL_SECONDS(
			"system.interest.interval.seconds",
			"60",
			"",
			"# How many seconds in between interest being given:",
			"# 1 minute = 60 seconds",
			"# 1 hour = 1 minute * 60 = 3600 seconds",
			"# 1 day = 1 hour * 24 = 86400 seconds",
			"# 1 week = 1 day * 7 = 604800 seconds"),
	INTEREST_AMOUNT_ROOT(
			"system.interest.amount", "", ""),
	INTEREST_AMOUNT_CUTOFF(
			"system.interest.amount.cutoff",
			"0.0",
			"",
			"# Amount limit to be met until we stop giving interest. (0.0 for no limit)"),
	INTEREST_AMOUNT_PERCENT(
			"system.interest.amount.percent",
			"0.0",
			"",
			"# Percent of holdings to give / take (Negative to take) (Overrides Min/Max)"),
	INTEREST_AMOUNT_MAXIMUM(
			"system.interest.amount.maximum",
			"1",
			"",
			"# (Range) Maximum amount for random in between. (Make Max/Min equal for a flat-rate amount ie: 5/5)"),
	INTEREST_AMOUNT_MINIMUM(
			"system.interest.amount.minimum",
			"2",
			"",
			"# (Range) Minimum amount for random in between."),
	DATABASE_ROOT(
			"system.database", "", ""),
	DATABASE_TYPE(
			"system.database.type",
			"H2SQL",
			"",
			"# H2 or MySQL"),
	DATABASE_SETTINGS_ROOT(
			"system.database.settings", "", ""),
	DATABASE_SETTINGS_NAME(
			"system.database.settings.name",
			"minecraft",
			""),
	DATABASE_SETTINGS_TABLE(
			"system.database.settings.table",
			"iConomy",
			""),
	DATABASE_SETTINGS_MYSQL_ROOT(
			"system.database.settings.mysql", "", ""),
	DATABASE_SETTINGS_MYSQL_USERNAME(
			"system.database.settings.mysql.username",
			"root",
			""),
	DATABASE_SETTINGS_MYSQL_PASSWORD(
			"system.database.settings.mysql.password",
			"pass",
			""),
	DATABASE_SETTINGS_MYSQL_HOSTNAME(
			"system.database.settings.mysql.hostname",
			"localhost",
			""),
	DATABASE_SETTINGS_MYSQL_PORT(
			"system.database.settings.mysql.port",
			"3306",
			""),
	DATABASE_SETTINGS_MYSQL_FLAGS(
			"system.database.settings.mysql.flags",
			"?verifyServerCertificate=false&useSSL=false",
			"");
	
	private final String root;
	private final String def;
	private String[] comments;

	ConfigNodes(String root, String def, String... comments) {

		this.root = root;
		this.def = def;
		this.comments = comments;
	}

	/**
	 * Retrieves the root for a config option
	 *
	 * @return The root for a config option
	 */
	public String getRoot() {

		return root;
	}

	/**
	 * Retrieves the default value for a config path
	 *
	 * @return The default value for a config path
	 */
	public String getDefault() {

		return def;
	}

	/**
	 * Retrieves the comment for a config path
	 *
	 * @return The comments for a config path
	 */
	public String[] getComments() {

		if (comments != null) {
			return comments;
		}

		String[] comments = new String[1];
		comments[0] = "";
		return comments;
	}
}