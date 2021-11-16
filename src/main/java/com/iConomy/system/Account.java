package com.iConomy.system;

import com.iConomy.events.AccountRemoveEvent;
import com.iConomy.iConomy;
import com.iConomy.util.Constants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Account {
    private String name;

    public Account(String name) {
        this.name = name;
    }

    Logger log = iConomy.instance.getLogger();

    /**
     * Get the id of this Account.
     * 
     * @return id
     */
    public int getId() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        int id = -1;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + Constants.SQLTable + " WHERE username = ? LIMIT 1");
            ps.setString(1, this.name);
            rs = ps.executeQuery();

            if (rs.next())
                id = rs.getInt("id");
        } catch (Exception ex) {
            id = -1;
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (rs != null)
                try { rs.close(); } catch (SQLException ex) {}
            
            if (conn != null)
                try { conn.close();  } catch (SQLException ex) {}
        }
        return id;
    }

    /**
     * Get this Account name.
     * 
     * @return the name of this Account.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get teh Holdings of this Account.
     * @return
     */
    public Holdings getHoldings() {
        return new Holdings(0, this.name);
    }

    public boolean createBankAccount(int bankID) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("INSERT INTO " + Constants.SQLTable + "_BankRelations (account_name, bank_id, holdings) VALUES (?, ?, ?)");
            ps.setString(1, this.name);
            ps.setInt(2, bankID);
            ps.setDouble(2, iConomy.getBank(bankID).getInitialHoldings());
            ps.executeUpdate();
        } catch (Exception e) {
            return false;
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ex) {}
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ex) {}
        }
        return true;
    }

    public boolean createBankAccount(String bank) {
        Connection conn = null;
        PreparedStatement ps = null;

        int bankID = iConomy.getBank(bank).getId();
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("INSERT INTO " + Constants.SQLTable + "_BankRelations (account_name, bank_id, holdings) VALUES (?, ?, ?)");
            ps.setString(1, this.name);
            ps.setInt(2, bankID);
            ps.setDouble(2, iConomy.getBank(bankID).getInitialHoldings());
            ps.executeUpdate();
        } catch (Exception e) {
            return false;
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ex) {}
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ex) {}
        }
        return true;
    }

    public ArrayList<Bank> withBanks() {
        if (!Constants.Banking) {
            return null;
        }
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        ArrayList<Bank> banks = new ArrayList<Bank>();
        try {
            conn = iConomy.getiCoDatabase().getConnection();

            ps = conn.prepareStatement("SELECT * FROM " + Constants.SQLTable + "_BankRelations WHERE account_name = ?");
            ps.setString(1, this.name);
            rs = ps.executeQuery();

            while (rs.next()) {
                Bank bank = new Bank(rs.getInt("bank_id"));
                banks.add(bank);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ex) {}
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ex) {}
        }
        return banks;
    }

    public void setMainBank(String name) {
        Bank bank = iConomy.Banks.get(name);
        int id = iConomy.Banks.get(name).getId();

        if (bank.hasAccount(this.name))
            setMainBank(id);
    }

    public void setMainBank(int id) {
        if (!Constants.Banking) {
            return;
        }
        if (!iConomy.Banks.get(id).hasAccount(this.name)) {
            return;
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();

            ps = conn.prepareStatement("UPDATE " + Constants.SQLTable + "_BankRelations SET main = 0 WHERE account_name = ? AND main = 1");
            ps.setString(1, this.name);
            ps.executeUpdate();
            
            ps.close();

            ps = conn.prepareStatement("UPDATE " + Constants.SQLTable + "_BankRelations SET main = 1 WHERE account_name = ? AND bank_id = ?");
            ps.setString(1, this.name);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception ex) {
            log.info(ex.getMessage());
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ex) {}
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ex) {}
        }
    }

    public Bank getMainBank() {
        if (!Constants.Banking) {
            return null;
        }
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Bank bank = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();

            ps = conn.prepareStatement("SELECT * FROM " + Constants.SQLTable + "_BankRelations WHERE account_name = ? AND main = 1 LIMIT 1");
            ps.setString(1, this.name);
            rs = ps.executeQuery();

            if (rs.next())
                bank = new Bank(rs.getInt("bank_id"));
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ex) {}
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ex) {}
        }
        return bank;
    }

    public BankAccount getMainBankAccount() {
        if (!Constants.Banking) {
            return null;
        }
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        BankAccount account = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();

            ps = conn.prepareStatement("SELECT * FROM " + Constants.SQLTable + "_BankRelations WHERE account_name = ? AND main = 1 LIMIT 1");
            ps.setString(1, this.name);
            rs = ps.executeQuery();

            if (rs.next()) {
                Bank bank = new Bank(rs.getInt("bank_id"));
                account = new BankAccount(bank.getName(), rs.getInt("bank_id"), this.name);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ex) {}
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ex) {}
        }
        return account;
    }

    public ArrayList<BankAccount> getBankAccounts() {
        if (!Constants.Banking) {
            return null;
        }
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        ArrayList<BankAccount> banks = new ArrayList<BankAccount>();
        try {
            conn = iConomy.getiCoDatabase().getConnection();

            ps = conn.prepareStatement("SELECT * FROM " + Constants.SQLTable + "_BankRelations WHERE account_name = ?");
            ps.setString(1, this.name);
            rs = ps.executeQuery();

            while (rs.next()) {
                Bank bank = new Bank(rs.getInt("bank_id"));
                banks.add(new BankAccount(bank.getName(), rs.getInt("bank_id"), this.name));
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ex) {}
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ex) {}
        }
        return banks;
    }

    public Holdings getBankHoldings(int id) {
        if (!Constants.Banking) {
            return null;
        }
        if (id == 0) {
            return new Holdings(id, this.name);
        }
        return new Holdings(id, this.name, true);
    }

    /**
     * Get the Hidden state of this Account.
     * 
     * @return true if hidden.
     */
    public boolean isHidden() {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT hidden FROM " + Constants.SQLTable + " WHERE username = ? LIMIT 1");
            ps.setString(1, this.name);
            rs = ps.executeQuery();

            if (rs != null && rs.next()) {
                boolean bool = rs.getBoolean("hidden");
                return bool;
            }
        } catch (Exception ex) {
            log.warning("Failed to check status: " + ex);
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (rs != null)
                try { rs.close(); } catch (SQLException ex) {}
            
            if (conn != null) {
                iConomy.getiCoDatabase().close(conn);
            }
        }
        return false;
    }

    /**
     * Set the Hidden flag on this account.
     * 
     * @param hidden	the hidden state to set.
     * @return true if successful
     */
    public boolean setHidden(boolean hidden) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();

            ps = conn.prepareStatement("UPDATE " + Constants.SQLTable + " SET hidden = ? WHERE username = ?");
            ps.setBoolean(1, hidden);
            ps.setString(2, this.name);

            ps.executeUpdate();
        } catch (Exception ex) {
            log.warning("Failed to update status: " + ex);
            return false;
        } finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (conn != null) {
                iConomy.getiCoDatabase().close(conn);
            }
        }
        return true;
    }

    /**
     * Returns the ranking number of an account
     *
     * @param name
     * @return Integer
     */
    public int getRank() {
        int i = 1;

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + Constants.SQLTable + " WHERE hidden = 0 ORDER BY balance DESC");
            rs = ps.executeQuery();

            while (rs.next()) {
                if (rs.getString("username").equalsIgnoreCase(this.name)) {
                    return i;
                }
                i++;
            }
        } catch (Exception ex) {} finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ex) {}
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException ex) {}
            iConomy.getiCoDatabase().close(conn);
        }

        return -1;
    }

    /**
     * Remove this account.
     */
    public void remove() {
    	
        AccountRemoveEvent event = new AccountRemoveEvent(this.name);
        event.schedule(event);
    }
}
