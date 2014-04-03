package cz.incad.vdkcr.server.index.solr;

import com.typesafe.config.Config;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.aplikator.client.shared.data.PrimaryKey;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.SearchResult;
import org.aplikator.client.shared.descriptor.EntityDTO;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.Persister;
import org.aplikator.server.persistence.PersisterFactory;
import org.aplikator.server.persistence.Transaction;
import org.aplikator.server.persistence.search.Search;
import org.aplikator.server.util.Configurator;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alberto
 */
public class SolrIndexer implements Search {

    private static final Logger logger = Logger.getLogger(SolrIndexer.class.getName());
    private String host;
    private String collection;
    HttpSolrServer server;
    private Transaction tx;
    private Persister persister;
    private static final Integer DEFAULT_TRAVERSE_LEVEL = 4;
    private static final Boolean DEFAULT_INCLUDE_COLLECTIONS = true;
    Transformer transformer;
    URL solrUrl;
    String xsl;
    SolrIndexerCommiter commiter;

    public SolrIndexer() throws Exception {
        Config config = Configurator.get().getConfig();
        this.host = config.getString("aplikator.solrHost");
        this.collection = config.getString("aplikator.solrCollection");
        this.xsl = config.getString("aplikator.solrXsl");

        this.idField = config.getString("aplikator.solrIdField");
        persister = PersisterFactory.getPersister();

        server = new HttpSolrServer(host + "/" + collection);
        //server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
        //server.setConnectionTimeout(1000); // 5 seconds to establish TCP
        // Setting the XML response parser is only required for cross
        // version compatibility and only when one side is 1.4.1 or
        // earlier and the other side is 3.1 or later.
        //server.setParser(new XMLResponseParser()); // binary parser is used by default
        // The following settings are provided here for completeness.
        // They will not normally be required, and should only be used 
        // after consulting javadocs to know whether they are truly required.
        //server.setSoTimeout(1000);  // socket read timeout
        //server.setDefaultMaxConnectionsPerHost(100);
        //server.setMaxTotalConnections(100);
        //server.setFollowRedirects(false);  // defaults to false
        // allowCompression defaults to false.
        // Server side must support gzip or deflate for this to have any effect.
        //server.setAllowCompression(true);

        commiter = new SolrIndexerCommiter();

        TransformerFactory tfactory = TransformerFactory.newInstance();
        StreamSource xslt = new StreamSource(new File(this.xsl));
        transformer = tfactory.newTransformer(xslt);
        solrUrl = new URL(this.host + "/" + collection + "/update");

        logger.info("Indexer initialized");
    }

    public void commit() throws Exception {
        //server.commit();
        
        SolrIndexerCommiter.postData(this.solrUrl, "<commit/>");
    }

    private void check2() throws Exception {
//        if (insertDocs.size() >= batchSize) {
//            server.add(insertDocs);
//            server.commit();
//            insertDocs.clear();
//        }
//        if (delDocs.size() >= batchSize) {
//            server.deleteById(delDocs);
//            server.commit();
//            delDocs.clear();
//        }
    }
    String idField;

