package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class Nazev extends Entity {

    public Property<String> typNazvu;
    public Property<String> nazev;

    public Nazev() {
        super("Nazev","Nazev","Nazev_ID");
        initFields();
    }

    protected void initFields() {
        typNazvu = stringProperty("typNazvu");
        nazev = stringProperty("nazev", 2048);
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(typNazvu).addProperty(nazev);
        retval.form(
                row(typNazvu,nazev)
            );
        return retval;
    }

}
