package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;
import static org.aplikator.server.descriptor.RepeatedForm.repeated;

import org.aplikator.server.descriptor.Collection;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

import cz.incad.vdkcr.server.functions.SkliditZdroj;

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
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(nazev).addProperty(typZdroje).addProperty(trida);
        retval.form(column(
                row(nazev,typZdroje, formatXML),
                row(trida, parametry),
                row(cron),
                row(skliditZdroj),
                row(repeated(sklizen))
            ));
        return retval;
    }

}
