/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.index.solr;

import cz.incad.vdkcr.server.datasources.AbstractPocessDataSource;
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
public class Reindex extends AbstractPocessDataSource {

    static final Logger logger = Logger.getLogger(Reindex.class.getName());

    Connection conn;
    SolrIndexer indexer;
    int total = 0;

    @Override
    public int harvest(String params, Record sklizen, Context ctx) throws Exception {
        try {
            connect();
            psReindex = conn.prepareStatement(sqlReindex);
            indexer = new SolrIndexer();
            getRecords();
            logger.log(Level.INFO, "REINDEX FINISHED. Total records: {0}", total);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error in reindex", ex);
        } finally {
            disconnect();
        }
        return total;

    }

    String sqlReindex = "select identifikator, uniqueCode, codeType, sourceXML from zaznam";
    PreparedStatement psReindex;

    private void getRecords() throws Exception {
        indexer.clean();
        logger.log(Level.INFO, "Getting records...");
        ResultSet rs = psReindex.executeQuery();
        while (rs.next()) {
            //logger.log(Level.INFO, rs.getString("sourceXML"));
            // check interrupted thread
            if (Thread.currentThread().isInterrupted()) {
                logger.log(Level.INFO, "REINDEX INTERRUPTED. Total records: {0}", total);
                throw new InterruptedException();
            }
            try {
                indexer.processXML(rs.getString("sourceXML"),
                        rs.getString("uniqueCode"),
                        rs.getString("codeType"),
                        rs.getString("identifikator"));
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error in record " + rs.getString("identifikator"), ex);
            }
            total++;

        }
        indexer.commit();
    }

    private void connect() throws ClassNotFoundException, SQLException {
        logger.fine("Connecting...");
        Persister persister = PersisterFactory.getPersister();
        conn = persister.getJDBCConnection();
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
