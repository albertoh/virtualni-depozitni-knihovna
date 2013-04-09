package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import org.aplikator.server.descriptor.*;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;
import static org.aplikator.server.descriptor.RepeatedForm.repeated;

public class Zaznam extends Entity {
    public Property<String> typDokumentu;
    public Collection<Identifikator> identifikator;
    public Collection<Autor> autor;
    public Collection<Jazyk> jazyk;
    public Property<String> hlavniNazev;
    public Collection<Nazev> nazev;
    public Collection<Vydani> vydani;
    public Collection<Rozsah> rozsah;
    public Collection<Periodicita> periodicita;
    public Collection<Edice> edice;
    public Collection<Exemplar> exemplar;
    public Collection<DigitalniVerze> digitalniVerze;
    public Property<String> urlZdroje;
    public Property<String> sourceXML;
    public Reference<Sklizen> sklizen;
    public Property<String> uzivatel;

    
    public Zaznam() {
        super("Zaznam","Zaznam","Zaznam_ID");
        initFields();
    }

    protected void initFields() {
        typDokumentu = stringProperty("typDokumentu");
        identifikator = collectionProperty(Structure.identifikator, "identifikator", "zaznam");
        autor = collectionProperty(Structure.autor, "autor", "zaznam");
        jazyk = collectionProperty(Structure.jazyk, "jazyk", "zaznam");
        hlavniNazev = stringProperty("hlavniNazev", 2048);
        nazev = collectionProperty(Structure.nazev, "nazev", "zaznam");
        vydani = collectionProperty(Structure.vydani, "vydani", "zaznam");
        rozsah = collectionProperty(Structure.rozsah, "extent", "zaznam");
        periodicita = collectionProperty(Structure.periodicita, "periodicita", "zaznam");
        edice = collectionProperty(Structure.edice, "edice", "zaznam");
        exemplar = collectionProperty(Structure.exemplar, "exemplar", "zaznam");
        digitalniVerze = collectionProperty(Structure.digitalniVerze, "digitalniVerze", "zaznam");
        urlZdroje = stringProperty("url");
        sourceXML = textProperty("sourceXML");
        sklizen = referenceProperty(Structure.sklizen, "sklizen");
        uzivatel = stringProperty("uzivatel");
        addIndex("url_zaznam_idx", true, urlZdroje);

    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(hlavniNazev).addProperty(typDokumentu).addProperty(urlZdroje);
        retval.form(column(
                row(hlavniNazev,typDokumentu),
                urlZdroje,
                repeated(identifikator),
                row(repeated(nazev),repeated(jazyk)),
                repeated(autor),
                repeated(vydani),
                repeated(rozsah),
                repeated(periodicita),
                repeated(edice),
                repeated(exemplar),
                repeated(digitalniVerze),
                ReferenceField.reference(sklizen, Structure.sklizen.spusteni,Structure.sklizen.stav),
                new TextArea(sourceXML).setSize(12)
            ));
        return retval;
    }



}
