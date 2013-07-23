package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import cz.incad.vdkcr.server.utils.JDBCQueryTemplate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.aplikator.client.shared.data.ListItem;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.Collection;
import static org.aplikator.server.descriptor.Panel.column;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.ListProvider;
import org.aplikator.server.descriptor.Property;
import static org.aplikator.server.descriptor.RepeatedForm.repeated;
import org.aplikator.server.descriptor.View;
import org.aplikator.server.persistence.PersisterFactory;
import org.aplikator.server.persistence.PersisterTriggers;

public class UniqueDoc extends Entity {
    public Property<String> code;
    public Property<String> nazev;
    public Collection<Zaznam> zaznam;

    public UniqueDoc() {
        super("uniquedoc","uniquedoc","UniqueDoc_ID");
        initFields();
    }

    private void initFields() {
        code = stringProperty("code");
        nazev = stringProperty("nazev");
        zaznam = collectionProperty(Structure.zaznam, "zaznam", "uniquedoc");
        addIndex("code_uniquedoc_idx", true, code);
        
        this.setPersistersTriggers(new PersisterTriggers.Default() {
            @Override
            public void onLoad(Record record, Context ctx) {
                record.setPreview("<b>"+record.getValue(Structure.uniquedoc.nazev.getId())
                +"</b> (" + record.getValue(Structure.uniquedoc.code.getId()) + ")");
            }
        });
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(code);
        retval.addProperty(nazev);
        retval.form(column(
                nazev,
                repeated(zaznam, Structure.zaznam.view())
            ), false);
        return retval;
    }

}
