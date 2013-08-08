package cz.incad.vdkcr.server.datasources.rdcz;

/**
 *
 * @author Alberto Hernandez
 */
import com.fastsearch.esp.content.DocumentFactory;
import com.fastsearch.esp.content.IDocument;
import com.typesafe.config.Config;
import cz.incad.vdkcr.server.Structure;
import cz.incad.vdkcr.server.datasources.DataSource;
import cz.incad.vdkcr.server.fast.FastIndexer;
import cz.incad.vdkcr.server.index.IndexTypes;
import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.server.Context;
import org.aplikator.server.util.Configurator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.aplikator.server.data.RecordUtils.newRecord;
import static org.aplikator.server.data.RecordUtils.newSubrecord;

public class Indexer implements DataSource {

    static final Logger logger = Logger.getLogger(Indexer.class.getName());
    private FastIndexer fastIndexer;
    private ProgramArguments arguments;
    private Connection conn;
    public Configuration conf;
    public String query;
    public ArrayList<ConfigQuery> update_queries = new ArrayList<ConfigQuery>();
    public ArrayList<ConfigQuery> delete_queries = new ArrayList<ConfigQuery>();
    private String id_field;
    DocumentBuilderFactory domFactory;
    DocumentBuilder builder;
    Context context;
    Record sklizen;
    int processed = 0;
    String homeDir;
    FileHandler logFileHandler;

    @Override
    public int harvest(String params, org.aplikator.client.shared.data.Record sklizen, Context ctx) {
        context = ctx;

        this.sklizen = sklizen;
        arguments = new ProgramArguments();
        if (!arguments.parse(params.split(" "))) {
            System.out.println("Program arguments are invalid");
        }

        conf = new Configuration(arguments.configFile);
        Config config = Configurator.get().getConfig();
        this.homeDir = Configurator.get().getConfig().getString(Configurator.HOME)
                + File.separator;
        try {
            File dir = new File(this.homeDir + "logs");
            if (!dir.exists()) {
                boolean success = dir.mkdirs();
                if (!success) {
                    logger.log(Level.WARNING, "Can''t create logs directory");
                }
            }
            logFileHandler = new FileHandler(this.homeDir + "logs" + File.separator + arguments.configFile + ".log");
            logFileHandler.setFormatter(new SimpleFormatter());

            Logger.getLogger("cz.incad.vdkcr.server").addHandler(logFileHandler);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        fastIndexer = new FastIndexer();
        fastIndexer.config(config);
        id_field = conf.getProperty("id_field");

        domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false);
        try {
            builder = domFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
        }
        run();
        return processed;

    }