    @Override
    public void finish() {
        try {
            server.commit();

        } catch (SolrServerException ex) {
            Logger.getLogger(SolrIndexer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SolrIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public void insertRecord(RecordContainer rc) throws Exception {
//
//        //System.out.println("rc: " + Marshalling.toJSON(rc));
//        SolrInputDocument doc = new SolrInputDocument();
//        doc.addField(idField, rc.getRecords().get(0).getEdited().getPrimaryKey().getId());
//        for (ContainerNode cn : rc.getRecords()) {
//            Record record = cn.getEdited();
//            String entityId = record.getPrimaryKey().getEntityId();
//            for (String name : record.getProperties()) {
//
//                String shortName = name.substring("Property:".length());
//                //if(schemaFields.containsKey(shortName)){
//                doc.addField(shortName, record.getValue(name));
//                //}
//                System.out.println("name: " + shortName + " val: " + record.getValue(name));
//            }
//        }
//        server.add(doc);
//        server.commit();
//    }
//
//    public void insertDoc(String id, Map<String, String> fields) throws Exception {
//        SolrInputDocument doc = new SolrInputDocument();
//        for (String name : fields.keySet()) {
//            doc.addField(name, fields.get(name));
//        }
//        server.add(doc);
//        server.commit();
//    }
//
//    public void updateDoc(String id, Map<String, String> fields) throws Exception {
//        insertDoc(id, fields);
//    }
//
//    public void removeDoc(String id) throws Exception {
//
//        server.deleteById(id);
//        server.commit();
//    }

    /* Search implements */
    @Override
    public SearchResult getPagefromSearch(String vdId, String searchArgument, int pageOffset, int pageSize, Context ctx) {

        //System.out.println("vd: " + vd.getEntity().toString());
        ArrayList<Record> records = new ArrayList<Record>();

        return search(searchArgument, pageOffset,
                pageSize);
        // TODO - missing info from entity to create record preview
    }

    @Override
    public SearchResult getPagefromSearch(String searchArgument, int pageOffset, int pageSize, Context ctx) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void index(Record record) {
        try {
            for (String p : record.getProperties()) {
                processProperty(p, record.getValue(p));
            }

        } catch (Exception ex) {
            Logger.getLogger(SolrIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void processXML(File file) throws Exception{
        logger.log(Level.INFO, "Sending {0} to index ...", file.getAbsolutePath());
        StreamResult destStream = new StreamResult(new StringWriter());
        transformer.transform(new StreamSource(file), destStream);
        StringWriter sw = (StringWriter) destStream.getWriter();
        SolrIndexerCommiter.postData(this.solrUrl, sw.toString());
    }
    
    public void processXML(Document doc) throws Exception{
        logger.log(Level.INFO, "Sending to index ...");
        StreamResult destStream = new StreamResult(new StringWriter());
        transformer.transform(new DOMSource(doc), destStream);
        StringWriter sw = (StringWriter) destStream.getWriter();
        SolrIndexerCommiter.postData(this.solrUrl, sw.toString());
    }
    
    
    
    public void clean() throws Exception{
        logger.log(Level.INFO, "Cleaning index...");
        String s = "<delete><query>*:*</query></delete>";
        SolrIndexerCommiter.postData(this.solrUrl, s);
        commit();
        logger.log(Level.INFO, "Index cleaned");
    }
    public void delete(String id) throws Exception{
        logger.log(Level.INFO, "deleting from index id: {0}", id);
        String s = "<delete><id>"+id+"</id></delete>";
        SolrIndexerCommiter.postData(this.solrUrl, s);
        logger.log(Level.INFO, "{0} deleted from index", id);
    }
    
    public void processXML(String xml) throws Exception{
        logger.log(Level.INFO, "Sending to index ...");
        StreamResult destStream = new StreamResult(new StringWriter());
        transformer.transform(new StreamSource(new StringReader(xml)), destStream);
        StringWriter sw = (StringWriter) destStream.getWriter();
        SolrIndexerCommiter.postData(this.solrUrl, sw.toString());
    }
    
    public void processXML(String xml, String uniqueCode, String codeType, String identifier) throws Exception{
        logger.log(Level.INFO, "Sending {0} to index ...", identifier);
        StreamResult destStream = new StreamResult(new StringWriter());
        transformer.setParameter("uniqueCode", uniqueCode);
        transformer.setParameter("codeType", codeType);
        transformer.transform(new StreamSource(new StringReader(xml)), destStream);
        StringWriter sw = (StringWriter) destStream.getWriter();
        SolrIndexerCommiter.postData(this.solrUrl, sw.toString());
    }

    @SuppressWarnings("unchecked")
    private void processProperty(String name, Object value) throws Exception {
        if (name.startsWith("Property")) {
            String shortName = name.substring("Property:".length());
            if ("sourcexml".equalsIgnoreCase(shortName)) {
                if (value != null) {
                    StreamResult destStream = new StreamResult(new StringWriter());
                    transformer.transform(new StreamSource(new StringReader((String) value)), destStream);
                    StringWriter sw = (StringWriter) destStream.getWriter();
                    //logger.info(sw.toString());
                    SolrIndexerCommiter.postData(this.solrUrl, sw.toString());
                    //commiter.commit(true);
                }
            }
        } else if (name.startsWith("Collection")) {
            if (value != null) {
                ArrayList<Record> r = (ArrayList<Record>) value;
                for (Record record : r) {
                    for (String p : record.getProperties()) {
                        processProperty(p, record.getValue(p));

                    }
                }
            }
        }
    }

    @Override
    public void index(EntityDTO entityDTO) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SearchResult search(String searchArgument, String type, int offset, int size) {

        try {
            SolrQuery query = new SolrQuery();
            query.setQuery(searchArgument);
            query.setStart(offset);
            query.setRows(size);
            QueryResponse response = server.query(query);


            List<Record> records = new ArrayList<Record>();
            for (SolrDocument sd : response.getResults()) {
                PrimaryKey pk = new PrimaryKey("Zaznam", Integer.parseInt(sd.get("id").toString()));
                Record record = new Record(pk);
                record.setPreview("<b>" + sd.getFieldValue("Zaznam.hlavniNazev") + "</b>(" + sd.getFieldValue("Zaznam.knihovna") + ")");
                records.add(record);
            }
            return new SearchResult(records, response.getResults().getNumFound());
        } catch (SolrServerException ex) {
            Logger.getLogger(SolrIndexer.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public SearchResult search(String searchArgument, int offset, int size) {
        return search(searchArgument, null, offset, size);
    }

    @Override
    public void update(PrimaryKey primaryKey) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void insert(PrimaryKey primaryKey) {
        Record newRecord = persister.getCompleteRecord(primaryKey,
                DEFAULT_TRAVERSE_LEVEL, DEFAULT_INCLUDE_COLLECTIONS, tx);
        index(newRecord);
    }

    @Override
    public void delete(PrimaryKey primaryKey) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(Transaction tx) {
        this.tx = tx;
    }
}
