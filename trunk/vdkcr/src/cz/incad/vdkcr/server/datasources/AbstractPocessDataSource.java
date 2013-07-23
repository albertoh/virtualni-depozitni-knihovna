/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.datasources;

import cz.incad.vdkcr.server.Structure;
import cz.incad.vdkcr.server.data.Sklizen;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.server.Context;
import org.aplikator.server.processes.CannotCallStartException;
import org.aplikator.server.processes.ProcessFactory;
import org.aplikator.server.processes.ProcessType;
import org.aplikator.server.processes.RunnableSerializationAware;

/**
 *
 * @author alberto
 */
public abstract class AbstractPocessDataSource implements DataSource {


    public AbstractPocessDataSource() {
    }

    public class _MRun implements RunnableSerializationAware {

        private final String params;
        private final Record sklizen;
        private final Context ctx;

        public _MRun(String params, Record sklizen, Context ctx) {
            this.params = params;
            this.sklizen = sklizen;
            this.ctx = ctx;
        }

        @Override
        public void run() {
            try {
                int harvest = harvest(params, sklizen, ctx);
                
                RecordContainer rc = new RecordContainer();
                Structure.sklizen.stav.setValue(sklizen, Sklizen.Stav.UKONCEN.getValue());
                Structure.sklizen.ukonceni.setValue(sklizen, new Date());
                rc.addRecord(null, sklizen, sklizen, Operation.UPDATE);
                rc = ctx.getAplikatorService().processRecords(rc);
                
            } catch (Exception ex) {
                Logger.getLogger(AbstractPocessDataSource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void runHarvestAsProcess(String params, Record sklizen, Context ctx) {
        org.aplikator.server.processes.Process process2 = ProcessFactory.get(ProcessType.THREAD).create(new _MRun(params,sklizen,ctx));
            try {
            process2.startMe();
            } catch (Exception ex) {
                Logger.getLogger(AbstractPocessDataSource.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
}
