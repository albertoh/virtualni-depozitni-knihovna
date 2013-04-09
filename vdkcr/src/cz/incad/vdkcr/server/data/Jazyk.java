package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class Jazyk extends Entity {
    public Property<String> kod;

    public Jazyk() {
        super("Jazyk","Jazyk","Jazyk_ID");
        initFields();
    }

    protected void initFields() {
        kod = stringProperty("kod");
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(kod);
        retval.form(
                row(kod)
            );
        return retval;
    }

}
