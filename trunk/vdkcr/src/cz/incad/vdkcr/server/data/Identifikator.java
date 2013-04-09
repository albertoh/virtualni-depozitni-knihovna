package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class Identifikator extends Entity {
    public Property<String> typ;
    public Property<String> hodnota;

    public Identifikator() {
        super("Identifikator","Identifikator","Identifikator_ID");
        initFields();
    }

    protected void initFields() {
        typ = stringProperty("typ");
        hodnota = stringProperty("hodnota");
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(typ).addProperty(hodnota);
        retval.form(
                row(typ,hodnota)
            );
        return retval;
    }


}
