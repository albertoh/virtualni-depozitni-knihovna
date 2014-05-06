package cz.incad.vdkcr.server.datasources.oai;

import com.typesafe.config.Config;
import cz.incad.vdkcr.server.Structure;
import cz.incad.vdkcr.server.data.SklizenStatus;
import cz.incad.vdkcr.server.datasources.AbstractPocessDataSource;
import cz.incad.vdkcr.server.datasources.util.XMLReader;
import cz.incad.vdkcr.server.index.solr.SolrIndexer;
import cz.incad.vdkcr.server.utils.MD5;
import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.server.AplikatorServiceServer;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.Persister;
import org.aplikator.server.persistence.PersisterFactory;
import org.aplikator.server.query.QueryCompareExpression;
import org.aplikator.server.query.QueryCompareOperator;
import org.aplikator.server.query.QueryExpression;
import org.aplikator.server.util.Configurator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.aplikator.server.data.RecordUtils.newRecord;

/**
 *
 * @author alberto
 */
public class OAIHarvester extends AbstractPocessDataSource {

    private static final Logger logger = Logger.getLogger(OAIHarvester.class.getName());
    private ProgramArguments arguments;
    private Configuration conf;
    XMLReader xmlReader;
    Connection conn;
    private String metadataPrefix;
    private int interval;
    String completeListSize;
    int currentDocsSent = 0;
    int currentIndex = 0;
    private SolrIndexer indexer;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH");
    SimpleDateFormat sdfoai;
    Transformer xformer;
    Context context;
    Record sklizen;
    String homeDir;
    BufferedWriter logFile;
    BufferedWriter errorLogFile;

    @Override
    public int harvest(String params, Record sklizen, Context ctx) throws Exception {
        context = ctx;

        this.sklizen = sklizen;
        arguments = new ProgramArguments();
        if (!arguments.parse(params.split(" "))) {
            System.out.println("Program arguments are invalid");
        }

        conf = new Configuration(arguments.configFile);
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
            logFile = new BufferedWriter(new FileWriter(this.homeDir + "logs" + File.separator + arguments.configFile + ".log"));
            errorLogFile = new BufferedWriter(new FileWriter(this.homeDir + "logs" + File.separator + arguments.configFile + ".error.log"));

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        }
        xmlReader = new XMLReader();
        Persister persister = PersisterFactory.getPersister();
        conn = persister.getJDBCConnection();

        psReindex = conn.prepareStatement(sqlReindex);
        sdfoai = new SimpleDateFormat(conf.getProperty("oaiDateFormat"));
        sdf = new SimpleDateFormat(conf.getProperty("filePathFormat"));
        if (arguments.metadataPrefix.equals("")) {
            this.metadataPrefix = conf.getProperty("metadataPrefix");
        } else {
            this.metadataPrefix = arguments.metadataPrefix;
        }

