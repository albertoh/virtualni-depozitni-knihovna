package cz.incad.vdkcr.server.index.solr;

import com.typesafe.config.Config;
import cz.incad.vdkcr.server.index.Indexer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 * @author alberto
 */
public class SolrIndexer implements Indexer {

    private static final Logger logger = Logger.getLogger(SolrIndexer.class.getName());
    private String host;
    private String collection;
    private int batchSize;
    HttpSolrServer server;
    Collection<SolrInputDocument> insertDocs = new ArrayList<SolrInputDocument>();
    List<String> delDocs = new ArrayList<String>();


    private void commit() throws Exception {
        server.commit();
    }

    private void check() throws Exception {
        if (insertDocs.size() > batchSize) {
            server.add(insertDocs);
            server.commit();
            insertDocs.clear();
        }
        if (delDocs.size() > batchSize) {
            server.deleteById(delDocs);
            server.commit();
            delDocs.clear();
        }
    }

    @Override
    public void config(Config config) throws Exception {
        this.host = config.getString("aplikator.solrHost");
        this.batchSize = config.getInt("aplikator.solrBatchSize");
        this.collection = config.getString("aplikator.solrCollection");
        

        server = new HttpSolrServer(host + "/" + collection);
        server.setMaxRetries(1); // defaults to 0.  > 1 not recommended.
        server.setConnectionTimeout(1000); // 5 seconds to establish TCP
        // Setting the XML response parser is only required for cross
        // version compatibility and only when one side is 1.4.1 or
        // earlier and the other side is 3.1 or later.
        server.setParser(new XMLResponseParser()); // binary parser is used by default
        // The following settings are provided here for completeness.
        // They will not normally be required, and should only be used 
        // after consulting javadocs to know whether they are truly required.
        server.setSoTimeout(1000);  // socket read timeout
        server.setDefaultMaxConnectionsPerHost(100);
        server.setMaxTotalConnections(100);
        server.setFollowRedirects(false);  // defaults to false
        // allowCompression defaults to false.
        // Server side must support gzip or deflate for this to have any effect.
        server.setAllowCompression(true);
    }

    @Override
    public void finish() throws Exception {
        server.add(insertDocs);
        server.deleteById(delDocs);
        server.commit();
        insertDocs.clear();
        delDocs.clear();
    }

    @Override
    public void insertDoc(String id, Map<String, String> fields) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        for (String name : fields.keySet()) {
            doc.addField(name, fields.get(name));
        }
        insertDocs.add(doc);
        check();
    }

    @Override
    public void updateDoc(String id, Map<String, String> fields) throws Exception {
        insertDoc(id, fields);
    }

    @Override
    public void removeDoc(String id) throws Exception {
        delDocs.add(id);
        check();
    }
}
