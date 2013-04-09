package cz.incad.vdkcr.server.datasources;

import static org.aplikator.server.data.RecordUtils.*;

import java.util.logging.Logger;

import org.aplikator.client.shared.data.Operation;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.data.RecordContainer;
import org.aplikator.client.shared.rpc.impl.ProcessRecords;
import org.aplikator.server.Context;
import org.aplikator.server.util.Configurator;

import cz.incad.vdkcr.server.Structure;

public class Random implements DataSource {

    Logger log = Logger.getLogger(Random.class.getName());

    @Override
    public int harvest(String params, Record sklizen, Context context) {
        //ukázka, jak použít parametry harvestu
        String userHome = Configurator.get().getConfig().getString(Configurator.HOME);//takto se čtou property z konfigurace aplikatoru
        String configFileName = userHome+System.getProperty("file.separator")+params;//tady by mohl být konfigurační soubor pro harvester, jehož jméno je zadáno ve formuláři - pole parametry
        log.info("Random harvester config file name: "+configFileName);

        int i = 0;
        while (i < 5) {
            i++;
            importRecord(sklizen, context, i);
        }
        return i;
    }

    private void importRecord(Record sklizen, Context context, int i) {
        RecordContainer rc = new RecordContainer(); //nový prázdný kontejner

        Record zaznam = newRecord(Structure.zaznam); //nový záznam pro tabulku zaznam
        Structure.zaznam.hlavniNazev.setValue(zaznam, "Náhodný název " + Math.random());//nastavit hodnoty v polích nového záznamu
        Structure.zaznam.sklizen.setValue(zaznam, sklizen.getPrimaryKey().getId());//nastavit referenci na související záznam sklizně
        Structure.zaznam.sourceXML.setValue(zaznam, "<xml>Náhodné xml " + Math.random() + " </xml>");
        rc.addRecord(null, zaznam, zaznam, Operation.CREATE);//přidat záznam do kontejneru

        Record identifikator = newSubrecord(zaznam.getPrimaryKey(), Structure.zaznam.identifikator);//nový záznam pro opakované pole (tabulku) identifikátor
        Structure.identifikator.hodnota.setValue(identifikator, "Náhodný id " + Math.random());//opět nastavit hodnoty v záznamu opakovaného pole
        Structure.identifikator.typ.setValue(identifikator, "ISSN");
        rc.addRecord(null, identifikator, identifikator, Operation.CREATE);//přidat záznam opakovaného pole do kontejneru

        Structure.sklizen.pocet.setValue(sklizen, i);  //aktualizovat hodnotu o počtu sklizených titulů v záznamu sklizně
        rc.addRecord(null, sklizen, sklizen, Operation.UPDATE); //přidat záznam sklizně do kontejneru pro aktualizaci

        rc = context.getAplikatorService().processRecords(rc);  //příkaz ProcessRecords uloží všechny záznamy v kontejenru do databáze (v jediné nové transakci) a vrátí zpět kontejner s aktualizovanými daty (tedy dočasné primární klíče nahradí skutečnými)

    }

}
