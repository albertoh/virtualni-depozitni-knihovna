package cz.incad.vdkcr.server;

import org.aplikator.client.shared.data.ListItem;
import org.aplikator.server.descriptor.ListProvider;

public class Utils {

    @SuppressWarnings("unchecked")
    static public ListProvider<String> namedList(String listName){
        return new ListProvider.Default<String>(new ListItem.Default<String>("", ""));//TODO dynamic lists
    }

}
