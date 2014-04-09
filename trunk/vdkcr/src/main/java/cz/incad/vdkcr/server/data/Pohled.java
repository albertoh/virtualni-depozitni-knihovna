package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.column;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class Pohled extends Entity {
    public Property<String> query;
    public Property<String> nazev;

    public Pohled() {
        super("Pohled","Pohled","Pohled_ID");
        initFields();
    }

    private void initFields() {
        query = stringProperty("query");
        nazev = stringProperty("nazev", 512);
        addIndex("code_pohled_idx", true, query);

    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(query).addProperty(nazev);
        retval.form(column(
                query,
                nazev
            ));
        return retval;
    }

}
