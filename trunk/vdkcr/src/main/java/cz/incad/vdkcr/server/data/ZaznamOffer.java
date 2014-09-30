package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import static org.aplikator.server.descriptor.Panel.column;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.Reference;
import org.aplikator.server.descriptor.View;

public class ZaznamOffer extends Entity {
    public Property<String> uniqueCode;
    public Property<String> zaznam;
    public Property<String> exemplar;
    public Reference<Knihovna> knihovna;
    public Reference<Offer> offer;
    public Property<String> fields;
    
    public ZaznamOffer() {
        super("ZaznamOffer","ZaznamOffer", "ZaznamOffer_ID");
        initFields();
    }

    private void initFields() {
        uniqueCode = stringProperty("uniqueCode");
        zaznam = stringProperty("zaznam");
        exemplar = stringProperty("exemplar");
        fields = stringProperty("fields");
        knihovna = referenceProperty(Structure.knihovna, "knihovna");
        offer = referenceProperty(Structure.offer, "offer");
        addIndex("id_kn_nabidky_idx", true, zaznam, knihovna, offer);

    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(zaznam).addProperty(knihovna);
        retval.form(column(
                uniqueCode,
                zaznam,
                knihovna,
                offer,
                fields
            ));
        return retval;
    }

}
