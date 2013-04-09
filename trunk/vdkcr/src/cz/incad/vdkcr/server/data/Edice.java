package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class Edice extends Entity {
    public Property<String> nazev;
    public Property<String> cisloCasti;
    public Property<String> nazevCasti;
    public Property<String> svazek;
    public Property<String> issn;

    public Edice() {
        super("Edice","Edice","Edice_ID");
        initFields();
    }

    protected void initFields() {
        nazev = stringProperty("nazev");
        cisloCasti = stringProperty("cisloCasti");
        nazevCasti = stringProperty("nazevCasti");
        svazek = stringProperty("svazek");
        issn = stringProperty("issn");
    }


    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(nazev).addProperty(cisloCasti).addProperty(nazevCasti).addProperty(svazek).addProperty(svazek).addProperty(issn);
        retval.form(column(
                nazev,
                row(cisloCasti,nazevCasti),
                row(svazek,issn)
            ));
        return retval;
    }
}
