package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import static org.aplikator.server.descriptor.Panel.column;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.Reference;
import org.aplikator.server.descriptor.View;

public class Demand extends Entity {
    public Property<String> nazev;
    public Reference<Knihovna> knihovna;
    public Property<Boolean> closed;

    public Demand() {
        super("Demand","Demand","Demand_ID");
        initFields();
    }

    private void initFields() {
        nazev = stringProperty("nazev", 512);
        knihovna = referenceProperty(Structure.knihovna, "knihovna");
        closed = booleanProperty("closed");

    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(nazev);
        retval.form(column(
                nazev,
                knihovna,
                closed
            ));
        return retval;
    }

}
