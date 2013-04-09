package cz.incad.vdkcr.server.datasources.oai;

import static org.aplikator.server.data.RecordUtils.newRecord;
import static org.aplikator.server.data.RecordUtils.newSubrecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.client.shared.rpc.impl.ProcessRecords;
import org.aplikator.server.Context;
import org.aplikator.server.util.Configurator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fastsearch.esp.content.DocumentFactory;
import com.fastsearch.esp.content.IDocument;

import com.typesafe.config.Config;
import cz.incad.vdkcr.server.Structure;
import cz.incad.vdkcr.server.datasources.DataSource;
import cz.incad.vdkcr.server.datasources.util.XMLReader;
import cz.incad.vdkcr.server.fast.FastIndexer;
import cz.incad.vdkcr.server.fast.IndexTypes;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author alberto
 */
public class OAIHarvester implements DataSource {

    private static final Logger logger = Logger.getLogger(OAIHarvester.class.getName());
    private ProgramArguments arguments;
    private Configuration conf;
    XMLReader xmlReader;
    private String responseDate;
    private String metadataPrefix;
    private int interval;
    String completeListSize;
    int currentDocsSent = 0;
    int currentIndex = 0;
    private FastIndexer fastIndexer;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH");
    SimpleDateFormat sdfoai;
    Transformer xformer;
    Context context;
    Record sklizen;
    String homeDir;
    BufferedWriter logFile;
    BufferedWriter errorLogFile;

    @Override
    public int harvest(String params, Record sklizen, Context ctx) {
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
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        xmlReader = new XMLReader();
        logger.info("Indexer initialized");
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
            fastIndexer = new FastIndexer(config.getString("aplikator.fastHost"),
                    config.getString("aplikator.fastCollection"),
                    config.getInt("aplikator.fastBatchSize"));
            harvest();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(OAIHarvester.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //logFileHandler.close();
            //Logger.getLogger("cz.incad.vdkcr.server").removeHandler(logFileHandler);

            try {
                if (logFile != null) {
                    logFile.flush();
                    logFile.close();
                }
                if (errorLogFile != null) {
                    errorLogFile.flush();
                    errorLogFile.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return currentDocsSent;

    }

    private void harvest() {
        long startTime = (new Date()).getTime();
        currentIndex = 0;
        try {


            if (!arguments.resumptionToken.equals("")) {
                getRecordWithResumptionToken(arguments.resumptionToken);
            } else if (arguments.fromDisk) {
                getRecordsFromDisk();
            } else {

                String from = "";
                String updateTimeFile = conf.getProperty("updateTimeFile");
                if (arguments.from != null) {
                    from = arguments.from;
                } else if (arguments.fullIndex) {
                    from = getInitialDate();
                } else {
                    if ((new File(updateTimeFile)).exists()) {
                        BufferedReader in = new BufferedReader(new FileReader(updateTimeFile));
                        from = in.readLine();
                    } else {
                        from = getInitialDate();
                    }
                }
                update(from);
            }

            logFile.newLine();
            logFile.write("Harvest success " + currentDocsSent + " records");

            long timeInMiliseconds = (new Date()).getTime() - startTime;
            logger.info(formatElapsedTime(timeInMiliseconds));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error", ex);
        } finally {
            //disconnect();
        }
    }

    private void writeResponseDate() throws FileNotFoundException, IOException {
        File dateFile = new File(this.homeDir + conf.getProperty("updateTimeFile"));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dateFile)));
        out.write(responseDate);
        out.close();
    }

