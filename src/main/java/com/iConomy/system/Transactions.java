package com.iConomy.system;

import com.iConomy.iConomy;
import com.iConomy.util.Constants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Transactions {
	
    public void insert(String from, String to, double from_balance, double to_balance, double set, double gain, double loss) {
        if (!Constants.Logging) {
            return;
        }
        int i = 1;
        long timestamp = System.currentTimeMillis() / 1000L;

        Object[] data = { from, to, Double.valueOf(from_balance), Double.valueOf(to_balance), Long.valueOf(timestamp), Double.valueOf(set), Double.valueOf(gain), Double.valueOf(loss) };

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = iConomy.getiCoDatabase().getConnection();
            ps = conn.prepareStatement("INSERT INTO " + Constants.SQLTable + "_Transactions(account_from, account_to, account_from_balance, account_to_balance, `timestamp`, `set`, gain, loss) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

            for (Object obj : data) {
                ps.setObject(i, obj);
                i++;
            }

            ps.executeUpdate();
        } catch (SQLException ex) {} finally {
            if (ps != null)
                try { ps.close(); } catch (SQLException ex) {}
            
            if (rs != null)
                try { rs.close(); } catch (SQLException ex) {}
            
            iConomy.getiCoDatabase().close(conn);
        }
    }
}
