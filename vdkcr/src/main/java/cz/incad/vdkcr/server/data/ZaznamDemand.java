package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import static org.aplikator.server.descriptor.Panel.column;

import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.Reference;
import org.aplikator.server.descriptor.View;

public class ZaznamDemand extends Entity {
    public Property<String> uniqueCode;
    public Property<String> zaznam;
    public Property<String> exemplar;
    public Reference<Knihovna> knihovna;
    public Reference<Demand> demand;
    public Property<String> fields;
    
    public ZaznamDemand() {
        super("ZaznamDemand","ZaznamDemand", "ZaznamDemand_ID");
        initFields();
    }

    private void initFields() {
        uniqueCode = stringProperty("uniqueCode");
        zaznam = stringProperty("zaznam");
        fields = stringProperty("fields");
        knihovna = referenceProperty(Structure.knihovna, "knihovna");
        demand = referenceProperty(Structure.demand, "demand");
        addIndex("id_kn_demand_idx", true, zaznam, knihovna, demand);

    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(zaznam).addProperty(knihovna);
        retval.form(column(
                uniqueCode,
                zaznam,
                knihovna,
                demand,
                fields
            ));
        return retval;
    }

}
