package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class Autor extends Entity {
    public Property<String> prijmeni;
    public Property<String> jmeno;
    public Property<String> nazev;
    public Property<String> datumNarozeni;
    public Property<String> datumUmrti;
    public Property<String> odpovednost;

    public Autor() {
        super("Autor","Autor","Autor_ID");
        initFields();
    }

    protected void initFields() {
        prijmeni = stringProperty("prijmeni");
        jmeno = stringProperty("jmeno");
        nazev = stringProperty("nazev", 512);
        datumNarozeni = stringProperty("datumNarozeni");
        datumUmrti = stringProperty("datumUmrti");
        odpovednost = stringProperty("odpovednost");

    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(prijmeni).addProperty(jmeno).addProperty(nazev).addProperty(odpovednost);
        retval.form(column(
                row(prijmeni,jmeno),
                row(datumNarozeni, datumUmrti),
                nazev,
                odpovednost
            ));
        return retval;
    }

}
