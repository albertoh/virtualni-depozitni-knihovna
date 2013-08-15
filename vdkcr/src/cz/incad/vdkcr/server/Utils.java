package cz.incad.vdkcr.server;

import org.aplikator.client.shared.data.ListItem;
import org.aplikator.server.descriptor.ListProvider;

public class Utils {

    @SuppressWarnings("unchecked")
    static public ListProvider namedList(String listName){
        return new ListProvider.Default(new ListItem.Default("", ""));//TODO dynamic lists
    }

}
