

package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;

import java.util.Date;

import org.aplikator.client.shared.data.ListItem;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.ListProvider;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.Reference;
import org.aplikator.server.descriptor.View;

import cz.incad.vdkcr.server.Structure;
import org.aplikator.server.descriptor.DateField;

public class Sklizen extends Entity {

    public static enum Stav implements ListItem<String>  {
        ZAHAJEN("zahajen"), UKONCEN("ukoncen"), CHYBA("chyba");

        private Stav(String value){
            this.value = value;
        }
        private String value;
        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String getName() {
            return value;
        }

    }

    public Property<Date> spusteni;
    public Property<Date> ukonceni;
    public Property<String> stav;
    public Property<Integer> pocet;
    public Property<String> uzivatel;
    public Reference<Zdroj> zdroj;

    public Sklizen() {
        super("Sklizen","Sklizen","Sklizen_ID");
        initFields();
    }

    protected void initFields() {
        spusteni = dateProperty("spusteni");
        ukonceni = dateProperty("ukonceni");
        stav = stringProperty("stav").setListProvider(new ListProvider.Default<String>(Stav.values()));
        pocet = integerProperty("pocet").setEditable(false);
        uzivatel = stringProperty("uzivatel").setEditable(false);

        zdroj = referenceProperty(Structure.zdroj, "zdroj");
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(stav).addProperty(spusteni).addProperty(ukonceni).addProperty(pocet).addProperty(uzivatel);
        retval.form(column(
                        row(
                            column(stav).setSize(4),
                            column(new DateField(spusteni).setEnabled(false).setFormatPattern("yyyy.MM.dd HH:mm")).setSize(4),
                            column(new DateField(ukonceni).setEnabled(false).setFormatPattern("yyyy.MM.dd HH:mm")).setSize(4)
                        ),
                        row(
                            column(pocet).setSize(4),
                            column(uzivatel).setSize(8)
                        )
            ), false);
        return retval;
    }



}
