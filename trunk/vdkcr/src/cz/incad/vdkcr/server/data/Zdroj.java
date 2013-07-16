package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;
import static org.aplikator.server.descriptor.RepeatedForm.repeated;

import org.aplikator.server.descriptor.Collection;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

import cz.incad.vdkcr.server.functions.SkliditZdroj;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.PersisterTriggers;

public class Zdroj extends Entity {

    public Property<String> typZdroje;
    public Property<String> nazev;
    public Property<String> formatXML;
    public Property<String> trida;
    public Property<String> parametry;
    public Property<String> cron;
    public Collection<Sklizen> sklizen;

    public Function skliditZdroj = new Function("SkliditZdroj", "SkliditZdroj", new SkliditZdroj());
    

    public Zdroj() {
        super("Zdroj","Zdroj","Zdroj_ID");
        initFields();
    }

    protected void initFields() {
        typZdroje = stringProperty("typZdroje");
        nazev = stringProperty("nazev");
        formatXML = stringProperty("formatXML");
        trida = stringProperty("trida");
        parametry = stringProperty("parametry");
        cron = stringProperty("cron");
        
        this.setPersistersTriggers(new PersisterTriggers.Default() {

            @Override
            public void onLoad(Record record, Context ctx) {
                record.setPreview("<b>"+record.getValue(Structure.zdroj.nazev.getId())
                +"</b> ("+record.getValue(Structure.zdroj.typZdroje.getId())+")"
                );
            }
            
        });
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(nazev).addProperty(typZdroje);
        retval.form(column(
                row(column(nazev).setSize(4),column(typZdroje).setSize(3),column(formatXML).setSize(3)),
                row(trida.widget().setSize(4), parametry.widget().setSize(6)),
                row(cron),
                row(skliditZdroj),
                row(repeated(sklizen))
            ), false);
        
        return retval;
    }

}
