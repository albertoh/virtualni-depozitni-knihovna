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

    String sqlZaznamy = "select zaznam_id, identifikator, uniqueCode, codeType, sourceXML from zaznam";
    PreparedStatement psZaznamy;

    String sqlNabidky = "select knihovna, offer from NABIDKY where zaznam=?";
    PreparedStatement psNabidky;

    @Override
    public int harvest(String params, Record sklizen, Context ctx) throws Exception {
        try {
            connect();
            psZaznamy = conn.prepareStatement(sqlZaznamy);
            psNabidky = conn.prepareStatement(sqlNabidky);
            indexer = new SolrIndexer();
            indexZaznamy();
            logger.log(Level.INFO, "REINDEX FINISHED. Total records: {0}", total);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error in reindex", ex);
        } finally {
            disconnect();
        }
        return total;

    }
    
//    private void indexNabidky() throws Exception{
//        String sql = "select NABIDKY.*, ZAZNAM.codeType from NABIDKY";
//        PreparedStatement ps;
//        ResultSet rs = ps.executeQuery();
//        StringBuilder sb = new StringBuilder();
//        while (rs.next()) {
//        sb.append("<add><doc>");
//            sb.append("<field name=\"code\">")
//                    .append(docCode)
//                    .append("</field>");
//            sb.append("<field name=\"md5\">")
//                    .append(docCode)
//                    .append("</field>");
//            sb.append("<field name=\"code_type\">")
//                    .append(codeType)
//                    .append("</field>");
//            sb.append("<field name=\"nabidka\" update=\"add\">")
//                    .append(rs.getInt("offer"))
//                    .append("</field>");
//            sb.append("</doc></add>");
//        }
//    }

    private void indexNabidky( String id, String docCode, String codeType) throws Exception {
        
        psNabidky.setString(1, id);
        ResultSet rs = psNabidky.executeQuery();

        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        boolean hasNabidky = false;
        
        while (rs.next()) {
            hasNabidky = true;
            sb1.append("<field name=\"nabidka\" update=\"add\">")
                    .append(rs.getInt("offer"))
                    .append("</field>");
        }
        if(hasNabidky){
            sb.append("<add><doc>");
            sb.append("<field name=\"code\">")
                    .append(docCode)
                    .append("</field>");
            sb.append("<field name=\"md5\">")
                    .append(docCode)
                    .append("</field>");
            sb.append("<field name=\"code_type\">")
                    .append(codeType)
                    .append("</field>");
            sb.append(sb1.toString());
            sb.append("</doc></add>");
            //logger.log(Level.INFO, "Indexace nabidky {0}...", sb.toString());
            indexer.sendXML(sb.toString());
            //indexer.commit();
        }
    }
    
    private void indexZaznamy() throws Exception {
        indexer.clean();
        logger.log(Level.INFO, "Indexace zaznamu...");
        ResultSet rs = psZaznamy.executeQuery();
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
                
                //indexer.commit();
                indexNabidky(rs.getString("uniqueCode"), rs.getString("uniqueCode"), rs.getString("codeType"));
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
