package cz.incad.vdkcr.server.datasources;

import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;

public interface DataSource {

    public int harvest(String params, Record sklizen, Context ctx ) throws Exception;

}