        interval = Interval.parseString(conf.getProperty("interval"));
        try {
            xformer = TransformerFactory.newInstance().newTransformer();

            Config config = Configurator.get().getConfig();

//            indexer = (Search)OAIHarvester.class.getClassLoader().loadClass(config.getString("aplikator.indexerClass")).newInstance();
            indexer = new SolrIndexer();
            logger.info("Harvester initialized");

            //indexer = new FastIndexer();
            harvest();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new Exception(ex);
        } finally {
            try {
                if (logFile != null) {
                    logFile.flush();
                    logFile.close();
                }
                if (errorLogFile != null) {
                    errorLogFile.flush();
                    errorLogFile.close();
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, null, ex);
            }
        }
        return currentDocsSent;

    }

    private void harvest() throws Exception {

        long startTime = (new Date()).getTime();
        currentIndex = 0;

        String from;
        File updateTimeFile = new File(this.homeDir + conf.getProperty("updateTimeFile"));
        if (arguments.from != null) {
            from = arguments.from;
        } else if (arguments.fullIndex) {
            from = getInitialDate();
        } else {
            if (updateTimeFile.exists()) {
                BufferedReader in = new BufferedReader(new FileReader(updateTimeFile));
                from = in.readLine();
            } else {
                from = getInitialDate();
            }
        }
        logger.log(Level.INFO, "updating from: " + from);
        if (!arguments.resumptionToken.equals("")) {
            getRecordWithResumptionToken(arguments.resumptionToken);
        } else if (arguments.fromDisk) {
            getRecordsFromDisk(from);
        } else {
            update(from);
        }

        RecordContainer rc = new RecordContainer();
        Structure.sklizen.stav.setValue(sklizen, SklizenStatus.Stav.UKONCEN.getValue());
        Structure.sklizen.ukonceni.setValue(sklizen, new Date());
        rc.addRecord(null, sklizen, sklizen, Operation.UPDATE);
        context.getAplikatorService().processRecords(rc);

        logFile.newLine();
        logFile.write("Harvest success " + currentDocsSent + " records");

        long timeInMiliseconds = (new Date()).getTime() - startTime;
        logger.info(formatElapsedTime(timeInMiliseconds));

    }

    private void writeResponseDate(String from) throws FileNotFoundException, IOException {
        File updateTimeFile = new File(this.homeDir + conf.getProperty("updateTimeFile"));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(updateTimeFile)));
        out.write(from);
        out.close();
    }

    private void update(String from) throws Exception {
        Calendar c_from = Calendar.getInstance();
        c_from.setTime(sdfoai.parse(from));
        Calendar c_to = Calendar.getInstance();
        c_to.setTime(sdfoai.parse(from));

        c_to.add(interval, 1);

        String to;
        Date date = new Date();
        //sdfoai.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (arguments.to == null) {
            to = sdfoai.format(date);
        } else {
            to = arguments.to;
        }
        Date final_date = sdfoai.parse(to);
        Date current = c_to.getTime();

        while (current.before(final_date)) {
            update(sdfoai.format(c_from.getTime()), sdfoai.format(current));
            c_to.add(interval, 1);
            c_from.add(interval, 1);
            current = c_to.getTime();
        }
        update(sdfoai.format(c_from.getTime()), sdfoai.format(final_date));
        if (!arguments.dontIndex) {
//            indexer.finish();
        }
        writeResponseDate(to);

    }

    private void update(String from, String until) throws Exception {
        logger.log(Level.INFO, "Harvesting from: {0} until: {1}", new Object[]{from, until});
        //responseDate = from;
        writeResponseDate(from);
        getRecords(from, until);
        writeResponseDate(until);
    }

    private void processRecord(Node node, String identifier, int recordNumber, RecordContainer rc) throws Exception {
        // check interrupted thread
        if (Thread.currentThread().isInterrupted()) {
            Structure.sklizen.stav.setValue(sklizen, SklizenStatus.Stav.UKONCEN.getValue());
            Structure.sklizen.ukonceni.setValue(sklizen, new Date());
            Structure.sklizen.pocet.setValue(sklizen, currentDocsSent++);
            rc.addRecord(null, sklizen, sklizen, Operation.UPDATE);
            context.getAplikatorService().processRecords(rc);
            logger.log(Level.INFO, "HARVERTER INTERRUPTED");
            throw new InterruptedException();
        }
        if (node != null) {
            String error = xmlReader.getNodeValue(node, "/oai:error/@code");
            if (error == null || error.equals("")) {

                String urlZdroje = conf.getProperty("baseUrl")
                        + "?verb=GetRecord&identifier=" + identifier
                        + "&metadataPrefix=" + metadataPrefix
                        + "#set=" + conf.getProperty("set");

                if ("deleted".equals(xmlReader.getNodeValue(node, "./oai:header/@status"))) {
                    if (arguments.fullIndex) {
                        logger.log(Level.FINE, "Skip deleted record when fullindex");
                        return;
                    }
                    //zpracovat deletes
                    if (!arguments.dontIndex) {
                        //indexer.removeDoc(urlZdroje);
                    }
                } else {
                    String xmlStr = nodeToString(xmlReader.getNodeElement(), recordNumber);
                    //System.out.println(xmlStr);

                    String hlavninazev = xmlReader.getNodeValue(node, "./oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='a']/text()");

                    boolean zaznamExists = true;
                    Record fr = getRecord(identifier);
                    if (fr == null) {
                        fr = newRecord(Structure.zaznam);
                        zaznamExists = false;
                    }
                    String cnbStr = xmlReader.getNodeValue(node, "./oai:metadata/marc:record/marc:datafield[@tag='015']/marc:subfield[@code='a']/text()");
                    String uniqueCode;
                    String codeType;
                    if ("".equals(cnbStr)) {

                        uniqueCode = MD5.generate(new String[]{
                            hlavninazev,
                            xmlReader.getNodeValue(node, "./oai:metadata/marc:record/marc:datafield[@tag='245']/marc:subfield[@code='b']/text()"),
                            xmlReader.getNodeValue(node, "./oai:metadata/marc:record/marc:datafield[@tag='700']/marc:subfield[@code='a']/text()"),
                            xmlReader.getNodeValue(node, "./oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='a']/text()"),
                            xmlReader.getNodeValue(node, "./oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='b']/text()"),
                            xmlReader.getNodeValue(node, "./oai:metadata/marc:record/marc:datafield[@tag='260']/marc:subfield[@code='c']/text()")
                        });
                        codeType = "md5";
                    } else {
                        uniqueCode = MD5.generate(new String[]{cnbStr});
                        Structure.zaznam.ccnb.setValue(fr, cnbStr);
                        codeType = "ccnb";
                    }

                    Structure.zaznam.sklizen.setValue(fr, sklizen.getPrimaryKey().getId());
                    Structure.zaznam.knihovna.setValue(fr, conf.getProperty("knihovna"));
                    Structure.zaznam.identifikator.setValue(fr, identifier);
                    Structure.zaznam.urlZdroje.setValue(fr, urlZdroje);
                    Structure.zaznam.hlavniNazev.setValue(fr, hlavninazev);
                    Structure.zaznam.uniqueCode.setValue(fr, uniqueCode);
                    Structure.zaznam.codeType.setValue(fr, codeType);
                    String leader = xmlReader.getNodeValue(node, "./oai:metadata/marc:record/marc:leader/text()");
                    if (leader != null && leader.length() > 9) {
                        String typDokumentu = typDokumentu(leader);
                        Structure.zaznam.typDokumentu.setValue(fr, typDokumentu);
                    }

                    Structure.zaznam.sourceXML.setValue(fr, xmlStr);
                    rc.addRecord(null, fr, fr, Operation.CREATE);

                    try {
                        rc = context.getAplikatorService().processRecords(rc);
                    } catch (Exception ex) {
                        if (arguments.continueOnDocError) {
                            logFile.newLine();
                            logFile.write("Error writing docs to db. Id: " + identifier);
                            logFile.flush();
                            logger.log(Level.WARNING, "Error writing doc to db. Id: {0}", identifier);
                        } else {
                            throw new Exception(ex);
                        }
                    }

                    Structure.sklizen.pocet.setValue(sklizen, currentDocsSent++);
                    rc.addRecord(null, sklizen, sklizen, Operation.UPDATE);
                    //try {
                    if (!arguments.dontIndex) {
                        if (zaznamExists) {
                            reindexRecords(uniqueCode, codeType, identifier);
                        } else {
                            indexer.processXML(xmlStr, uniqueCode, codeType, identifier);
                        }

                    }
//                    } catch (Exception ex) {
//                        currentDocsSent--;
//                        errorLogFile.newLine();
//                        errorLogFile.write("Cant procces record  " + urlZdroje);
//                        logger.log(Level.WARNING, "Cant procces record  " + urlZdroje, ex);
//                    }

                }
            } else {
                logger.log(Level.SEVERE, "Can't proccess xml {0}", error);
            }
        }
    }

    private void getRecordWithResumptionToken(String resumptionToken) throws Exception {
        while (resumptionToken != null && !resumptionToken.equals("")) {
            resumptionToken = getRecords("?verb=" + conf.getProperty("verb") + "&resumptionToken=" + resumptionToken);
        }
    }

    private String getInitialDate() throws Exception {
        String urlString = conf.getProperty("baseUrl") + "?verb=Identify";

        // check interrupted thread
        if (Thread.currentThread().isInterrupted()) {
            logger.log(Level.INFO, "HARVERTER INTERRUPTED");
            throw new InterruptedException();
        }

        URL url = new URL(urlString.replace("\n", ""));

        logger.log(Level.FINE, "url: {0}", url.toString());
        xmlReader.readUrl(url.toString());
        return xmlReader.getNodeValue("//Identify/earliestDatestamp/text()");
    }

    private void getRecords(String from, String until) throws Exception {
        String query = String.format("?verb=%s&from=%s&until=%s&metadataPrefix=%s&set=%s",
                conf.getProperty("verb"),
                from,
                until,
                metadataPrefix,
                conf.getProperty("set"));
        String resumptionToken = getRecords(query);
        while (resumptionToken != null && !resumptionToken.equals("")) {
            resumptionToken = getRecords("?verb=" + conf.getProperty("verb") + "&resumptionToken=" + resumptionToken);
        }
    }

    private String getRecords(String query) throws Exception {

        // check interrupted thread
        if (Thread.currentThread().isInterrupted()) {
            logger.log(Level.INFO, "HARVERTER INTERRUPTED");
            throw new InterruptedException();
        }

        String urlString = conf.getProperty("baseUrl") + query;
        URL url = new URL(urlString.replace("\n", ""));
        logFile.newLine();
        logFile.write(url.toString());
        logFile.flush();
        try {
            xmlReader.readUrl(url.toString());
        } catch (Exception ex) {
            logger.log(Level.WARNING, ex.toString());
            logFile.newLine();
            logFile.write("retrying url: " + url.toString());
            xmlReader.readUrl(url.toString());
        }
        String error = xmlReader.getNodeValue("//oai:error/@code");
        if (error.equals("")) {
            completeListSize = xmlReader.getNodeValue("//oai:resumptionToken/@completeListSize");
            String date;
            String identifier;
//oai:ListRecords/oai:record/oai:header
            String fileName = null;
            if (arguments.saveToDisk) {
                fileName = writeNodeToFile(xmlReader.getNodeElement(),
                        xmlReader.getNodeValue("//oai:record[position()=1]/oai:header/oai:datestamp/text()"),
                        xmlReader.getNodeValue("//oai:record[position()=1]/oai:header/oai:identifier/text()"));
            }
            NodeList nodes = xmlReader.getListOfNodes("//oai:record");
            if (arguments.onlyIdentifiers) {
                //TODO
            } else {
                if (!arguments.onlyHarvest && currentIndex > arguments.startIndex) {
                    
                    for (int i = 0; i < nodes.getLength(); i++) {
                        identifier = xmlReader.getNodeValue("//oai:record[position()=" + (i + 1) + "]/oai:header/oai:identifier/text()");
                        RecordContainer rc = new RecordContainer();
                        processRecord(nodes.item(i), identifier, i + 1, rc);
                        currentIndex++;
                        logger.log(Level.FINE, "number: {0} of {1}", new Object[]{(currentDocsSent), completeListSize});
                    }
                    //context.getAplikatorService().processRecords(rc);
                }
                if (!arguments.dontIndex) {
//                    if (fileName != null) {
//                        indexer.processXML(new File(fileName));
//                    } else {
//                        indexer.processXML(xmlReader.getDoc());
//                    }
                    indexer.commit();
                }
            }
            //logger.log(Level.INFO, "number: {0} of {1}", new Object[]{(currentDocsSent), completeListSize});
            return xmlReader.getNodeValue("//oai:resumptionToken/text()");
        } else {
            logger.log(Level.INFO, "{0} for url {1}", new Object[]{error, urlString});
        }
        return null;
    }

    private void getRecordsFromDisk(String from) throws Exception {
        logger.info("Processing dowloaded files");
//        Calendar c_from = Calendar.getInstance();
//        c_from.setTime(sdfoai.parse(from));
//        String dirFrom = sdf.format(c_from.getTime());
        if (arguments.pathToData.equals("")) {
            getRecordsFromDir(new File(conf.getProperty("indexDirectory")), from);
        } else {
            getRecordsFromDir(new File(arguments.pathToData), from);
        }
        if (!arguments.dontIndex) {
//            indexer.finish();
        }
    }

    private void getRecordsFromDir(File dir, String from) throws Exception {
        File[] children = dir.listFiles();
        Arrays.sort(children);
        for (int i = 0; i < children.length; i++) {
            if (currentDocsSent >= arguments.maxDocuments && arguments.maxDocuments > 0) {
                break;
            }

            if (children[i].isDirectory()) {
                getRecordsFromDir(children[i], from);
            } else {
                String identifier;
                logger.info("Loading file " + children[i].getPath());
                xmlReader.loadXmlFromFile(children[i]);
                NodeList nodes = xmlReader.getListOfNodes("//oai:record");
                
                for (int j = 0; j < nodes.getLength(); j++) {
                    if (currentIndex > arguments.startIndex) {
                        identifier = xmlReader.getNodeValue("//oai:record[position()=" + (j + 1) + "]/oai:header/oai:identifier/text()");
                        RecordContainer rc = new RecordContainer();
                        processRecord(nodes.item(j), identifier, j + 1, rc);
                    }
                    currentIndex++;
                    logger.log(Level.FINE, "number: {0}", currentDocsSent);
                }
                

//                if (!arguments.dontIndex) {
//                    indexer.processXML(children[i]);
//                    indexer.commit();
//                }
            }
        }
    }

    private String writeNodeToFile(Node node, String date, String identifier) throws Exception {
        String dirName = conf.getProperty("indexDirectory") + File.separatorChar + sdf.format(sdfoai.parse(date));

        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                logger.log(Level.WARNING, "Can''t create: {0}", dirName);
            }
        }
        String xmlFileName = dirName + File.separatorChar + identifier.substring(conf.getProperty("identifierPrefix").length()) + ".xml";

        Source source = new DOMSource(node);
        File file = new File(xmlFileName);
        Result result = new StreamResult(file);
        xformer.transform(source, result);
        return xmlFileName;
    }

    private String nodeToString(Node node, int pos) throws Exception {

        String xslt = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xsl:stylesheet version=\"1.0\" xmlns:oai=\"http://www.openarchives.org/OAI/2.0/\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"  >"
                + "<xsl:output omit-xml-declaration=\"yes\" method=\"xml\" indent=\"yes\" encoding=\"UTF-8\" />"
                + "<xsl:template  match=\"/\"><xsl:copy-of select=\"//oai:ListRecords/oai:record[position()=" + pos + "]\" /></xsl:template>"
                + "</xsl:stylesheet>";
        Transformer xformer2 = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(xslt)));
        StringWriter sw = new StringWriter();

        Source source = new DOMSource(node);

        xformer2.transform(source, new StreamResult(sw));
        return sw.toString();
    }

    private String nodeToString(Node node) throws Exception {

        StringWriter sw = new StringWriter();

        Source source = new DOMSource(node);

        xformer.transform(source, new StreamResult(sw));
        //System.out.println("sw.toString(): " + sw.toString());
        return sw.toString();
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

    private String typDokumentu(String leader) {
        if (leader != null && leader.length() > 9) {
            String code = leader.substring(6, 8);
            if ("aa".equals(code)
                    || "ac".equals(code)
                    || "ad".equals(code)
                    || "am".equals(code)
                    || code.startsWith("t")) {
                return "BK";
            } else if ("bi".equals(code)
                    || "bs".equals(code)) {
                return "SE";
            } else if (code.startsWith("p")) {
                return "MM";
            } else if (code.startsWith("e")) {
                return "MP";
            } else if (code.startsWith("f")) {
                return "MP";
            } else if (code.startsWith("g")) {
                return "VM";
            } else if (code.startsWith("k")) {
                return "VM";
            } else if (code.startsWith("o")) {
                return "VM";
            } else if (code.startsWith("c")) {
                return "MU";
            } else if (code.startsWith("d")) {
                return "MU";
            } else if (code.startsWith("i")) {
                return "MU";
            } else if (code.startsWith("j")) {
                return "MU";
            } else {
                return code;
            }
        } else {
            return "none";
        }
    }

    public static void main(String[] args) throws Exception {
        OAIHarvester oh = new OAIHarvester();
        //oh.harvest("-cfgFile nkp_vdk -dontIndex -fromDisk ", null, null);
        //oh.harvest("-cfgFile VKOL -dontIndex -saveToDisk -onlyHarvest", null, null);201304191450353201304220725599VKOLOAI:VKOL-M

        oh.harvest("-cfgFile VKOL -dontIndex -saveToDisk -onlyHarvest resumptionToken 201304191450353201304220725599VKOLOAI:VKOL-M", null, null);

        //oh.harvest("-cfgFile MZK01-VDK -dontIndex -saveToDisk -onlyHarvest", null, null);
        //oh.harvest("-cfgFile MZK03-VDK -dontIndex -saveToDisk -onlyHarvest", null, null);
        //oh.harvest("-cfgFile nkp_vdk -dontIndex -saveToDisk -onlyHarvest ", null, null);
        //oh.harvest("-cfgFile nkp_vdk -dontIndex -saveToDisk -onlyHarvest -resumptionToken 201305160612203201305162300009NKC-VDK:NKC-VDKM", null, null);
    }

    private Record getRecord(String identifier) {
        QueryExpression queryExpression = new QueryCompareExpression<String>(Structure.zaznam.identifikator,
                QueryCompareOperator.EQUAL, identifier);
        List<Record> recs = ((AplikatorServiceServer) context.getAplikatorService()).getRecords(
                Structure.zaznam.view(), queryExpression, null, null, null, 0, 1, context);
        if (recs.isEmpty()) {
            return null;
        } else {
            logger.log(Level.FINE, "Identifier {0} already in db", identifier);
//            logger.log(Level.INFO, "getValue(sourceXML): ", Structure.zaznam.sourceXML.getValue(recs.get(0)));
//            logger.log(Level.INFO, "getValue(hlavniNazev): ", Structure.zaznam.hlavniNazev.getValue(recs.get(0)));
//            logger.log(Level.INFO, "getProperties(): ", recs.get(0).getProperties());
            return recs.get(0);
        }

    }

    String sqlReindex = "select sourceXML from zaznam where uniqueCode=?";
    PreparedStatement psReindex;

    private void reindexRecords(String uniqueCode, String codeType, String identifier) throws Exception {
        indexer.delete(uniqueCode);
        psReindex.setString(1, uniqueCode);
        ResultSet rs = psReindex.executeQuery();
        while (rs.next()) {
            //logger.log(Level.INFO, rs.getString("sourceXML"));
            indexer.processXML(rs.getString("sourceXML"), uniqueCode, codeType, identifier);

        }
//        throw new Exception("KONEC");
    }
}
