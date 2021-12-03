package com.iConomy.system;

import com.iConomy.iConomy;
import com.iConomy.util.Constants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Banks {
    Logger log = iConomy.instance.getLogger();

    public boolean exists(int id) {
        if (!Constants.Banking) {
            return false;
        }

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        boolean exists = false;

        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + Constants.SQLTable + "_Banks WHERE id = ? LIMIT 1");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            exists = rs.next();
        } catch (Exception ex) {
            exists = false;
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ex) {}
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException ex) {}
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ex) {}
        }

        return exists;
    }

    public boolean exists(String name) {
        if (!Constants.Banking) {
            return false;
        }

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        boolean exists = false;

        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + Constants.SQLTable + "_Banks WHERE name = ? LIMIT 1");
            ps.setString(1, name);
            rs = ps.executeQuery();
            exists = rs.next();
        } catch (Exception ex) {
            exists = false;
        } finally {
            if (ps != null)
                try {
                    ps.close();
                } catch (SQLException ex) {}
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException ex) {}
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException ex) {}
        }

        return exists;
    }

    /*
     * private int getId(String name) { if (!Constants.Banking) { return -1; }
     * Connection conn = null; ResultSet rs = null; PreparedStatement ps = null;
     * int id = 0; try { conn = iConomy.getiCoDatabase().getConnection(); ps =
     * conn.prepareStatement("SELECT id FROM " + Constants.SQLTable +
     * "_Banks WHERE name = ? LIMIT 1"); ps.setString(1, name); rs =
     * ps.executeQuery(); if (rs.next()) id = rs.getInt("id"); } catch
     * (Exception ex) { id = 0; } finally { if (ps != null) try { ps.close(); }
     * catch (SQLException ex) { } if (rs != null) try { rs.close(); } catch
     * (SQLException ex) { } if (conn != null) try { conn.close(); } catch
     * (SQLException ex) { } } return id; }
     */

    public Bank create(String name, String major, String minor, double initial, double fee) {
        if (!Constants.Banking) {
            return null;
        }

        if (!exists(name)) {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = iConomy.getiCoDatabase().getConnection();
                ps = conn.prepareStatement("INSERT INTO " + Constants.SQLTable + "_Banks(name, major, minor, initial, fee) VALUES (?, ?, ?, ?, ?)");

                ps.setString(1, name);
                ps.setString(2, Constants.Major.get(0) + "," + Constants.Major.get(1));
                ps.setString(3, Constants.Minor.get(0) + "," + Constants.Minor.get(1));
                ps.setDouble(4, initial);
                ps.setDouble(5, fee);

                ps.executeUpdate();
            } catch (Exception ex) {
                log.warning("Failed to set holdings balance: " + ex);
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

        return new Bank(name);
    }

    public Bank create(String name) {
        if (!Constants.Banking) {
            return null;
        }

        if (!exists(name)) {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = iConomy.getiCoDatabase().getConnection();
                ps = conn.prepareStatement("INSERT INTO " + Constants.SQLTable + "_Banks(name, major, minor, initial, fee) VALUES (?, ?, ?, ?, ?)");

                ps.setString(1, name);
                ps.setString(2, Constants.BankMajor.get(0) + "," + Constants.BankMajor.get(1));
                ps.setString(3, Constants.BankMinor.get(0) + "," + Constants.BankMinor.get(1));
                ps.setDouble(4, Constants.BankHoldings);
                ps.setDouble(5, Constants.BankFee);

                ps.executeUpdate();
            } catch (Exception ex) {
                log.warning("Failed to set holdings balance: " + ex);
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

        return new Bank(name);
    }

    public int count() {
        if (!Constants.Banking) {
            return -1;
        }

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        int count = -1;

        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT COUNT(id) AS count FROM " + Constants.SQLTable + "_Banks");
            rs = ps.executeQuery();

            if (rs.next())
                count = rs.getInt("count");
        } catch (Exception e) {
            return count;
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

        return count;
    }

    public int count(String name) {
        if (!Constants.Banking) {
            return -1;
        }

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        int count = -1;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT COUNT(id) AS count FROM " + Constants.SQLTable + "_BankRelations WHERE account_name = ?");
            ps.setString(1, name);
            rs = ps.executeQuery();

            if (rs.next())
                count = rs.getInt("count");
        } catch (Exception e) {
            return count;
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

        return count;
    }

    public boolean purge() {
        if (!Constants.Banking) {
            return false;
        }

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT * FROM " + Constants.SQLTable + "_Banks");
            rs = ps.executeQuery();
            ps.close();

            ps = conn.prepareStatement("DELETE FROM " + Constants.SQLTable + "_BankRelations WHERE bank_id = ? AND holdings = ?");

            while (rs.next()) {
                ps.setInt(1, rs.getInt("id"));
                ps.setDouble(2, rs.getDouble("initial"));
                ps.addBatch();
            }

            ps.executeBatch();
            conn.commit();
            ps.clearBatch();
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

    public boolean purge(String name) {
        if (!Constants.Banking) {
            return false;
        }

        Bank bank = iConomy.getBank(name);

        if (bank != null) {
            return purge(bank.getId());
        }

        return false;
    }

    public boolean purge(int id) {
        if (!Constants.Banking) {
            return false;
        }

        Bank bank = iConomy.getBank(id);

        if (bank != null) {
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = iConomy.getiCoDatabase().getConnection();
                ps = conn.prepareStatement("DELETE FROM " + Constants.SQLTable + "_BankRelations WHERE bank_id = ? AND holdings = ?");
                ps.setInt(1, id);
                ps.setDouble(2, bank.getInitialHoldings());
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

        return false;
    }

    public Bank get(String name) {
        if (!Constants.Banking) {
            return null;
        }

        if (exists(name)) {
            return new Bank(name);
        }

        return null;
    }

    public Bank get(int id) {
        if (!Constants.Banking) {
            return null;
        }

        if (exists(id)) {
            return new Bank(id);
        }

        return null;
    }

    public List<Double> values() {
        if (!Constants.Banking) {
            return null;
        }

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<Double> Values = new ArrayList<Double>();
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("SELECT holdings FROM " + Constants.SQLTable + "_BankRelations");
            rs = ps.executeQuery();

            while (rs.next())
                Values.add(Double.valueOf(rs.getDouble("holdings")));
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

        return Values;
    }
}
