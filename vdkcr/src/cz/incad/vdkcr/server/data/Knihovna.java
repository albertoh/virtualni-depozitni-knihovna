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
import org.aplikator.server.descriptor.View;
import org.aplikator.server.persistence.PersisterFactory;
import org.aplikator.server.persistence.PersisterTriggers;

public class Knihovna extends Entity {
    public Property<String> code;
    public Property<String> nazev;
    public Collection<Pohled> pohled;

    public Knihovna() {
        super("Knihovna","Knihovna","Knihovna_ID");
        initFields();
    }

    protected void initFields() {
        code = stringProperty("code");
        nazev = stringProperty("nazev", 512);
        pohled = collectionProperty(Structure.pohled, "identifikator", "zaznam");
        addIndex("code_knihovna_idx", true, code);
        
        this.setPersistersTriggers(new PersisterTriggers.Default() {
            @Override
            public void onLoad(Record record, Context ctx) {
                record.setPreview("<b>"+record.getValue(Structure.knihovna.nazev.getId())
                +"</b> ("+record.getValue(Structure.knihovna.code.getId())+")");
            }
            
        });

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
    
    public static ListProvider<String> getGroupList() {
        String query = "select code, nazev from knihovna";
        
        List<ListItem<String>> groupsList = new JDBCQueryTemplate<ListItem<String>>(PersisterFactory.getPersister().getJDBCConnection()) {
            @Override
            public boolean handleRow(ResultSet rs, List<ListItem<String>> retList) throws SQLException {
                String id = rs.getString("code");
                String name = rs.getString("nazev");
                retList.add(new ListItem.Default<String>(id, name));
                return true;
            }

        }.executeQuery(query);

        return new ListProvider.Default<String>(groupsList);
    }

}
