package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class Rozsah extends Entity {
    public Property<String> strankovani;
    public Property<String> vybaveni;
    public Property<String> rozmer;

    public Rozsah() {
        super("Rozsah","Rozsah","Rozsah_ID");
        initFields();
    }

    protected void initFields() {
        strankovani = stringProperty("strankovani");
        vybaveni = stringProperty("vybaveni");
        rozmer = stringProperty("rozmer");
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(strankovani).addProperty(vybaveni).addProperty(rozmer);
        retval.form(
                row(strankovani, vybaveni, rozmer)
            );
        return retval;
    }
}
