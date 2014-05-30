package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import cz.incad.vdkcr.server.functions.KnihovnaPohled;
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
import org.aplikator.server.descriptor.Function;
//import org.aplikator.server.descriptor.Link;
import org.aplikator.server.descriptor.ListProvider;
import static org.aplikator.server.descriptor.Panel.row;
import org.aplikator.server.descriptor.Property;
import static org.aplikator.server.descriptor.RepeatedForm.repeated;
import org.aplikator.server.descriptor.View;
import org.aplikator.server.descriptor.WizardPage;
import org.aplikator.server.persistence.PersisterFactory;
import org.aplikator.server.persistence.PersisterTriggers;

public class Knihovna extends Entity {
    public Property<String> code;
    public Property<String> nazev;
    public Property<String> heslo;
    public Property<String> userrole;
    public Property<String> email;
    public Property<String> telefon;
    public Collection<Pohled> pohled;
    public Function knihovnaPohled = new Function("KnihovnaPohled", "KnihovnaPohled", new KnihovnaPohled());
    
     {

        WizardPage p1 = new WizardPage(knihovnaPohled, "first");
        Property<String> p1input = p1.stringProperty("finput", 3);
        Property<String> status = p1.stringProperty("status", 3);
        status.setListProvider(Status.getGroupList());
        p1.form(row(
                column(p1input, status)
        ), false);
        
        WizardPage p2 = new WizardPage(knihovnaPohled, "second");
        Property<String> p2prop = p2.stringProperty("sinput", 3);
        
        
        
        p2.form(
                row(
                    row(column(p2prop))
                
        ), false);

        
    }

    public Knihovna() {
        super("Knihovna","Knihovna","Knihovna_ID");
        initFields();
    }

    private void initFields() {
        code = stringProperty("code");
        nazev = stringProperty("nazev", 512);
        userrole = stringProperty("userrole");
        heslo = stringProperty("heslo", 128);
        email = stringProperty("email", 255);
        telefon = stringProperty("telefon");
        pohled = collectionProperty(Structure.pohled, "knihovna", "knihovna");
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
//        Link link = new Link("view", "org.aplikator.client.local.command.ListEntities:View:Zaznam", code);
        retval.addProperty(code).addProperty(nazev);
        retval.form(column(
                row(column(code).setSize(3), column(nazev).setSize(6), column(userrole).setSize(2)),
                row(column(telefon).setSize(3), column(email).setSize(8)),// column(link).setSize(2)),
                row(knihovnaPohled),
                row(repeated(pohled))
            ));
        return retval;
    }
    
    public static ListProvider getGroupList() {
        String query = "select code, nazev from knihovna";
        
        List<ListItem> groupsList = new JDBCQueryTemplate<ListItem>(PersisterFactory.getPersister().getJDBCConnection()) {
            @Override
            public boolean handleRow(ResultSet rs, List<ListItem> retList) throws SQLException {
                String id = rs.getString("code");
                String name = rs.getString("nazev");
                retList.add(new ListItem.Default(id, name));
                return true;
            }

        }.executeQuery(query);

        return new ListProvider.Default(groupsList);
    }

}
