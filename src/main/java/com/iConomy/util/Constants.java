package com.iConomy.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Constants {
    public static final String Codename = "Towny Edition";
    public static File Configuration;
    public static String Plugin_Directory;
    public static String H2_Jar_Location = "http://palmergames.com/file-repo/iConomy/libs/h2.jar";
    public static String MySQL_Jar_Location = "http://palmergames.com/file-repo/iConomy/libs/mysql-connector-java-bin.jar";

    // iConomy basics
    public static List<String> Major = new LinkedList<String>();
    public static List<String> Minor = new LinkedList<String>();
    public static double Holdings = 30.0D;
    
    // iConomy Bank
    public static boolean Banking = false;
    public static boolean BankingMultiple = true;
    public static String BankName = "iConomy";
    public static List<String> BankMajor = new LinkedList<String>();
    public static List<String> BankMinor = new LinkedList<String>();
    public static double BankHoldings = 30.0D;
    public static double BankFee = 20.0D;

    // System formatting
    public static boolean FormatMinor = false;
    public static boolean FormatSeperated = false;

    // System Logging
    public static boolean Logging = false;

    public static int InterestSeconds = 60;
    public static boolean Interest = false;
    public static boolean InterestAnn = false;
    public static boolean InterestOnline = false;
    public static String InterestType = "Players";
    public static double InterestCutoff = 0.0D;
    public static double InterestPercentage = 0.0D;
    public static double InterestMin = 1.0D;
    public static double InterestMax = 2.0D;

    // Database Type
    public static String DatabaseType = "H2SQL";

    // Relational SQL Generics
    public static String SQLHostname = "localhost";
    public static String SQLPort = "3306";
    public static String SQLUsername = "root";
    public static String SQLPassword = "";
    public static boolean SQLUseSSL = false;

    // SQL Generics
    public static String SQLDatabase = "minecraft";
    public static String SQLTable = "iConomy";

    public static void load(File file) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);

            Major.add("Dollar"); Major.add("Dollars");
            Minor.add("Coin"); Minor.add("Coins");
            
            BankMajor.add("Dollar"); BankMajor.add("Dollars");
            BankMinor.add("Coin"); BankMinor.add("Coins");

            // System Configuration
            Major = config.getStringList("System.Default.Currency.Major");
            Minor = config.getStringList("System.Default.Currency.Minor");
            Holdings = config.getDouble("System.Default.Account.Holdings", Holdings);

            // System Bank
            Banking = config.getBoolean("System.Banking.Enabled", Banking);
            BankingMultiple = config.getBoolean("System.Banking.Accounts.Multiple", BankingMultiple);
            BankName = config.getString("System.Default.Bank.Name", BankName);
            BankMajor = config.getStringList("System.Default.Bank.Currency.Major");
            BankMinor = config.getStringList("System.Default.Bank.Currency.Minor");
            BankHoldings = config.getDouble("System.Default.Bank.Account.Holdings", BankHoldings);
            BankFee = config.getDouble("System.Default.Bank.Account.Fee", BankFee);

            // System Logging
            Logging = config.getBoolean("System.Logging.Enabled", Logging);

            // Formatting
            FormatMinor = config.getBoolean("System.Formatting.Minor", FormatMinor);
            FormatSeperated = config.getBoolean("System.Formatting.Seperate", FormatSeperated);

            // System Interest
            Interest = config.getBoolean("System.Interest.Enabled", Interest);
            InterestOnline = config.getBoolean("System.Interest.Online", InterestOnline);
            InterestType = config.getString("System.Interest.Amount.On", InterestType);
            InterestAnn = config.getBoolean("System.Interest.Announce.Enabled", InterestAnn);
            InterestSeconds = config.getInt("System.Interest.Interval.Seconds", InterestSeconds);
            InterestPercentage = config.getDouble("System.Interest.Amount.Percent", InterestPercentage);
            InterestCutoff = config.getDouble("System.Interest.Amount.Cutoff", InterestCutoff);
            InterestMin = config.getDouble("System.Interest.Amount.Minimum", InterestMin);
            InterestMax = config.getDouble("System.Interest.Amount.Maximum", InterestMax);

            // Database Configuration
            DatabaseType = config.getString("System.Database.Type", DatabaseType);

            // Generic
            SQLDatabase = config.getString("System.Database.Settings.Name", SQLDatabase);
            SQLTable = config.getString("System.Database.Settings.Table", SQLTable);

            // MySQL
            SQLHostname = config.getString("System.Database.Settings.MySQL.Hostname", SQLHostname);
            SQLPort = config.getString("System.Database.Settings.MySQL.Port", SQLPort);
            SQLUsername = config.getString("System.Database.Settings.MySQL.Username", SQLUsername);
            SQLPassword = config.getString("System.Database.Settings.MySQL.Password", SQLPassword);
            SQLUseSSL = config.getBoolean("System.Database.Settings.MySQL.UseSSL", false);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    private static String[] h2Types = new String[] { "sqlite", "h2", "h2sql", "h2db" };
    
    public static boolean isDatabaseTypeH2() {
        return Misc.is(Constants.DatabaseType, h2Types);
    }
}
