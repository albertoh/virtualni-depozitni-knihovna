package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import org.aplikator.server.descriptor.Collection;
import static org.aplikator.server.descriptor.Panel.column;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class Knihovna extends Entity {
    public Property<String> code;
    public Property<String> nazev;
    public Collection<Pohled> pohled;

    public Knihovna() {
        super("Knihovna","Knihovna","Knihovna_ID");
        initFields();
    }

    private void initFields() {
        code = stringProperty("code");
        nazev = stringProperty("nazev", 512);
        pohled = collectionProperty(Structure.pohled, "identifikator", "zaznam");
        addIndex("code_knihovna_idx", true, code);

    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(code).addProperty(nazev);
        retval.form(column(
                code,
                nazev
            ));
        return retval;
    }

}
