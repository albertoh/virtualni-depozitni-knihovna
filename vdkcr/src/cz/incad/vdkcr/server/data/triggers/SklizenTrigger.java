/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.data.triggers;

import cz.incad.vdkcr.server.Structure;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.descriptor.PropertyDTO;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.PersisterTriggers;

/**
 *
 * @author pavels
 */
public class SklizenTrigger extends PersisterTriggers.Default{

    @Override
    public void onCreate(Record record, Context ctx) {
        Set<String> properties = record.getProperties();
        System.out.println("properties = "+properties);
        HttpServletRequest request = ctx.getHttpServletRequest();
        String remoteUser = request.getRemoteUser();
        record.setValue(remoteUser, request);
        PropertyDTO<String> uzivatel = Structure.sklizen.uzivatel.clientClone(ctx);
        if (remoteUser != null) {
            uzivatel.setValue(record, remoteUser);
        } else {
            uzivatel.setValue(record, " ---- ");
        }
    }

}
