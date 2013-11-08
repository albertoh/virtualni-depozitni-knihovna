package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.descriptor.*;
import org.aplikator.server.persistence.PersisterTriggers;

import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;
import static org.aplikator.server.descriptor.RepeatedForm.repeated;

public class Zaznam extends Entity {
    public Property<String> typDokumentu;
    public Property<String> hlavniNazev;
    public Property<String> ccnb;
    public Property<String> identifikator;
    public Property<String> urlZdroje;
    public Property<String> sourceXML;
    public Reference<Sklizen> sklizen;
    public Property<String> uzivatel;
    public Property<String> knihovna;
    public Property<String> uniqueCode;

    
    public Zaznam() {
        super("Zaznam","Zaznam","Zaznam_ID");
        initFields();
    }

    private void initFields() {
        typDokumentu = stringProperty("typDokumentu");
        identifikator = stringProperty("identifikator");
        hlavniNazev = stringProperty("hlavniNazev", 2048);
        ccnb = stringProperty("ccnb");
        urlZdroje = stringProperty("url");
        sourceXML = textProperty("sourceXML");
        sklizen = referenceProperty(Structure.sklizen, "sklizen");
        uzivatel = stringProperty("uzivatel");
        uniqueCode = stringProperty("uniqueCode");
        knihovna = stringProperty("knihovna").setListProvider(Knihovna.getGroupList());
        addIndex("url_zaznam_idx", true, urlZdroje);
        addIndex("identif_zaznam_idx", true, identifikator);
        addIndex("view_zaznam_idx", false, getPrimaryKey(), hlavniNazev, typDokumentu);
        
//        this.setIndexed(true);
        
        this.setPersistersTriggers(new PersisterTriggers.Default() {
            @Override
            public void onLoad(Record record, Context ctx) {
                record.setPreview("<b>"+record.getValue(Structure.zaznam.hlavniNazev.getId())
                +"</b> ("+record.getValue(Structure.zaznam.typDokumentu.getId())+")");
            }
            
        });

    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(hlavniNazev).addProperty(typDokumentu);
        retval.setPageSize(20);
        retval.form(column(
                knihovna,
                row(hlavniNazev.widget().setSize(9), typDokumentu.widget().setSize(3)),
                urlZdroje, uniqueCode, ccnb, identifikator,
                ReferenceField.reference(sklizen, Structure.sklizen.spusteni,Structure.sklizen.stav),
                new TextArea(sourceXML)
                
            ), false);
        retval.addSortDescriptor("default", "default", SortItem.descending(this.getPrimaryKey()));    // hack kvůli řazení záznamů pozpátku. v případě nespokojenosti zakomentovat tento řádek
        return retval;
    }



}
