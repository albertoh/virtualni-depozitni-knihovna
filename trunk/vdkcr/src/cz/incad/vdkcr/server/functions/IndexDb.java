/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.vdkcr.server.functions;

import cz.incad.vdkcr.server.index.solr.Reindex;
import java.util.logging.Logger;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

/**
 *
 * @author alberto
 */
public class IndexDb extends Executable {
    
    static final Logger log = Logger.getLogger(IndexDb.class.getName());

    @Override
    public FunctionResult execute(FunctionParameters parameters, Context context) {
        Record sklizen = null;
        try {
            Reindex ri = new Reindex();
            ri.runHarvestAsProcess(null, null, context);
            return new FunctionResult("Proces bezi ... ", true);
        } catch (Throwable t) {
            return new FunctionResult("Index db selhala: " + t, false);
        }
    }
    
}
