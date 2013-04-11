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
    public Property<String> dilciKnih;
    public Property<String> sbirka;
    public Property<String> statusJednotky;
    public Property<String> pocetVypujcek;
    public Property<String> poznXerokopii;

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
        dilciKnih = stringProperty("dilciKnih");
        sbirka = stringProperty("sbirka");
        statusJednotky = stringProperty("statusJednotky");
        pocetVypujcek = stringProperty("pocetVypujcek");
        poznXerokopii = stringProperty("poznXerokopii");
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(signatura).addProperty(popis);
        retval.form(column(
                row(column(signatura).setSize(6),column(carovyKod).setSize(6)),
                row(column(popis)),
                row(column(svazek,rocnik,cislo,rok)),
                row(column(dilciKnih,sbirka,statusJednotky,pocetVypujcek,poznXerokopii))
            ));
        return retval;
    }

}
