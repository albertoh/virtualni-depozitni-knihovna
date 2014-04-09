package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import cz.incad.vdkcr.server.utils.JDBCQueryTemplate;
import org.aplikator.client.shared.data.ListItem;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.ListProvider;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;
import org.aplikator.server.persistence.PersisterFactory;
import org.aplikator.server.persistence.PersisterTriggers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.aplikator.server.descriptor.Panel.column;

public class Status extends Entity {
    public Property<String> code;
    public Property<String> nazev;

    public Status() {
        super("Status","Status","Status_ID");
        initFields();
    }

    private void initFields() {
        code = stringProperty("code");
        nazev = stringProperty("nazev", 512);
        addIndex("code_status_idx", true, code);
        
        
        
        this.setPersistersTriggers(new PersisterTriggers.Default() {
            @Override
            public void onLoad(Record record, Context ctx) {
                record.setPreview("<b>"+record.getValue(Structure.status.nazev.getId())
                +"</b>");
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

    @SuppressWarnings("unchecked")
    public static ListProvider getGroupList() {
        String query = "select code, nazev from status";
        
        List groupsList = new JDBCQueryTemplate<ListItem>(PersisterFactory.getPersister().getJDBCConnection()) {
            @Override
            public boolean handleRow(ResultSet rs, List retList) throws SQLException {
                String id = rs.getString("code");
                String name = rs.getString("nazev");
                retList.add(new ListItem.Default(id, name));
                return true;
            }

        }.executeQuery(query);

        return new ListProvider.Default(groupsList);
    }

}
