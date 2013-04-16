package cz.incad.vdkcr.server.functions;

import com.fastsearch.esp.content.DocumentFactory;
import com.fastsearch.esp.content.IDocument;
import java.util.logging.Logger;

import org.aplikator.server.Context;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;
import org.aplikator.server.util.Configurator;

import com.typesafe.config.Config;

import cz.incad.vdkcr.server.fast.FastIndexer;
import cz.incad.vdkcr.server.fast.IndexTypes;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.aplikator.server.persistence.Persister;
import org.aplikator.server.persistence.PersisterFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class ReindexFast implements Executable {

    Logger logger = Logger.getLogger(ReindexFast.class.getName());
    private FastIndexer fastIndexer;
    Connection conn;
    DocumentBuilderFactory domFactory;

    @Override
    public FunctionResult execute(FunctionParameters parameters, Context context) {
        Config config = Configurator.get().getConfig();
        fastIndexer = new FastIndexer(config.getString("aplikator.fastHost"),
                config.getString("aplikator.fastCollection"),
                config.getInt("aplikator.fastBatchSize"));

        domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false);
        try {
            builder = domFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        if (getRecords()) {

            return new FunctionResult("Reindexovano", true);
        } else {
            return new FunctionResult("Reindexace se nepovedla", false);
        }
    }
    
    public static void main(String[] args){
        ReindexFast ri = new ReindexFast();
        ri.execute(null, null);
    }

    private void connect() throws ClassNotFoundException, SQLException {
        logger.fine("Connecting...");
        Persister persister = PersisterFactory.getPersister();
        conn = persister.getJDBCConnection();
    }

    private void disconnect() {
        try {
            conn.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Cant disconnect", ex);
        }

    }

    private void addFastElement(IDocument doc, String name, String value) {
        try {
            if (value != null) {
                doc.addElement(DocumentFactory.newString(name, value));
            }
        } catch (Exception ex) {
            logger.log(Level.FINE, "Cant add element  " + name, ex);
        }
    }
    String sqlIdentifikator = "select * from identifikator where zaznam=?";
    PreparedStatement psId;

    private void getIdentifikator(int zaznam_id, IDocument doc) {
        try {
            psId.setInt(1, zaznam_id);
            ResultSet rs = psId.executeQuery();
            while (rs.next()) {
                String typ = rs.getString("typ");
                if(typ.equals("cCNB")){
                    addFastElement(doc, "generic1", rs.getString("hodnota"));
                    addFastElement(doc, "igeneric1", rs.getString("hodnota"));
                }else if(typ.equals("ISBN") || typ.equals("ISSN")){
                    addFastElement(doc, "isbn", rs.getString("hodnota"));
                    addFastElement(doc, "igeneric2", rs.getString("hodnota"));
                }
                //addFastElement(doc, rs.getString("typ").toLowerCase(), rs.getString("hodnota"));
//                addFastElement(doc, "isxn", rs.getString("ISSN"));
//                addFastElement(doc, "isxn", rs.getString("ISBN"));
//                addFastElement(doc, "ccnb", rs.getString("ccnb"));
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Cant get identifikator for zaznam_id " + zaznam_id, ex);
        }

    }
    String sqlAutori = "select * from autor where zaznam=?";
    PreparedStatement psAutori;

    private void getAutori(int zaznam_id, IDocument doc) {
        try {
            psAutori.setInt(1, zaznam_id);
            ResultSet rs = psAutori.executeQuery();
            String autori = "";
            while (rs.next()) {
                autori += rs.getString("nazev") + ";";
            }
            addFastElement(doc, "autor", autori);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Cant get autori for zaznam_id " + zaznam_id, ex);
        }

    }
    
    
    String sqlZdroj = "select nazev, typZdroje, formatxml from sklizen, zdroj where sklizen.ZDROJ=zdroj.ZDROJ_ID and sklizen.SKLIZEN_ID=?";
    PreparedStatement psZdroj;

    private void getZdroj(int sklizen_id, IDocument doc) {
        try {
            psZdroj.setInt(1, sklizen_id);
            ResultSet rs = psZdroj.executeQuery();
            while (rs.next()) {
                addFastElement(doc, "zdroj", rs.getString("nazev"));
                addFastElement(doc, "base", rs.getString("nazev"));
                addFastElement(doc, "harvester", rs.getString("typZdroje"));
                addFastElement(doc, "originformat", rs.getString("formatxml"));
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Cant get zdroj for sklizen_id " + sklizen_id, ex);
        }

    }
    DocumentBuilder builder;
    private String getClob(Clob data) {
        if (data != null) {
            Reader reader = null;
            try {
                StringBuilder sb = new StringBuilder();
                reader = data.getCharacterStream();
                BufferedReader br = new BufferedReader(reader);
                String line;
                while (null != (line = br.readLine())) {
                    sb.append(line);
                }
                br.close();
                if (!sb.toString().equals("")) {
                    try {

                        InputSource source = new InputSource(new StringReader(sb.toString()));
                        @SuppressWarnings("unused")
                        Document doc = builder.parse(source);

                        return sb.toString();
                    } catch (Exception ex) {
                        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record></record>";
                    }
                } else {

                    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record></record>";
                }
            } catch (Exception ex) {
                return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record></record>";
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record></record>";
                }
            }
        } else {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record></record>";
        }

    }

    private boolean getRecords() {
        try {
            connect();
            String sqlZaznam = "select ZAZNAM_ID, hlavninazev, url, typdokumentu, SKLIZEN from zaznam";
            PreparedStatement ps = conn.prepareStatement(sqlZaznam);
            psId = conn.prepareStatement(sqlIdentifikator);
            psAutori = conn.prepareStatement(sqlAutori);
            psZdroj = conn.prepareStatement(sqlZdroj);
            ResultSet rs = ps.executeQuery();
            int zaznam_id;
            while (rs.next()) {
                zaznam_id = rs.getInt("ZAZNAM_ID");
                IDocument doc = DocumentFactory.newDocument(rs.getString("url"));
                addFastElement(doc, "title", rs.getString("hlavninazev"));
                doc.addElement(DocumentFactory.newInteger("dbid", zaznam_id));
                doc.addElement(DocumentFactory.newString("url", rs.getString("url")));
                addFastElement(doc, "druhdokumentu", rs.getString("typdokumentu"));

                getIdentifikator(zaznam_id, doc);
                getAutori(zaznam_id, doc);
                getZdroj(rs.getInt("SKLIZEN"), doc);
                
                //String xmlStr = getClob(rs.getClob("sourcexml"));
                String xmlStr = "<record />";
                addFastElement(doc, "data", xmlStr);
                fastIndexer.add(doc, IndexTypes.INSERTED);
            }
            return true;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        } finally {
            disconnect();
        }
    }
}
