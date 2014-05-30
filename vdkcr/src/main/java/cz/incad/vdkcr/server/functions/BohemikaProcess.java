/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.functions;

import cz.incad.vdkcr.server.datasources.AbstractPocessDataSource;
import cz.incad.vdkcommon.xml.XMLReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.Persister;
import org.aplikator.server.persistence.PersisterFactory;

/**
 *
 * @author alberto
 */
public class BohemikaProcess extends AbstractPocessDataSource {

    static final Logger logger = Logger.getLogger(BohemikaProcess.class.getName());

    XMLReader xmlReader;
    Connection conn;
    int total = 0;

    String usql = "update zaznam set bohemika=? where zaznam_id=?";
    String sql = "select zaznam_id, sourceXML from zaznam";
    PreparedStatement ps;
    PreparedStatement ups;

    @Override
    public int harvest(String params, Record sklizen, Context ctx) throws Exception {
        try {
            connect();
            xmlReader = new XMLReader();
            ps = conn.prepareStatement(sql);
            ups = conn.prepareStatement(usql);
            getRecords();
            logger.log(Level.INFO, "GENERATE BOHEMIKA FINISHED. Total records: {0}", total);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error generating bohemika", ex);
        } finally {
            disconnect();
        }
        return total;

    }

    private void updateZaznam(boolean isBohemika, int id) throws SQLException {
        ups.setBoolean(1, isBohemika);
        ups.setInt(2, id);
        int n = ups.executeUpdate();
        logger.log(Level.INFO, "updated {3} records. {0} updated with bohemika={1}. Total: {2}", new Object[]{id, isBohemika, total, n});
    }

    private void getRecords() throws Exception {
        logger.log(Level.INFO, "Getting records...");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            
            //logger.log(Level.INFO, rs.getString("sourceXML"));
            // check interrupted thread
            if (Thread.currentThread().isInterrupted()) {
                logger.log(Level.INFO, "GENERATE BOHEMIKA INTERRUPTED. Total records: {0}", total);
                throw new InterruptedException();
            }
            int id = 0;
            try {
                id = rs.getInt("zaznam_id");
                boolean isBohemika = Bohemika.isBohemika(rs.getString("sourceXML"));
                updateZaznam(isBohemika, id);
            } catch (SQLException ex) {
                logger.log(Level.WARNING, "Error in record " + id, ex);
            }
            total++;

        }
        conn.commit();
        rs.close();
    }

    private void connect() throws ClassNotFoundException, SQLException {
        logger.fine("Connecting...");
        Persister persister = PersisterFactory.getPersister();
        conn = persister.getJDBCConnection();
        //conn.setAutoCommit(true);
    }

    private void disconnect() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Cant disconnect", ex);
        }

    }
}
