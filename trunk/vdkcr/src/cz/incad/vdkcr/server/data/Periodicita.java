package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class Periodicita extends Entity {
    public Property<String> typ;
    public Property<String> platnost;

    public Periodicita() {
        super("Periodicita","Periodicita","Periodicita_ID");
        initFields();
    }

    protected void initFields() {
        typ = stringProperty("typ");
        platnost = stringProperty("platnost");
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(typ).addProperty(platnost);
        retval.form(
                row(typ,platnost)
            );
        return retval;
    }

}
