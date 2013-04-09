package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.functions.SpustitAnalyzu;
import cz.incad.vdkcr.server.utils.JDBCQueryTemplate;
import cz.incad.vdkcr.server.utils.PersisterUtils;
import org.aplikator.client.shared.data.ListItem;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.*;
import org.aplikator.server.descriptor.wizards.WizardView;
import org.aplikator.server.persistence.PersisterTriggers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;

public class Modul extends Entity {

    public static String STARTED_TASKS = "started_task";
    
    
    public Property<String> typModulu;
    public Property<String> nazev;
    public Property<String> formatXML;
    public Property<String> trida;
    public Property<String> parametry;
    public Collection<Analyza> analyza;
    
    public static class DefaultListItem implements ListItem<String>{
        private String name;
        private String value;

        public DefaultListItem(String name, String value) {
            this.name = name;
            this.value = value;
        }

        
        @Override
        public String getValue() {
            return this.value;
        }

        @Override
        public String getName() {
            return this.name;
        }
    } 
    
    public static List<ListItem<String>> readItems() {
        Connection conn = PersisterUtils.getConnection();
        return new JDBCQueryTemplate<ListItem<String>>(conn, true) {
            @Override
            public boolean handleRow(ResultSet rs, List<ListItem<String>> returnsList) throws SQLException {
                String name = rs.getString("Nazev");
                Integer i = rs.getInt("Zdroj_id");
                returnsList.add(new DefaultListItem(name, ""+i));
                return super.handleRow(rs, returnsList); //To change body of generated methods, choose Tools | Templates.
            }
        }.executeQuery("select Nazev, Zdroj_id from DEV_vdkcr.ZDROJ");
    }
    

    
    /** Vstupni analyza - funkce a wizard */
    public Function spustitAnalyzu = new Function("SpustitAnalyzu", "SpustitAnalyzu", new SpustitAnalyzu()); {
        Property<String> vstupniHodnota = stringProperty("Vstupni parametr", 10);
        
        vstupniHodnota.setListProvider(new ListProvider<String>() {

            @Override
            public List<ListItem<String>> getListValues() {
                return readItems();
            }
        });
        WizardView vw = new WizardView(this);
        vw.addProperty(vstupniHodnota);
        vw.form(column(
                row(vstupniHodnota)
        ), true);
        //spustitAnalyzu.reg
        spustitAnalyzu.registerWizard(Function.DEFAULT_WIZARD_KEY, vw);
    }

    
    
    public Modul() {
        super("Modul","Modul","Modul_ID");
        initFields();
    }

    protected void initFields() {
        typModulu = stringProperty("typModulu");
        nazev = stringProperty("nazev");
        formatXML = stringProperty("formatXML");
        trida = stringProperty("trida");
        parametry = stringProperty("parametry");

        this.setPersistersTriggers(new PersisterTriggers() {

            @Override
            public void beforeCreate(Record record, Context ctx) {
            }

            @Override
            public void afterCreate(Record record, Context ctx) {
            }

            @Override
            public void beforeUpdate(Record record, Context ctx) {
            }

            @Override
            public void afterUpdate(Record record, Context ctx) {
            }

            @Override
            public void beforeDelete(Record record, Context ctx) {
            }

            @Override
            public void afterDelete(Record record, Context ctx) {
            }

            @Override
            public void afterLoad(Record record, Context ctx) {
                System.out.println("Modul, spustit analyzu :0x"+Integer.toHexString(System.identityHashCode(Modul.this.spustitAnalyzu)));
                View defView = Modul.this.getInitializedView();
                if (defView != null) {
                    boolean found = false; 
                    Set<String> properties = record.getProperties();
                    for (String propKey : properties) {
                        if (propKey.equals("trida")) {
                            found = true;
                        }
                    }
                    /*
                    if (found) {
                        // check
                        try {
                            String trida = (String) record.getValue("trida");
                            Class clz = Class.forName(trida);
                            String state = (String) clz.getField("COMPUTED_STATE").get(null);
                            defView.registerClientProperty("trida", trida);
                            defView.registerClientProperty("state", state);
                            
                            
                        } catch( Exception ex ) {
                            defView.registerClientProperty("exception", ex.getMessage());
                        }
                    }
                    */
                } else {
                    System.out.println(" NOT INITIALIZED ...");
                }
            }
        });
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(typModulu).addProperty(nazev);
        retval.form(column(
                row(typModulu,nazev, formatXML),
                row(trida, parametry),
                row(spustitAnalyzu),
                //row(zastavitAnalyzu),
                RepeatedForm.repeated(analyza)
        ));
        return retval;
    }

    private View reverseView;
    View getReverseView(){
        if(reverseView == null){
            reverseView = new View(this, "reverseView");
            reverseView.addProperty(typModulu).addProperty(nazev);
            reverseView.form(column(
                row(typModulu,nazev, formatXML)
            ));

        }
        return reverseView;
    }

}
