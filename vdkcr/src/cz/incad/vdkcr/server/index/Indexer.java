package cz.incad.vdkcr.server.index;

import com.typesafe.config.Config;
import java.util.Map;

/**
 *
 * @author alberto
 */
public interface Indexer {
    public void config(Config config) throws Exception;
    public void finish() throws Exception;
    public void insertDoc(String id, Map<String, String> fields) throws Exception;
    public void updateDoc(String id, Map<String, String> fields) throws Exception;
    public void removeDoc(String id) throws Exception;
}
