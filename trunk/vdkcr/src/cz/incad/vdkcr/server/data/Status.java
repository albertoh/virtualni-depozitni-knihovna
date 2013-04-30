package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.utils.JDBCQueryTemplate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.aplikator.client.shared.data.ListItem;
import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.ListProvider;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;
import org.aplikator.server.persistence.PersisterFactory;

public class Status extends Entity {
    public Property<String> code;
    public Property<String> nazev;

    public Status() {
        super("Status","Status","Status_ID");
        initFields();
    }

    protected void initFields() {
        code = stringProperty("code");
        nazev = stringProperty("nazev", 512);
        addIndex("code_status_idx", true, code);

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
        String query = "select code, name from status";
        
        List<ListItem<String>> groupsList = new JDBCQueryTemplate<ListItem<String>>(PersisterFactory.getPersister().getJDBCConnection()) {
            @Override
            public boolean handleRow(ResultSet rs, List<ListItem<String>> retList) throws SQLException {
                String id = rs.getString("code");
                String name = rs.getString("name");
                retList.add(new ListItem.Default<String>(id, name));
                return true;
            }

        }.executeQuery(query);

        return new ListProvider.Default<String>(groupsList);
    }

}
