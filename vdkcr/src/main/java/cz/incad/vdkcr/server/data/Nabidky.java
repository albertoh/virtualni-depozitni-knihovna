package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import static org.aplikator.server.descriptor.Panel.column;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.Reference;
import org.aplikator.server.descriptor.View;

public class Nabidky extends Entity {
    public Property<String> zaznam;
    public Property<String> exemplar;
    public Reference<Knihovna> knihovna;

    public Nabidky() {
        super("Nabidky","Nabidky", "Nabidky_ID");
        initFields();
    }

    private void initFields() {
        zaznam = stringProperty("zaznam");
        exemplar = stringProperty("exemplar");
        knihovna = referenceProperty(Structure.knihovna, "knihovna");
        addIndex("id_kn_nabidky_idx", true, zaznam, exemplar, knihovna);

    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(zaznam).addProperty(knihovna);
        retval.form(column(
                zaznam,
                exemplar,
                knihovna
            ));
        return retval;
    }

}
