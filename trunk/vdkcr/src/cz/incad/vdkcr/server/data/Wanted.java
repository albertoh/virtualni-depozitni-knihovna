package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import static org.aplikator.server.descriptor.Panel.column;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.Reference;
import org.aplikator.server.descriptor.View;

public class Wanted extends Entity {
    public Property<String> zaznam;
    public Reference<Knihovna> knihovna;
    public Property<Boolean> wants;

    public Wanted() {
        super("Wanted","Wanted", "Wanted_ID");
        initFields();
    }

    private void initFields() {
        zaznam = stringProperty("zaznam");
        wants = booleanProperty("wants");
        knihovna = referenceProperty(Structure.knihovna, "knihovna");
        addIndex("id_kn_wanted_idx", true, zaznam, knihovna);

    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(zaznam).addProperty(knihovna);
        retval.form(column(
                zaznam,
                knihovna
            ));
        return retval;
    }

}
