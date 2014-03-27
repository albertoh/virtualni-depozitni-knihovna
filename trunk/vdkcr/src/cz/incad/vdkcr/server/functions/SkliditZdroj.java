package cz.incad.vdkcr.server.functions;

import cz.incad.vdkcr.server.Structure;
import cz.incad.vdkcr.server.data.Sklizen;
import cz.incad.vdkcr.server.data.SklizenStatus;
import cz.incad.vdkcr.server.datasources.AbstractPocessDataSource;
import cz.incad.vdkcr.server.datasources.DataSource;
import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.server.Context;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.aplikator.server.data.RecordUtils.getValue;
import static org.aplikator.server.data.RecordUtils.newSubrecord;
import static org.aplikator.server.data.RecordUtils.getValue;
import static org.aplikator.server.data.RecordUtils.newSubrecord;
import org.aplikator.server.descriptor.WizardPage;

public class SkliditZdroj extends Executable {

    Logger log = Logger.getLogger(SkliditZdroj.class.getName());
    
    public FunctionResult run(Record zdroj, Context context, boolean asThread){
        Record sklizen = null;
        try {

            String dsName = getValue(zdroj, Structure.zdroj.trida);
            DataSource ds = null;
            try {
                ds = (DataSource) Class.forName(dsName).newInstance();

            } catch (Throwable e) {
                log.log(Level.SEVERE, "Cannot instantiate datasource", e);
                throw e;
            }
            String parametrySklizne = getValue(zdroj, Structure.zdroj.parametry);
            RecordContainer rc = new RecordContainer();
            sklizen = newSubrecord(zdroj.getPrimaryKey(), Structure.zdroj.sklizen);
            Structure.sklizen.pocet.setValue(sklizen, 0);
            Structure.sklizen.spusteni.setValue(sklizen, new Date());
            Structure.sklizen.stav.setValue(sklizen, SklizenStatus.Stav.ZAHAJEN.getValue());
            rc.addRecord(null, sklizen, sklizen, Operation.CREATE);
            rc = context.getAplikatorService().processRecords(rc);



            //int sklizeno = ds.harvest(parametrySklizne, rc.getRecords().get(0).getEdited(), context);

            if (ds instanceof AbstractPocessDataSource && asThread) {
                ((AbstractPocessDataSource) ds).runHarvestAsProcess(parametrySklizne, rc.getRecords().get(0).getEdited(), context);
                
                /*
                rc = new RecordContainer();
                Structure.sklizen.stav.setValue(sklizen, Sklizen.Stav.UKONCEN.getValue());
                Structure.sklizen.ukonceni.setValue(sklizen, new Date());
                rc.addRecord(null, sklizen, sklizen, Operation.UPDATE);
                rc = context.getAplikatorService().processRecords(rc);
                */
                
                return new FunctionResult("Proces bezi ... ", true);


            } else {
                int sklizeno = ds.harvest(parametrySklizne, rc.getRecords().get(0).getEdited(), context);

                rc = new RecordContainer();
                Structure.sklizen.stav.setValue(sklizen, SklizenStatus.Stav.UKONCEN.getValue());
                Structure.sklizen.ukonceni.setValue(sklizen, new Date());
                rc.addRecord(null, sklizen, sklizen, Operation.UPDATE);
                rc = context.getAplikatorService().processRecords(rc);

                return new FunctionResult("Sklizeno " + sklizeno + " záznamů ze zdroje " + zdroj.getValue(Structure.zdroj.nazev.getId()), true);

            }

        } catch (Throwable t) {

            RecordContainer rc = new RecordContainer();
            Structure.sklizen.stav.setValue(sklizen, SklizenStatus.Stav.CHYBA.getValue());
            rc.addRecord(null, sklizen, sklizen, Operation.UPDATE);
            rc = context.getAplikatorService().processRecords(rc);

            return new FunctionResult("Sklizeň zdroje " + zdroj.getValue(Structure.zdroj.nazev.getId()) + "selhala: " + t, false);
        }
    }

    @Override
    public FunctionResult execute(FunctionParameters functionParameters, Context context) {
        Record zdroj = functionParameters.getClientContext().getCurrentRecord();
        return run(zdroj, context, true);
    }

    @Override
    public WizardPage getWizardPage(String currentPage, boolean forwardFlag, Record currentProcessingRecord, Record clientParameters, Context context) {
        return null;
    }
}
