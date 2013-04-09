package cz.incad.vdkcr.server.data;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

public class Exemplar extends Entity {
    public Property<String> signatura;
    public Property<String> carovyKod;
    public Property<String> popis;
    public Property<String> svazek;
    public Property<String> rocnik;
    public Property<String> cislo;
    public Property<String> rok;

    public Exemplar() {
        super("Exemplar","Exemplar","Exemplar_ID");
        initFields();
    }

    protected void initFields() {
        signatura = stringProperty("signatura");
        carovyKod = stringProperty("carovyKod");
        popis = stringProperty("popis");
        svazek = stringProperty("svazek");
        rocnik = stringProperty("rocnik");
        cislo = stringProperty("cislo");
        rok = stringProperty("rok");
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(signatura).addProperty(popis);
        retval.form(column(
                row(signatura,carovyKod,popis),
                row(svazek,rocnik, cislo, rok)
            ));
        return retval;
    }

}
