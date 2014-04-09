/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.utils;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * JDBC Template pattern. Useful for SQL querying. <br>
 * Typical usecase:
 * <pre>
 *  List<Integer> ids = new JDBCQueryTemplate(connection){
 *      public boolean handleRow(ResultSet rs, List<Integer> returnsList) throws SQLException {
 *          returnsList.add(rs.get("id"));
 *          // should processing continue
 *          return true;
 *      }
 *  }.executeQuery("select id from sometable where name=? and surname=?","karlos","dakos");
 *  .... 
 * </pre>
 * 
 * @author pavels
 */
public class JDBCQueryTemplate<T> {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(JDBCQueryTemplate.class.getName());
    
    private Connection connection;
    private boolean closeConnection = true;
    
    
    public JDBCQueryTemplate(Connection connection) {
        super();
        this.connection = connection;
    }

    public JDBCQueryTemplate(Connection connection, boolean closeConnection) {
        super();
        this.connection = connection;
        this.closeConnection = closeConnection;
    }


    /**
     * Execute query 
     * @param sql Query
     * @param params Query parameters
     * @return
     */
    public List<T> executeQuery(String sql, Object... params) {

        List<T> result = new ArrayList<T>();
        PreparedStatement pstm = null;
        ResultSet rs=null;
        try {
            pstm = connection.prepareStatement(sql);
            for (int i = 0, index = 1; i < params.length; i++) {
                int changedIndex = setParam(index, params[i], pstm);
                index = changedIndex + 1;
            }
            rs= pstm.executeQuery();
            while(rs.next()) {
                if(!handleRow(rs, result)) break;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            if (pstm != null ) {
                try {
                    pstm.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (closeConnection && connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
        return result;
    }
    
    private int setParam(int i, Object object, PreparedStatement pstm) throws SQLException {
        if (object instanceof String) {
            pstm.setString(i, (String) object);
            return i;
        } else if (object instanceof Integer) {
            pstm.setInt(i, (Integer) object);
            return i;
        } else if (object instanceof Timestamp) {
            pstm.setTimestamp(i, (java.sql.Timestamp) object);
            return i;
        } else if (object instanceof Long) {
            pstm.setLong(i, (Long) object);
            return i;
        } else if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            for (int j = 0; j < length; j++) {
                setParam(i+j, Array.get(object, j), pstm);
            }
            return i+(length-1);
        } else throw new IllegalArgumentException("unsupported type of argument "+object.getClass().getName());
        
    }

    public boolean handleRow(ResultSet rs, List<T> returnsList) throws SQLException {return true;}

}