    public void run() {

        long startTime = (new Date()).getTime();
        try {
            connect();
            Date date = new Date();
            DateFormat formatter = new SimpleDateFormat(conf.getProperty("dateFormat"));
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            String to = formatter.format(date);
            logger.log(Level.INFO, "Current index time: {0}", to);

            String from = "";
            String updateTimeFile = conf.getProperty("updateTimeFile");
            File dateFile = new File(updateTimeFile);
            if (arguments.from == null) {
                if ((new File(updateTimeFile)).exists()) {
                    BufferedReader in = new BufferedReader(new FileReader(updateTimeFile));
                    from = in.readLine();
                } else {
                    from = "1900-01-01";
                }
            } else {
                from = arguments.from;
            }

            if (arguments.fullIndex) {
                logger.info("full index from db...");
                fullIndex();
            } else {
                update(from, to);
            }

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateFile)));
            out.write(to);
            out.close();
            logger.info("Index success");


            long timeInMiliseconds = (new Date()).getTime() - startTime;
            logger.info(formatElapsedTime(timeInMiliseconds));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error", ex);
        } finally {
            disconnect();
        }
    }

    private void fullIndex() throws Exception {
        getRecords();
    }

    private void getRecords() throws Exception {
        prepare();
        ResultSet rs = prepStatment.executeQuery();
        while (rs.next()) {
            processRecord(rs, IndexTypes.INSERTED);
        }
        rs.close();
        fastIndexer.finished();
    }

    private void update(String from, String to) throws Exception {
        getUpdatedRecords(from, to);
        getDeletedRecords(from, to);
        fastIndexer.finished();
    }

    private void getUpdatedRecords(String from, String to) throws Exception {
        for (ConfigQuery update_query : update_queries) {
            logger.info(update_query.description);
            prepareUpdate(from, update_query.query);
            ResultSet rs = prepStatmentUpdate.executeQuery();
            while (rs.next()) {
                processRecord(rs, IndexTypes.MODIFIED);
            }
            rs.close();
        }
    }
    PreparedStatement prepStatment;
    String finalQuery;

    public void prepare() throws SQLException {
        finalQuery = conf.getProperty("query");
        if (arguments.maxDocuments > 0) {
            if (finalQuery.toUpperCase().indexOf("WHERE") > 0) {
                finalQuery += " AND ";
            } else {
                finalQuery += " WHERE ";
            }
            finalQuery += " rownum<=" + arguments.maxDocuments;
        }
        finalQuery += " ORDER BY " + id_field;
        prepStatment = conn.prepareStatement(finalQuery);
    }
    PreparedStatement prepStatmentUpdate;
    String finalQueryUpdate;

    public void prepareUpdate(String from, String update_query) throws SQLException {
        String fieldNames = id_field;
        for (FieldMapping fm : conf.fieldMappings) {
            fieldNames += ", " + fm.source;
        }
        finalQueryUpdate = update_query.replace("#fields#", fieldNames).replace("#from#", from);

        prepStatmentUpdate = conn.prepareStatement(finalQueryUpdate);

    }

    private void getDeletedRecords(String from, String to) throws Exception {
        for (ConfigQuery delete_query : delete_queries) {
            logger.info(delete_query.description);
            prepareUpdate(from, delete_query.query);
            ResultSet rs = prepStatmentUpdate.executeQuery();
            while (rs.next()) {
//                Record fr = new Record(rs.getString(1), IndexTypes.DELETED);
//                vdkcr.addRecord(fr);
            }
            rs.close();
        }
    }

    private void connect() throws ClassNotFoundException, SQLException {
        logger.fine("Connecting...");
        Class.forName(conf.getProperty("dbDriver"));

        conn = DriverManager.getConnection(conf.getProperty("dbUrl"),
                conf.getProperty("dbUser"),
                conf.getProperty("dbPwd"));
        logger.info("Spojeno");
    }

    public void disconnect() {
        try {
            conn.close();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Cant disconnect", ex);
        }

    }

    private String formatElapsedTime(long timeInMiliseconds) {



        long hours, minutes, seconds;
        long timeInSeconds = timeInMiliseconds / 1000;
        hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hours * 3600);
        minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (minutes * 60);
        seconds = timeInSeconds;
        return hours + " hour(s) " + minutes + " minute(s) " + seconds + " second(s)";
    }

    private void processRecord(ResultSet rs, IndexTypes indexTypes) throws Exception {


        RecordContainer rc = new RecordContainer();
        Record fr = newRecord(Structure.zaznam);

        if (indexTypes != IndexTypes.DELETED) {
            Structure.zaznam.sklizen.setValue(fr, sklizen.getPrimaryKey().getId());
            String urlZdroje = "RDCZ:" + rs.getString("ID");
            Structure.zaznam.urlZdroje.setValue(fr, urlZdroje);
            String hlavninazev = rs.getString("nazev");
            Structure.zaznam.hlavniNazev.setValue(fr, hlavninazev);
            String typDokumentu = rs.getString("druhdokumentu");
            Structure.zaznam.typDokumentu.setValue(fr, typDokumentu);
            String xmlStr = getClob(rs.getClob("xml"));
            Structure.zaznam.sourceXML.setValue(fr, xmlStr);
            rc.addRecord(null, fr, fr, Operation.CREATE);


            //Identifikatory
            Record ISSN = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.identifikator);
            Structure.identifikator.hodnota.setValue(ISSN, rs.getString("ISSN"));
            Structure.identifikator.typ.setValue(ISSN, "ISSN");
            rc.addRecord(null, ISSN, ISSN, Operation.CREATE);

            Record ISBN = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.identifikator);
            Structure.identifikator.hodnota.setValue(ISBN, rs.getString("ISBN"));
            Structure.identifikator.typ.setValue(ISBN, "ISBN");
            rc.addRecord(null, ISBN, ISBN, Operation.CREATE);

            Record cnb = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.identifikator);
            String cnbStr = rs.getString("ccnb");
            Structure.identifikator.hodnota.setValue(cnb, cnbStr);
            Structure.identifikator.typ.setValue(cnb, "cCNB");
            rc.addRecord(null, cnb, cnb, Operation.CREATE);

            //Autori
            Record autor = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.autor);
            Structure.autor.nazev.setValue(autor, rs.getString("autor"));
            rc.addRecord(null, autor, autor, Operation.CREATE);


            //Nazvy
            Record j = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.nazev);
            Structure.nazev.nazev.setValue(j, rs.getString("podnazev"));
            Structure.nazev.typNazvu.setValue(j, "Podnázev");
            rc.addRecord(null, j, j, Operation.CREATE);

            //Exemplar
            Record exemplar = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.exemplar);
            Structure.exemplar.carovyKod.setValue(exemplar, rs.getString("carkod"));
            Structure.exemplar.signatura.setValue(exemplar, rs.getString("signatura"));
            Structure.exemplar.rok.setValue(exemplar, rs.getString("rokVyd"));
            rc.addRecord(null, exemplar, exemplar, Operation.CREATE);

            Structure.sklizen.pocet.setValue(sklizen, processed++);
            rc.addRecord(null, sklizen, sklizen, Operation.UPDATE);

            rc = context.getAplikatorService().processRecords(rc);
            try {

                Record z = rc.getRecords().get(0).getEdited();
                IDocument doc = DocumentFactory.newDocument(urlZdroje);
                addFastElement(doc, "title", hlavninazev);
                doc.addElement(DocumentFactory.newInteger("dbid", z.getPrimaryKey().getId()));
                doc.addElement(DocumentFactory.newString("url", urlZdroje));
                addFastElement(doc, "druhdokumentu", typDokumentu);

                addFastElement(doc, "autor", rs.getString("autor"));
                addFastElement(doc, "isxn", rs.getString("ISSN"));
                addFastElement(doc, "isxn", rs.getString("ISBN"));
                addFastElement(doc, "ccnb", rs.getString("ccnb"));
                addFastElement(doc, "zdroj", conf.getProperty("zdroj"));
                addFastElement(doc, "base", conf.getProperty("base"));
                addFastElement(doc, "harvester", conf.getProperty("harvester"));
                addFastElement(doc, "originformat", conf.getProperty("originformat"));
                addFastElement(doc, "data", xmlStr);
                fastIndexer.add(doc, indexTypes);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Cant procces record  " + urlZdroje, ex);
            }

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
}
