/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.vdkcr.server.functions;

import cz.incad.vdkcr.server.datasources.AbstractPocessDataSource;
import cz.incad.vdkcr.server.datasources.util.XMLReader;
import cz.incad.vdkcr.server.utils.MD5;
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
public class RegenerateMD5Process extends AbstractPocessDataSource {

    static final Logger logger = Logger.getLogger(RegenerateMD5Process.class.getName());

    XMLReader xmlReader;
    Connection conn;
    int total = 0;

    String usql = "update zaznam set uniqueCode=?, codeType=? where zaznam_id=?";
    String sql = "select zaznam_id, sourceXML from zaznam where codeType = 'md5'";
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
            logger.log(Level.INFO, "REGENERATE MD5 CODE FINISHED. Total records: {0}", total);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error generating MD5 codes", ex);
        } finally {
            disconnect();
        }
        return total;

    }
    
    private void updateZaznam(String code, String codeType, int id) throws SQLException{
        ups.setString(1, code);
        ups.setString(2, codeType);
        ups.setInt(3, id);
        ups.executeUpdate();
        logger.log(Level.INFO, "Record {0} updated", id);
    }

    private void getRecords() throws Exception {
        logger.log(Level.INFO, "Getting records...");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            //logger.log(Level.INFO, rs.getString("sourceXML"));
            // check interrupted thread
            if (Thread.currentThread().isInterrupted()) {
                logger.log(Level.INFO, "REGENERATE MD5 CODE INTERRUPTED. Total records: {0}", total);
                throw new InterruptedException();
            }
            try {
                int id = rs.getInt("zaznam_id");
                xmlReader.loadXml(rs.getString("sourceXML"));
                String cnbStr = xmlReader.getNodeValue("./oai:metadata/marc:record/marc:datafield[@tag='015']/marc:subfield[@code='a']/text()");
                String uniqueCode;
                    String codeType;
                    if ("".equals(cnbStr)) {

                        uniqueCode = MD5.generate(new String[]{
                            xmlReader.getNodeValue("./oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='a']/text()"),
                            xmlReader.getNodeValue("./oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='b']/text()"),
                            xmlReader.getNodeValue("./oai:metadata/marc:record/marc:datafield[@tag='700']/marc:subfield[@code='a']/text()"),
                            xmlReader.getNodeValue("./oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='a']/text()"),
                            xmlReader.getNodeValue("./oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='b']/text()"),
                            xmlReader.getNodeValue("./oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='c']/text()")
                        });
                        codeType = "md5";
                    } else {
                        uniqueCode = MD5.generate(new String[]{cnbStr});
                        codeType = "ccnb";
                    }
                    updateZaznam(uniqueCode, codeType, id);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error in record " + rs.getString("identifikator"), ex);
            }
            total++;

        }
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
