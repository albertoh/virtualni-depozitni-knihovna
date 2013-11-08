package cz.incad.vdkcr.server.data;

import org.aplikator.server.data.BinaryData;
import static org.aplikator.server.descriptor.Panel.column;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class Offer extends Entity {
    public Property<String> nazev;
    public Property<BinaryData> bData;

    public Offer() {
        super("Offer","Offer","Offer_ID");
        initFields();
    }

    private void initFields() {
        nazev = stringProperty("nazev", 512);
        bData = binaryProperty("bdata");

    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(nazev);
        retval.form(column(
                nazev,
                bData
            ));
        return retval;
    }

}
