package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import static org.aplikator.server.descriptor.Panel.column;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.Reference;
import org.aplikator.server.descriptor.View;

public class Nabidky extends Entity {
    public Property<String> zaznam;
    public Reference<Knihovna> knihovna;
    public Reference<Offer> offer;

    public Nabidky() {
        super("Nabidky","Nabidky", "Nabidky_ID");
        initFields();
    }

    private void initFields() {
        zaznam = stringProperty("zaznam");
        knihovna = referenceProperty(Structure.knihovna, "knihovna");
        offer = referenceProperty(Structure.offer, "offer");
        addIndex("id_kn_nabidky_idx", true, zaznam, knihovna, offer);

    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(zaznam).addProperty(knihovna);
        retval.form(column(
                zaznam,
                knihovna,
                offer
            ));
        return retval;
    }

}
