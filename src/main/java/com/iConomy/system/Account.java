package com.iConomy.system;

import com.iConomy.events.AccountRemoveEvent;
import com.iConomy.iConomy;
import com.iConomy.util.Constants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        return new Holdings(this.name);
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
