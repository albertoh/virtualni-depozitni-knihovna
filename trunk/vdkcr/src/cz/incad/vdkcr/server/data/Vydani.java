package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class Vydani extends Entity {
    public Property<String> oznaceni;
    public Property<String> nakladatel;
    public Property<String> misto;
    public Property<String> datum;

    public Vydani() {
        super("Vydani","Vydani","Vydani_ID");
        initFields();
    }

    protected void initFields() {
        oznaceni = stringProperty("oznaceni");
        nakladatel = stringProperty("nakladatel");
        misto = stringProperty("misto");
        datum = stringProperty("datum");
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(oznaceni).addProperty(nakladatel);
        retval.form(
                row(oznaceni,nakladatel,misto,datum),
                false
            );
        return retval;
    }

}