    private void update(String from) throws Exception {
        Calendar c_from = Calendar.getInstance();
        c_from.setTime(sdfoai.parse(from));
        Calendar c_to = Calendar.getInstance();
        c_to.setTime(sdfoai.parse(from));

        c_to.add(interval, 1);

        String to = "";
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
            fastIndexer.sendPendingRecords();
        }

    }

    private void update(String from, String until) throws Exception {
        logger.log(Level.INFO, "Harvesting from: {0} until: {1}", new Object[]{from, until});
        responseDate = from;
        writeResponseDate();
        getRecords(from, until);
    }

    private void processRecord(Node node, String identifier, int recordNumber) throws Exception {
        if (node != null) {
            String error = xmlReader.getNodeValue(node, "/error/@code");
            if (error.equals("")) {
                IndexTypes it = IndexTypes.INSERTED;

                if (xmlReader.getNodeValue(node, "./header/@status").equals("deleted")) {
                    if (arguments.fullIndex) {
                        logger.log(Level.FINE, "Skip deleted record when fullindex");
                        return;
                    }
                    it = IndexTypes.DELETED;
                }
                if (it != IndexTypes.DELETED) {
                    RecordContainer rc = new RecordContainer();


                    Record fr = newRecord(Structure.zaznam);
                    Structure.zaznam.sklizen.setValue(fr, sklizen.getPrimaryKey().getId());
                    String urlZdroje = conf.getProperty("baseUrl")
                            + "?verb=GetRecord&identifier=" + identifier
                            + "&metadataPrefix=" + metadataPrefix
                            + "#set=" + conf.getProperty("set");
                    Structure.zaznam.urlZdroje.setValue(fr, urlZdroje);
                    String hlavninazev = xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='245']/subfield[@code='a']/text()");
                    Structure.zaznam.hlavniNazev.setValue(fr, hlavninazev);
                    String leader = xmlReader.getNodeValue(node, "./metadata/record/leader/text()");
                    String typDokumentu = typDokumentu(leader);
                    if (leader != null && leader.length() > 9) {
                        Structure.zaznam.typDokumentu.setValue(fr, typDokumentu);
                    }
                    //String xmlStr = nodeToString(node);
                    String xmlStr = nodeToString(xmlReader.getNodeElement(), recordNumber);
                    Structure.zaznam.sourceXML.setValue(fr, xmlStr);
                    rc.addRecord(null, fr, fr, Operation.CREATE);


                    //Identifikatory
                    String isxn = xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='022']/subfield[@code='a']/text()");
                    if (!"".equals(isxn)) {
                        Record ISSN = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.identifikator);
                        Structure.identifikator.hodnota.setValue(ISSN, isxn);
                        Structure.identifikator.typ.setValue(ISSN, "ISSN");
                        rc.addRecord(null, ISSN, ISSN, Operation.CREATE);

                    }
                    isxn = xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='020']/subfield[@code='a']/text()");
                    if (!"".equals(isxn)) {
                        Record ISBN = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.identifikator);
                        Structure.identifikator.hodnota.setValue(ISBN, isxn);
                        Structure.identifikator.typ.setValue(ISBN, "ISBN");
                        rc.addRecord(null, ISBN, ISBN, Operation.CREATE);
                    }
                    Record cnb = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.identifikator);
                    String cnbStr = xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='015']/subfield[@code='a']/text()");
                    Structure.identifikator.hodnota.setValue(cnb, cnbStr);
                    Structure.identifikator.typ.setValue(cnb, "cCNB");
                    rc.addRecord(null, cnb, cnb, Operation.CREATE);

                    //Autori
                    NodeList autori = xmlReader.getListOfNodes(node, "./metadata/record/datafield[@tag='100']");
                    String autoriStr = "";
                    for (int i = 0; i < autori.getLength(); i++) {
                        Record autor = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.autor);
                        String autorStr = xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='100'][position()=" + (i + 1) + "]/subfield[@code='a']/text()");
                        Structure.autor.nazev.setValue(autor,
                                autorStr);
                        autoriStr += autorStr + ";";
                        rc.addRecord(null, autor, autor, Operation.CREATE);
                    }

                    //Jazyky
                    String[] jazyky = xmlReader.getListOfValues(node, "./metadata/record/datafield[@tag='041']/subfield[@code='a']/text()");
                    for (String jazyk : jazyky) {
                        Record j = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.jazyk);
                        Structure.jazyk.kod.setValue(j, jazyk);
                        rc.addRecord(null, j, j, Operation.CREATE);
                    }

                    //Nazvy
                    String[] nazvy = xmlReader.getListOfValues(node, "./metadata/record/datafield[@tag='245']/subfield[@code='a']/text()");
                    for (String nazev : nazvy) {
                        Record j = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.nazev);
                        Structure.nazev.nazev.setValue(j, nazev);
                        Structure.nazev.typNazvu.setValue(j, "Hlavní název");
                        rc.addRecord(null, j, j, Operation.CREATE);
                    }
                    nazvy = xmlReader.getListOfValues(node, "./metadata/record/datafield[@tag='245']/subfield[@code='b']/text()");
                    for (String nazev : nazvy) {
                        Record j = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.nazev);
                        Structure.nazev.nazev.setValue(j, nazev);
                        Structure.nazev.typNazvu.setValue(j, "Podnázev");
                        rc.addRecord(null, j, j, Operation.CREATE);
                    }
                    nazvy = xmlReader.getListOfValues(node, "./metadata/record/datafield[@tag='245']/subfield[@code='p']/text()");
                    for (String nazev : nazvy) {
                        Record j = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.nazev);
                        Structure.nazev.nazev.setValue(j, nazev);
                        Structure.nazev.typNazvu.setValue(j, "Název části");
                        rc.addRecord(null, j, j, Operation.CREATE);
                    }
                    nazvy = xmlReader.getListOfValues(node, "./metadata/record/datafield[@tag='246']/subfield[@code='a']/text()");
                    for (String nazev : nazvy) {
                        Record j = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.nazev);
                        Structure.nazev.nazev.setValue(j, nazev);
                        Structure.nazev.typNazvu.setValue(j, "Alternativní název");
                        rc.addRecord(null, j, j, Operation.CREATE);
                    }
                    
                    //Vydani
                    String vydaniX = "./metadata/record/datafield[@tag='260']";
                    NodeList vydani = xmlReader.getListOfNodes(node, vydaniX);
                    for (int i = 0; i < vydani.getLength(); i++) {
                        
                        Record j = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.vydani);
                                
                        Structure.vydani.misto.setValue(j, 
                                xmlReader.getNodeValue(node, vydaniX + "[position()=" + (i + 1) + "]/subfield[@code='a']/text()"));
                        Structure.vydani.nakladatel.setValue(j, 
                                xmlReader.getNodeValue(node, vydaniX + "[position()=" + (i + 1) + "]/subfield[@code='b']/text()"));
                        Structure.vydani.datum.setValue(j, 
                                xmlReader.getNodeValue(node, vydaniX + "[position()=" + (i + 1) + "]/subfield[@code='c']/text()"));
                        
                        rc.addRecord(null, j, j, Operation.CREATE);
                    }
                    
                    //Zpracovani digitalni verze
                    NodeList dvs = xmlReader.getListOfNodes(node, "./metadata/record/datafield[@tag='856']");
                    for (int i = 0; i < dvs.getLength(); i++) {
                         Record dv = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.digitalniVerze);
                         Structure.digitalniVerze.url.setValue(dv,
                                xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='856'][position()=" + (i + 1) + "]/subfield[@code='u']/text()"));
                        rc.addRecord(null, dv, dv, Operation.CREATE);
                    }


                    //Zpracování exemplářů - ITM

                    NodeList exs = xmlReader.getListOfNodes(node, "./metadata/record/datafield[@tag='996']");
                    //String exsStr = "";
                    for (int i = 0; i < exs.getLength(); i++) {
                        Record ex = newSubrecord(fr.getPrimaryKey(), Structure.zaznam.exemplar);
                        //String exStr = xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='996'][position()=" + (i + 1) + "]/subfield[@code='b']/text()");
                        Structure.exemplar.carovyKod.setValue(ex,
                                xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='996'][position()=" + (i + 1) + "]/subfield[@code='b']/text()"));
                        Structure.exemplar.signatura.setValue(ex,
                                xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='996'][position()=" + (i + 1) + "]/subfield[@code='c']/text()"));
                        Structure.exemplar.popis.setValue(ex,
                                xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='996'][position()=" + (i + 1) + "]/subfield[@code='d']/text()"));
                        Structure.exemplar.svazek.setValue(ex,
                                xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='996'][position()=" + (i + 1) + "]/subfield[@code='v']/text()"));
                        Structure.exemplar.rok.setValue(ex,
                                xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='996'][position()=" + (i + 1) + "]/subfield[@code='y']/text()"));
                        Structure.exemplar.cislo.setValue(ex,
                                xmlReader.getNodeValue(node, "./metadata/record/datafield[@tag='996'][position()=" + (i + 1) + "]/subfield[@code='i']/text()"));
                        //exsStr += exStr + ";";
                        rc.addRecord(null, ex, ex, Operation.CREATE);
                    }

                    Structure.sklizen.pocet.setValue(sklizen, currentDocsSent++);
                    rc.addRecord(null, sklizen, sklizen, Operation.UPDATE);
                    try {
                        rc = context.getAplikatorService().processRecords(rc);
                        if(!arguments.dontIndex){
                            Record z = rc.getRecords().get(0).getEdited();
                            IDocument doc = DocumentFactory.newDocument(urlZdroje);
                            doc.addElement(DocumentFactory.newString("title", hlavninazev));
                            doc.addElement(DocumentFactory.newInteger("dbid", z.getPrimaryKey().getId()));
                            doc.addElement(DocumentFactory.newString("url", urlZdroje));
                            doc.addElement(DocumentFactory.newString("druhdokumentu", typDokumentu));
                            doc.addElement(DocumentFactory.newString("autor", autoriStr));
                            doc.addElement(DocumentFactory.newString("zdroj", conf.getProperty("zdroj")));
                            doc.addElement(DocumentFactory.newString("isxn", isxn));
                            doc.addElement(DocumentFactory.newString("ccnb", cnbStr));
                            doc.addElement(DocumentFactory.newString("base", conf.getProperty("base")));
                            doc.addElement(DocumentFactory.newString("harvester", conf.getProperty("harvester")));
                            doc.addElement(DocumentFactory.newString("originformat", conf.getProperty("originformat")));
                            //doc.addElement(DocumentFactory.newString("data", xmlStr));
                            doc.addElement(DocumentFactory.newString("data", "<record />"));
                            fastIndexer.add(doc, it);
                        }
                    } catch (Exception ex) {
                        currentDocsSent--;
                        errorLogFile.newLine();
                        errorLogFile.write("Cant procces record  " + urlZdroje);
                        logger.log(Level.WARNING, "Cant procces record  " + urlZdroje, ex);
                    }

                } else {
                    //zpracovat deletes
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
        String urlString = conf.getProperty("baseUrl") + query;
        URL url = new URL(urlString.replace("\n", ""));
        logFile.newLine();
        logFile.write("url: " + url.toString());
        try {
            xmlReader.readUrl(url.toString());
        } catch (Exception ex) {
            logFile.newLine();
            logFile.write("retrying url: " + url.toString());
            xmlReader.readUrl(url.toString());
        }
        String error = xmlReader.getNodeValue("//error/@code");
        if (error.equals("")) {
            completeListSize = xmlReader.getNodeValue("//resumptionToken/@completeListSize");
            String date;
            String identifier;
            //saveToIndexDir(date, xml, xmlNumber);

            if (arguments.saveToDisk) {
                writeNodeToFile(xmlReader.getNodeElement(),
                        xmlReader.getNodeValue("//record[position()=1]/header/datestamp/text()"),
                        xmlReader.getNodeValue("//record[position()=1]/header/identifier/text()"));
            }
            NodeList nodes = xmlReader.getListOfNodes("//record");
            if (arguments.onlyIdentifiers) {
                //TODO
            } else {
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (!arguments.onlyHarvest && currentIndex>arguments.startIndex) {
//                    date = xmlReader.getNodeValue("//record[position()=" + (i + 1) + "]/header/datestamp/text()");
                        identifier = xmlReader.getNodeValue("//record[position()=" + (i + 1) + "]/header/identifier/text()");
                        processRecord(nodes.item(i), identifier, i+1);
                    }
                    currentIndex++;
                    logger.log(Level.FINE, "number: {0} of {1}", new Object[]{(currentDocsSent), completeListSize});
                }
            }
            logger.log(Level.INFO, "number: {0} of {1}", new Object[]{(currentDocsSent), completeListSize});
            return xmlReader.getNodeValue("//resumptionToken/text()");
        } else {
            logger.log(Level.INFO, "{0} for url {1}", new Object[]{error, urlString});
        }
        return null;
    }

    private void getRecordsFromDisk() throws Exception {
        logger.fine("Processing dowloaded files");
        if (arguments.pathToData.equals("")) {
            getRecordsFromDir(new File(conf.getProperty("indexDirectory")));
        } else {
            getRecordsFromDir(new File(arguments.pathToData));
        }
        if (!arguments.dontIndex) {
            fastIndexer.sendPendingRecords();
        }
    }

    private String readFileAsString(FileReader freader)
            throws java.io.IOException {
        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(
                freader);

        char[] buf = new char[1024];

        int numRead = 0;

        while ((numRead = reader.read(buf)) != -1) {

            String readData = String.valueOf(buf, 0, numRead);

            fileData.append(readData);

            buf = new char[1024];

        }

        reader.close();

        return fileData.toString();

    }

    private void getRecordsFromDir(File dir) throws Exception {
        File[] children = dir.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (currentDocsSent >= arguments.maxDocuments && arguments.maxDocuments > 0) {
                break;
            }
            
            if (children[i].isDirectory()) {
                getRecordsFromDir(children[i]);
            } else {
                String identifier;
//                String xmlText = readFileAsString(new FileReader(children[i]));
//                xmlText = xmlText.replaceAll("<marc:record ", "<marc:record xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
//                xmlReader.loadXml(xmlText);
                xmlReader.loadXmlFromFile(children[i]);
                NodeList nodes = xmlReader.getListOfNodes("//record");
                for (int j = 0; j < nodes.getLength(); j++) {
                    //date = xmlReader.getNodeValue("//record[position()=" + (i + 1) + "]/header/datestamp/text()");
                    if(currentIndex>arguments.startIndex){
                        identifier = xmlReader.getNodeValue("//record[position()=" + (j + 1) + "]/header/identifier/text()");
                        processRecord(nodes.item(j), identifier, j+1);
                    }
                    currentIndex++;
                    logger.log(Level.FINE, "number: {0}", currentDocsSent);
                }

            }
        }
    }
    
    private void writeNodeToFile(Node node, String date, String identifier) throws Exception {
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
    }
    
    private String nodeToString(Node node, int pos) throws Exception{
        
        String xslt = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"  >" +
            "<xsl:output omit-xml-declaration=\"yes\" method=\"xml\" indent=\"yes\" encoding=\"UTF-8\" />"+
            "<xsl:template  match=\"/\"><xsl:copy-of select=\"//record[position()=" + pos + "]\" /></xsl:template>" +
                "</xsl:stylesheet>";
        Transformer xformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(xslt)));
        StringWriter sw = new StringWriter();

        Source source = new DOMSource(node);
        
        xformer.transform(source, new StreamResult(sw));
        return sw.toString();
    }

    private String nodeToString(Node node) throws Exception {

        StringWriter sw = new StringWriter();

        Source source = new DOMSource(node);
        
        xformer.transform(source, new StreamResult(sw));
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
}
