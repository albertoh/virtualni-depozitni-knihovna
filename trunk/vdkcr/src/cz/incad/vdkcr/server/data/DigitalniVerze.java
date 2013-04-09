package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class DigitalniVerze extends Entity {
    public Property<String> url;

    public DigitalniVerze() {
        super("DigitalniVerze","DigitalniVerze","DigitalniVerze_ID");
        initFields();
    }

    protected void initFields() {
        url = stringProperty("url");
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(url);
        retval.form(
                row(url)
            );
        return retval;
    }

}
