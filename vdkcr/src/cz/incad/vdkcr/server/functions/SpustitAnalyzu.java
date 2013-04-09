package cz.incad.vdkcr.server.functions;

import static org.aplikator.server.data.RecordUtils.getValue;
import static org.aplikator.server.data.RecordUtils.newSubrecord;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.client.shared.rpc.impl.ProcessRecords;
import org.aplikator.server.Context;
import org.aplikator.server.function.Executable;
import org.aplikator.server.function.FunctionParameters;
import org.aplikator.server.function.FunctionResult;

import cz.incad.vdkcr.server.Structure;
import cz.incad.vdkcr.server.analytics.Analytic;
import cz.incad.vdkcr.server.data.Analyza;

public class SpustitAnalyzu implements Executable {

    Logger log = Logger.getLogger(SpustitAnalyzu.class.getName());

    @Override
    public FunctionResult execute(FunctionParameters functionParameters, Context context) {
        Record modul = functionParameters.getClientContext().getCurrentRecord();
        Record analyza = null;
        try {

            String analyticName = getValue(modul, Structure.modul.trida);
            Analytic an = null;
            try {
                an = (Analytic) Class.forName(analyticName).newInstance();
            } catch (Throwable e) {
                log.log(Level.SEVERE, "Cannot instantiate analytic", e);
                throw e;
            }
            String parametryAnalyzy = getValue(modul, Structure.modul.parametry);
            RecordContainer rc = new RecordContainer();
            analyza = newSubrecord(modul.getPrimaryKey(), Structure.modul.analyza);
            Structure.analyza.spusteni.setValue(analyza, new Date());
            Structure.analyza.stav.setValue(analyza, Analyza.Stav.ZAHAJENA.getValue());
            rc.addRecord(null, analyza, analyza, Operation.CREATE);
            rc = context.getAplikatorService().processRecords(rc);

            an.analyze(parametryAnalyzy, rc.getRecords().get(0).getEdited(), context);

            /*
            rc = new RecordContainer();
            Structure.analyza.stav.setValue(analyza, Analyza.Stav.UKONCENA.getValue());
            Structure.analyza.ukonceni.setValue(analyza, new Date());
            rc.addRecord(null, analyza, analyza, Operation.UPDATE);
            rc = context.getAplikatorService().processRecords(rc);
            */
            
            return new FunctionResult("Analýza pro modul " + modul.getValue(Structure.modul.nazev.getId())+" bezi na pozadi. Pri dobehnuti bude zaznam upraven.", true);
        } catch (Throwable t) {
            log.log(Level.SEVERE, "Error analyzing: ", t);
            RecordContainer rc = new RecordContainer();
            Structure.analyza.stav.setValue(analyza, Analyza.Stav.CHYBA.getValue());
            rc.addRecord(null, analyza, analyza, Operation.UPDATE);
            rc = context.getAplikatorService().processRecords(rc);

            return new FunctionResult("Analýza pro modul " + modul.getValue(Structure.modul.nazev.getId()) + "selhala: " + t, false);
        }
    }

    @Override
    public FunctionResult execute(FunctionParameters parameters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
